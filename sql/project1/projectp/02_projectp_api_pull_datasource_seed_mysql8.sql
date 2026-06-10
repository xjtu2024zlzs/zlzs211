-- projectp API Pull datasource seed.
-- Default base_url uses localhost. Replace 127.0.0.1 with the server IP when RuoYi runs on another host.

use `ry-cloud`;
set names utf8mb4;

-- PLM
insert into `p1p_datasource` (
  `datasource_name`, `access_mode`, `use_status`, `connection_status`,
  `status`, `del_flag`, `create_by`, `create_time`, `remark`
) values (
  'CF PLM API Pull', 'api_pull', 'enabled', 'untested',
  '0', '0', 'projectp', now(), 'projectp simulated external PLM source system'
) on duplicate key update
  `access_mode` = values(`access_mode`),
  `use_status` = values(`use_status`),
  `connection_status` = 'untested',
  `update_by` = 'projectp',
  `update_time` = now(),
  `remark` = values(`remark`);
set @projectp_plm_datasource_id = (select `datasource_id` from `p1p_datasource` where `datasource_name` = 'CF PLM API Pull' limit 1);
insert into `p1p_api_pull_datasource` (
  `datasource_id`, `base_url`, `pull_endpoint`, `schema_endpoint`, `request_method`,
  `auth_type`, `health_endpoint`, `headers_config`, `extra_config`, `create_time`, `remark`
) values (
  @projectp_plm_datasource_id, 'http://127.0.0.1:9711', '/api/pull', '/api/schema', 'POST',
  'none', '/api/health', json_object(), json_object('schemaKey', 'cf_plm', 'sourceLabel', 'PLM', 'simulatedStorage', 'mysql', 'physicalTablePrefix', 'p1p_ext_plm_'), now(),
  'projectp PLM API Pull adapter on port 9711'
) on duplicate key update
  `base_url` = values(`base_url`),
  `pull_endpoint` = values(`pull_endpoint`),
  `schema_endpoint` = values(`schema_endpoint`),
  `request_method` = values(`request_method`),
  `auth_type` = values(`auth_type`),
  `health_endpoint` = values(`health_endpoint`),
  `headers_config` = values(`headers_config`),
  `extra_config` = values(`extra_config`),
  `update_time` = now(),
  `remark` = values(`remark`);

-- ERP
insert into `p1p_datasource` (
  `datasource_name`, `access_mode`, `use_status`, `connection_status`,
  `status`, `del_flag`, `create_by`, `create_time`, `remark`
) values (
  'CF ERP API Pull', 'api_pull', 'enabled', 'untested',
  '0', '0', 'projectp', now(), 'projectp simulated external ERP source system'
) on duplicate key update
  `access_mode` = values(`access_mode`),
  `use_status` = values(`use_status`),
  `connection_status` = 'untested',
  `update_by` = 'projectp',
  `update_time` = now(),
  `remark` = values(`remark`);
set @projectp_erp_datasource_id = (select `datasource_id` from `p1p_datasource` where `datasource_name` = 'CF ERP API Pull' limit 1);
insert into `p1p_api_pull_datasource` (
  `datasource_id`, `base_url`, `pull_endpoint`, `schema_endpoint`, `request_method`,
  `auth_type`, `health_endpoint`, `headers_config`, `extra_config`, `create_time`, `remark`
) values (
  @projectp_erp_datasource_id, 'http://127.0.0.1:9712', '/api/pull', '/api/schema', 'POST',
  'none', '/api/health', json_object(), json_object('schemaKey', 'cf_erp', 'sourceLabel', 'ERP', 'simulatedStorage', 'mysql', 'physicalTablePrefix', 'p1p_ext_erp_'), now(),
  'projectp ERP API Pull adapter on port 9712'
) on duplicate key update
  `base_url` = values(`base_url`),
  `pull_endpoint` = values(`pull_endpoint`),
  `schema_endpoint` = values(`schema_endpoint`),
  `request_method` = values(`request_method`),
  `auth_type` = values(`auth_type`),
  `health_endpoint` = values(`health_endpoint`),
  `headers_config` = values(`headers_config`),
  `extra_config` = values(`extra_config`),
  `update_time` = now(),
  `remark` = values(`remark`);

-- MES
insert into `p1p_datasource` (
  `datasource_name`, `access_mode`, `use_status`, `connection_status`,
  `status`, `del_flag`, `create_by`, `create_time`, `remark`
) values (
  'CF MES API Pull', 'api_pull', 'enabled', 'untested',
  '0', '0', 'projectp', now(), 'projectp simulated external MES source system'
) on duplicate key update
  `access_mode` = values(`access_mode`),
  `use_status` = values(`use_status`),
  `connection_status` = 'untested',
  `update_by` = 'projectp',
  `update_time` = now(),
  `remark` = values(`remark`);
set @projectp_mes_datasource_id = (select `datasource_id` from `p1p_datasource` where `datasource_name` = 'CF MES API Pull' limit 1);
insert into `p1p_api_pull_datasource` (
  `datasource_id`, `base_url`, `pull_endpoint`, `schema_endpoint`, `request_method`,
  `auth_type`, `health_endpoint`, `headers_config`, `extra_config`, `create_time`, `remark`
) values (
  @projectp_mes_datasource_id, 'http://127.0.0.1:9713', '/api/pull', '/api/schema', 'POST',
  'none', '/api/health', json_object(), json_object('schemaKey', 'cf_mes', 'sourceLabel', 'MES', 'simulatedStorage', 'mysql', 'physicalTablePrefix', 'p1p_ext_mes_'), now(),
  'projectp MES API Pull adapter on port 9713'
) on duplicate key update
  `base_url` = values(`base_url`),
  `pull_endpoint` = values(`pull_endpoint`),
  `schema_endpoint` = values(`schema_endpoint`),
  `request_method` = values(`request_method`),
  `auth_type` = values(`auth_type`),
  `health_endpoint` = values(`health_endpoint`),
  `headers_config` = values(`headers_config`),
  `extra_config` = values(`extra_config`),
  `update_time` = now(),
  `remark` = values(`remark`);

-- QMS
insert into `p1p_datasource` (
  `datasource_name`, `access_mode`, `use_status`, `connection_status`,
  `status`, `del_flag`, `create_by`, `create_time`, `remark`
) values (
  'CF QMS API Pull', 'api_pull', 'enabled', 'untested',
  '0', '0', 'projectp', now(), 'projectp simulated external QMS source system'
) on duplicate key update
  `access_mode` = values(`access_mode`),
  `use_status` = values(`use_status`),
  `connection_status` = 'untested',
  `update_by` = 'projectp',
  `update_time` = now(),
  `remark` = values(`remark`);
set @projectp_qms_datasource_id = (select `datasource_id` from `p1p_datasource` where `datasource_name` = 'CF QMS API Pull' limit 1);
insert into `p1p_api_pull_datasource` (
  `datasource_id`, `base_url`, `pull_endpoint`, `schema_endpoint`, `request_method`,
  `auth_type`, `health_endpoint`, `headers_config`, `extra_config`, `create_time`, `remark`
) values (
  @projectp_qms_datasource_id, 'http://127.0.0.1:9714', '/api/pull', '/api/schema', 'POST',
  'none', '/api/health', json_object(), json_object('schemaKey', 'cf_qms', 'sourceLabel', 'QMS', 'simulatedStorage', 'mysql', 'physicalTablePrefix', 'p1p_ext_qms_'), now(),
  'projectp QMS API Pull adapter on port 9714'
) on duplicate key update
  `base_url` = values(`base_url`),
  `pull_endpoint` = values(`pull_endpoint`),
  `schema_endpoint` = values(`schema_endpoint`),
  `request_method` = values(`request_method`),
  `auth_type` = values(`auth_type`),
  `health_endpoint` = values(`health_endpoint`),
  `headers_config` = values(`headers_config`),
  `extra_config` = values(`extra_config`),
  `update_time` = now(),
  `remark` = values(`remark`);

-- MRO
insert into `p1p_datasource` (
  `datasource_name`, `access_mode`, `use_status`, `connection_status`,
  `status`, `del_flag`, `create_by`, `create_time`, `remark`
) values (
  'CF MRO API Pull', 'api_pull', 'enabled', 'untested',
  '0', '0', 'projectp', now(), 'projectp simulated external MRO source system'
) on duplicate key update
  `access_mode` = values(`access_mode`),
  `use_status` = values(`use_status`),
  `connection_status` = 'untested',
  `update_by` = 'projectp',
  `update_time` = now(),
  `remark` = values(`remark`);
set @projectp_mro_datasource_id = (select `datasource_id` from `p1p_datasource` where `datasource_name` = 'CF MRO API Pull' limit 1);
insert into `p1p_api_pull_datasource` (
  `datasource_id`, `base_url`, `pull_endpoint`, `schema_endpoint`, `request_method`,
  `auth_type`, `health_endpoint`, `headers_config`, `extra_config`, `create_time`, `remark`
) values (
  @projectp_mro_datasource_id, 'http://127.0.0.1:9715', '/api/pull', '/api/schema', 'POST',
  'none', '/api/health', json_object(), json_object('schemaKey', 'cf_mro', 'sourceLabel', 'MRO', 'simulatedStorage', 'mysql', 'physicalTablePrefix', 'p1p_ext_mro_'), now(),
  'projectp MRO API Pull adapter on port 9715'
) on duplicate key update
  `base_url` = values(`base_url`),
  `pull_endpoint` = values(`pull_endpoint`),
  `schema_endpoint` = values(`schema_endpoint`),
  `request_method` = values(`request_method`),
  `auth_type` = values(`auth_type`),
  `health_endpoint` = values(`health_endpoint`),
  `headers_config` = values(`headers_config`),
  `extra_config` = values(`extra_config`),
  `update_time` = now(),
  `remark` = values(`remark`);
