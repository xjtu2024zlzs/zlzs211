from __future__ import annotations

import argparse
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "backend"))

from services.etl.common import DEFAULT_ETL_OUTPUT_DIR, DEFAULT_SEED_DATA_DIR, read_json  # noqa: E402
from services.etl.validator import write_validation_artifacts  # noqa: E402


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Validate CF ETL CSV outputs.")
    parser.add_argument("--plan", type=Path, default=DEFAULT_ETL_OUTPUT_DIR / "etl_plan.json")
    parser.add_argument("--report", type=Path, default=DEFAULT_ETL_OUTPUT_DIR / "raw_migration_report.json")
    parser.add_argument("--seed-data", type=Path, default=DEFAULT_SEED_DATA_DIR)
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_ETL_OUTPUT_DIR)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    plan = read_json(args.plan)
    report = read_json(args.report) if args.report.exists() else {}
    summary = write_validation_artifacts(plan, report, seed_data_dir=args.seed_data, output_dir=args.output_dir)
    print(summary)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
