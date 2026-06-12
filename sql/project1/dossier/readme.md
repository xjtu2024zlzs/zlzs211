
# 数字卷宗 SQL 说明

## 执行顺序

1. `dossier_full_schema_mysql8.sql`
2. `dossier_template_management_tables_mysql8.sql`
3. `dossier_object_lifecycle_tables_mysql8.sql`
4. `dossier_detail_tables_mysql8.sql`
5. `dossier_instance_management_mysql8.sql`
6. `dossier_feature_tables_mysql8.sql`
7. `dossier_template_management_seed_mysql8.sql`
8. `dossier_instance_management_menu_mysql8.sql`
9. 其他 `*_patch_mysql8.sql` 和 `*_enrichment_mysql8.sql` 按演示数据需要执行

## 说明

- 运行数据库为 `ry-cloud`。
- 数字卷宗业务表已统一使用 `t1_` 前缀。
- 菜单写入 RuoYi 系统菜单表 `sys_menu` 和 `sys_role_menu`。
- 本构件没有算法服务，不需要 Python/FastAPI 脚本。
