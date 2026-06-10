# projectp SQL 执行说明

本目录存放 project1 使用的 5 个 CF 外部源系统模拟库 SQL。MySQL 版本按 `8.0.46` 处理，物理表统一放入 `ry-cloud`，并使用 `p1p_ext_` 前缀隔离。

## 文件说明

| 文件 | 用途 |
| --- | --- |
| `00_projectp_ext_schema_mysql8.sql` | 创建 PLM/ERP/MES/QMS/MRO 共 45 张模拟外部源系统表 |
| `01_projectp_ext_seed_mysql8.sql` | 导入 9000 条模拟源系统数据 |
| `02_projectp_api_pull_datasource_seed_mysql8.sql` | 写入或更新 5 个 API Pull 数据源配置 |
| `03_projectp_verify.sql` | 验证表数量、行数、外键、索引和 API Pull 数据源配置 |

## 首次执行顺序

在已经导入若依基础库和 project1 业务表后执行：

```text
1. 00_projectp_ext_schema_mysql8.sql
2. 01_projectp_ext_seed_mysql8.sql
3. 02_projectp_api_pull_datasource_seed_mysql8.sql
4. 03_projectp_verify.sql
```

## 注意事项

- 建表脚本只会 drop `p1p_ext_%` 表，不会 drop `p1p_dossier_%`、`p1p_datasource`、`p1p_match_%`、`sys_%`、`QRTZ_%`。
- `02_projectp_api_pull_datasource_seed_mysql8.sql` 默认写入 `http://127.0.0.1:9711-9715`。如果 projectp 服务和 RuoYi 后端不在同一台服务器，请先把 `127.0.0.1` 替换为 projectp 服务所在服务器 IP。
- 这些表只用于模拟外部企业源库。RuoYi 后端仍应通过 API Pull 接口读取数据，不直接读取 `p1p_ext_%` 表。
