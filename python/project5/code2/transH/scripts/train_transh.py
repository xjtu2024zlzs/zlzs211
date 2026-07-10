from __future__ import annotations

import argparse
import json
import os
import sys
import time
from pathlib import Path

os.environ.setdefault("KMP_DUPLICATE_LIB_OK", "TRUE")

import numpy as np
import pandas as pd
import torch
from torch.utils.data import DataLoader, TensorDataset

ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from src.models.transh import TransHModel


DEFAULT_MAX_TRIPLES_PER_EPOCH = {
    "bearing": 120000,
    "hydraulic": 200000,
}

RCA_KEY_RELATIONS = {
    "has_root_sensor",
    "has_secondary_sensor",
    "diagnosed_root_component",
    "has_component_confidence",
    "has_subtype_candidate",
    "activates_sensor",
    "most_activated_sensor",
    "indicates_fault_of",
}

MANDATORY_RELATIONS = {"may_caused_by", "attributed_to_stage"}

LIFECYCLE_RELATIONS = {
    "designed_by",
    "uses_material_batch",
    "manufactured_in",
    "assembled_in",
    "inspected_by",
    "maintained_by",
    "occurs_on",
}

ATTRIBUTE_PREFIXES = (
    "has_",
    "located_at",
    "belongs_to_",
    "uses_inspection_type",
    "uses_process",
    "processed_by",
    "supplied_by",
    "operated_by",
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Train a TransH model for lifecycle quality traceability triples.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic"], required=True)
    parser.add_argument("--epochs", type=int, default=100)
    parser.add_argument("--embedding_dim", type=int, default=64)
    parser.add_argument("--batch_size", type=int, default=2048)
    parser.add_argument("--lr", type=float, default=0.001)
    parser.add_argument("--margin", type=float, default=1.0)
    parser.add_argument("--neg_ratio", type=int, default=1)
    parser.add_argument("--max_triples_per_epoch", type=int, default=None)
    parser.add_argument("--seed", type=int, default=20260603)
    parser.add_argument("--use_train_split", action="store_true", help="Use leakage-free train triples for evaluation retraining.")
    return parser.parse_args()


def require_triples(domain: str, use_train_split: bool = False) -> Path:
    if use_train_split:
        path = ROOT / "data" / domain / f"{domain}_triples_train.csv"
        if not path.exists():
            raise FileNotFoundError(f"{path} 不存在。未找到无泄露训练三元组，请先运行 scripts/create_eval_split.py")
        return path

    path = ROOT / "data" / domain / f"{domain}_triples.csv"
    if not path.exists():
        raise FileNotFoundError(f"{path} 不存在，请先运行 python scripts/build_triples.py --domain {domain}")
    return path


def check_eval_split_leakage(domain: str, triples: pd.DataFrame, triples_path: Path) -> None:
    test_cases_path = ROOT / "data" / domain / f"{domain}_test_cases.csv"
    if not test_cases_path.exists():
        print(f"warning: {test_cases_path} 不存在，跳过 test cases 泄露边检查。")
        return

    test_cases = pd.read_csv(test_cases_path, encoding="utf-8-sig", dtype=str).fillna("")
    if "feedback_id" not in test_cases.columns:
        print(f"warning: {test_cases_path} 缺少 feedback_id 列，跳过 test cases 泄露边检查。")
        return

    test_feedback_ids = set(test_cases["feedback_id"].astype(str))
    test_head_mask = triples["head"].astype(str).isin(test_feedback_ids)
    may_remaining = int((test_head_mask & triples["relation"].eq("may_caused_by")).sum())
    stage_remaining = int((test_head_mask & triples["relation"].eq("attributed_to_stage")).sum())
    if may_remaining > 0 or stage_remaining > 0:
        raise ValueError(
            f"{triples_path} 仍包含 test cases 泄露边: "
            f"may_caused_by={may_remaining}, attributed_to_stage={stage_remaining}。"
        )


def load_triples(domain: str, use_train_split: bool = False) -> tuple[pd.DataFrame, Path]:
    path = require_triples(domain, use_train_split)
    triples = pd.read_csv(path, encoding="utf-8-sig")
    required = {"head", "relation", "tail", "head_type", "tail_type", "domain", "source_table", "weight"}
    missing = required - set(triples.columns)
    if missing:
        raise ValueError(f"{path} 缺少列: {sorted(missing)}")
    may_count = int((triples["relation"] == "may_caused_by").sum())
    stage_count = int((triples["relation"] == "attributed_to_stage").sum())
    if may_count <= 0:
        raise ValueError(f"{path} 中 may_caused_by 数量为 0，无法训练原因追溯模型。")
    if use_train_split:
        check_eval_split_leakage(domain, triples, path)
    print(f"triples_count: {len(triples)}")
    print(f"may_caused_by_count: {may_count}")
    print(f"attributed_to_stage_count: {stage_count}")
    return triples, path


def build_mappings(triples: pd.DataFrame) -> tuple[dict[str, int], dict[str, int]]:
    entities = pd.concat([triples["head"].astype(str), triples["tail"].astype(str)], ignore_index=True).drop_duplicates()
    relations = triples["relation"].astype(str).drop_duplicates()
    entity2id = {entity: idx for idx, entity in enumerate(entities.tolist())}
    relation2id = {relation: idx for idx, relation in enumerate(relations.tolist())}
    return entity2id, relation2id


def relation_training_weight(relation: str) -> float:
    if relation == "may_caused_by":
        return 3.0
    if relation == "attributed_to_stage":
        return 1.5
    if relation in {"has_root_sensor", "diagnosed_root_component", "has_subtype_candidate", "activates_sensor"}:
        return 1.2
    if relation in LIFECYCLE_RELATIONS:
        return 1.0
    if relation.startswith(ATTRIBUTE_PREFIXES):
        return 0.5
    return 1.0


def make_triples_id(triples: pd.DataFrame, entity2id: dict[str, int], relation2id: dict[str, int]) -> pd.DataFrame:
    triples_id = pd.DataFrame(
        {
            "h": triples["head"].astype(str).map(entity2id).astype(int),
            "r": triples["relation"].astype(str).map(relation2id).astype(int),
            "t": triples["tail"].astype(str).map(entity2id).astype(int),
            "relation": triples["relation"].astype(str),
            "head_type": triples["head_type"].astype(str),
            "tail_type": triples["tail_type"].astype(str),
            "weight": triples["relation"].astype(str).map(relation_training_weight).astype(float),
        }
    )
    return triples_id


def build_type_indices(triples: pd.DataFrame, entity2id: dict[str, int]) -> dict[str, np.ndarray]:
    type_to_entities: dict[str, set[int]] = {}
    for entity_col, type_col in [("head", "head_type"), ("tail", "tail_type")]:
        for entity, entity_type in triples[[entity_col, type_col]].drop_duplicates().itertuples(index=False):
            type_to_entities.setdefault(str(entity_type), set()).add(entity2id[str(entity)])
    return {entity_type: np.asarray(sorted(ids), dtype=np.int64) for entity_type, ids in type_to_entities.items()}


def build_negative_sampling_context(triples_id: pd.DataFrame, relation2id: dict[str, int], type_indices: dict[str, np.ndarray], num_entities: int):
    relation_to_tail_types: dict[int, np.ndarray] = {}
    for relation, group in triples_id.groupby("relation", sort=False):
        relation_id = relation2id[relation]
        tail_ids: list[int] = []
        if relation == "may_caused_by":
            for tail_type in group["tail_type"].drop_duplicates().astype(str):
                tail_ids.extend(type_indices.get(tail_type, np.asarray([], dtype=np.int64)).tolist())
        elif relation == "occurs_on":
            for tail_type in ["Bearing", "HydraulicComponent"]:
                tail_ids.extend(type_indices.get(tail_type, np.asarray([], dtype=np.int64)).tolist())
        if tail_ids:
            relation_to_tail_types[relation_id] = np.asarray(sorted(set(tail_ids)), dtype=np.int64)
    all_entities = np.arange(num_entities, dtype=np.int64)
    return relation_to_tail_types, all_entities


def sample_epoch_frame(triples_id: pd.DataFrame, max_count: int, rng: np.random.Generator) -> pd.DataFrame:
    if len(triples_id) <= max_count:
        return triples_id

    mandatory = triples_id[triples_id["relation"].isin(MANDATORY_RELATIONS)]
    remaining_budget = max_count - len(mandatory)
    if remaining_budget <= 0:
        return mandatory.sample(frac=1.0, random_state=int(rng.integers(0, 2**31 - 1))).reset_index(drop=True)

    rest = triples_id[~triples_id["relation"].isin(MANDATORY_RELATIONS)]
    rca = rest[rest["relation"].isin(RCA_KEY_RELATIONS)]
    rca_take = min(len(rca), remaining_budget)
    sampled_parts = [mandatory]
    if rca_take > 0:
        sampled_parts.append(rca.sample(n=rca_take, random_state=int(rng.integers(0, 2**31 - 1))))
        remaining_budget -= rca_take

    if remaining_budget <= 0:
        sampled = pd.concat(sampled_parts, ignore_index=True)
        return sampled.sample(frac=1.0, random_state=int(rng.integers(0, 2**31 - 1))).reset_index(drop=True)

    rest = rest[~rest["relation"].isin(RCA_KEY_RELATIONS)]
    lifecycle = rest[rest["relation"].isin(LIFECYCLE_RELATIONS)]
    attrs = rest[~rest["relation"].isin(LIFECYCLE_RELATIONS)]

    lifecycle_take = min(len(lifecycle), int(remaining_budget * 0.55))
    attr_take = remaining_budget - lifecycle_take
    if lifecycle_take > 0:
        sampled_parts.append(lifecycle.sample(n=lifecycle_take, random_state=int(rng.integers(0, 2**31 - 1))))
    if attr_take > 0 and not attrs.empty:
        attr_take = min(attr_take, len(attrs))
        weights = (1.0 / attrs["weight"].astype(float).clip(lower=0.1)).to_numpy()
        weights = weights / weights.sum()
        sampled_idx = rng.choice(attrs.index.to_numpy(), size=attr_take, replace=False, p=weights)
        sampled_parts.append(attrs.loc[sampled_idx])

    sampled = pd.concat(sampled_parts, ignore_index=True).drop_duplicates(["h", "r", "t", "relation"])
    return sampled.sample(frac=1.0, random_state=int(rng.integers(0, 2**31 - 1))).reset_index(drop=True)


def make_negative_batch(
    pos: torch.Tensor,
    relation_to_tail_candidates: dict[int, np.ndarray],
    all_entities: np.ndarray,
    rng: np.random.Generator,
) -> torch.Tensor:
    neg = pos.detach().cpu().numpy().copy()
    corrupt_tail = rng.random(len(neg)) < 0.5
    for i in range(len(neg)):
        relation_id = int(neg[i, 1])
        if corrupt_tail[i]:
            candidates = relation_to_tail_candidates.get(relation_id, all_entities)
            neg[i, 2] = int(rng.choice(candidates))
        else:
            neg[i, 0] = int(rng.choice(all_entities))
    return torch.from_numpy(neg).long()


def save_json(data: dict, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding="utf-8")


def train() -> None:
    args = parse_args()
    rng = np.random.default_rng(args.seed)
    torch.manual_seed(args.seed)

    triples, triples_path = load_triples(args.domain, args.use_train_split)
    entity2id, relation2id = build_mappings(triples)
    triples_id = make_triples_id(triples, entity2id, relation2id)

    model_dir_name = "models_eval" if args.use_train_split else "models"
    train_log_name = "train_log_eval.csv" if args.use_train_split else "train_log.csv"
    training_mode = "eval_split" if args.use_train_split else "full"

    model_dir = ROOT / "outputs" / args.domain / model_dir_name
    report_dir = ROOT / "outputs" / args.domain / "reports"
    model_dir.mkdir(parents=True, exist_ok=True)
    report_dir.mkdir(parents=True, exist_ok=True)

    print(f"training_mode: {training_mode}")
    print(f"triples_file: {triples_path}")
    print(f"output_model_dir: {model_dir}")
    print(f"entity_count: {len(entity2id)}")
    print(f"relation_count: {len(relation2id)}")

    save_json(entity2id, model_dir / "entity2id.json")
    save_json(relation2id, model_dir / "relation2id.json")
    triples_id.to_csv(model_dir / "triples_id.csv", index=False, encoding="utf-8-sig")

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    if torch.cuda.is_available():
        print(f"device=cuda gpu={torch.cuda.get_device_name(0)}")
    else:
        print("device=cpu")

    model = TransHModel(
        num_entities=len(entity2id),
        num_relations=len(relation2id),
        embedding_dim=args.embedding_dim,
        margin=args.margin,
    ).to(device)
    optimizer = torch.optim.Adam(model.parameters(), lr=args.lr)

    type_indices = build_type_indices(triples, entity2id)
    relation_to_tail_candidates, all_entities = build_negative_sampling_context(triples_id, relation2id, type_indices, len(entity2id))
    max_triples_per_epoch = args.max_triples_per_epoch or DEFAULT_MAX_TRIPLES_PER_EPOCH[args.domain]
    may_relation_id = relation2id["may_caused_by"]

    logs: list[dict[str, object]] = []
    start_time = time.time()
    for epoch in range(1, args.epochs + 1):
        sampled = sample_epoch_frame(triples_id, max_triples_per_epoch, rng)
        pos_array = sampled[["h", "r", "t"]].to_numpy(dtype=np.int64)
        weight_array = sampled["weight"].to_numpy(dtype=np.float32)
        dataset = TensorDataset(torch.from_numpy(pos_array).long(), torch.from_numpy(weight_array).float())
        loader = DataLoader(dataset, batch_size=args.batch_size, shuffle=True, drop_last=False)

        model.train()
        total_loss = 0.0
        total_examples = 0
        for pos_cpu, weights_cpu in loader:
            if args.neg_ratio > 1:
                pos_cpu = pos_cpu.repeat_interleave(args.neg_ratio, dim=0)
                weights_cpu = weights_cpu.repeat_interleave(args.neg_ratio, dim=0)
            neg_cpu = make_negative_batch(pos_cpu, relation_to_tail_candidates, all_entities, rng)
            pos = pos_cpu.to(device, non_blocking=True)
            neg = neg_cpu.to(device, non_blocking=True)
            weights = weights_cpu.to(device, non_blocking=True)

            optimizer.zero_grad(set_to_none=True)
            loss = model(pos, neg, weights)
            loss.backward()
            optimizer.step()

            batch_size = len(pos_cpu)
            total_loss += float(loss.detach().cpu()) * batch_size
            total_examples += batch_size

        avg_loss = total_loss / max(total_examples, 1)
        elapsed = time.time() - start_time
        sampled_may_count = int((sampled["r"] == may_relation_id).sum())
        log_row = {
            "epoch": epoch,
            "loss": round(avg_loss, 6),
            "sampled_triple_count": int(len(sampled)),
            "may_caused_by_count": sampled_may_count,
            "device": str(device),
            "elapsed_time": round(elapsed, 3),
        }
        logs.append(log_row)
        if epoch == 1 or epoch % 5 == 0 or epoch == args.epochs:
            print(
                f"epoch={epoch} loss={avg_loss:.6f} sampled_triple_count={len(sampled)} "
                f"may_caused_by_count={sampled_may_count} device={device} elapsed_time={elapsed:.2f}s"
            )

    train_log_path = report_dir / train_log_name
    pd.DataFrame(logs).to_csv(train_log_path, index=False, encoding="utf-8-sig")
    torch.save(
        {
            "model_state_dict": model.state_dict(),
            "num_entities": len(entity2id),
            "num_relations": len(relation2id),
            "embedding_dim": args.embedding_dim,
            "margin": args.margin,
            "entity2id": entity2id,
            "relation2id": relation2id,
        },
        model_dir / "transh_model.pt",
    )
    print(f"saved_model={model_dir / 'transh_model.pt'}")
    print(f"saved_train_log={train_log_path}")


if __name__ == "__main__":
    train()
