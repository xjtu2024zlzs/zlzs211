import argparse
from dataclasses import dataclass
import json
import os
from pathlib import Path
import random

import pandas as pd
import tqdm


PROJECT_ROOT = Path(__file__).resolve().parents[4]
DEEPSEEK_DEFAULT_API_BASE = "https://api.deepseek.com"


@dataclass
class ApiConfig:
    api_key: str
    api_base: str
    api_key_source: str
    is_deepseek: bool


def is_deepseek_context(model, api_base=None):
    model_name = (model or "").lower()
    base_url = (api_base or "").lower()
    return model_name.startswith("deepseek") or "deepseek.com" in base_url


def mask_api_key(api_key):
    if not api_key:
        return "<missing>"
    if len(api_key) <= 8:
        return "*" * len(api_key)
    return f"{api_key[:3]}...{api_key[-4:]}"


def sanitize_error_message(message, api_key=None):
    text = str(message)
    if api_key:
        text = text.replace(api_key, mask_api_key(api_key))
    return text


def resolve_api_config(model, api_key_arg=None, api_base_arg=None, env=None):
    env = os.environ if env is None else env
    is_deepseek = is_deepseek_context(model, api_base_arg)
    api_base = api_base_arg or (DEEPSEEK_DEFAULT_API_BASE if is_deepseek else None)

    if api_key_arg:
        return ApiConfig(api_key_arg, api_base, "--api_key", is_deepseek)

    if is_deepseek and env.get("DEEPSEEK_API_KEY"):
        return ApiConfig(
            env["DEEPSEEK_API_KEY"],
            api_base,
            "DEEPSEEK_API_KEY",
            is_deepseek,
        )

    if env.get("OPENAI_API_KEY"):
        return ApiConfig(env["OPENAI_API_KEY"], api_base, "OPENAI_API_KEY", is_deepseek)

    return ApiConfig(None, api_base, "<missing>", is_deepseek)


class SemanticGenerator:
    def __init__(self, api_key, api_base=None):
        from openai import OpenAI

        self.api_key = api_key
        if api_base:
            self.client = OpenAI(api_key=self.api_key, base_url=api_base)
            print(f"Using custom API base: {api_base}")
        else:
            self.client = OpenAI(api_key=self.api_key)
            print("Using OpenAI-compatible default API base")

    def _generate_prompt(self, column_name, column_values):
        if len(column_values) > 0:
            prompt = (
                f"Given the table column '{column_name}' with values {column_values}, "
                "generate three alternative column names that adhere to typical "
                "database naming conventions such as underscores and abbreviations. "
                "Additionally, provide distinct, technically correct synonyms, "
                "variants, or abbreviations for the listed values "
                "For columns with numerical or datetime data, generate random "
                "numbers or dates appropriate to the column's semantic meaning. "
                "Ensure that each set does not exceed 15 values. "
                "Format your output as follows: "
                "alternative_name_1, value1, value2, value3, ...; "
                "alternative_name_2, value1, value2, value3, ...; "
                "alternative_name_3, value1, value2, value3, ... "
                "Ensure your response excludes additional information and quotations."
            )
        else:
            prompt = (
                f"Given the table column '{column_name}', generate three alternative "
                "column names that adhere to typical database naming conventions "
                "such as underscores and abbreviations. Additionally, suggest "
                "distinct, technically accurate values appropriate for the data type "
                "of the column. Ensure that each set does not exceed 15 values. "
                "Format your output as follows: "
                "alternative_name_1, value1, value2, value3, ...; "
                "alternative_name_2, value1, value2, value3, ...; "
                "alternative_name_3, value1, value2, value3, ... "
                "Ensure your response excludes additional information and quotations."
            )
        return prompt

    def get_semantic_matches(self, column_name, column_values, model="gpt-4o-mini"):
        prompt = self._generate_prompt(column_name, column_values)
        messages = [
            {
                "role": "system",
                "content": (
                    "You are an AI trained for generating alternative table column "
                    "names and appropriate value variants"
                ),
            },
            {
                "role": "user",
                "content": prompt,
            },
        ]
        response = self.client.chat.completions.create(
            model=model,
            messages=messages,
            temperature=0.3,
        )

        matches_content = response.choices[0].message.content or ""
        alternative_name_values = {}
        for match in matches_content.split(";"):
            match = match.strip().replace("\n", "")
            if not match or ", " not in match:
                continue
            alternative_name, values = match.split(", ", 1)
            alternative_name = alternative_name.strip().strip('"').strip("'")
            values = [value.strip() for value in values.split(",") if value.strip()]
            if alternative_name:
                alternative_name_values[alternative_name] = values
        return alternative_name_values


class ExactGenerator:
    def __init__(self, threshold=1):
        self.threshold = threshold

    def get_exact_matches(self, column_name, column_values):
        value_size = len(column_values)
        if random.random() < 0.3 or value_size < self.threshold:
            alternative_column_name = list(column_name)
            alternative_column_name[
                random.randint(0, len(alternative_column_name) - 1)
            ] = random.choice("abcdefghijklmnopqrstuvwxyz0123456789 ")
            alternative_column_name = "".join(alternative_column_name)
        else:
            alternative_column_name = column_name

        if value_size < self.threshold:
            return {
                f"{column_name}_1": [],
                f"{alternative_column_name}_2": [],
            }

        return {
            f"{column_name}_1": random.sample(
                column_values, random.randint(1, min(value_size, 15))
            ),
            f"{alternative_column_name}_2": random.sample(
                column_values, random.randint(1, min(value_size, 15))
            ),
        }


def get_column_values(column_data):
    if isinstance(column_data, dict):
        return [str(value) for value in column_data.get("values", [])]
    return [str(value) for value in column_data]


def limit_unique_columns(unique_columns, limit):
    if limit is None:
        return dict(unique_columns)
    if limit < 0:
        raise ValueError("--limit must be greater than or equal to 0")
    return dict(list(unique_columns.items())[:limit])


def is_complete_match_entry(entry):
    return (
        isinstance(entry, dict)
        and "table" not in entry
        and bool(entry.get("original"))
        and bool(entry.get("exact"))
        and len(entry.get("semantic", {})) >= 3
    )


def find_incomplete_match_keys(matches, known_columns):
    known_column_names = set(known_columns)
    return [
        column_name
        for column_name, entry in matches.items()
        if column_name in known_column_names and not is_complete_match_entry(entry)
    ]


def build_match_entry(
    column_name,
    column_data,
    exact_generator,
    semantic_generator,
    llm_model,
):
    column_values = get_column_values(column_data)
    matches = {"exact": {}, "semantic": {}, "original": {}}

    values = (
        column_values
        if len(column_values) < 15
        else random.sample(column_values, 15)
    )
    matches["original"] = {column_name: values}

    exact_matches = exact_generator.get_exact_matches(column_name, column_values)
    if exact_matches:
        matches["exact"].update(exact_matches)

    semantic_matches = {}
    for _ in range(3):
        semantic_matches = semantic_generator.get_semantic_matches(
            column_name, column_values, model=llm_model
        )
        if len(semantic_matches) >= 3:
            break

    if len(semantic_matches) < 3:
        raise RuntimeError(
            f"Expected at least 3 semantic variants for {column_name}, "
            f"got {len(semantic_matches)}."
        )

    matches["semantic"].update(dict(list(semantic_matches.items())[:3]))
    return matches


def ensure_generation_progress(
    total_columns,
    processed_selected,
    generated_count,
    llm_model=None,
    api_base=None,
    api_key_source=None,
    api_key=None,
    first_error=None,
):
    if total_columns > processed_selected and generated_count == 0:
        details = [
            "No new complete synthetic columns were generated.",
            "Check the API key, API base URL, and model name.",
        ]
        if llm_model:
            details.append(f"model={llm_model}")
        if api_base:
            details.append(f"api_base={api_base}")
        if api_key_source:
            details.append(f"api_key_source={api_key_source}")
        if first_error:
            details.append(
                f"first_error={sanitize_error_message(first_error, api_key)}"
            )
        raise RuntimeError(" ".join(details))


def generate_matches(
    dataset,
    unique_columns,
    llm_model="gpt-4o-mini",
    api_key=None,
    api_base=None,
    api_key_source=None,
    limit=None,
):
    selected_columns = limit_unique_columns(unique_columns, limit)
    output_dir = PROJECT_ROOT / "data" / "synthetic"
    output_dir.mkdir(parents=True, exist_ok=True)
    file_path = output_dir / f"{dataset}_synthetic_matches.json"

    matches = {}
    if file_path.exists():
        with file_path.open("r", encoding="utf-8") as file:
            matches = json.load(file)

        incomplete_keys = find_incomplete_match_keys(matches, unique_columns)
        for column_name in incomplete_keys:
            del matches[column_name]

        if incomplete_keys:
            with file_path.open("w", encoding="utf-8") as file:
                json.dump(matches, file, indent=2, ensure_ascii=False)
            print(
                f"Removed {len(incomplete_keys)} incomplete or table-aware entries "
                "so they can be retried."
            )
        print(f"Loaded existing synthetic data: {len(matches)} columns")

    exact_generator = ExactGenerator()
    semantic_generator = SemanticGenerator(api_key, api_base)

    total_columns = len(selected_columns)
    processed_selected = sum(1 for column_name in selected_columns if column_name in matches)
    generated_count = 0
    first_error = None

    print("\nStarting synthetic training data generation")
    print(f"  Dataset: {dataset}")
    print(f"  LLM model: {llm_model}")
    print(f"  Selected columns: {total_columns}")
    print(f"  Already completed in selected set: {processed_selected}")
    print(f"  Remaining in selected set: {total_columns - processed_selected}")
    print(f"  Output file: {file_path}")
    print(f"  Estimated API calls: {total_columns - processed_selected}")
    if api_key_source:
        print(f"  API key source: {api_key_source} ({mask_api_key(api_key)})")
    if api_base:
        print(f"  API base: {api_base}")
    print("=" * 60)

    for column_name, column_data in tqdm.tqdm(
        selected_columns.items(), desc="Generating synthetic data"
    ):
        if column_name in matches:
            continue

        try:
            matches[column_name] = build_match_entry(
                column_name,
                column_data,
                exact_generator,
                semantic_generator,
                llm_model,
            )
        except Exception as exc:
            if first_error is None:
                first_error = str(exc)
            print(
                f"\nWarning: skipped {column_name} because semantic generation "
                f"failed: {sanitize_error_message(exc, api_key)}"
            )
            continue

        with file_path.open("w", encoding="utf-8") as file:
            json.dump(matches, file, indent=2, ensure_ascii=False)
        generated_count += 1

    ensure_generation_progress(
        total_columns,
        processed_selected,
        generated_count,
        llm_model=llm_model,
        api_base=api_base,
        api_key_source=api_key_source,
        api_key=api_key,
        first_error=first_error,
    )

    print(f"\nGeneration complete. Synthetic data columns: {len(matches)}")
    print(f"Saved to: {file_path}")


def extract_unique_columns(file_path):
    df = pd.read_csv(file_path, low_memory=False)
    unique_columns = {}

    for column in df.columns:
        value_counts = df[column].value_counts()
        if len(value_counts) > 50:
            value_counts = value_counts.head(50)
        unique_columns[column] = value_counts.index.tolist()

    return unique_columns


def load_unique_columns(dataset):
    if dataset in ["faa", "cf", "gdc", "wikidata", "magellan"]:
        unique_cols_path = (
            PROJECT_ROOT / "data" / "unique_columns" / f"{dataset}_unique_columns.json"
        )
        with unique_cols_path.open("r", encoding="utf-8") as file:
            return json.load(file)

    dataset_dict = {
        "chembl": "Chembl",
        "opendata": "Opendata-base",
        "tpc": "TPCH-base",
    }
    if dataset not in dataset_dict:
        raise ValueError(
            "Unknown dataset. Supported datasets: "
            "gdc, wikidata, magellan, faa, cf, chembl, opendata, tpc."
        )

    file_path = f"model_generation_valentine/{dataset_dict[dataset]}.csv"
    return extract_unique_columns(file_path)


def main():
    parser = argparse.ArgumentParser(
        description="Generate synthetic column variants for finetuning."
    )
    parser.add_argument(
        "--dataset",
        default="gdc",
        help="Dataset name: gdc, wikidata, magellan, faa, cf, chembl, opendata, tpc.",
    )
    parser.add_argument(
        "--model",
        default="gpt-4o-mini",
        help="OpenAI-compatible chat model name, for example gpt-4o-mini or deepseek-chat.",
    )
    parser.add_argument(
        "--api_key",
        default=None,
        help=(
            "API key. Priority: --api_key, DEEPSEEK_API_KEY for DeepSeek, "
            "then OPENAI_API_KEY."
        ),
    )
    parser.add_argument(
        "--api_base",
        default=None,
        help="Optional API base URL, for example https://api.deepseek.com.",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="Only process the first N fields. Use this for smoke tests.",
    )
    args = parser.parse_args()

    api_config = resolve_api_config(args.model, args.api_key, args.api_base)
    if not api_config.api_key:
        print("Error: API key is required.")
        print(
            "Provide it with --api_key, DEEPSEEK_API_KEY for DeepSeek, "
            "or OPENAI_API_KEY."
        )
        raise SystemExit(1)

    print(
        f"Using API key from {api_config.api_key_source}: "
        f"{mask_api_key(api_config.api_key)}"
    )

    unique_columns = load_unique_columns(args.dataset)
    generate_matches(
        args.dataset,
        unique_columns,
        llm_model=args.model,
        api_key=api_config.api_key,
        api_base=api_config.api_base,
        api_key_source=api_config.api_key_source,
        limit=args.limit,
    )


if __name__ == "__main__":
    main()
