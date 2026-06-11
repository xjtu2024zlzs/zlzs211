-- projectp verification SQL.

use `ry-cloud`;
set names utf8mb4;

select count(*) as p1p_ext_table_count
from information_schema.tables
where table_schema = 'ry-cloud'
  and table_name like 'p1p_ext\_%' escape '\\';

select count(*) as p1p_ext_fk_count
from information_schema.referential_constraints
where constraint_schema = 'ry-cloud'
  and constraint_name like 'fk\_p1p\_ext\_%' escape '\\';

select count(*) as p1p_ext_non_primary_index_count
from information_schema.statistics
where table_schema = 'ry-cloud'
  and table_name like 'p1p_ext\_%' escape '\\'
  and index_name <> 'PRIMARY';

select 'PLM' as source_system, count(*) as table_count
from information_schema.tables
where table_schema = 'ry-cloud'
  and table_name like 'p1p_ext_plm_%';

select 'ERP' as source_system, count(*) as table_count
from information_schema.tables
where table_schema = 'ry-cloud'
  and table_name like 'p1p_ext_erp_%';

select 'MES' as source_system, count(*) as table_count
from information_schema.tables
where table_schema = 'ry-cloud'
  and table_name like 'p1p_ext_mes_%';

select 'QMS' as source_system, count(*) as table_count
from information_schema.tables
where table_schema = 'ry-cloud'
  and table_name like 'p1p_ext_qms_%';

select 'MRO' as source_system, count(*) as table_count
from information_schema.tables
where table_schema = 'ry-cloud'
  and table_name like 'p1p_ext_mro_%';

select 'PLM' as source_system, (select count(*) from `p1p_ext_plm_product_model`) + (select count(*) from `p1p_ext_plm_gear_system_def`) + (select count(*) from `p1p_ext_plm_actuator_model`) + (select count(*) from `p1p_ext_plm_design_revision`) + (select count(*) from `p1p_ext_plm_part_master`) + (select count(*) from `p1p_ext_plm_bom_header`) + (select count(*) from `p1p_ext_plm_bom_line`) + (select count(*) from `p1p_ext_plm_route_card`) + (select count(*) from `p1p_ext_plm_operation_card`) + (select count(*) from `p1p_ext_plm_technical_parameter`) + (select count(*) from `p1p_ext_plm_inspection_standard`) as row_count, 2200 as expected_row_count
union all
select 'ERP' as source_system, (select count(*) from `p1p_ext_erp_item_master`) + (select count(*) from `p1p_ext_erp_vendor_master`) + (select count(*) from `p1p_ext_erp_purchase_order`) + (select count(*) from `p1p_ext_erp_purchase_line`) + (select count(*) from `p1p_ext_erp_vendor_batch`) + (select count(*) from `p1p_ext_erp_inventory_lot`) + (select count(*) from `p1p_ext_erp_receipt_bill`) + (select count(*) from `p1p_ext_erp_issue_bill`) as row_count, 1600 as expected_row_count
union all
select 'MES' as source_system, (select count(*) from `p1p_ext_mes_sys_task`) + (select count(*) from `p1p_ext_mes_batch_card`) + (select count(*) from `p1p_ext_mes_part_track`) + (select count(*) from `p1p_ext_mes_assembly_record`) + (select count(*) from `p1p_ext_mes_sys_station`) + (select count(*) from `p1p_ext_mes_equipment_asset`) + (select count(*) from `p1p_ext_mes_person_info`) + (select count(*) from `p1p_ext_mes_tooling_asset`) + (select count(*) from `p1p_ext_mes_work_log`) + (select count(*) from `p1p_ext_mes_step_log`) + (select count(*) from `p1p_ext_mes_resource_usage`) + (select count(*) from `p1p_ext_mes_process_param`) as row_count, 2400 as expected_row_count
union all
select 'QMS' as source_system, (select count(*) from `p1p_ext_qms_quality_event_doc`) + (select count(*) from `p1p_ext_qms_inspection_doc`) + (select count(*) from `p1p_ext_qms_inspection_item`) + (select count(*) from `p1p_ext_qms_defect_log`) + (select count(*) from `p1p_ext_qms_iqs_failure`) + (select count(*) from `p1p_ext_qms_iqs_failure_content`) + (select count(*) from `p1p_ext_qms_iqs_failure_duty`) + (select count(*) from `p1p_ext_qms_disposal_order`) + (select count(*) from `p1p_ext_qms_recheck_record`) as row_count, 1800 as expected_row_count
union all
select 'MRO' as source_system, (select count(*) from `p1p_ext_mro_service_event`) + (select count(*) from `p1p_ext_mro_repair_order`) + (select count(*) from `p1p_ext_mro_fault_report`) + (select count(*) from `p1p_ext_mro_remove_install_record`) + (select count(*) from `p1p_ext_mro_field_feedback`) as row_count, 1000 as expected_row_count;

select d.datasource_name, d.access_mode, a.base_url, a.schema_endpoint, a.pull_endpoint, a.health_endpoint
from p1p_datasource d
join p1p_api_pull_datasource a on a.datasource_id = d.datasource_id
where d.datasource_name in ('CF PLM API Pull', 'CF ERP API Pull', 'CF MES API Pull', 'CF QMS API Pull', 'CF MRO API Pull')
order by d.datasource_name;
