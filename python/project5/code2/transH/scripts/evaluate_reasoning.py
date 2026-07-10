from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

import numpy as np
import pandas as pd
from tqdm import tqdm


ROOT = Path(__file__).resolve().parents[1]
if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

from scripts.run_reasoning import prepare_reasoning_context, run_reasoning_for_case


STAGE_NAME_TO_CODE = {
    "设计阶段": "design",
    "材料阶段": "material",
    "制造阶段": "manufacturing",
    "装配阶段": "assembly",
    "检测阶段": "inspection",
    "使用/运维阶段": "operation",
}

SCOPE_KEYS = [
    "lifecycle_candidate",
    "expanded_candidate",
    "rca_root_component_candidate",
    "rca_root_component_expanded_candidate",
    "global_same_type_candidate",
]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Batch evaluate TransH Top6 reasoning quality.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic", "all"], required=True)
    parser.add_argument("--sample_size", type=int, default=200)
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--exclude_normal", action=argparse.BooleanOptionalAction, default=True)
    parser.add_argument("--save_each_reasoning", action=argparse.BooleanOptionalAction, default=False)
    parser.add_argument("--use_eval_split", action="store_true", help="Evaluate on leakage-free test cases with models_eval.")
    return parser.parse_args()


def domains_for(domain: str) -> list[str]:
    return ["bearing", "hydraulic"] if domain == "all" else [domain]


def normalize_stage(stage: Any, stage_name: Any = None) -> str:
    text = "" if pd.isna(stage) else str(stage)
    if text in {"design", "material", "manufacturing", "assembly", "inspection", "operation"}:
        return text
    name = "" if stage_name is None or pd.isna(stage_name) else str(stage_name)
    return STAGE_NAME_TO_CODE.get(name, text)


def select_samples(feedback: pd.DataFrame, domain: str, sample_size: int, seed: int, exclude_normal: bool) -> tuple[pd.DataFrame, int]:
    total = len(feedback)
    samples = feedback.copy()
    if domain == "bearing" and exclude_normal:
        true_reason = samples.get("true_reason_id", pd.Series([""] * len(samples))).fillna("").astype(str).str.strip()
        samples = samples[
            (samples.get("raw_label", "").astype(str) != "Normal")
            & (samples.get("fault_type", "").astype(str) != "normal")
            & (true_reason != "")
        ]
    skipped_by_filter = total - len(samples)
    take = min(sample_size, len(samples))
    if take < len(samples):
        samples = samples.sample(n=take, random_state=seed)
    return samples.reset_index(drop=True), skipped_by_filter


def load_eval_test_cases(domain: str) -> tuple[pd.DataFrame, Path]:
    test_cases_path = ROOT / "data" / domain / f"{domain}_test_cases.csv"
    if not test_cases_path.exists():
        raise FileNotFoundError(f"{test_cases_path} 不存在，请先运行 scripts/create_eval_split.py。")
    test_cases = pd.read_csv(test_cases_path, encoding="utf-8-sig", dtype=str).fillna("")
    required = {"feedback_id", "fault_type", "fault_position", "true_reason_id", "true_reason_type", "true_stage"}
    missing = required - set(test_cases.columns)
    if missing:
        raise ValueError(f"{test_cases_path} 缺少列: {sorted(missing)}")
    return test_cases, test_cases_path


def select_eval_samples(test_cases: pd.DataFrame, sample_size: int, seed: int) -> pd.DataFrame:
    take = min(sample_size, len(test_cases))
    if take < len(test_cases):
        test_cases = test_cases.sample(n=take, random_state=seed)
    return test_cases.reset_index(drop=True)


def check_eval_split_leakage(domain: str, test_cases: pd.DataFrame) -> dict[str, Any]:
    train_triples_path = ROOT / "data" / domain / f"{domain}_triples_train.csv"
    test_cases_path = ROOT / "data" / domain / f"{domain}_test_cases.csv"
    if not train_triples_path.exists():
        raise FileNotFoundError(f"{train_triples_path} 不存在，请先运行 scripts/create_eval_split.py。")
    if not test_cases_path.exists():
        raise FileNotFoundError(f"{test_cases_path} 不存在，请先运行 scripts/create_eval_split.py。")

    triples = pd.read_csv(train_triples_path, encoding="utf-8-sig", dtype=str).fillna("")
    required = {"head", "relation", "tail"}
    missing = required - set(triples.columns)
    if missing:
        raise ValueError(f"{train_triples_path} 缺少列: {sorted(missing)}")

    answer_edges = set(
        zip(
            test_cases["feedback_id"].astype(str),
            test_cases["true_reason_id"].astype(str),
        )
    )
    may_edges = triples[triples["relation"].eq("may_caused_by")]
    leak_count = sum((str(row.head), str(row.tail)) in answer_edges for row in may_edges.itertuples(index=False))
    leak_ratio = round(leak_count / len(test_cases), 6) if len(test_cases) else 0.0
    if leak_count > 0:
        print(f"warning: eval split train triples still contain {leak_count} test may_caused_by answer edges.")
    return {
        "train_triples_file": str(train_triples_path),
        "test_cases_file": str(test_cases_path),
        "leak_count": int(leak_count),
        "leak_ratio": leak_ratio,
    }


def reciprocal_rank(top_ids: list[str], true_reason_id: str) -> float:
    for idx, candidate_id in enumerate(top_ids, start=1):
        if candidate_id == true_reason_id:
            return 1.0 / idx
    return 0.0


def row_from_reasoning(feedback_row: pd.Series, reasoning: dict[str, Any]) -> dict[str, Any]:
    top6 = reasoning.get("top6_candidates", [])
    top_ids = [str(item.get("candidate_id", "")) for item in top6]
    top_types = [str(item.get("candidate_type", "")) for item in top6]
    top_scopes = [str(item.get("candidate_scope", "")) for item in top6]
    candidate_stages = [normalize_stage(item.get("candidate_stage", ""), item.get("candidate_stage_name", "")) for item in top6]
    true_reason_id = str(feedback_row.get("true_reason_id", ""))
    true_reason_type = str(feedback_row.get("true_reason_type", ""))
    true_stage = normalize_stage(feedback_row.get("true_stage", ""))
    attribution = reasoning.get("stage_attribution", {})
    primary_stage = normalize_stage(attribution.get("primary_stage", ""), attribution.get("primary_stage_name", ""))
    focus_items = attribution.get("focus_stage_top3", [])
    focus_stages = [
        normalize_stage(item.get("stage", ""), item.get("stage_name", ""))
        for item in focus_items
        if normalize_stage(item.get("stage", ""), item.get("stage_name", ""))
    ]
    feedback_subsystems = [
        str(item.get("feedback_subsystem", ""))
        for item in focus_items
        if str(item.get("feedback_subsystem", "")).strip()
    ]
    top1 = top6[0] if top6 else {}
    return {
        "case_id": reasoning.get("case_id", str(feedback_row.get("feedback_id", ""))),
        "feedback_id": str(feedback_row.get("feedback_id", "")),
        "fault_type": str(feedback_row.get("fault_type", "")),
        "fault_position": str(feedback_row.get("fault_position", "")),
        "true_reason_id": true_reason_id,
        "true_reason_type": true_reason_type,
        "true_stage": true_stage,
        "top1_candidate_id": str(top1.get("candidate_id", "")),
        "top1_candidate_type": str(top1.get("candidate_type", "")),
        "top1_stage": normalize_stage(top1.get("candidate_stage", ""), top1.get("candidate_stage_name", "")),
        "top1_score": float(top1.get("final_score", 0.0) or 0.0),
        "top1_hit": bool(len(top_ids) >= 1 and top_ids[0] == true_reason_id),
        "top3_hit": bool(true_reason_id in top_ids[:3]),
        "top6_hit": bool(true_reason_id in top_ids[:6]),
        "type_hit": bool(true_reason_type in top_types[:6]),
        "stage_hit": bool(primary_stage == true_stage),
        "stage_top1_hit": bool(len(focus_stages) >= 1 and focus_stages[0] == true_stage),
        "stage_top2_hit": bool(true_stage in focus_stages[:2]),
        "stage_top3_hit": bool(true_stage in focus_stages[:3]),
        "stage_top6_covered": bool(true_stage in candidate_stages[:6]),
        "mrr": reciprocal_rank(top_ids[:6], true_reason_id),
        "primary_stage": primary_stage,
        "primary_stage_name": str(attribution.get("primary_stage_name", "")),
        "feedback_target_subsystem": str(attribution.get("feedback_target_subsystem", "")),
        "focus_stage_top3": "|".join(focus_stages[:3]),
        "feedback_subsystem_top3": "|".join(feedback_subsystems[:3]),
        "top6_ids": "|".join(top_ids[:6]),
        "top6_types": "|".join(top_types[:6]),
        "top6_scopes": "|".join(top_scopes[:6]),
        "top6_score_sum": float(sum(float(item.get("final_score", 0.0) or 0.0) for item in top6[:6])),
        "root_component_different": bool(reasoning.get("rca_context", {}).get("is_root_component_different_from_feedback", False)),
        "has_rca_root_candidate": any(scope in {"rca_root_component_candidate", "rca_root_component_expanded_candidate"} for scope in top_scopes[:6]),
        "error": "",
    }


def error_row(feedback_row: pd.Series, error: Exception) -> dict[str, Any]:
    return {
        "case_id": str(feedback_row.get("feedback_id", "")),
        "feedback_id": str(feedback_row.get("feedback_id", "")),
        "fault_type": str(feedback_row.get("fault_type", "")),
        "fault_position": str(feedback_row.get("fault_position", "")),
        "true_reason_id": str(feedback_row.get("true_reason_id", "")),
        "true_reason_type": str(feedback_row.get("true_reason_type", "")),
        "true_stage": normalize_stage(feedback_row.get("true_stage", "")),
        "top1_candidate_id": "",
        "top1_candidate_type": "",
        "top1_stage": "",
        "top1_score": 0.0,
        "top1_hit": False,
        "top3_hit": False,
        "top6_hit": False,
        "type_hit": False,
        "stage_hit": False,
        "stage_top1_hit": False,
        "stage_top2_hit": False,
        "stage_top3_hit": False,
        "stage_top6_covered": False,
        "mrr": 0.0,
        "primary_stage": "",
        "primary_stage_name": "",
        "feedback_target_subsystem": "",
        "focus_stage_top3": "",
        "feedback_subsystem_top3": "",
        "top6_ids": "",
        "top6_types": "",
        "top6_scopes": "",
        "top6_score_sum": 0.0,
        "root_component_different": False,
        "has_rca_root_candidate": False,
        "error": str(error),
    }


def rate(series: pd.Series) -> float:
    return round(float(series.astype(bool).mean()), 6) if len(series) else 0.0


def mean(series: pd.Series) -> float:
    return round(float(pd.to_numeric(series, errors="coerce").fillna(0).mean()), 6) if len(series) else 0.0


def grouped_metrics(df: pd.DataFrame, group_col: str) -> dict[str, dict[str, float]]:
    if df.empty or group_col not in df.columns:
        return {}
    metrics: dict[str, dict[str, float]] = {}
    for key, group in df.groupby(group_col, dropna=False):
        metrics[str(key)] = {
            "case_count": int(len(group)),
            "top1_hit_rate": rate(group["top1_hit"]),
            "top3_hit_rate": rate(group["top3_hit"]),
            "top6_hit_rate": rate(group["top6_hit"]),
            "mean_mrr": mean(group["mrr"]),
            "stage_accuracy": rate(group["stage_hit"]),
            "stage_top1_accuracy": rate(group["stage_top1_hit"]),
            "stage_top2_accuracy": rate(group["stage_top2_hit"]),
            "stage_top3_accuracy": rate(group["stage_top3_hit"]),
            "stage_top6_coverage": rate(group["stage_top6_covered"]),
        }
    return metrics


def scope_distribution(df: pd.DataFrame) -> dict[str, dict[str, float]]:
    scopes: list[str] = []
    for text in df.get("top6_scopes", pd.Series(dtype=str)).fillna("").astype(str):
        scopes.extend([scope for scope in text.split("|") if scope])
    total = len(scopes)
    result = {}
    for scope in SCOPE_KEYS:
        count = scopes.count(scope)
        result[scope] = {"count": count, "ratio": round(count / total, 6) if total else 0.0}
    return result


def overall_metrics(df: pd.DataFrame, total_cases: int, skipped_cases: int) -> dict[str, Any]:
    ok = df[df["error"].fillna("") == ""]
    avg_top6 = mean(ok["top6_score_sum"] / 6.0) if not ok.empty else 0.0
    return {
        "total_cases": int(total_cases),
        "evaluated_cases": int(len(ok)),
        "skipped_cases": int(skipped_cases + int((df["error"].fillna("") != "").sum())),
        "top1_hit_rate": rate(ok["top1_hit"]),
        "top3_hit_rate": rate(ok["top3_hit"]),
        "top6_hit_rate": rate(ok["top6_hit"]),
        "mean_mrr": mean(ok["mrr"]),
        "type_hit_rate": rate(ok["type_hit"]),
        "stage_accuracy": rate(ok["stage_hit"]),
        "stage_top1_accuracy": rate(ok["stage_top1_hit"]),
        "stage_top2_accuracy": rate(ok["stage_top2_hit"]),
        "stage_top3_accuracy": rate(ok["stage_top3_hit"]),
        "stage_top6_coverage": rate(ok["stage_top6_covered"]),
        "average_top1_score": mean(ok["top1_score"]),
        "average_top6_score": avg_top6,
        "scope_distribution": scope_distribution(ok),
    }


def hydraulic_metrics(df: pd.DataFrame) -> dict[str, Any]:
    ok = df[df["error"].fillna("") == ""]
    with_rca = ok[ok["has_rca_root_candidate"].astype(bool)]
    without_rca = ok[~ok["has_rca_root_candidate"].astype(bool)]
    return {
        "root_component_diff_ratio": rate(ok["root_component_different"]),
        "top6_contains_rca_root_candidate_ratio": rate(ok["has_rca_root_candidate"]),
        "top6_hit_rate_with_rca_root_candidate": rate(with_rca["top6_hit"]),
        "top6_hit_rate_without_rca_root_candidate": rate(without_rca["top6_hit"]),
    }


def render_summary(domain: str, result: dict[str, Any]) -> str:
    overall = result["overall_metrics"]
    lines = [
        f"# {domain} Top6追溯批量评估摘要",
        "",
        "## 总体指标",
        f"- 评估样本数量：{overall['evaluated_cases']}",
        f"- 跳过/失败样本数量：{overall['skipped_cases']}",
        f"- Top1命中率：{overall['top1_hit_rate']}",
        f"- Top3命中率：{overall['top3_hit_rate']}",
        f"- Top6命中率：{overall['top6_hit_rate']}",
        f"- MRR：{overall['mean_mrr']}",
        f"- 类型命中率：{overall['type_hit_rate']}",
        f"- 阶段归因准确率：{overall['stage_accuracy']}",
        f"- 阶段Top1准确率：{overall['stage_top1_accuracy']}",
        f"- 阶段Top2准确率：{overall['stage_top2_accuracy']}",
        f"- 阶段Top3覆盖率：{overall['stage_top3_accuracy']}",
        f"- 阶段Top6覆盖率：{overall['stage_top6_coverage']}",
        "",
        "阶段Top3覆盖率表示真实生命周期阶段是否出现在系统建议重点关注的前三个阶段中，相比单一 primary_stage 更适合工程追溯系统的多阶段联合复核场景。",
        "",
        "## 不同 fault_type 的表现",
        markdown_metric_table(result["fault_type_metrics"]),
        "",
        "## 不同 true_stage 的表现",
        markdown_metric_table(result["stage_metrics"]),
        "",
        "## 候选来源分布",
        "| scope | count | ratio |",
        "| --- | --- | --- |",
    ]
    for scope, item in overall["scope_distribution"].items():
        lines.append(f"| {scope} | {item['count']} | {item['ratio']} |")
    if domain == "hydraulic":
        h = result.get("hydraulic_metrics", {})
        lines.extend(
            [
                "",
                "## hydraulic RCA 根因部件候选作用",
                f"- RCA根因部件与反馈部件不一致比例：{h.get('root_component_diff_ratio', 0.0)}",
                f"- Top6包含RCA根因部件候选比例：{h.get('top6_contains_rca_root_candidate_ratio', 0.0)}",
                f"- 包含RCA根因部件候选时Top6命中率：{h.get('top6_hit_rate_with_rca_root_candidate', 0.0)}",
                f"- 不包含RCA根因部件候选时Top6命中率：{h.get('top6_hit_rate_without_rca_root_candidate', 0.0)}",
            ]
        )
    lines.extend(
        [
            "",
            "## 简短结论",
            f"本次评估显示，{domain} 入口的 Top6 命中率为 {overall['top6_hit_rate']}，阶段归因准确率为 {overall['stage_accuracy']}，阶段Top3覆盖率为 {overall['stage_top3_accuracy']}。后续可结合错误样本进一步优化候选池、关系权重和重排序策略。",
            "",
        ]
    )
    return "\n".join(lines)


def markdown_metric_table(metrics: dict[str, dict[str, Any]]) -> str:
    lines = [
        "| group | case_count | top1 | top3 | top6 | mrr | stage_acc | stage_top1 | stage_top2 | stage_top3 | stage_top6 |",
        "| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |",
    ]
    for key, item in metrics.items():
        lines.append(
            f"| {key} | {item['case_count']} | {item['top1_hit_rate']} | {item['top3_hit_rate']} | {item['top6_hit_rate']} | {item['mean_mrr']} | {item['stage_accuracy']} | {item.get('stage_top1_accuracy', 0.0)} | {item.get('stage_top2_accuracy', 0.0)} | {item.get('stage_top3_accuracy', 0.0)} | {item.get('stage_top6_coverage', 0.0)} |"
        )
    return "\n".join(lines)


def evaluate_domain(
    domain: str,
    sample_size: int,
    seed: int,
    exclude_normal: bool,
    save_each_reasoning: bool,
    use_eval_split: bool = False,
) -> dict[str, Any]:
    model_variant = "eval" if use_eval_split else "default"
    prepared = prepare_reasoning_context(domain, model_variant=model_variant)
    feedback = prepared["data"]["feedback"]
    leak_check: dict[str, Any] = {"leak_count": 0, "leak_ratio": 0.0}

    if use_eval_split:
        test_cases, test_cases_path = load_eval_test_cases(domain)
        samples = select_eval_samples(test_cases, sample_size, seed)
        skipped_by_filter = 0
        total_cases = len(test_cases)
        model_dir = prepared["data"]["paths"]["model_dir"]
        leak_check = check_eval_split_leakage(domain, test_cases)
        print(f"evaluation_mode: eval_split")
        print(f"test_cases_file: {test_cases_path}")
        print(f"model_dir: {model_dir}")
        print(f"leak_count: {leak_check['leak_count']}")
        print(f"leak_ratio: {leak_check['leak_ratio']}")
    else:
        samples, skipped_by_filter = select_samples(feedback, domain, sample_size, seed, exclude_normal)
        total_cases = len(feedback)

    rows = []
    for _, feedback_row in tqdm(samples.iterrows(), total=len(samples), desc=f"evaluate {domain}"):
        case_id = str(feedback_row["feedback_id"])
        try:
            reasoning = run_reasoning_for_case(
                domain,
                case_id,
                save_json=save_each_reasoning,
                seed=seed,
                prepared=prepared,
                model_variant=model_variant,
            )
            rows.append(row_from_reasoning(feedback_row, reasoning))
        except Exception as exc:
            rows.append(error_row(feedback_row, exc))
    df = pd.DataFrame(rows)
    ok = df[df["error"].fillna("") == ""]
    result = {
        "domain": domain,
        "evaluation_mode": "eval_split" if use_eval_split else "default",
        "model_dir": str(prepared["data"]["paths"]["model_dir"]),
        "overall_metrics": overall_metrics(df, total_cases=total_cases, skipped_cases=skipped_by_filter),
        "fault_type_metrics": grouped_metrics(ok, "fault_type"),
        "stage_metrics": grouped_metrics(ok, "true_stage"),
    }
    if use_eval_split:
        result["test_cases_file"] = leak_check.get("test_cases_file", str(test_cases_path))
        result["train_triples_file"] = leak_check.get("train_triples_file", "")
        result["leak_count"] = int(leak_check["leak_count"])
        result["leak_ratio"] = float(leak_check["leak_ratio"])
    result["scope_distribution"] = result["overall_metrics"]["scope_distribution"]
    if domain == "hydraulic":
        result["hydraulic_metrics"] = hydraulic_metrics(ok)
    output_dir = ROOT / "outputs" / domain / "reports"
    output_dir.mkdir(parents=True, exist_ok=True)
    suffix = "_eval" if use_eval_split else ""
    csv_path = output_dir / f"evaluation_result{suffix}.csv"
    json_path = output_dir / f"evaluation_result{suffix}.json"
    md_path = output_dir / f"evaluation_summary{suffix}.md"
    df.to_csv(csv_path, index=False, encoding="utf-8-sig")
    json_path.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    md_path.write_text(render_summary(domain, result), encoding="utf-8")
    result["output_files"] = {"csv": str(csv_path), "json": str(json_path), "md": str(md_path)}
    return result


def print_console(result: dict[str, Any]) -> None:
    overall = result["overall_metrics"]
    print(f"\ndomain: {result['domain']}")
    print(f"evaluation_mode: {result.get('evaluation_mode', 'default')}")
    if result.get("evaluation_mode") == "eval_split":
        print(f"test_cases_file: {result.get('test_cases_file', '')}")
        print(f"model_dir: {result.get('model_dir', '')}")
        print(f"leak_count: {result.get('leak_count', 0)}")
        print(f"leak_ratio: {result.get('leak_ratio', 0.0)}")
    print(f"evaluated_cases: {overall['evaluated_cases']}")
    print(f"skipped_cases: {overall['skipped_cases']}")
    print(f"top1_hit_rate: {overall['top1_hit_rate']}")
    print(f"top3_hit_rate: {overall['top3_hit_rate']}")
    print(f"top6_hit_rate: {overall['top6_hit_rate']}")
    print(f"mean_mrr: {overall['mean_mrr']}")
    print(f"type_hit_rate: {overall['type_hit_rate']}")
    print(f"stage_accuracy: {overall['stage_accuracy']}")
    print(f"stage_top1_accuracy: {overall['stage_top1_accuracy']}")
    print(f"stage_top2_accuracy: {overall['stage_top2_accuracy']}")
    print(f"stage_top3_accuracy: {overall['stage_top3_accuracy']}")
    print(f"stage_top6_coverage: {overall['stage_top6_coverage']}")
    print("output_csv:", result["output_files"]["csv"])
    print("output_json:", result["output_files"]["json"])
    print("output_md:", result["output_files"]["md"])


def main() -> None:
    args = parse_args()
    for domain in domains_for(args.domain):
        result = evaluate_domain(
            domain,
            args.sample_size,
            args.seed,
            args.exclude_normal,
            args.save_each_reasoning,
            args.use_eval_split,
        )
        print_console(result)


if __name__ == "__main__":
    main()
