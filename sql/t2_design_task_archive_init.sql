-- Design task archive table.
-- The archive table stores a completion-time snapshot assembled from existing task tables.

CREATE TABLE IF NOT EXISTS t2_design_task_archive (
  archive_id bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Archive ID',
  task_id bigint(20) NOT NULL COMMENT 'Task ID',
  archive_code varchar(80) NOT NULL COMMENT 'Archive code',
  archive_title varchar(300) NOT NULL COMMENT 'Archive title',
  archive_status varchar(30) DEFAULT 'ARCHIVED' COMMENT 'Archive status',
  archive_json longtext COMMENT 'Archive snapshot JSON',
  archive_time datetime DEFAULT NULL COMMENT 'Archive time',
  archive_by varchar(64) DEFAULT '' COMMENT 'Archive user',
  export_file_id bigint(20) DEFAULT NULL COMMENT 'Export file ID',
  create_time datetime DEFAULT NULL,
  update_time datetime DEFAULT NULL,
  PRIMARY KEY (archive_id),
  UNIQUE KEY uk_t2_task_archive_task (task_id),
  KEY idx_t2_task_archive_code (archive_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Design task archive';
