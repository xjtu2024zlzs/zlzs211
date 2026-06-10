"""
Neo4j 模式知识图谱交互层

将数据库 Schema（表、列、PK/FK）加载到 Neo4j 构成结构图，
并支持在 LLM 两轮重排过程中写入和查询语义边（SEMANTIC_MATCH）。

节点类型:
  (:Table  {name, database})
  (:Column {name, table_name, is_pk, is_fk})

边类型:
  (:Column)-[:BELONGS_TO]->(:Table)
  (:Column)-[:FK_REFERENCES]->(:Column)
  (:Column)-[:SEMANTIC_MATCH {score, dataset}]->(:Column)   -- Pass 1 写入
"""

from typing import Dict, List, Optional, Set

try:
    from neo4j import GraphDatabase
    HAS_NEO4J = True
except ImportError:
    HAS_NEO4J = False


class SchemaGraphNeo4j:
    """封装 Neo4j 连接及所有图操作。"""

    def __init__(
        self,
        uri: str = "bolt://localhost:7687",
        user: str = "neo4j",
        password: str = "magneto123",
    ):
        if not HAS_NEO4J:
            raise ImportError("neo4j Python driver 未安装，请执行 pip install neo4j")
        self._driver = GraphDatabase.driver(uri, auth=(user, password))
        self._ensure_indexes()

    def close(self):
        self._driver.close()

    def _ensure_indexes(self):
        with self._driver.session() as s:
            s.run("CREATE INDEX IF NOT EXISTS FOR (t:Table) ON (t.name, t.database)")
            s.run("CREATE INDEX IF NOT EXISTS FOR (c:Column) ON (c.table_name, c.name)")

    # ------------------------------------------------------------------
    # Schema 加载（结构图）
    # ------------------------------------------------------------------

    def load_schema(self, schema):
        """将 SchemaInfo 写入 Neo4j（幂等，MERGE 不会重复创建）。"""
        db_name = schema.name
        with self._driver.session() as s:
            for table_name, table_info in schema.tables.items():
                s.run(
                    "MERGE (t:Table {name: $name, database: $db})",
                    name=table_name, db=db_name,
                )
                for col_name, col_info in table_info.columns.items():
                    is_pk = (col_info.is_primary_key
                             or table_info.primary_key == col_name)
                    is_fk = any(
                        fk.source_column == col_name
                        for fk in table_info.foreign_keys
                    )
                    s.run(
                        """
                        MERGE (c:Column {table_name: $tbl, name: $col})
                        SET c.is_pk = $is_pk, c.is_fk = $is_fk, c.database = $db
                        WITH c
                        MATCH (t:Table {name: $tbl, database: $db})
                        MERGE (c)-[:BELONGS_TO]->(t)
                        """,
                        tbl=table_name, col=col_name,
                        is_pk=is_pk, is_fk=is_fk, db=db_name,
                    )

                for fk in table_info.foreign_keys:
                    s.run(
                        """
                        MATCH (src:Column {table_name: $src_tbl, name: $src_col})
                        MATCH (tgt:Column {table_name: $tgt_tbl, name: $tgt_col})
                        MERGE (src)-[:FK_REFERENCES]->(tgt)
                        """,
                        src_tbl=fk.source_table, src_col=fk.source_column,
                        tgt_tbl=fk.target_table, tgt_col=fk.target_column,
                    )

    # ------------------------------------------------------------------
    # 锚点列查询
    # ------------------------------------------------------------------

    def get_anchor_columns(self, table_name: str) -> Set[str]:
        """返回表的 PK/FK 列集合（包括被其他表引用的列）。"""
        with self._driver.session() as s:
            result = s.run(
                """
                MATCH (c:Column {table_name: $tbl})
                WHERE c.is_pk = true OR c.is_fk = true
                RETURN c.name AS col
                UNION
                MATCH (other:Column)-[:FK_REFERENCES]->(c:Column {table_name: $tbl})
                RETURN c.name AS col
                """,
                tbl=table_name,
            )
            return {r["col"] for r in result}

    # ------------------------------------------------------------------
    # 语义边 CRUD
    # ------------------------------------------------------------------

    def add_semantic_edge(
        self,
        src_table: str, src_col: str,
        tgt_table: str, tgt_col: str,
        score: float, dataset: str = "",
    ):
        """写入一条 SEMANTIC_MATCH 边。"""
        with self._driver.session() as s:
            s.run(
                """
                MATCH (sc:Column {table_name: $src_tbl, name: $src_col})
                MATCH (tc:Column {table_name: $tgt_tbl, name: $tgt_col})
                MERGE (sc)-[r:SEMANTIC_MATCH]->(tc)
                SET r.score = $score, r.dataset = $dataset
                """,
                src_tbl=src_table, src_col=src_col,
                tgt_tbl=tgt_table, tgt_col=tgt_col,
                score=score, dataset=dataset,
            )

    def get_semantic_edges(
        self,
        src_table: str,
        candidate_tables: List[str],
    ) -> List[Dict]:
        """
        查询源表与给定目标表集合之间的所有 SEMANTIC_MATCH 边。

        Returns:
            [{"src_col": ..., "src_table": ..., "tgt_col": ..., "tgt_table": ..., "score": ...}]
        """
        if not candidate_tables:
            return []
        with self._driver.session() as s:
            result = s.run(
                """
                MATCH (sc:Column)-[:BELONGS_TO]->(st:Table {name: $src_tbl})
                MATCH (sc)-[r:SEMANTIC_MATCH]->(tc:Column)-[:BELONGS_TO]->(tt:Table)
                WHERE tt.name IN $tgt_tables
                RETURN sc.name AS src_col, st.name AS src_table,
                       tc.name AS tgt_col, tt.name AS tgt_table,
                       r.score AS score
                """,
                src_tbl=src_table, tgt_tables=candidate_tables,
            )
            return [dict(r) for r in result]

    def clear_semantic_edges(self, dataset: str = ""):
        """清除指定 dataset 的所有语义边（实验前重置）。"""
        with self._driver.session() as s:
            if dataset:
                s.run(
                    "MATCH ()-[r:SEMANTIC_MATCH {dataset: $ds}]->() DELETE r",
                    ds=dataset,
                )
            else:
                s.run("MATCH ()-[r:SEMANTIC_MATCH]->() DELETE r")

    def clear_all(self):
        """清空整个图（调试用）。"""
        with self._driver.session() as s:
            s.run("MATCH (n) DETACH DELETE n")
