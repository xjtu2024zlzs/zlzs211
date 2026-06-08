from __future__ import annotations

import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def check(name: str, ok: bool, detail: str, failures: list[str]) -> None:
    prefix = "PASS" if ok else "FAIL"
    print(f"[{prefix}] {name}: {detail}")
    if not ok:
        failures.append(name)


def class_body(text: str, class_name: str) -> str:
    match = re.search(rf"class\s+{class_name}\(BaseModel\):(.*?)(?:\nclass\s|\Z)", text, re.S)
    return match.group(1) if match else ""


def main() -> int:
    failures: list[str] = []

    router_text = read_text(ROOT / "frontend/src/router.tsx")
    datasource_page = read_text(ROOT / "frontend/src/pages/datasources/DatasourcesPageV7.tsx")
    api_schemas = read_text(ROOT / "backend/models/api_schemas.py")
    datasources_router = read_text(ROOT / "backend/routers/datasources_v3.py")
    matches_router = read_text(ROOT / "backend/routers/matches_v2.py")
    mapping_specs_router = read_text(ROOT / "backend/routers/mapping_specs_v2.py")
    migration_router = read_text(ROOT / "backend/routers/migration_v2.py")
    staging_runtime = read_text(ROOT / "backend/services/staging_runtime.py")
    main_py = read_text(ROOT / "backend/main.py")
    task_list_page = read_text(ROOT / "frontend/src/pages/tasks/TaskListPageV4.direct.tsx")
    task_new_page = read_text(ROOT / "frontend/src/pages/tasks/TaskNewPageV4.direct.tsx")

    create_body = class_body(api_schemas, "DataSourceCreate")
    update_body = class_body(api_schemas, "DataSourceUpdate")
    auto_approve_body = class_body(api_schemas, "AutoApproveRequest")
    migration_execute_body = class_body(api_schemas, "MigrationExecuteRequest")
    task_create_body = class_body(api_schemas, "TaskCreate")
    task_update_body = class_body(api_schemas, "TaskUpdate")
    llm_config_body = class_body(api_schemas, "LlmConfigCreate")

    check(
        "router_uses_datasources_v7",
        "DatasourcesPageV7" in router_text,
        "active router points at DatasourcesPageV7",
        failures,
    )
    check(
        "datasource_page_wires_test_and_ingest",
        "testDatasource" in datasource_page and "ingestDatasource" in datasource_page,
        "datasource page still exposes test and ingest actions",
        failures,
    )
    check(
        "datasource_page_no_legacy_tokens",
        all(token not in datasource_page for token in ("system_type", "source_kind", "mock_database_key")),
        "active datasource page no longer exposes legacy compatibility tokens",
        failures,
    )
    check(
        "datasource_page_chinese_labels",
        all(token in datasource_page for token in ("数据库直连", "API 拉取", "API 推送", "测试连接", "接入数据", "名称", "接入方式")),
        "active datasource page exposes Chinese labels and actions",
        failures,
    )
    check(
        "datasource_request_models_clean",
        all(token not in create_body + update_body for token in ("system_type", "source_kind", "auth_type", "auth_config", "connection_config")),
        "public datasource request models no longer expose legacy datasource fields",
        failures,
    )
    check(
        "datasource_router_clean",
        all(token not in datasources_router for token in ("system_type", "source_kind", "_derive_source_kind", "auth_type", "auth_config", "connection_config")),
        "active datasource router no longer contains legacy datasource compatibility logic",
        failures,
    )
    check(
        "staging_runtime_no_mock_database_key",
        "mock_database_key" not in staging_runtime,
        "formal staging runtime no longer contains mock_database_key",
        failures,
    )
    check(
        "staging_runtime_no_api_db_fallback",
        "api_pull failed" in staging_runtime and "api_push trigger failed" in staging_runtime and "pull_adapters" not in staging_runtime,
        "api_pull/api_push runtime uses HTTP ingestion paths instead of adapter/database fallback",
        failures,
    )
    check(
        "active_routers_no_task_run_id",
        all("task_run_id" not in text for text in (matches_router, mapping_specs_router, migration_router, auto_approve_body, migration_execute_body)),
        "active routers and request models no longer depend on task_run_id",
        failures,
    )
    check(
        "task_models_no_legacy_config_fields",
        all(
            token not in task_create_body + task_update_body
            for token in (
                "\n    encoding_mode:",
                "\n    embedding_model:",
                "\n    eval_mode:",
                "\n    llm_model:",
                "\n    api_key:",
                "algorithm_profile_id",
                "llm_config_id",
                "algorithm_params",
            )
        ),
        "task request models no longer expose legacy compatibility config fields",
        failures,
    )
    check(
        "llm_config_models_slimmed",
        all(token not in llm_config_body for token in ("auth_type", "config")) and "class AlgorithmProfileCreate" not in api_schemas,
        "llm config request models no longer expose auth_type/config and algorithm profile create model is gone",
        failures,
    )
    check(
        "main_uses_datasources_v3",
        "from routers.datasources_v3 import router as datasources_router" in main_py,
        "main entrypoint uses datasources_v3",
        failures,
    )
    check(
        "main_imports_runtime_db_v3",
        "from models.runtime_db_v3 import init_meta_tables" in main_py,
        "main entrypoint uses runtime_db_v3",
        failures,
    )
    check(
        "main_imports_tasks_router_v4_directly",
        "from routers.tasks_router_v4 import task_runs_router, tasks_router" in main_py,
        "main entrypoint uses tasks_router_v4 directly",
        failures,
    )
    check(
        "main_no_system_options_router",
        "system_options_v1" not in main_py and "system_options_router" not in main_py,
        "main entrypoint no longer imports the removed system_options router",
        failures,
    )
    check(
        "task_pages_no_system_options_endpoint",
        all("/api/system-options" not in text for text in (task_list_page, task_new_page)),
        "active task pages no longer call /api/system-options",
        failures,
    )
    check(
        "task_pages_use_catalog_endpoints",
        all(token in task_new_page + task_list_page for token in ("/api/encoding-modes", "/api/embedding-models", "/api/eval-modes")),
        "active task pages now call the three physical option catalog endpoints",
        failures,
    )

    if failures:
        print("\nStatic acceptance audit failed:")
        for item in failures:
            print(f" - {item}")
        return 1

    print("\nStatic acceptance audit passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
