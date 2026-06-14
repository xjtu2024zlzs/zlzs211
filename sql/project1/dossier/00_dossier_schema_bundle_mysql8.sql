-- ============================================================
-- Digital dossier schema bundle
-- Target MySQL: 8.0.46
--
-- Run this file from the repository root with the mysql client:
--   mysql --default-character-set=utf8mb4 -uroot -p ry-cloud < sql/project1/dossier/00_dossier_schema_bundle_mysql8.sql
--
-- This is an execution entrypoint. Keep the real DDL in the source files
-- below to avoid maintaining duplicated schema SQL.
-- ============================================================

SOURCE sql/project1/dossier/dossier_full_schema_mysql8.sql;
SOURCE sql/project1/dossier/dossier_file_unified_tables_mysql8.sql;
SOURCE sql/project1/dossier/dossier_instance_management_mysql8.sql;
