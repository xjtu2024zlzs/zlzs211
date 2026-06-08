from __future__ import annotations

import argparse
import subprocess
import sys
from collections import Counter
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "backend"))

from services.etl.common import DEFAULT_ETL_OUTPUT_DIR, DEFAULT_INIT_SCRIPTS_DIR, DEFAULT_SEED_DATA_DIR, default_gt_path, default_join_plan_path, read_json  # noqa: E402
from services.etl.dictionary_loader import load_dictionary_lookup  # noqa: E402
from services.etl.executor import execute_etl_plan_to_csv  # noqa: E402
from services.etl.gt_catalog_loader import count_by_mapping_type, load_gt_catalog  # noqa: E402
from services.etl.join_plan_loader import infer_join_plans, save_join_plans  # noqa: E402
from services.etl.planner import build_etl_plan  # noqa: E402
from services.etl.reporting import environment_status  # noqa: E402
from services.etl.validator import write_validation_artifacts  # noqa: E402


EXPECTED_COUNTS = {
    "Direct Copy": 164,
    "Rename + Cast": 81,
    "Lookup Normalize": 85,
    "Joined Copy": 127,
}


def expect(condition: bool, message: str) -> None:
    if not condition:
        raise AssertionError(message)


def conda_check(env_name: str, imports: str) -> None:
    command = ["conda", "run", "-n", env_name, "python", "-c", imports]
    completed = subprocess.run(command, capture_output=True, text=True, timeout=60)
    expect(completed.returncode == 0, f"{env_name} dependency check failed: {completed.stderr}")


def run_acceptance(*, with_tabulax: bool) -> dict:
    conda_check("py310-magneto", "import fastapi,pandas,sqlalchemy")
    conda_check("py311-tabulax", "import openai,pandas,scipy,sklearn")

    env = environment_status()
    expect(env["py310_magneto"]["ok"], "py310-magneto environment is not healthy")
    expect(env["py311_tabulax"]["ok"], "py311-tabulax environment is not healthy")

    rules = load_gt_catalog(default_gt_path())
    counts = count_by_mapping_type(rules)
    expect(len(rules) == 457, f"expected 457 GT rows, got {len(rules)}")
    expect(counts == EXPECTED_COUNTS, f"unexpected mapping counts: {counts}")

    plans = infer_join_plans(rules, DEFAULT_INIT_SCRIPTS_DIR)
    save_join_plans(default_join_plan_path(), plans)
    expect(len(plans) == 44, f"expected 44 join plans, got {len(plans)}")

    transform_results = {}
    if with_tabulax:
        command = [
            "conda",
            "run",
            "-n",
            "py311-tabulax",
            "python",
            str(ROOT / "scripts" / "cf_run_tabulax_transforms.py"),
        ]
        completed = subprocess.run(command, cwd=str(ROOT), text=True)
        expect(completed.returncode == 0, "TabulaX transform generation failed")
        transform_path = DEFAULT_ETL_OUTPUT_DIR / "tabulax_transform_results.json"
        transform_results = read_json(transform_path) if transform_path.exists() else {}

    plan = build_etl_plan(rules, join_plans=plans, transform_results=transform_results).to_dict()
    plan_path = DEFAULT_ETL_OUTPUT_DIR / "etl_plan.json"
    plan_path.write_text(__import__("json").dumps(plan, ensure_ascii=False, indent=2), encoding="utf-8")

    expect(len(plan["blocked_fields"]) == 0 or with_tabulax, f"unexpected blocked fields: {len(plan['blocked_fields'])}")
    expect(len(plan["tabulax_required"]) == 81, f"expected 81 TabulaX tasks, got {len(plan['tabulax_required'])}")

    dictionary_lookup = load_dictionary_lookup(DEFAULT_SEED_DATA_DIR / "lookup_dictionary_mappings.csv")
    migration_report = execute_etl_plan_to_csv(
        plan,
        seed_data_dir=DEFAULT_SEED_DATA_DIR,
        output_dir=DEFAULT_ETL_OUTPUT_DIR,
        dictionary_lookup=dictionary_lookup,
        limit=3,
    )
    (DEFAULT_ETL_OUTPUT_DIR / "raw_migration_report.json").write_text(
        __import__("json").dumps(migration_report, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    write_validation_artifacts(plan, migration_report, seed_data_dir=DEFAULT_SEED_DATA_DIR, output_dir=DEFAULT_ETL_OUTPUT_DIR)

    error_counts = Counter(item["reason"] for item in migration_report.get("field_errors") or [])
    expect(error_counts.get("missing_join_plan_definition", 0) == 0, "missing join plan error found")
    expect(error_counts.get("dictionary_miss", 0) == 0, "dictionary miss found")
    if not with_tabulax:
        expect(set(error_counts) <= {"tabulax_pending"}, f"unexpected skip-llm errors: {dict(error_counts)}")

    return {
        "rules": len(rules),
        "mapping_counts": counts,
        "join_plans": len(plans),
        "target_tables": len(migration_report.get("target_table_rows") or {}),
        "field_error_counts": dict(error_counts),
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="CF ETL acceptance checks.")
    parser.add_argument("--with-tabulax", action="store_true", help="Run real TabulaX LLM transform generation.")
    parser.add_argument("--skip-llm", action="store_true", help="Skip real TabulaX LLM transform generation.")
    args = parser.parse_args()
    result = run_acceptance(with_tabulax=args.with_tabulax and not args.skip_llm)
    print(result)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
