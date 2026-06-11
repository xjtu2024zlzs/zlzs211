"""
pgvector 向量存储模块

封装所有与 PostgreSQL + pgvector 的交互，提供嵌入向量的
持久化存储、批量加载和存在性检查功能。

替代原先的 .pt 文件缓存方案，优势：
- 持久化存储，跨实验复用
- 按 dataset/model/mode 维度隔离
- 支持元数据查询
"""

import numpy as np
import torch
from typing import Dict, Optional

try:
    import psycopg2
    import psycopg2.extras
    HAS_PSYCOPG2 = True
except ImportError:
    HAS_PSYCOPG2 = False


class VectorStore:
    """pgvector 嵌入向量存储。"""

    def __init__(
        self,
        host: str = "localhost",
        port: int = 5433,
        dbname: str = "magneto_vectors",
        user: str = "magneto",
        password: str = "magneto123",
    ):
        if not HAS_PSYCOPG2:
            raise ImportError(
                "psycopg2 未安装。请执行: pip install psycopg2-binary"
            )
        self.conn = psycopg2.connect(
            host=host, port=port, dbname=dbname, user=user, password=password
        )
        self.conn.autocommit = True

    def has_embeddings(
        self, dataset: str, embedding_model: str, encoding_mode: str
    ) -> bool:
        """检查指定配置的嵌入是否已存在。"""
        with self.conn.cursor() as cur:
            cur.execute(
                "SELECT COUNT(*) FROM column_embeddings "
                "WHERE dataset=%s AND embedding_model=%s AND encoding_mode=%s",
                (dataset, embedding_model, encoding_mode),
            )
            count = cur.fetchone()[0]
        return count > 0

    def store_embeddings(
        self,
        dataset: str,
        embedding_model: str,
        encoding_mode: str,
        embeddings_dict: Dict[str, torch.Tensor],
    ):
        """
        批量写入嵌入向量。

        Args:
            dataset: 数据集标识 (如 "faa_fkp")
            embedding_model: 模型名 (如 "mpnet")
            encoding_mode: 编码模式 (如 "header_values_default")
            embeddings_dict: {"table_name.column_name": tensor(768,), ...}
        """
        rows = []
        for key, tensor in embeddings_dict.items():
            parts = key.split(".", 1)
            if len(parts) != 2:
                continue
            table_name, column_name = parts
            db_name = table_name.split("_")[0] if "_" in table_name else "unknown"
            vec = tensor.cpu().numpy().tolist()
            rows.append((
                dataset, db_name, table_name, column_name,
                encoding_mode, embedding_model, str(vec),
            ))

        if not rows:
            return

        sql = (
            "INSERT INTO column_embeddings "
            "(dataset, database_name, table_name, column_name, "
            "encoding_mode, embedding_model, embedding) "
            "VALUES (%s, %s, %s, %s, %s, %s, %s) "
            "ON CONFLICT (dataset, table_name, column_name, encoding_mode, embedding_model) "
            "DO UPDATE SET embedding = EXCLUDED.embedding, "
            "created_at = CURRENT_TIMESTAMP"
        )

        with self.conn.cursor() as cur:
            psycopg2.extras.execute_batch(cur, sql, rows, page_size=200)

        print(f"已写入 {len(rows)} 条嵌入到 pgvector")

    def load_embeddings(
        self,
        dataset: str,
        embedding_model: str,
        encoding_mode: str,
        device: Optional[torch.device] = None,
    ) -> Dict[str, torch.Tensor]:
        """
        批量加载嵌入向量到内存。

        Returns:
            {"table_name.column_name": tensor(768,), ...}
        """
        if device is None:
            device = torch.device("cpu")

        with self.conn.cursor() as cur:
            cur.execute(
                "SELECT table_name, column_name, embedding "
                "FROM column_embeddings "
                "WHERE dataset=%s AND embedding_model=%s AND encoding_mode=%s",
                (dataset, embedding_model, encoding_mode),
            )
            rows = cur.fetchall()

        cache = {}
        for table_name, column_name, embedding_str in rows:
            vec = np.fromstring(
                embedding_str.strip("[]"), sep=",", dtype=np.float32
            )
            cache[f"{table_name}.{column_name}"] = torch.from_numpy(vec).to(device)

        return cache

    def get_embedding_count(
        self, dataset: str, embedding_model: str, encoding_mode: str
    ) -> int:
        """返回指定配置的嵌入数量。"""
        with self.conn.cursor() as cur:
            cur.execute(
                "SELECT COUNT(*) FROM column_embeddings "
                "WHERE dataset=%s AND embedding_model=%s AND encoding_mode=%s",
                (dataset, embedding_model, encoding_mode),
            )
            return cur.fetchone()[0]

    def close(self):
        """关闭数据库连接。"""
        if self.conn and not self.conn.closed:
            self.conn.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.close()
