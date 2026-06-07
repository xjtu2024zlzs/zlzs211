-- 模式匹配结果库 match_result_store 初始化占位
-- 真正的表结构由后端 init_result_tables() 自动创建（每次启动时 IDEMPOTENT）。

CREATE EXTENSION IF NOT EXISTS pgcrypto;
