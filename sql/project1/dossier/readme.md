
# 数字卷宗 SQL 说明

## 执行顺序

1. 先执行结构文件，例如 `dossier_full_schema_mysql8.sql` 和必要的建表补充文件。
2. 再执行 `dossier_mock_data_seed_mysql8.sql`。
3. 将 `data/dossier/files/2026/06/B-1234` 放到服务器对应附件目录。

## 重要说明

- `dossier_mock_data_seed_mysql8.sql` 是后续模拟数据的统一入口。
- 旧的分散模拟数据脚本，例如 `*_seed_mysql8.sql`、`*_demo_*`、`*_enrichment_*`，后续不要和总 seed 重复执行。
- 本构件使用数据库 `ry-cloud`。
- 数字卷宗业务表统一使用 `t1_` 前缀。
- 本构件没有算法服务，不需要 Python/FastAPI 脚本。
