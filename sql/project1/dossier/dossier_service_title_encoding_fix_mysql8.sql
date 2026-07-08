-- Fix two Project 1 dossier service titles that were stored with mojibake.
-- Scope: demo part HYD-TUBE-MLG-32A only.

START TRANSACTION;

UPDATE t1_dossier_content_item
   SET item_name = '液压弯管装机动作'
 WHERE item_code = 'CONTENT-1943-SRC-102'
   AND bom_node_id = 'f1000006-0006-4006-8006-000000000006'
   AND source_table = 't1_install_removal'
   AND source_record_key = 'c9100002-0002-4002-8002-000000000002';

UPDATE t1_dossier_content_item
   SET item_name = '液压弯管拆卸/复查履历'
 WHERE item_code = 'CONTENT-1943-SRC-103'
   AND bom_node_id = 'f1000006-0006-4006-8006-000000000006'
   AND source_table = 't1_install_removal'
   AND source_record_key = 'c9100001-0001-4001-8001-000000000001';

COMMIT;
