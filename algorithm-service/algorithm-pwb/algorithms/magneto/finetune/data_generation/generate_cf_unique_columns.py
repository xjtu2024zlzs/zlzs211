"""Generate CF target-column metadata for MPNet fine-tuning.

The CF thesis experiment uses only target column names that occur exactly once
across all dossier target tables. Repeated public fields are intentionally
excluded from fine-tuning classes and handled later by table context,
ConstraintBoost, and LLM reranking.
"""

import csv
import json
from collections import defaultdict
from pathlib import Path

import pandas as pd


MAX_VALUES_PER_COLUMN = 50
EXPECTED_UNIQUE_COLUMNS = 192
EXPECTED_DUPLICATE_COLUMNS = 101


def extract_table_name(filename):
    table_name = Path(filename).stem
    if table_name.startswith("dossier_"):
        table_name = table_name[len("dossier_") :]
    return table_name


def _sample_column_values(series, max_values=MAX_VALUES_PER_COLUMN):
    value_counts = series.dropna().value_counts()
    if len(value_counts) > max_values:
        value_counts = value_counts.head(max_values)
    return [str(value) for value in value_counts.index.tolist()]


def build_unique_column_artifacts(target_dir, max_values=MAX_VALUES_PER_COLUMN):
    target_dir = Path(target_dir)
    occurrences = defaultdict(list)
    table_stats = []
    total_column_positions = 0

    for csv_path in sorted(target_dir.glob("*.csv")):
        table_name = extract_table_name(csv_path.name)
        df = pd.read_csv(csv_path, low_memory=False)
        table_stats.append(
            {
                "table": table_name,
                "columns": len(df.columns),
                "rows": len(df),
            }
        )
        total_column_positions += len(df.columns)

        for column_name in df.columns:
            occurrences[column_name].append(
                {
                    "table": table_name,
                    "values": _sample_column_values(df[column_name], max_values),
                }
            )

    unique_columns = {}
    skipped_duplicates = []

    for column_name in sorted(occurrences):
        column_occurrences = occurrences[column_name]
        if len(column_occurrences) == 1:
            unique_columns[column_name] = column_occurrences[0]
            continue

        skipped_duplicates.append(
            {
                "column_name": column_name,
                "occurrence_count": len(column_occurrences),
                "tables": ";".join(item["table"] for item in column_occurrences),
            }
        )

    stats = {
        "table_count": len(table_stats),
        "total_column_positions": total_column_positions,
        "distinct_column_count": len(occurrences),
        "unique_column_count": len(unique_columns),
        "duplicate_column_count": len(skipped_duplicates),
        "table_stats": table_stats,
    }
    return unique_columns, skipped_duplicates, stats


def write_unique_column_artifacts(unique_columns, skipped_duplicates, output_dir):
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    json_path = output_dir / "cf_unique_columns.json"
    report_path = output_dir / "cf_unique_columns_skipped_duplicates.csv"

    with json_path.open("w", encoding="utf-8") as handle:
        json.dump(unique_columns, handle, indent=2, ensure_ascii=False)

    with report_path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(
            handle,
            fieldnames=["column_name", "occurrence_count", "tables"],
        )
        writer.writeheader()
        writer.writerows(skipped_duplicates)

    return json_path, report_path


def generate_cf_unique_columns():
    project_root = Path(__file__).resolve().parents[4]
    target_dir = project_root / "data" / "magneto_cf" / "target-tables"
    output_dir = project_root / "data" / "unique_columns"

    print(f"Reading CF target tables from: {target_dir}")
    print(f"Writing unique-column artifacts to: {output_dir}")

    if not target_dir.exists():
        raise FileNotFoundError(f"Target table directory does not exist: {target_dir}")

    unique_columns, skipped_duplicates, stats = build_unique_column_artifacts(target_dir)
    json_path, report_path = write_unique_column_artifacts(
        unique_columns, skipped_duplicates, output_dir
    )

    print("\nCF unique-column extraction complete")
    print(f"  Target tables: {stats['table_count']}")
    print(f"  Total column positions: {stats['total_column_positions']}")
    print(f"  Distinct column names: {stats['distinct_column_count']}")
    print(
        f"  Retained unique column names: {stats['unique_column_count']} "
        f"(expected current CF version: {EXPECTED_UNIQUE_COLUMNS})"
    )
    print(
        f"  Skipped duplicate column names: {stats['duplicate_column_count']} "
        f"(expected current CF version: {EXPECTED_DUPLICATE_COLUMNS})"
    )
    print(f"  JSON: {json_path}")
    print(f"  Duplicate report: {report_path}")

    return unique_columns


if __name__ == "__main__":
    generate_cf_unique_columns()
