-- ============================================================
-- Digital dossier seed bundle
-- Target MySQL: 8.0.46
--
-- Prerequisite:
--   Run sql/project1/dossier/00_dossier_schema_bundle_mysql8.sql first.
--
-- Run this file from the repository root with the mysql client:
--   mysql --default-character-set=utf8mb4 -uroot -p ry-cloud < sql/project1/dossier/01_dossier_seed_bundle_mysql8.sql
--
-- This is an execution entrypoint. Keep the real seed/enrichment SQL in
-- the source files below to avoid maintaining duplicated data scripts.
-- ============================================================

SOURCE sql/project1/dossier/dossier_mock_data_seed_mysql8.sql;
SOURCE sql/project1/dossier/dossier_aircraft_profile_enrichment_mysql8.sql;
SOURCE sql/project1/dossier/dossier_system_subsystem_profile_enrichment_mysql8.sql;
SOURCE sql/project1/dossier/dossier_equipment_component_part_profile_enrichment_mysql8.sql;
SOURCE sql/project1/dossier/dossier_b1234_version_normalize_mysql8.sql;
