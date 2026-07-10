from __future__ import annotations

import argparse
import json
from pathlib import Path

import numpy as np
import pandas as pd


ROOT = Path(__file__).resolve().parents[1]

EXPECTED_ROWS = {
    "bearing": {
        "bearing_component.csv": 5000,
        "bearing_design.csv": 800,
        "bearing_material.csv": 500,
        "bearing_manufacturing.csv": 1200,
        "bearing_assembly.csv": 5000,
        "bearing_inspection.csv": 7000,
        "bearing_feedback.csv": 5000,
    },
    "hydraulic": {
        "hydraulic_component.csv": 5000,
        "hydraulic_design.csv": 1200,
        "pipe_design_param.csv": 800,
        "hydraulic_material.csv": 600,
        "hydraulic_manufacturing.csv": 1200,
        "hydraulic_assembly.csv": 5000,
        "hydraulic_inspection.csv": 7000,
        "hydraulic_feedback.csv": 5000,
        "hydraulic_rca_case.csv": 5000,
        "hydraulic_sensor_energy.csv": 5000,
        "hydraulic_component_diagnosis.csv": 60000,
        "hydraulic_subtype_probability.csv": 5000 * 41,
    },
}

FORBIDDEN_NAMES = ["调速阀/节流阀", "密封件/管路总成"]

COMPONENTS = {
    "C001": "液压泵总成",
    "C002": "方向控制阀",
    "C003": "作动筒",
    "C004": "溢流阀",
    "C005": "蓄能器",
    "C006": "冷却器",
    "C007": "过滤器",
    "C008": "液压油箱",
    "C009": "单向阀",
    "C010": "节流阀",
    "C011": "管路总成",
    "C012": "卸荷阀",
}

SENSORS = ["PS1", "PS2", "PS3", "PS4", "PS5", "PS6", "EPS1", "FS1", "FS2", "TS1", "TS2", "TS3", "TS4", "VS1"]

SENSOR_MAP = {
    "C001": {"VS1": 0.50, "PS1": 0.30, "TS1": 0.20},
    "C002": {"PS3": 0.55, "FS1": 0.30, "PS4": 0.15},
    "C003": {"PS4": 0.35, "PS5": 0.35, "FS2": 0.30},
    "C004": {"PS2": 0.60, "EPS1": 0.40},
    "C005": {"PS6": 0.75, "PS2": 0.25},
    "C006": {"TS2": 0.50, "TS3": 0.50},
    "C007": {"TS4": 0.55, "FS1": 0.45},
    "C008": {"FS1": 0.40, "TS3": 0.35, "FS2": 0.25},
    "C009": {"PS2": 0.45, "PS3": 0.35, "FS1": 0.20},
    "C010": {"FS1": 0.40, "PS3": 0.30, "PS5": 0.30},
    "C011": {"PS1": 0.25, "PS2": 0.25, "PS6": 0.25, "VS1": 0.25},
    "C012": {"PS1": 0.30, "EPS1": 0.30, "FS1": 0.25, "TS1": 0.15},
}


def read_csv(path: Path) -> pd.DataFrame:
    return pd.read_csv(path, encoding="utf-8-sig")


def pass_item(name: str, passed: bool, details=None) -> dict:
    return {"name": name, "passed": bool(passed), "details": details if details is not None else {}}


def check_files(domain: str, data_dir: Path) -> tuple[dict[str, pd.DataFrame], list[dict]]:
    dataframes: dict[str, pd.DataFrame] = {}
    checks = []
    for filename, expected in EXPECTED_ROWS[domain].items():
        path = data_dir / filename
        exists = path.exists()
        row_count = None
        if exists:
            df = read_csv(path)
            row_count = len(df)
            dataframes[filename] = df
        checks.append(
            pass_item(
                f"{filename} exists and row count",
                exists and row_count is not None and row_count >= expected,
                {"path": str(path), "expected_min_rows": expected, "actual_rows": row_count},
            )
        )
    return dataframes, checks


def check_forbidden_names(dataframes: dict[str, pd.DataFrame]) -> list[dict]:
    hits = []
    c010_bad, c011_bad = [], []
    for filename, df in dataframes.items():
        object_cols = [col for col in df.columns if df[col].dtype == object]
        for col in object_cols:
            values = df[col].dropna().astype(str)
            for forbidden in FORBIDDEN_NAMES:
                count = int(values.str.contains(forbidden, regex=False).sum())
                if count:
                    hits.append({"file": filename, "column": col, "forbidden": forbidden, "count": count})
            if "component_code" in df.columns and col in {"component_name", "root_component_name"}:
                bad_c010 = df[(df["component_code"].astype(str) == "C010") & (df[col].astype(str) != "节流阀")]
                bad_c011 = df[(df["component_code"].astype(str) == "C011") & (df[col].astype(str) != "管路总成")]
                if len(bad_c010):
                    c010_bad.append({"file": filename, "column": col, "count": int(len(bad_c010))})
                if len(bad_c011):
                    c011_bad.append({"file": filename, "column": col, "count": int(len(bad_c011))})
        if "root_component_code" in df.columns and "root_component_name" in df.columns:
            bad_c010 = df[(df["root_component_code"].astype(str) == "C010") & (df["root_component_name"].astype(str) != "节流阀")]
            bad_c011 = df[(df["root_component_code"].astype(str) == "C011") & (df["root_component_name"].astype(str) != "管路总成")]
            if len(bad_c010):
                c010_bad.append({"file": filename, "column": "root_component_name", "count": int(len(bad_c010))})
            if len(bad_c011):
                c011_bad.append({"file": filename, "column": "root_component_name", "count": int(len(bad_c011))})
    return [
        pass_item("forbidden names absent", len(hits) == 0, {"hits": hits}),
        pass_item("C010 only uses 节流阀", len(c010_bad) == 0, {"violations": c010_bad}),
        pass_item("C011 only uses 管路总成", len(c011_bad) == 0, {"violations": c011_bad}),
    ]


def check_feedback_common(feedback: pd.DataFrame, domain: str) -> list[dict]:
    checks = []
    empty_reason = int(feedback["true_reason_id"].isna().sum() + (feedback["true_reason_id"].astype(str).str.strip() == "").sum())
    stage_dist = feedback["true_stage"].value_counts(normalize=True).round(4).to_dict()
    fault_dist = feedback["fault_type"].value_counts(normalize=True).round(4).to_dict()
    non_normal = feedback if domain == "hydraulic" else feedback[feedback["fault_type"] != "normal"]
    non_normal_stage_dist = non_normal["true_stage"].value_counts(normalize=True).round(4).to_dict() if len(non_normal) else {}
    max_fault_share = float(max(fault_dist.values())) if fault_dist else 1.0
    required_stages = {"design", "material", "manufacturing", "assembly", "inspection", "operation"}
    covered_stages = set(non_normal["true_stage"].dropna().astype(str).unique())
    checks.append(pass_item("true_reason_id not empty", empty_reason == 0, {"empty_count": empty_reason}))
    checks.append(
        pass_item(
            "true_stage distribution reasonable",
            required_stages.issubset(covered_stages) and (max(non_normal_stage_dist.values()) if non_normal_stage_dist else 1.0) <= 0.45,
            {"stage_distribution": stage_dist, "non_normal_stage_distribution": non_normal_stage_dist},
        )
    )
    checks.append(pass_item("fault_type distribution not over-skewed", max_fault_share <= 0.70, {"fault_distribution": fault_dist}))
    return checks


def check_bearing(dataframes: dict[str, pd.DataFrame]) -> list[dict]:
    feedback = dataframes.get("bearing_feedback.csv")
    if feedback is None:
        return [pass_item("bearing feedback checks", False, {"error": "bearing_feedback.csv missing"})]
    checks = check_feedback_common(feedback, "bearing")
    valid_labels = {"Normal", "IR007", "IR014", "IR021", "B007", "B014", "B021", "OR007", "OR014", "OR021"}
    bad_labels = sorted(set(feedback["raw_label"].astype(str)) - valid_labels)
    checks.append(pass_item("bearing labels are valid", len(bad_labels) == 0, {"bad_labels": bad_labels}))
    return checks


def check_pipe_constraints(pipe: pd.DataFrame) -> dict:
    if pipe.empty:
        return {"passed": False, "violations": {"empty": True}}
    checks = {
        "L1_range": int((~pipe["L1"].between(80, 450)).sum()),
        "L2_range": int((~pipe["L2"].between(80, 450)).sum()),
        "theta1_range": int((~pipe["theta1"].between(60, 160)).sum()),
        "theta2_range": int((~pipe["theta2"].between(60, 160)).sum()),
        "R_range": int((~pipe["R"].between(5, 40)).sum()),
        "L3_min": int((pipe["L3"] < 50).sum()),
        "straight_segment_min_2R": int(((pipe["L1"] < 2 * pipe["R"]) | (pipe["L2"] < 2 * pipe["R"]) | (pipe["L3"] < 2 * pipe["R"])).sum()),
    }
    return {"passed": all(v == 0 for v in checks.values()), "violations": checks}


def check_sensor_energy(feedback: pd.DataFrame, rca: pd.DataFrame, sensor: pd.DataFrame) -> dict:
    merged = feedback[["case_id"]].merge(rca[["case_id", "root_component_code"]], on="case_id").merge(sensor, on="case_id")
    if merged.empty:
        return {"passed": False, "details": {"error": "no merged sensor rows"}}
    related_means, unrelated_means = [], []
    for _, row in merged.iterrows():
        root_code = str(row["root_component_code"])
        related = list(SENSOR_MAP[root_code])
        unrelated = [sensor_name for sensor_name in SENSORS if sensor_name not in related]
        related_means.append(float(row[related].astype(float).mean()))
        unrelated_means.append(float(row[unrelated].astype(float).mean()))
    related_mean = float(np.mean(related_means))
    unrelated_mean = float(np.mean(unrelated_means))
    ratio = related_mean / max(unrelated_mean, 1e-6)
    return {"passed": ratio >= 1.35, "details": {"related_mean": round(related_mean, 5), "unrelated_mean": round(unrelated_mean, 5), "ratio": round(ratio, 4)}}


def check_root_component_rank(rca: pd.DataFrame, diagnosis: pd.DataFrame) -> dict:
    root = rca[["case_id", "root_component_code"]]
    merged = diagnosis.merge(root, on="case_id")
    root_rows = merged[merged["component_code"] == merged["root_component_code"]]
    if root_rows.empty:
        return {"passed": False, "details": {"error": "no root diagnosis rows"}}
    top3_rate = float((root_rows["rank_no"] <= 3).mean())
    top1_rate = float((root_rows["rank_no"] == 1).mean())
    return {"passed": top3_rate >= 0.85, "details": {"top3_rate": round(top3_rate, 4), "top1_rate": round(top1_rate, 4)}}


def check_hydraulic(dataframes: dict[str, pd.DataFrame]) -> list[dict]:
    required = ["hydraulic_feedback.csv", "hydraulic_rca_case.csv", "hydraulic_sensor_energy.csv", "hydraulic_component_diagnosis.csv", "pipe_design_param.csv"]
    missing = [name for name in required if name not in dataframes]
    if missing:
        return [pass_item("hydraulic checks", False, {"missing": missing})]
    feedback = dataframes["hydraulic_feedback.csv"]
    rca = dataframes["hydraulic_rca_case.csv"]
    sensor = dataframes["hydraulic_sensor_energy.csv"]
    diagnosis = dataframes["hydraulic_component_diagnosis.csv"]
    pipe = dataframes["pipe_design_param.csv"]
    checks = check_feedback_common(feedback, "hydraulic")
    sensor_check = check_sensor_energy(feedback, rca, sensor)
    rank_check = check_root_component_rank(rca, diagnosis)
    pipe_check = check_pipe_constraints(pipe)
    checks.append(pass_item("root component sensors higher than unrelated sensors", sensor_check["passed"], sensor_check["details"]))
    checks.append(pass_item("root component usually ranks top 3", rank_check["passed"], rank_check["details"]))
    checks.append(pass_item("pipe design constraints satisfied", pipe_check["passed"], pipe_check["violations"]))
    checks.append(pass_item("pipe design only references C011 components", check_pipe_only_c011(dataframes), {}))
    return checks


def check_pipe_only_c011(dataframes: dict[str, pd.DataFrame]) -> bool:
    components = dataframes.get("hydraulic_component.csv")
    pipe = dataframes.get("pipe_design_param.csv")
    if components is None or pipe is None:
        return False
    uid_to_code = components.set_index("component_uid")["component_code"]
    codes = pipe["component_uid"].map(uid_to_code)
    return bool((codes == "C011").all())


def write_report(domain: str, checks: list[dict]) -> Path:
    output_dir = ROOT / "outputs" / domain / "reports"
    output_dir.mkdir(parents=True, exist_ok=True)
    passed = all(item["passed"] for item in checks)
    report = {
        "domain": domain,
        "passed": passed,
        "summary": {
            "total_checks": len(checks),
            "passed_checks": int(sum(item["passed"] for item in checks)),
            "failed_checks": int(sum(not item["passed"] for item in checks)),
        },
        "checks": checks,
    }
    path = output_dir / "data_quality_report.json"
    path.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    return path


def main() -> None:
    parser = argparse.ArgumentParser(description="Check generated data quality.")
    parser.add_argument("--domain", choices=["bearing", "hydraulic"], required=True)
    args = parser.parse_args()

    data_dir = ROOT / "data" / args.domain
    dataframes, checks = check_files(args.domain, data_dir)
    checks.extend(check_forbidden_names(dataframes))
    if args.domain == "bearing":
        checks.extend(check_bearing(dataframes))
    else:
        checks.extend(check_hydraulic(dataframes))
    report_path = write_report(args.domain, checks)
    print(f"{report_path} passed={all(item['passed'] for item in checks)} checks={len(checks)}")


if __name__ == "__main__":
    main()
