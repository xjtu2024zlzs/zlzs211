-- 统一结构中间库 integration_staging 初始化占位
-- 真正的表结构由后端 init_staging_tables() 自动创建（每次启动时 IDEMPOTENT）。

CREATE EXTENSION IF NOT EXISTS pgcrypto;
