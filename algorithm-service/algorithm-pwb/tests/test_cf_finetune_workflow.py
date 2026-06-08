import csv
import json
import sys
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parents[1]
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))


def test_cf_unique_columns_keep_only_single_occurrence_names(tmp_path):
    from algorithms.magneto.finetune.data_generation.generate_cf_unique_columns import (
        build_unique_column_artifacts,
    )

    target_dir = tmp_path / "target-tables"
    target_dir.mkdir()
    (target_dir / "dossier_alpha.csv").write_text(
        "id,only_alpha,shared\n1,A,X\n2,B,Y\n", encoding="utf-8"
    )
    (target_dir / "dossier_beta.csv").write_text(
        "id,only_beta,shared\n3,C,X\n4,D,Z\n", encoding="utf-8"
    )

    unique_columns, skipped_duplicates, stats = build_unique_column_artifacts(target_dir)

    assert set(unique_columns) == {"only_alpha", "only_beta"}
    assert unique_columns["only_alpha"]["table"] == "alpha"
    assert unique_columns["only_beta"]["values"] == ["C", "D"]
    assert {row["column_name"] for row in skipped_duplicates} == {"id", "shared"}
    assert stats["table_count"] == 2
    assert stats["distinct_column_count"] == 4
    assert stats["unique_column_count"] == 2
    assert stats["duplicate_column_count"] == 2


def test_write_cf_unique_columns_outputs_json_and_duplicate_report(tmp_path):
    from algorithms.magneto.finetune.data_generation.generate_cf_unique_columns import (
        write_unique_column_artifacts,
    )

    unique_columns = {"serial_no": {"table": "component", "values": ["S1"]}}
    skipped_duplicates = [
        {"column_name": "updated_at", "occurrence_count": 2, "tables": "a;b"}
    ]

    json_path, report_path = write_unique_column_artifacts(
        unique_columns, skipped_duplicates, tmp_path
    )

    assert json.loads(json_path.read_text(encoding="utf-8")) == unique_columns
    with report_path.open("r", encoding="utf-8", newline="") as handle:
        rows = list(csv.DictReader(handle))
    assert rows == [
        {"column_name": "updated_at", "occurrence_count": "2", "tables": "a;b"}
    ]


def test_limit_unique_columns_preserves_order_and_returns_copy():
    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        limit_unique_columns,
    )

    source = {
        "a": {"table": "t1", "values": []},
        "b": {"table": "t2", "values": []},
        "c": {"table": "t3", "values": []},
    }

    assert list(limit_unique_columns(source, 2)) == ["a", "b"]
    assert limit_unique_columns(source, None) == source
    assert limit_unique_columns(source, 0) == {}


def test_build_match_entry_requires_three_semantic_variants():
    import pytest

    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        ExactGenerator,
        build_match_entry,
    )

    class EmptySemanticGenerator:
        def get_semantic_matches(self, column_name, column_values, model):
            return {}

    with pytest.raises(RuntimeError):
        build_match_entry(
            "serial_no",
            {"table": "component", "values": ["S1", "S2"]},
            ExactGenerator(),
            EmptySemanticGenerator(),
            "deepseek-chat",
        )


def test_semantic_prompt_uses_only_column_name_without_table_context():
    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        SemanticGenerator,
    )

    generator = SemanticGenerator(api_key="sk-test")
    prompt = generator._generate_prompt("part_no", ["A"])

    assert "part_no" in prompt
    assert "component.part_no" not in prompt
    assert "Table:" not in prompt


def test_build_match_entry_outputs_original_format_without_table_key():
    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        ExactGenerator,
        build_match_entry,
    )

    class SemanticGeneratorStub:
        def get_semantic_matches(self, column_name, column_values, model):
            assert column_name == "serial_no"
            assert column_values == ["S1", "S2"]
            return {
                "serial_number": ["S1"],
                "serial_code": ["S2"],
                "sn": ["S1", "S2"],
            }

    entry = build_match_entry(
        "serial_no",
        {"table": "component_instance", "values": ["S1", "S2"]},
        ExactGenerator(),
        SemanticGeneratorStub(),
        "deepseek-chat",
    )

    assert set(entry) == {"exact", "semantic", "original"}
    assert "table" not in entry
    assert entry["original"]["serial_no"] == ["S1", "S2"]
    assert len(entry["semantic"]) == 3


def test_table_metadata_marks_existing_synthetic_entry_incomplete():
    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        is_complete_match_entry,
    )

    old_entry = {
        "table": "component_instance",
        "original": {"serial_no": ["S1"]},
        "exact": {"serial_no_1": ["S1"]},
        "semantic": {
            "serial_number": ["S1"],
            "serial_code": ["S1"],
            "sn": ["S1"],
        },
    }

    assert not is_complete_match_entry(old_entry)


def test_find_incomplete_match_keys_cleans_old_known_columns_only():
    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        find_incomplete_match_keys,
    )

    matches = {
        "serial_no": {
            "table": "component_instance",
            "original": {"serial_no": ["S1"]},
            "exact": {"serial_no_1": ["S1"]},
            "semantic": {
                "serial_number": ["S1"],
                "serial_code": ["S1"],
                "sn": ["S1"],
            },
        },
        "part_no": {
            "original": {"part_no": ["P1"]},
            "exact": {"part_no_1": ["P1"]},
            "semantic": {
                "part_number": ["P1"],
                "part_code": ["P1"],
                "pn": ["P1"],
            },
        },
        "outside_column": {"table": "legacy"},
    }

    assert find_incomplete_match_keys(matches, {"serial_no", "part_no"}) == [
        "serial_no"
    ]


def test_ensure_generation_progress_fails_when_all_new_columns_fail():
    import pytest

    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        ensure_generation_progress,
    )

    with pytest.raises(RuntimeError):
        ensure_generation_progress(total_columns=10, processed_selected=0, generated_count=0)

    ensure_generation_progress(total_columns=10, processed_selected=10, generated_count=0)
    ensure_generation_progress(total_columns=10, processed_selected=0, generated_count=1)


def test_resolve_api_config_prefers_deepseek_key_for_deepseek_model():
    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        resolve_api_config,
    )

    config = resolve_api_config(
        model="deepseek-chat",
        api_key_arg=None,
        api_base_arg=None,
        env={
            "OPENAI_API_KEY": "sk-openai-bad",
            "DEEPSEEK_API_KEY": "sk-deepseek-good",
        },
    )

    assert config.api_key == "sk-deepseek-good"
    assert config.api_key_source == "DEEPSEEK_API_KEY"
    assert config.api_base == "https://api.deepseek.com"


def test_resolve_api_config_explicit_key_overrides_environment():
    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        resolve_api_config,
    )

    config = resolve_api_config(
        model="deepseek-chat",
        api_key_arg="sk-explicit-key",
        api_base_arg=None,
        env={
            "OPENAI_API_KEY": "sk-openai-bad",
            "DEEPSEEK_API_KEY": "sk-deepseek-good",
        },
    )

    assert config.api_key == "sk-explicit-key"
    assert config.api_key_source == "--api_key"
    assert config.api_base == "https://api.deepseek.com"


def test_resolve_api_config_non_deepseek_uses_openai_key():
    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        resolve_api_config,
    )

    config = resolve_api_config(
        model="gpt-4o-mini",
        api_key_arg=None,
        api_base_arg=None,
        env={
            "OPENAI_API_KEY": "sk-openai-good",
            "DEEPSEEK_API_KEY": "sk-deepseek-good",
        },
    )

    assert config.api_key == "sk-openai-good"
    assert config.api_key_source == "OPENAI_API_KEY"
    assert config.api_base is None


def test_generation_progress_error_includes_config_without_leaking_key():
    import pytest

    from algorithms.magneto.finetune.data_generation.synthetic_data_gen import (
        ensure_generation_progress,
    )

    with pytest.raises(RuntimeError) as exc_info:
        ensure_generation_progress(
            total_columns=10,
            processed_selected=0,
            generated_count=0,
            llm_model="deepseek-chat",
            api_base="https://api.deepseek.com",
            api_key_source="DEEPSEEK_API_KEY",
            api_key="sk-secret-full-value",
            first_error="401 invalid key sk-secret-full-value",
        )

    message = str(exc_info.value)
    assert "deepseek-chat" in message
    assert "https://api.deepseek.com" in message
    assert "DEEPSEEK_API_KEY" in message
    assert "401 invalid key" in message
    assert "sk-secret-full-value" not in message


def test_split_indices_by_label_keeps_train_and_validation_examples():
    from algorithms.magneto.finetune.train import split_indices_by_label

    labels = [0] * 6 + [1] * 6 + [2] * 6
    train_indices, val_indices = split_indices_by_label(labels, val_ratio=0.2, seed=7)

    assert len(train_indices) == 12
    assert len(val_indices) == 6
    for label in {0, 1, 2}:
        assert sum(labels[i] == label for i in train_indices) == 4
        assert sum(labels[i] == label for i in val_indices) == 2


def test_global_unknown_method_names_distinguish_finetuned_weights():
    from experiments.benchmarks.cf_benchmark import get_global_unknown_method_names

    assert get_global_unknown_method_names(skip_gpt=True, has_neo4j=False, is_finetuned=False) == [
        "Magneto",
        "MagnetoBoost",
    ]
    assert get_global_unknown_method_names(skip_gpt=True, has_neo4j=False, is_finetuned=True) == [
        "MagnetoFT",
        "MagnetoFTBoost",
    ]
    assert get_global_unknown_method_names(skip_gpt=False, has_neo4j=True, is_finetuned=True) == [
        "MagnetoFT",
        "MagnetoFTBoost",
        "MagnetoFTGPT",
        "MagnetoFTGPT_neo4j2",
    ]
