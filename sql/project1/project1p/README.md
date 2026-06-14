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

- `00_p1p_business_schema_mysql8.sql` 是首次建表脚本
- `01_project1_menu_permission.sql` 会重建 `2100-2199` 范围内的 project1 菜单，并给 `common` 角色授权。
- 执行菜单脚本后，需要重新登录若依或刷新路由缓存。

## 已有数据库重建建议

当前数据库仅用于开发或联调，且不需要保留 project1 历史业务数据，建议删除所有 `p1p_` 开头的 project1 业务表后，按首次建库顺序重新导入。

注意：只删除 `p1p_` 开头表，不要删除若依系统表，也不要删除 `t1_` 开头的 dossier 表。

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

- 由于最开始设计前缀已经定为p1p，不便修改，故异构信息集成和异构数据接入构件业务表前缀保留为 `p1p_`，暂不改为 `t1_`。
- 所有页面接口都通过若依后端 `/project1/...` 暴露，前端不得直连 Python/FastAPI。
- Python/FastAPI 算法服务不直接写 MySQL 业务表，由 `ruoyi-project1` 负责落库。
