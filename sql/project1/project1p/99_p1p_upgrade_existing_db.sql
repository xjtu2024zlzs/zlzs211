-- ============================================================
-- project1 existing database upgrade script
-- Target MySQL: 8.0.46
-- Purpose:
--   1. Support multiple result sets under the same match record.
--   2. Support independent metrics for each result set.
--   3. Keep audited rows unique by match result row, not by source field.
-- ============================================================

set names utf8mb4;

set @schema_name = database();

-- p1p_reviewed_match:
-- Old wrong unique key: one source field can appear only once in the same record.
-- New correct unique key: one p1p_match_result_row owns one reviewed row.
set @sql = (
    select if(count(*) > 0,
        'alter table p1p_reviewed_match drop key uq_p1p_review_record_source',
        'select ''uq_p1p_review_record_source not found''')
    from information_schema.statistics
    where table_schema = @schema_name
      and table_name = 'p1p_reviewed_match'
      and index_name = 'uq_p1p_review_record_source'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @sql = (
    select if(count(*) = 0,
        'alter table p1p_reviewed_match add unique key uq_p1p_review_result_row (result_row_id)',
        'select ''uq_p1p_review_result_row already exists''')
    from information_schema.statistics
    where table_schema = @schema_name
      and table_name = 'p1p_reviewed_match'
      and index_name = 'uq_p1p_review_result_row'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @sql = (
    select if(count(*) = 0,
        'alter table p1p_reviewed_match add key idx_p1p_review_record_source (record_id, source_table, source_column)',
        'select ''idx_p1p_review_record_source already exists''')
    from information_schema.statistics
    where table_schema = @schema_name
      and table_name = 'p1p_reviewed_match'
      and index_name = 'idx_p1p_review_record_source'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

-- p1p_task_metric:
-- Old wrong unique key: one metric key can appear only once in the same record.
-- New correct unique key: one metric key can appear once in one result set.
set @sql = (
    select if(count(*) > 0,
        'alter table p1p_task_metric drop key uq_p1p_task_metric_record_key',
        'select ''uq_p1p_task_metric_record_key not found''')
    from information_schema.statistics
    where table_schema = @schema_name
      and table_name = 'p1p_task_metric'
      and index_name = 'uq_p1p_task_metric_record_key'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @sql = (
    select if(count(*) = 0,
        'alter table p1p_task_metric add unique key uq_p1p_task_metric_result_key (result_set_id, metric_key)',
        'select ''uq_p1p_task_metric_result_key already exists''')
    from information_schema.statistics
    where table_schema = @schema_name
      and table_name = 'p1p_task_metric'
      and index_name = 'uq_p1p_task_metric_result_key'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;

set @sql = (
    select if(count(*) = 0,
        'alter table p1p_task_metric add key idx_p1p_task_metric_record_key (record_id, metric_key)',
        'select ''idx_p1p_task_metric_record_key already exists''')
    from information_schema.statistics
    where table_schema = @schema_name
      and table_name = 'p1p_task_metric'
      and index_name = 'idx_p1p_task_metric_record_key'
);
prepare stmt from @sql;
execute stmt;
deallocate prepare stmt;
