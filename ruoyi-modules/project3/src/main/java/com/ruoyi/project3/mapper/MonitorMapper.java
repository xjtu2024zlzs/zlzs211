package com.ruoyi.project3.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MonitorMapper {

    List<Map<String, Object>> sel_all_nodes();

    Map<String, Object> sel_node_by_id(@Param("node_id") String node_id);
    List<Map<String, Object>> sel_child_nodes(@Param("node_id") String node_id);

    List<Map<String, Object>> sel_bread_nodes(@Param("node_id") String node_id);

    Long cnt_child_nodes(@Param("node_id") String node_id);

    Long cnt_part_by_node(@Param("node_id") String node_id);
    List<Map<String, Object>> sel_part_list(
            @Param("module_id") String module_id,
            @Param("keyword") String keyword,
            @Param("material") String material,
            @Param("status") String status,
            @Param("page_num") Integer page_num,
            @Param("page_size") Integer page_size
    );

    Long cnt_part_list(
            @Param("module_id") String module_id,
            @Param("keyword") String keyword,
            @Param("material") String material,
            @Param("status") String status
    );

    List<Map<String, Object>> sel_table_cols(@Param("table_name") String table_name);

    List<Map<String, Object>> sel_dsn_rows(@Param("part_instance_id") String part_instance_id);

    List<Map<String, Object>> sel_manu_rows(@Param("part_instance_id") String part_instance_id);

    List<Map<String, Object>> sel_srv_rows(@Param("part_instance_id") String part_instance_id);

    int ins_subsystem(
            @Param("subsystem_id") String subsystem_id,
            @Param("subsystem_name") String subsystem_name,
            @Param("aircraft_id") String aircraft_id
    );

    int ins_aircraft(
            @Param("aircraft_id") String aircraft_id,
            @Param("aircraft_name") String aircraft_name
    );

    int ins_equipment(
            @Param("equipment_id") String equipment_id,
            @Param("equipment_name") String equipment_name,
            @Param("subsystem_id") String subsystem_id
    );

    int ins_component(
            @Param("component_id") String component_id,
            @Param("component_name") String component_name,
            @Param("equipment_id") String equipment_id
    );

    int ins_part_tpl(
            @Param("part_template_id") String part_template_id,
            @Param("part_name") String part_name,
            @Param("component_id") String component_id,
            @Param("part_number") String part_number,
            @Param("material") String material,
            @Param("specification") String specification
    );

    Long cnt_subs_by_air_id(@Param("aircraft_id") String aircraft_id);

    Long cnt_equip_by_sub_id(@Param("subsystem_id") String subsystem_id);

    Long cnt_comp_by_equ_id(@Param("equipment_id") String equipment_id);

    Long cnt_tpl_by_comp_id(@Param("component_id") String component_id);

    Long cnt_ins_by_tpl_id(@Param("part_template_id") String part_template_id);

    int del_aircraft(@Param("aircraft_id") String aircraft_id);

    int del_subsystem(@Param("subsystem_id") String subsystem_id);

    int del_equipment(@Param("equipment_id") String equipment_id);

    int del_component(@Param("component_id") String component_id);

    int del_part_tpl(@Param("part_template_id") String part_template_id);

    int del_part_ins(@Param("part_instance_id") String part_instance_id);

    int upd_aircraft_name(
            @Param("aircraft_id") String aircraft_id,
            @Param("aircraft_name") String aircraft_name
    );

    int upd_subsystem_name(
            @Param("subsystem_id") String subsystem_id,
            @Param("subsystem_name") String subsystem_name
    );

    int upd_equipment_name(
            @Param("equipment_id") String equipment_id,
            @Param("equipment_name") String equipment_name
    );

    int upd_component_name(
            @Param("component_id") String component_id,
            @Param("component_name") String component_name
    );

    int upd_part_tpl_name(
            @Param("part_template_id") String part_template_id,
            @Param("part_name") String part_name
    );

    int upd_part_instance(
            @Param("part_instance_id") String part_instance_id,
            @Param("serial_number") String serial_number,
            @Param("batch_number") String batch_number,
            @Param("manufacturer") String manufacturer,
            @Param("production_date") String production_date,
            @Param("current_status") String current_status,
            @Param("quality_level") String quality_level,
            @Param("key_degree") String key_degree
    );

    int ins_part_instance(
            @Param("part_instance_id") String part_instance_id,
            @Param("part_template_id") String part_template_id,
            @Param("serial_number") String serial_number,
            @Param("batch_number") String batch_number,
            @Param("manufacturer") String manufacturer,
            @Param("production_date") String production_date,
            @Param("current_status") String current_status,
            @Param("quality_level") String quality_level,
            @Param("key_degree") String key_degree
    );

    int upd_part_tpl_basic(
            @Param("part_template_id") String part_template_id,
            @Param("part_number") String part_number,
            @Param("part_name") String part_name,
            @Param("material") String material,
            @Param("specification") String specification
    );

    Map<String, Object> sel_part_tpl_basic(@Param("part_template_id") String part_template_id);

    Map<String, Object> sel_component_basic(@Param("component_id") String component_id);

    Map<String, Object> sel_part_tpl_by_comp_num(
            @Param("component_id") String component_id,
            @Param("part_number") String part_number
    );

    List<Map<String, Object>> sel_all_part_tpl();

    List<Map<String, Object>> sel_all_part_template_ids();

    List<Map<String, Object>> sel_all_process_route_ids();

    List<Map<String, Object>> sel_all_process_def_ids();

    int ins_process_route(
            @Param("route_id") String route_id,
            @Param("part_template_id") String part_template_id,
            @Param("route_name") String route_name
    );

    int ins_process_def(
            @Param("process_def_id") String process_def_id,
            @Param("route_id") String route_id,
            @Param("process_number") Integer process_number,
            @Param("process_name") String process_name,
            @Param("equipment_type") String equipment_type
    );

    int upsert_process_route(Map<String, Object> row);

    int upsert_process_def(Map<String, Object> row);

    int upsert_aircraft(Map<String, Object> row);

    int upsert_subsystem(Map<String, Object> row);

    int upsert_equipment(Map<String, Object> row);

    int upsert_component(Map<String, Object> row);

    int upsert_part_template(Map<String, Object> row);

    int upsert_part_instance(Map<String, Object> row);

    int count_part_tpl_by_id(@Param("part_template_id") String part_template_id);

    int count_part_instance_by_id(@Param("part_instance_id") String part_instance_id);

    int count_process_route_by_id(@Param("route_id") String route_id);

    int count_process_def_by_id(@Param("process_def_id") String process_def_id);

    int count_work_order_by_id(@Param("work_order_id") String work_order_id);

    int count_manufacturing_quality_by_id(@Param("manufacturing_quality_id") String manufacturing_quality_id);

    int count_manufacturing_device_by_id(@Param("device_id") String device_id);

    Map<String, Object> sel_part_instance_ref(@Param("part_instance_id") String part_instance_id);

    Map<String, Object> sel_process_route_ref(@Param("route_id") String route_id);

    Map<String, Object> sel_process_def_ref(@Param("process_def_id") String process_def_id);

    Map<String, Object> sel_work_order_ref(@Param("work_order_id") String work_order_id);

    int upsert_work_order(Map<String, Object> row);

    int upsert_process_execution(Map<String, Object> row);
}


