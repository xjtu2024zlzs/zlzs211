
# 数字卷宗 SQL 说明

## 执行顺序

1. 先执行结构文件，例如 `dossier_full_schema_mysql8.sql` 和必要的建表补充文件。
2. 再执行 `dossier_mock_data_seed_mysql8.sql`。
3. 如需规范 B-1234 联调版本号，执行 `dossier_b1234_version_normalize_mysql8.sql`，只保留最新三个有效版本并从 `V1.1` 开始编号。
4. 将 `data/dossier/files/2026/06/B-1234` 放到服务器对应附件目录。

## 重要说明

- `dossier_mock_data_seed_mysql8.sql` 是后续模拟数据的统一入口。
- `dossier_b1234_version_normalize_mysql8.sql` 是 B-1234 版本号整理补丁，用于联调库反复生成后的版本压缩；它不会替代总 seed。
- 旧的分散 seed 已移除，后续不要再执行历史实例 seed、历史模板 seed 或 `project1p` 下的旧卷宗 seed。
- `*_demo_*`、`*_enrichment_*` 这类脚本仅作为历史补丁/数据来源参考，不要和总 seed 重复执行。
- 本构件使用数据库 `ry-cloud`。
- 数字卷宗业务表统一使用 `t1_` 前缀。
- 本构件没有算法服务，不需要 Python/FastAPI 脚本。
