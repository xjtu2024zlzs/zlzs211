from __future__ import annotations

import argparse
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "backend"))

from services.etl.common import (  # noqa: E402
    DEFAULT_ETL_OUTPUT_DIR,
    DEFAULT_INIT_SCRIPTS_DIR,
    DEFAULT_SEED_DATA_DIR,
    DEFAULT_TABULAX_ROOT,
    default_gt_path,
    ensure_dir,
)
from services.etl.gt_catalog_loader import load_gt_catalog  # noqa: E402
from services.etl.schema_loader import load_dossier_schema, load_source_schemas  # noqa: E402
from services.etl.tabulax_adapter import prepare_tabulax_transforms  # noqa: E402


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run TabulaX for CF Rename + Cast mappings.")
    parser.add_argument("--gt", type=Path, default=default_gt_path())
    parser.add_argument("--seed-data", type=Path, default=DEFAULT_SEED_DATA_DIR)
    parser.add_argument("--init-scripts", type=Path, default=DEFAULT_INIT_SCRIPTS_DIR)
    parser.add_argument("--tabulax-root", type=Path, default=DEFAULT_TABULAX_ROOT)
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_ETL_OUTPUT_DIR)
    parser.add_argument("--model-name", default="gpt-4o-2024-05-13")
    parser.add_argument("--prompt-version", default="v001")
    parser.add_argument("--classifier-prompt-version", default="v002")
    parser.add_argument("--example-size", type=int, default=5)
    parser.add_argument("--sample-limit", type=int, default=10)
    parser.add_argument("--force", action="store_true")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    rules = load_gt_catalog(args.gt)
    output_dir = ensure_dir(args.output_dir)
    results = prepare_tabulax_transforms(
        rules,
        seed_data_dir=args.seed_data,
        output_path=output_dir / "tabulax_transform_results.json",
        tabulax_root=args.tabulax_root,
        source_schemas=load_source_schemas(args.init_scripts),
        dossier_schema=load_dossier_schema(args.init_scripts),
        model_name=args.model_name,
        prompt_version=args.prompt_version,
        classifier_prompt_version=args.classifier_prompt_version,
        example_size=args.example_size,
        sample_limit=args.sample_limit,
        force=args.force,
    )
    status_counts: dict[str, int] = {}
    for item in results.values():
        status = str(item.get("status") or "unknown")
        status_counts[status] = status_counts.get(status, 0) + 1
    print(
        {
            "transform_tasks": len(results),
            "status_counts": status_counts,
            "output": str(output_dir / "tabulax_transform_results.json"),
        }
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
