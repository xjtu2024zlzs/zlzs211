-- pgvector 向量数据库初始化脚本
-- 用于存储 Magneto 列嵌入向量，替代 .pt 文件缓存

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE column_embeddings (
    id SERIAL PRIMARY KEY,
    dataset VARCHAR(50) NOT NULL,
    database_name VARCHAR(50) NOT NULL,
    table_name VARCHAR(100) NOT NULL,
    column_name VARCHAR(100) NOT NULL,
    encoding_mode VARCHAR(50) NOT NULL,
    embedding_model VARCHAR(200) NOT NULL,
    embedding vector(768),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(dataset, table_name, column_name, encoding_mode, embedding_model)
);

CREATE INDEX idx_embeddings_lookup
ON column_embeddings(dataset, encoding_mode, embedding_model);

CREATE INDEX idx_embeddings_table
ON column_embeddings(dataset, table_name);
