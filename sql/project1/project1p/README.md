# project1 SQL 执行说明

本目录存放课题一 project1 需要提交给项目组的 SQL 文件。MySQL 版本按 `8.0.46` 处理，业务表前缀保持 `p1p_`。

## 文件说明

| 文件 | 用途 |
| --- | --- |
| `00_p1p_business_schema_mysql8.sql` | project1 30 张 `p1p_` 业务表，供若依代码生成器和业务流程使用 |
| `01_project1_menu_permission.sql` | 若依菜单、页面权限、按钮权限 |
| `02_p1p_dossier_schema_mysql8.sql` | 数字卷宗目标库表 |
| `03_p1p_dossier_seed_mysql8.sql` | `p1p_dossier_*` 卷宗目标库种子数据 |
| `05_p1p_access_scope_field_unique_upgrade_mysql8.sql` | 已有数据库升级脚本，修正接入范围字段唯一键 |
| `04_p1p_verify.sql` | 数据库验证 SQL |
| `99_p1p_upgrade_existing_db.sql` | 已有数据库升级脚本，主要修正多结果集相关唯一键 |

说明：`project1p` 与 `dossier` 是两套独立 SQL 构件。`03_p1p_dossier_seed_mysql8.sql` 只写入 `p1p_dossier_*` 表；`../dossier/dossier_mock_data_seed_mysql8.sql` 只用于 `t1_*` 数字卷宗表，不要互相替代。

## 首次建库执行顺序

在已经导入若依基础库后，按下面顺序执行：

```text
1. 00_p1p_business_schema_mysql8.sql
2. 02_p1p_dossier_schema_mysql8.sql
3. 03_p1p_dossier_seed_mysql8.sql
4. 01_project1_menu_permission.sql
5. 04_p1p_verify.sql
```

说明：

- `00_p1p_business_schema_mysql8.sql` 是首次建表脚本，包含 `drop table if exists`，不要在已有业务数据环境直接执行。
- `01_project1_menu_permission.sql` 会重建 `2100-2199` 范围内的 project1 菜单，并给 `common` 角色授权。
- 执行菜单脚本后，需要重新登录若依或刷新路由缓存。

## 已有数据库升级顺序

如果当前数据库已经存在 project1 表和数据，不要执行首次建表脚本。按需执行：

```text
1. 05_p1p_access_scope_field_unique_upgrade_mysql8.sql
2. 99_p1p_upgrade_existing_db.sql
3. 01_project1_menu_permission.sql
4. 04_p1p_verify.sql
```

`99_p1p_upgrade_existing_db.sql` 用于修正以下旧索引问题：

- `p1p_reviewed_match`：审核唯一粒度从 `record_id + source_table + source_column` 改为 `result_row_id`。
- `p1p_task_metric`：指标唯一粒度从 `record_id + metric_key` 改为 `result_set_id + metric_key`。

这样才能支持一次模式映射运行生成 4 或 6 个候选结果集，并让每个结果集拥有独立的指标和审核记录。

## 菜单结构

执行菜单脚本后，若依菜单应为：

```text
数字卷宗
├─ 异构信息集成
│  ├─ 数据源管理
│  ├─ 模式映射任务创建
│  └─ 模式映射结果展示
├─ 异构数据接入
│  ├─ 数据接入管理
│  └─ 数据接入结果展示
└─ 卷宗生成管理
   ├─ 卷宗实例管理
   ├─ 卷宗模板管理
   ├─ 卷宗生成控制
   └─ 卷宗详情可视化
```

## 注意事项

- 业务表前缀当前按用户要求保留为 `p1p_`，暂不改为 `t1_`。
- 所有页面接口都通过若依后端 `/project1/...` 暴露，前端不得直连 Python/FastAPI。
- Python/FastAPI 算法服务不直接写 MySQL 业务表，由 `ruoyi-project1` 负责落库。

## 已有库接入索引升级

如果当前库来自早期 `project1` 分支，需要先执行
`05_p1p_access_scope_field_unique_upgrade_mysql8.sql`，再执行验证脚本。该脚本会把
`p1p_access_scope_field.uq_p1p_access_scope_field` 从旧的
`scope_table_id + source_column` 唯一粒度升级为
`scope_table_id + source_column + target_column`，用于支持同一源字段映射多个目标字段。

全新库直接执行 `00_p1p_business_schema_mysql8.sql` 即可，该建表脚本已包含正确索引。
