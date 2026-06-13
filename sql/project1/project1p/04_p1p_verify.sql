-- Verification queries for p1p dossier target tables.
-- Run after 02_p1p_dossier_schema_mysql8.sql and ../dossier/dossier_mock_data_seed_mysql8.sql.

use `ry-cloud`;
set names utf8mb4;

select
  'table_count' as check_item,
  count(*) as actual_count,
  48 as expected_count,
  if(count(*) = 48, 'PASS', 'CHECK') as status
from information_schema.tables
where table_schema = database()
  and table_name regexp '^p1p_dossier_';

select
  'foreign_key_count' as check_item,
  count(*) as actual_count,
  134 as expected_count,
  if(count(*) = 134, 'PASS', 'CHECK') as status
from information_schema.table_constraints
where constraint_schema = database()
  and table_name regexp '^p1p_dossier_'
  and constraint_type = 'FOREIGN KEY';

select
  'source_defined_index_count' as check_item,
  count(distinct concat(table_name, '.', index_name)) as actual_count,
  100 as expected_count,
  if(count(distinct concat(table_name, '.', index_name)) = 100, 'PASS', 'CHECK') as status
from information_schema.statistics
where table_schema = database()
  and table_name regexp '^p1p_dossier_'
  and index_name regexp '^(idx|uq)_p1p_dossier_';

select
  'seed_total_rows' as check_item,
  sum(actual_count) as actual_count,
  723 as expected_count,
  if(sum(actual_count) = 723, 'PASS', 'CHECK') as status
from (
  select count(*) as actual_count from `p1p_dossier_upper_equipment`
  union all
  select count(*) as actual_count from `p1p_dossier_landing_gear_system`
  union all
  select count(*) as actual_count from `p1p_dossier_installed_position`
  union all
  select count(*) as actual_count from `p1p_dossier_component_type`
  union all
  select count(*) as actual_count from `p1p_dossier_design_version`
  union all
  select count(*) as actual_count from `p1p_dossier_standard_dictionary`
  union all
  select count(*) as actual_count from `p1p_dossier_material`
  union all
  select count(*) as actual_count from `p1p_dossier_part_definition`
  union all
  select count(*) as actual_count from `p1p_dossier_component_instance`
  union all
  select count(*) as actual_count from `p1p_dossier_bom_item`
  union all
  select count(*) as actual_count from `p1p_dossier_process_route`
  union all
  select count(*) as actual_count from `p1p_dossier_operation_definition`
  union all
  select count(*) as actual_count from `p1p_dossier_parameter_standard`
  union all
  select count(*) as actual_count from `p1p_dossier_inspection_spec`
  union all
  select count(*) as actual_count from `p1p_dossier_supplier`
  union all
  select count(*) as actual_count from `p1p_dossier_purchase_order`
  union all
  select count(*) as actual_count from `p1p_dossier_purchase_order_line`
  union all
  select count(*) as actual_count from `p1p_dossier_supplier_batch`
  union all
  select count(*) as actual_count from `p1p_dossier_inventory_batch`
  union all
  select count(*) as actual_count from `p1p_dossier_receiving_record`
  union all
  select count(*) as actual_count from `p1p_dossier_issue_record`
  union all
  select count(*) as actual_count from `p1p_dossier_work_order`
  union all
  select count(*) as actual_count from `p1p_dossier_production_batch`
  union all
  select count(*) as actual_count from `p1p_dossier_part_instance`
  union all
  select count(*) as actual_count from `p1p_dossier_component_part_installation`
  union all
  select count(*) as actual_count from `p1p_dossier_workstation`
  union all
  select count(*) as actual_count from `p1p_dossier_equipment`
  union all
  select count(*) as actual_count from `p1p_dossier_personnel`
  union all
  select count(*) as actual_count from `p1p_dossier_tooling`
  union all
  select count(*) as actual_count from `p1p_dossier_operation_execution`
  union all
  select count(*) as actual_count from `p1p_dossier_step_execution`
  union all
  select count(*) as actual_count from `p1p_dossier_resource_usage`
  union all
  select count(*) as actual_count from `p1p_dossier_process_parameter_record`
  union all
  select count(*) as actual_count from `p1p_dossier_inspection_record`
  union all
  select count(*) as actual_count from `p1p_dossier_inspection_item_result`
  union all
  select count(*) as actual_count from `p1p_dossier_defect_record`
  union all
  select count(*) as actual_count from `p1p_dossier_maintenance_event`
  union all
  select count(*) as actual_count from `p1p_dossier_quality_event`
  union all
  select count(*) as actual_count from `p1p_dossier_iqs_failure`
  union all
  select count(*) as actual_count from `p1p_dossier_iqs_failure_content`
  union all
  select count(*) as actual_count from `p1p_dossier_iqs_failure_duty`
  union all
  select count(*) as actual_count from `p1p_dossier_disposition_record`
  union all
  select count(*) as actual_count from `p1p_dossier_reinspection_record`
  union all
  select count(*) as actual_count from `p1p_dossier_maintenance_order`
  union all
  select count(*) as actual_count from `p1p_dossier_fault_record`
  union all
  select count(*) as actual_count from `p1p_dossier_replacement_record`
  union all
  select count(*) as actual_count from `p1p_dossier_service_feedback`
  union all
  select count(*) as actual_count from `p1p_dossier_quality_event_subject`
) row_counts;

select
  check_item,
  actual_count,
  expected_count,
  if(actual_count = expected_count, 'PASS', 'CHECK') as status
from (
  select 'p1p_dossier_upper_equipment_rows' as check_item, (select count(*) from `p1p_dossier_upper_equipment`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_landing_gear_system_rows' as check_item, (select count(*) from `p1p_dossier_landing_gear_system`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_installed_position_rows' as check_item, (select count(*) from `p1p_dossier_installed_position`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_component_type_rows' as check_item, (select count(*) from `p1p_dossier_component_type`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_design_version_rows' as check_item, (select count(*) from `p1p_dossier_design_version`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_standard_dictionary_rows' as check_item, (select count(*) from `p1p_dossier_standard_dictionary`) as actual_count, 253 as expected_count
  union all
  select 'p1p_dossier_material_rows' as check_item, (select count(*) from `p1p_dossier_material`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_part_definition_rows' as check_item, (select count(*) from `p1p_dossier_part_definition`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_component_instance_rows' as check_item, (select count(*) from `p1p_dossier_component_instance`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_bom_item_rows' as check_item, (select count(*) from `p1p_dossier_bom_item`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_process_route_rows' as check_item, (select count(*) from `p1p_dossier_process_route`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_operation_definition_rows' as check_item, (select count(*) from `p1p_dossier_operation_definition`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_parameter_standard_rows' as check_item, (select count(*) from `p1p_dossier_parameter_standard`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_inspection_spec_rows' as check_item, (select count(*) from `p1p_dossier_inspection_spec`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_supplier_rows' as check_item, (select count(*) from `p1p_dossier_supplier`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_purchase_order_rows' as check_item, (select count(*) from `p1p_dossier_purchase_order`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_purchase_order_line_rows' as check_item, (select count(*) from `p1p_dossier_purchase_order_line`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_supplier_batch_rows' as check_item, (select count(*) from `p1p_dossier_supplier_batch`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_inventory_batch_rows' as check_item, (select count(*) from `p1p_dossier_inventory_batch`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_receiving_record_rows' as check_item, (select count(*) from `p1p_dossier_receiving_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_issue_record_rows' as check_item, (select count(*) from `p1p_dossier_issue_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_work_order_rows' as check_item, (select count(*) from `p1p_dossier_work_order`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_production_batch_rows' as check_item, (select count(*) from `p1p_dossier_production_batch`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_part_instance_rows' as check_item, (select count(*) from `p1p_dossier_part_instance`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_component_part_installation_rows' as check_item, (select count(*) from `p1p_dossier_component_part_installation`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_workstation_rows' as check_item, (select count(*) from `p1p_dossier_workstation`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_equipment_rows' as check_item, (select count(*) from `p1p_dossier_equipment`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_personnel_rows' as check_item, (select count(*) from `p1p_dossier_personnel`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_tooling_rows' as check_item, (select count(*) from `p1p_dossier_tooling`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_operation_execution_rows' as check_item, (select count(*) from `p1p_dossier_operation_execution`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_step_execution_rows' as check_item, (select count(*) from `p1p_dossier_step_execution`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_resource_usage_rows' as check_item, (select count(*) from `p1p_dossier_resource_usage`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_process_parameter_record_rows' as check_item, (select count(*) from `p1p_dossier_process_parameter_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_inspection_record_rows' as check_item, (select count(*) from `p1p_dossier_inspection_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_inspection_item_result_rows' as check_item, (select count(*) from `p1p_dossier_inspection_item_result`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_defect_record_rows' as check_item, (select count(*) from `p1p_dossier_defect_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_maintenance_event_rows' as check_item, (select count(*) from `p1p_dossier_maintenance_event`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_quality_event_rows' as check_item, (select count(*) from `p1p_dossier_quality_event`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_iqs_failure_rows' as check_item, (select count(*) from `p1p_dossier_iqs_failure`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_iqs_failure_content_rows' as check_item, (select count(*) from `p1p_dossier_iqs_failure_content`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_iqs_failure_duty_rows' as check_item, (select count(*) from `p1p_dossier_iqs_failure_duty`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_disposition_record_rows' as check_item, (select count(*) from `p1p_dossier_disposition_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_reinspection_record_rows' as check_item, (select count(*) from `p1p_dossier_reinspection_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_maintenance_order_rows' as check_item, (select count(*) from `p1p_dossier_maintenance_order`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_fault_record_rows' as check_item, (select count(*) from `p1p_dossier_fault_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_replacement_record_rows' as check_item, (select count(*) from `p1p_dossier_replacement_record`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_service_feedback_rows' as check_item, (select count(*) from `p1p_dossier_service_feedback`) as actual_count, 10 as expected_count
  union all
  select 'p1p_dossier_quality_event_subject_rows' as check_item, (select count(*) from `p1p_dossier_quality_event_subject`) as actual_count, 10 as expected_count
) row_counts
order by check_item;
