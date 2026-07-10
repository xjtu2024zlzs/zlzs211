"""Create leakage-free train triples and test cases for reasoning evaluation."""

from __future__ import annotations

import argparse
import json
from pathlib import Path
from typing import Any

import numpy as np
import pandas as pd

DOMAINS = ("bearing", "hydraulic")
LEAKAGE_RELATIONS = ("may_caused_by", "attributed_to_stage")
REQUIRED_FEEDBACK_COLUMNS = {
    "feedback_id",
    "fault_type",
    "fault_position",
    "true_reason_id",
    "true_reason_type",
    "true_stage",
}
REQUIRED_TRIPLE_COLUMNS = {"head", "relation", "tail"}
OPTIONAL_TEST_COLUMNS = [
    "component_uid",
    "component_code",
    "component_name",
    "bearing_id",
    "diagnosis_confidence",
    "rca_confidence",
]


def parse_bool(value: str | bool) -> bool:
    """Parse a CLI boolean value."""
    if isinstance(value, bool):
        return value
    normalized = value.strip().lower()
    if normalized in {"true", "1", "yes", "y", "t"}:
        return True
    if normalized in {"false", "0", "no", "n", "f"}:
        return False
    raise argparse.ArgumentTypeError(f"Invalid boolean value: {value}")


def project_root() -> Path:
    """Return the project root based on this script location."""
    return Path(__file__).resolve().parents[1]


def require_file(path: Path, description: str) -> None:
    """Raise a clear error when an input file is missing."""
    if not path.exists():
        raise FileNotFoundError(f"Missing {description}: {path}")


def load_feedback(domain: str, root: Path) -> pd.DataFrame:
    """Load feedback CSV for a domain."""
    path = root / "data" / domain / f"{domain}_feedback.csv"
    require_file(path, f"{domain} feedback file")
    feedback = pd.read_csv(path, dtype=str).fillna("")
    missing = REQUIRED_FEEDBACK_COLUMNS - set(feedback.columns)
    if missing:
        raise ValueError(f"{path} is missing required columns: {sorted(missing)}")
    return feedback


def load_triples(domain: str, root: Path) -> pd.DataFrame:
    """Load original triples CSV for a domain."""
    path = root / "data" / domain / f"{domain}_triples.csv"
    require_file(path, f"{domain} triples file")
    triples = pd.read_csv(path, dtype=str).fillna("")
    missing = REQUIRED_TRIPLE_COLUMNS - set(triples.columns)
    if missing:
        raise ValueError(f"{path} is missing required columns: {sorted(missing)}")
    return triples


def _non_empty_mask(series: pd.Series) -> pd.Series:
    return series.astype(str).str.strip().ne("")


def select_test_cases(
    feedback: pd.DataFrame,
    domain: str,
    test_size: int,
    seed: int,
    exclude_normal: bool,
) -> pd.DataFrame:
    """Select reproducible, approximately stratified test cases from feedback."""
    candidates = feedback[_non_empty_mask(feedback["true_reason_id"])].copy()

    if domain == "bearing" and exclude_normal:
        normal_mask = pd.Series(False, index=candidates.index)
        if "raw_label" in candidates.columns:
            normal_mask |= candidates["raw_label"].astype(str).str.lower().eq("normal")
        if "fault_type" in candidates.columns:
            normal_mask |= candidates["fault_type"].astype(str).str.lower().eq("normal")
        candidates = candidates[~normal_mask].copy()

    if candidates.empty:
        return candidates

    target_size = min(int(test_size), len(candidates))
    if target_size <= 0:
        return candidates.iloc[0:0].copy()

    rng = np.random.default_rng(seed)
    groups = [(name, group.copy()) for name, group in candidates.groupby("fault_type", sort=True)]
    selected_indices: list[Any] = []

    if target_size >= len(groups):
        for _, group in groups:
            selected_indices.append(rng.choice(group.index.to_numpy(), size=1, replace=False)[0])
    else:
        group_names = np.array([name for name, _ in groups], dtype=object)
        chosen_names = set(rng.choice(group_names, size=target_size, replace=False).tolist())
        groups = [(name, group) for name, group in groups if name in chosen_names]
        for _, group in groups:
            selected_indices.append(rng.choice(group.index.to_numpy(), size=1, replace=False)[0])

    remaining_count = target_size - len(selected_indices)
    if remaining_count > 0:
        remaining = candidates.drop(index=selected_indices, errors="ignore")
        if not remaining.empty:
            weights = remaining["fault_type"].map(remaining["fault_type"].value_counts()).astype(float)
            weights = weights / weights.sum()
            extra = rng.choice(
                remaining.index.to_numpy(),
                size=min(remaining_count, len(remaining)),
                replace=False,
                p=weights.to_numpy(),
            )
            selected_indices.extend(extra.tolist())

    selected = candidates.loc[selected_indices].copy()
    shuffle_order = rng.permutation(len(selected))
    return selected.iloc[shuffle_order].reset_index(drop=True)


def remove_leakage_triples(
    triples: pd.DataFrame,
    test_cases: pd.DataFrame,
) -> tuple[pd.DataFrame, int, int]:
    """Remove answer leakage relations for selected test feedback ids."""
    test_feedback_ids = set(test_cases["feedback_id"].astype(str))
    test_head_mask = triples["head"].astype(str).isin(test_feedback_ids)
    may_mask = test_head_mask & triples["relation"].eq("may_caused_by")
    stage_mask = test_head_mask & triples["relation"].eq("attributed_to_stage")
    remove_mask = may_mask | stage_mask

    train_triples = triples.loc[~remove_mask].copy().reset_index(drop=True)
    return train_triples, int(may_mask.sum()), int(stage_mask.sum())


def check_leakage(train_triples: pd.DataFrame, test_cases: pd.DataFrame) -> dict[str, int]:
    """Check whether test answer relations remain in train triples."""
    test_feedback_ids = set(test_cases["feedback_id"].astype(str))
    test_head_mask = train_triples["head"].astype(str).isin(test_feedback_ids)
    return {
        "test_may_caused_by_remaining_in_train": int(
            (test_head_mask & train_triples["relation"].eq("may_caused_by")).sum()
        ),
        "test_attributed_to_stage_remaining_in_train": int(
            (test_head_mask & train_triples["relation"].eq("attributed_to_stage")).sum()
        ),
    }


def build_test_cases_output(test_cases: pd.DataFrame) -> pd.DataFrame:
    """Keep required and available optional columns for test case output."""
    required_order = [
        "feedback_id",
        "fault_type",
        "fault_position",
        "true_reason_id",
        "true_reason_type",
        "true_stage",
    ]
    columns = required_order + [col for col in OPTIONAL_TEST_COLUMNS if col in test_cases.columns]
    return test_cases.loc[:, columns].copy()


def save_outputs(
    domain: str,
    root: Path,
    train_triples: pd.DataFrame,
    test_cases: pd.DataFrame,
    summary: dict[str, Any],
) -> dict[str, Path]:
    """Save train triples, test cases and split summary."""
    data_dir = root / "data" / domain
    report_dir = root / "outputs" / domain / "reports"
    report_dir.mkdir(parents=True, exist_ok=True)

    train_path = data_dir / f"{domain}_triples_train.csv"
    test_path = data_dir / f"{domain}_test_cases.csv"
    summary_path = report_dir / "eval_split_summary.json"

    train_triples.to_csv(train_path, index=False, encoding="utf-8-sig")
    build_test_cases_output(test_cases).to_csv(test_path, index=False, encoding="utf-8-sig")
    summary_path.write_text(
        json.dumps(summary, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )

    return {"train_triples": train_path, "test_cases": test_path, "summary": summary_path}


def process_domain(domain: str, test_size: int, seed: int, exclude_normal: bool, root: Path) -> dict[str, Any]:
    """Process one domain and return the generated summary."""
    feedback = load_feedback(domain, root)
    triples = load_triples(domain, root)
    test_cases = select_test_cases(feedback, domain, test_size, seed, exclude_normal)
    train_triples, removed_may, removed_stage = remove_leakage_triples(triples, test_cases)
    leak_check = check_leakage(train_triples, test_cases)

    summary: dict[str, Any] = {
        "domain": domain,
        "original_triple_count": int(len(triples)),
        "train_triple_count": int(len(train_triples)),
        "removed_may_caused_by_count": int(removed_may),
        "removed_attributed_to_stage_count": int(removed_stage),
        "test_case_count": int(len(test_cases)),
        "fault_type_distribution": test_cases["fault_type"].value_counts().sort_index().astype(int).to_dict(),
        "true_stage_distribution": test_cases["true_stage"].value_counts().sort_index().astype(int).to_dict(),
        "leak_check": leak_check,
    }
    paths = save_outputs(domain, root, train_triples, test_cases, summary)
    print_summary(summary, paths)
    return summary


def print_summary(summary: dict[str, Any], paths: dict[str, Path]) -> None:
    """Print required console summary."""
    leak_check = summary["leak_check"]
    print("=" * 72)
    print(f"domain: {summary['domain']}")
    print(f"original_triple_count: {summary['original_triple_count']}")
    print(f"train_triple_count: {summary['train_triple_count']}")
    print(f"removed_may_caused_by_count: {summary['removed_may_caused_by_count']}")
    print(f"removed_attributed_to_stage_count: {summary['removed_attributed_to_stage_count']}")
    print(f"test_case_count: {summary['test_case_count']}")
    print(
        "test_may_caused_by_remaining_in_train: "
        f"{leak_check['test_may_caused_by_remaining_in_train']}"
    )
    print(
        "test_attributed_to_stage_remaining_in_train: "
        f"{leak_check['test_attributed_to_stage_remaining_in_train']}"
    )
    print(f"train_triples_path: {paths['train_triples']}")
    print(f"test_cases_path: {paths['test_cases']}")
    print(f"summary_path: {paths['summary']}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Create leakage-free train triples and test cases for reasoning evaluation."
    )
    parser.add_argument("--domain", choices=[*DOMAINS, "all"], required=True)
    parser.add_argument("--test_size", type=int, default=500)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--exclude_normal", type=parse_bool, default=True)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    root = project_root()
    domains = DOMAINS if args.domain == "all" else (args.domain,)
    for domain in domains:
        process_domain(domain, args.test_size, args.seed, args.exclude_normal, root)


if __name__ == "__main__":
    main()
