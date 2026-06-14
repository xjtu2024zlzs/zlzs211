-- ============================================================
-- project1 access scope field unique key upgrade
-- Target MySQL: 8.0.46
-- Purpose:
--   Support one source field mapping to multiple target fields in the same
--   access scope table.
-- ============================================================

set names utf8mb4;

set @schema_name = database();

-- Precheck: this query must return zero rows before the unique key can be
-- safely created.
select scope_table_id, source_column, target_column, count(*) as cnt
from p1p_access_scope_field
group by scope_table_id, source_column, target_column
having count(*) > 1;

select group_concat(column_name order by seq_in_index separator ',')
into @scope_field_key_columns
from information_schema.statistics
where table_schema = @schema_name
  and table_name = 'p1p_access_scope_field'
  and index_name = 'uq_p1p_access_scope_field';

set @sql = if(@scope_field_key_columns is not null
        and @scope_field_key_columns <> 'scope_table_id,source_column,target_column',
    'alter table p1p_access_scope_field drop index uq_p1p_access_scope_field',
    'select ''uq_p1p_access_scope_field already compatible or not found''');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

select group_concat(column_name order by seq_in_index separator ',')
into @scope_field_key_columns
from information_schema.statistics
where table_schema = @schema_name
  and table_name = 'p1p_access_scope_field'
  and index_name = 'uq_p1p_access_scope_field';

set @sql = if(@scope_field_key_columns is null,
    'alter table p1p_access_scope_field add unique key uq_p1p_access_scope_field (scope_table_id, source_column, target_column)',
    'select ''uq_p1p_access_scope_field already exists''');
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

select index_name, seq_in_index, column_name
from information_schema.statistics
where table_schema = @schema_name
  and table_name = 'p1p_access_scope_field'
  and index_name = 'uq_p1p_access_scope_field'
order by seq_in_index;
