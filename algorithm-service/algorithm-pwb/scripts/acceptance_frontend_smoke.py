from __future__ import annotations

from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def check(name: str, ok: bool, detail: str, failures: list[str]) -> None:
    prefix = "PASS" if ok else "FAIL"
    print(f"[{prefix}] {name}: {detail}")
    if not ok:
        failures.append(name)


def main() -> int:
    failures: list[str] = []

    router_text = read_text(ROOT / "frontend/src/router.tsx")
    datasource_page = read_text(ROOT / "frontend/src/pages/datasources/DatasourcesPageV7.tsx")
    task_list_page = read_text(ROOT / "frontend/src/pages/tasks/TaskListPageV4.direct.tsx")
    task_new_page = read_text(ROOT / "frontend/src/pages/tasks/TaskNewPageV4.direct.tsx")
    task_detail_page = read_text(ROOT / "frontend/src/pages/tasks/TaskDetailPageV3.tsx")
    monitor_page = read_text(ROOT / "frontend/src/pages/mapping-monitor/MappingMonitorPageV3.tsx")
    results_page = read_text(ROOT / "frontend/src/pages/mapping-results/MappingResultsPageV3.tsx")
    review_page = read_text(ROOT / "frontend/src/pages/review/ReviewPageV4.tsx")
    confirmed_page = read_text(ROOT / "frontend/src/pages/confirmed-mappings/ConfirmedMappingsPageV4.tsx")
    rules_page = read_text(ROOT / "frontend/src/pages/mapping-rules/MappingRulesPageV3.tsx")
    migration_page = read_text(ROOT / "frontend/src/pages/migration-monitor/MigrationMonitorPageV3.tsx")

    check(
        "datasource_route_active",
        "DatasourcesPageV7" in router_text,
        "active datasource route points to DatasourcesPageV7",
        failures,
    )
    check(
        "datasource_page_actions_wired",
        "testDatasource" in datasource_page and "ingestDatasource" in datasource_page,
        "datasource page keeps the current test/ingest actions",
        failures,
    )
    check(
        "datasource_page_clean",
        all(token not in datasource_page for token in ("system_type", "source_kind", "mock_database_key")),
        "datasource page no longer references legacy compatibility tokens",
        failures,
    )
    check(
        "datasource_page_chinese_ui",
        all(token in datasource_page for token in ("异构数据源管理", "测试连接", "接入数据", "数据库直连", "API 拉取", "API 推送")),
        "datasource page uses Chinese UI labels",
        failures,
    )
    check(
        "task_pages_use_dictionary_apis",
        all(
            token in task_new_page
            for token in ("/api/encoding-modes", "/api/embedding-models", "/api/eval-modes")
        ) and all(token not in task_new_page for token in ("/api/system-options", "/api/llm-configs", "/api/algorithm-profiles")),
        "task creation page loads active option catalogs directly and no longer depends on system-options, llm-config or algorithm-profile endpoints",
        failures,
    )
    check(
        "task_pages_no_algorithm_profile_ui",
        all(token not in task_new_page + task_list_page for token in ("algorithm_profile_id", "algorithmProfiles", "/api/algorithm-profiles")),
        "task pages no longer expose algorithm profile compatibility UI",
        failures,
    )
    check(
        "task_pages_no_algorithm_params_or_llm_select",
        all(
            token not in task_new_page + task_list_page
            for token in ("algorithm_params_text", "algorithm_params", "llm_config_id", "/api/llm-configs")
        ),
        "task pages no longer expose algorithm params or llm config selector UI",
        failures,
    )
    check(
        "task_pages_expose_direct_llm_fields",
        all(token in task_new_page + task_list_page for token in ("llm_provider", "llm_model_name", "llm_base_url", "llm_api_key")),
        "task pages expose direct LLM input fields",
        failures,
    )
    check(
        "task_list_uses_match_tasks",
        "/api/match-tasks" in task_list_page,
        "task list page is wired to match-tasks API",
        failures,
    )
    check(
        "task_detail_uses_match_tasks",
        "/api/match-tasks/" in task_detail_page,
        "task detail page is wired to match-tasks API",
        failures,
    )
    check(
        "task_detail_displays_llm_summary",
        all(token in task_detail_page for token in ("api_key_masked", "llmSummary")) and "llm_config_id" not in task_detail_page,
        "task detail page shows LLM summary fields instead of raw llm_config_id",
        failures,
    )
    check(
        "monitor_uses_match_task_records",
        "/api/match-task-records" in monitor_page and "/api/match-tasks/" in monitor_page,
        "monitor page is wired to match_task_record APIs",
        failures,
    )
    check(
        "monitor_no_canceling_status",
        "canceling" not in monitor_page,
        "monitor page no longer references canceling status",
        failures,
    )
    check(
        "results_uses_result_sets",
        "/api/result-sets" in results_page,
        "result page is wired to result-set APIs",
        failures,
    )
    check(
        "review_uses_task_record_id",
        "taskRecordId" in review_page or "task_record_id" in review_page,
        "review page retains task_record semantics",
        failures,
    )
    check(
        "confirmed_page_no_task_run_alias",
        "task_run" not in confirmed_page and "/api/tasks" not in confirmed_page,
        "confirmed mappings page no longer depends on task_run or /api/tasks compatibility",
        failures,
    )
    check(
        "rules_uses_task_record_id",
        "taskRecordId" in rules_page or "task_record_id" in rules_page,
        "mapping rules page retains task_record semantics",
        failures,
    )
    check(
        "migration_uses_task_record_id",
        "taskRecordId" in migration_page or "task_record_id" in migration_page,
        "migration monitor page retains task_record semantics",
        failures,
    )
    check(
        "active_pages_no_legacy_task_routes",
        all(
            token not in "\n".join(
                [
                    datasource_page,
                    task_list_page,
                    task_new_page,
                    task_detail_page,
                    monitor_page,
                    results_page,
                    review_page,
                    confirmed_page,
                    rules_page,
                    migration_page,
                ]
            )
            for token in ("/api/tasks", "/api/task-runs")
        ),
        "active frontend pages no longer call legacy task/task-run routes",
        failures,
    )

    if failures:
        print("\nFrontend smoke audit failed:")
        for item in failures:
            print(f" - {item}")
        return 1

    print("\nFrontend smoke audit passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
