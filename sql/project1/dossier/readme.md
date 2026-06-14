
# 数字卷宗 SQL 说明

## 执行顺序

主分支做卷宗联调时，优先执行两个聚合入口脚本：

1. `00_dossier_schema_bundle_mysql8.sql`
2. `01_dossier_seed_bundle_mysql8.sql`
3. 将 `data/dossier/files/2026/06/B-1234` 放到服务器对应附件目录。

聚合脚本内部使用 MySQL 客户端的 `SOURCE` 命令，请在仓库根目录执行：

```bash
mysql --default-character-set=utf8mb4 -uroot -p ry-cloud < sql/project1/dossier/00_dossier_schema_bundle_mysql8.sql
mysql --default-character-set=utf8mb4 -uroot -p ry-cloud < sql/project1/dossier/01_dossier_seed_bundle_mysql8.sql
```

如果使用的数据库工具不支持 `SOURCE`，则按下面顺序手工执行源脚本：

1. `dossier_full_schema_mysql8.sql`
2. `dossier_file_unified_tables_mysql8.sql`
3. `dossier_instance_management_mysql8.sql`
4. `dossier_mock_data_seed_mysql8.sql`
5. `dossier_aircraft_profile_enrichment_mysql8.sql`
6. `dossier_system_subsystem_profile_enrichment_mysql8.sql`
7. `dossier_equipment_component_part_profile_enrichment_mysql8.sql`
8. `dossier_b1234_version_normalize_mysql8.sql`

## 重要说明

- `dossier_mock_data_seed_mysql8.sql` 是后续模拟数据的统一入口。
- `dossier_b1234_version_normalize_mysql8.sql` 是 B-1234 版本号整理补丁，用于联调库反复生成后的版本压缩；它不会替代总 seed。
- 旧的 `t1_` 分散 seed 已移除，后续不要再执行历史实例 seed 或历史模板 seed。
- `project1p/03_p1p_dossier_seed_mysql8.sql` 属于独立的 `p1p_dossier_*` 体系，不由本目录的 `t1_*` 总 seed 替代。
- `dossier_aircraft_profile_enrichment_mysql8.sql`、`dossier_system_subsystem_profile_enrichment_mysql8.sql`、`dossier_equipment_component_part_profile_enrichment_mysql8.sql` 是当前详情和生成逻辑依赖的 profile 视图补充脚本，已纳入 `01_dossier_seed_bundle_mysql8.sql`。
- 其他 `*_demo_*`、`*_enrichment_*` 脚本仅作为历史补丁/数据来源参考，不要和总 seed 重复执行。
- 本构件使用数据库 `ry-cloud`。
- 数字卷宗业务表统一使用 `t1_` 前缀。
- 本构件没有算法服务，不需要 Python/FastAPI 脚本。
