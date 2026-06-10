package com.ruoyi.designtask1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DesignPlatformStartupCheck implements ApplicationRunner {

    private static final List<String> REQUIRED_TABLES = List.of(
        "p2_design_task",
        "p2_design_template",
        "p2_design_template_node",
        "p2_design_template_node_role",
        "p2_design_task_node",
        "p2_design_task_file",
        "p2_design_task_log",
        "p2_design_objective_catalog",
        "p2_design_objective_constraint",
        "p2_design_variable_catalog",
        "p2_design_task_variable_selection",
        "p2_design_conflict_check",
        "p2_design_subtask_solution",
        "p2_design_simulation_result",
        "p2_design_approval_record",
        "p2_design_resource"
    );

    private static final Map<String, String> PLATFORM_ROLES = new LinkedHashMap<>() {{
        put("design_task_owner", "任务负责人");
        put("structure_engineer", "结构工程师");
        put("layout_engineer", "布局工程师");
        put("aero_engineer", "气动工程师");
        put("hydraulic_engineer", "液压工程师");
        put("manufacturing_engineer", "制造工程师");
        put("approval_leader", "审批领导");
    }};

    private final JdbcTemplate jdbcTemplate;

    @Value("${designtask.startup-check.enabled:true}")
    private boolean startupCheckEnabled;

    public DesignPlatformStartupCheck(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!startupCheckEnabled) {
            return;
        }
        List<String> missingTables = new ArrayList<>();
        for (String tableName : REQUIRED_TABLES) {
            if (!tableExists(tableName)) {
                missingTables.add(tableName);
            }
        }
        if (!missingTables.isEmpty()) {
            throw new IllegalStateException(
                "设计制造协同优化模块缺少数据库表：" + String.join(", ", missingTables)
                    + "。请先执行 sql/p2_table_rename.sql，或在新库中执行设计平台初始化 SQL。"
            );
        }
        repairPlatformRoleKeys();
        repairPlatformRoleMenus();
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
            """
            select count(1)
            from information_schema.tables
            where table_schema = database()
              and table_name = ?
            """,
            Integer.class,
            tableName
        );
        return count != null && count > 0;
    }

    private void repairPlatformRoleKeys() {
        PLATFORM_ROLES.forEach((roleKey, roleName) -> jdbcTemplate.update(
            """
            update sys_role
            set role_key = ?,
                status = '0',
                del_flag = '0',
                update_by = 'admin',
                update_time = sysdate()
            where role_name = ?
              and del_flag <> '2'
            """,
            roleKey,
            roleName
        ));
    }

    private void repairPlatformRoleMenus() {
        grantMenus(List.of(2600L, 2601L, 2606L), PLATFORM_ROLES.keySet().stream().toList());
        grantMenus(List.of(2602L, 2604L, 2605L, 2608L, 2609L, 2610L, 2611L, 2613L, 2614L, 2615L, 2616L), List.of("design_task_owner"));
        grantMenus(List.of(2603L, 2607L), List.of("structure_engineer", "layout_engineer", "aero_engineer", "hydraulic_engineer"));
        grantMenus(List.of(2603L, 2605L, 2607L, 2611L), List.of("manufacturing_engineer"));
        grantMenus(List.of(2605L, 2612L), List.of("approval_leader"));
    }

    private void grantMenus(List<Long> menuIds, List<String> roleKeys) {
        for (String roleKey : roleKeys) {
            for (Long menuId : menuIds) {
                jdbcTemplate.update(
                    """
                    insert ignore into sys_role_menu(role_id, menu_id)
                    select role_id, ?
                    from sys_role
                    where role_key = ?
                      and del_flag <> '2'
                    """,
                    menuId,
                    roleKey
                );
            }
        }
    }
}
