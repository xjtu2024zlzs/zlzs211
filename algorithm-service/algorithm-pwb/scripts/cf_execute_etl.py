from __future__ import annotations

import argparse
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "backend"))

from services.etl.common import DEFAULT_ETL_OUTPUT_DIR, DEFAULT_SEED_DATA_DIR, read_json, write_json  # noqa: E402
from services.etl.dictionary_loader import load_dictionary_lookup  # noqa: E402
from services.etl.executor import execute_etl_plan_to_csv  # noqa: E402
from services.etl.validator import write_validation_artifacts  # noqa: E402


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Execute the CF ETL plan to CSV artifacts.")
    parser.add_argument("--plan", type=Path, default=DEFAULT_ETL_OUTPUT_DIR / "etl_plan.json")
    parser.add_argument("--seed-data", type=Path, default=DEFAULT_SEED_DATA_DIR)
    parser.add_argument("--dictionary", type=Path, default=DEFAULT_SEED_DATA_DIR / "lookup_dictionary_mappings.csv")
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_ETL_OUTPUT_DIR)
    parser.add_argument("--limit", type=int, default=None, help="Optional row limit per ETL group.")
    parser.add_argument("--target-dsn", default=None, help="Optional PostgreSQL DSN for writing migrated rows to dossier DB.")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    plan = read_json(args.plan)
    dictionary_lookup = load_dictionary_lookup(args.dictionary)
    migration_report = execute_etl_plan_to_csv(
        plan,
        seed_data_dir=args.seed_data,
        output_dir=args.output_dir,
        dictionary_lookup=dictionary_lookup,
        limit=args.limit,
        target_dsn=args.target_dsn,
    )
    write_json(args.output_dir / "raw_migration_report.json", migration_report)
    summary = write_validation_artifacts(
        plan,
        migration_report,
        seed_data_dir=args.seed_data,
        output_dir=args.output_dir,
    )
    print(
        {
            "target_tables": len(migration_report.get("target_table_rows") or {}),
            "field_errors": len(migration_report.get("field_errors") or []),
            "blocked_fields": summary["blocked_fields"],
            "output_dir": str(args.output_dir),
        }
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
