from __future__ import annotations

import argparse
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "backend"))

from services.etl.common import (  # noqa: E402
    DEFAULT_ETL_OUTPUT_DIR,
    DEFAULT_INIT_SCRIPTS_DIR,
    default_gt_path,
    default_join_plan_path,
    ensure_dir,
    read_json,
    write_json,
)
from services.etl.gt_catalog_loader import count_by_mapping_type, load_gt_catalog, validate_gt_catalog  # noqa: E402
from services.etl.join_plan_loader import infer_join_plans, load_join_plans, save_join_plans  # noqa: E402
from services.etl.planner import build_etl_plan  # noqa: E402


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Build a CF GT-driven ETL plan.")
    parser.add_argument("--gt", type=Path, default=default_gt_path())
    parser.add_argument("--init-scripts", type=Path, default=DEFAULT_INIT_SCRIPTS_DIR)
    parser.add_argument("--join-plan", type=Path, default=default_join_plan_path())
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_ETL_OUTPUT_DIR)
    parser.add_argument("--transforms", type=Path, default=None)
    parser.add_argument("--infer-join-plans", action="store_true", help="Infer and write join_plan_definitions.json from source FKs.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    rules = load_gt_catalog(args.gt)
    issues = validate_gt_catalog(rules)

    if args.infer_join_plans or not args.join_plan.exists():
        plans = infer_join_plans(rules, args.init_scripts)
        save_join_plans(args.join_plan, plans)
    else:
        plans = load_join_plans(args.join_plan)

    transform_results = read_json(args.transforms) if args.transforms and args.transforms.exists() else {}
    plan = build_etl_plan(rules, join_plans=plans, transform_results=transform_results).to_dict()

    output_dir = ensure_dir(args.output_dir)
    write_json(output_dir / "etl_plan.json", plan)
    write_json(output_dir / "gt_validation_issues.json", issues)

    print(
        {
            "rules": len(rules),
            "mapping_counts": count_by_mapping_type(rules),
            "join_plans": len(plans),
            "blocked_fields": len(plan["blocked_fields"]),
            "tabulax_required": len(plan["tabulax_required"]),
            "etl_plan": str(output_dir / "etl_plan.json"),
        }
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
