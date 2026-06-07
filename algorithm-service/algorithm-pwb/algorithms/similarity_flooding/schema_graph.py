"""
Similarity Flooding 模式图构建器

从 Valentine 的 Graph 类扩展，支持：
- 5 种节点类型：Table, Column, ColumnType, Constraint, Literal
- 9 种边标签：name, type, column, SQLtype, is_pk, fk_source_table, fk_target_table, fk_source_col, fk_target_col
"""

import networkx as nx
from typing import Dict, List, Optional, Set, Tuple

from .node import Node
from .sql_schema_parser import SchemaInfo, TableInfo, ColumnInfo, ForeignKeyInfo
from . import (
    TABLE, COLUMN, COLUMN_TYPE, CONSTRAINT, LITERAL,
    EDGE_NAME, EDGE_TYPE, EDGE_COLUMN, EDGE_SQLTYPE,
    EDGE_IS_PK, EDGE_FK_SOURCE_TABLE, EDGE_FK_TARGET_TABLE,
    EDGE_FK_SOURCE_COL, EDGE_FK_TARGET_COL
)


class SchemaGraph:
    """
    Similarity Flooding 算法的模式图构建器。
    
    构建一个 NetworkX 有向图，包含：
    - 节点类型：
        1. Table Node（每个表的 NodeID）
        2. Column Node（每个列的 NodeID）
        3. ColumnType Node（每个数据类型的 NodeID）
        4. Constraint Node（每个 PK/FK 约束的 NodeID）
        5. Literal Node（名称的实际字符串值）
    
    - 边标签（9 种）：
        1. 'name'：NodeID → Literal（表/列名）
        2. 'type'：NodeID → 类型标记（Table/Column/ColumnType）
        3. 'column'：Table NodeID → Column NodeID
        4. 'SQLtype'：Column NodeID → ColumnType NodeID
        5. 'is_pk'：PK Constraint NodeID → Column NodeID
        6. 'fk_source_table'：FK Constraint NodeID → 源 Table NodeID
        7. 'fk_target_table'：FK Constraint NodeID → 目标 Table NodeID
        8. 'fk_source_col'：FK Constraint NodeID → 源 Column NodeID
        9. 'fk_target_col'：FK Constraint NodeID → 目标 Column NodeID
    """
    
    def __init__(self, schema: SchemaInfo, db_name: Optional[str] = None):
        """
        初始化模式图构建器。
        
        Args:
            schema: 包含解析后模式元数据的 SchemaInfo 对象
            db_name: 数据库/模式标识符（默认为 schema.name）
        """
        self.graph = nx.DiGraph()
        self.schema = schema
        self.db_name = db_name or schema.name
        
        # 用于生成唯一 NodeID 的计数器
        self._node_id_counter = 0
        
        # 节点检索的查找表
        self._table_nodes: Dict[str, Node] = {}      # table_name -> Table NodeID
        self._column_nodes: Dict[str, Node] = {}     # "table.column" -> Column NodeID
        self._type_nodes: Dict[str, Node] = {}       # data_type -> ColumnType NodeID
        self._pk_constraint_nodes: Dict[str, Node] = {}  # table_name -> PK Constraint NodeID
        self._fk_constraint_nodes: Dict[str, Node] = {}  # constraint_name -> FK Constraint NodeID
        
        # 类型标记节点（所有节点共享）
        self._type_marker_table: Optional[Node] = None
        self._type_marker_column: Optional[Node] = None
        self._type_marker_coltype: Optional[Node] = None
        self._type_marker_constraint: Optional[Node] = None
        
        # 构建图
        self._build_graph()
    
    def _next_node_id(self) -> str:
        """生成下一个唯一的节点 ID。"""
        self._node_id_counter += 1
        return f"NodeID{self._node_id_counter}"
    
    def _create_node(self, name: str, node_type: str = None) -> Node:
        """使用给定名称创建新节点。"""
        node = Node(name=name, db=self.db_name, node_type=node_type)
        return node
    
    def _create_type_marker_nodes(self):
        """创建类型标记节点（Table, Column, ColumnType, Constraint）。"""
        # 这些是用于标记其他节点类型的单例节点
        self._type_marker_table = self._create_node(TABLE, node_type="TypeMarker")
        self._type_marker_column = self._create_node(COLUMN, node_type="TypeMarker")
        self._type_marker_coltype = self._create_node(COLUMN_TYPE, node_type="TypeMarker")
        self._type_marker_constraint = self._create_node(CONSTRAINT, node_type="TypeMarker")
        
        self.graph.add_node(self._type_marker_table)
        self.graph.add_node(self._type_marker_column)
        self.graph.add_node(self._type_marker_coltype)
        self.graph.add_node(self._type_marker_constraint)
    
    def _add_table_node(self, table: TableInfo) -> Node:
        """
        向图中添加表节点。
        
        创建：
        - Table NodeID
        - 表名的 Literal 节点
        - 边：Table NodeID --[name]--> Literal
        - 边：Table NodeID --[type]--> Table 标记
        """
        node_id = self._next_node_id()
        table_node = self._create_node(node_id, node_type=TABLE)
        
        # 添加用于输出格式化的 long_name
        table_node.add_long_name(
            table_name=table.name,
            table_guid=f"{self.db_name}.{table.name}",
            column_name=None,
            column_guid=None
        )
        
        self.graph.add_node(table_node)
        self._table_nodes[table.name] = table_node
        
        # 创建表名的 Literal 节点
        literal_node = self._create_node(table.name, node_type=LITERAL)
        literal_node.add_long_name(
            table_name=table.name,
            table_guid=f"{self.db_name}.{table.name}",
            column_name=None,
            column_guid=None
        )
        self.graph.add_node(literal_node)
        
        # 添加边
        self.graph.add_edge(table_node, literal_node, label=EDGE_NAME)
        self.graph.add_edge(table_node, self._type_marker_table, label=EDGE_TYPE)
        
        return table_node
    
    def _add_column_node(self, table: TableInfo, column: ColumnInfo) -> Node:
        """
        向图中添加列节点。
        
        创建：
        - Column NodeID
        - 列名的 Literal 节点
        - 边：Column NodeID --[name]--> Literal
        - 边：Column NodeID --[type]--> Column 标记
        - 边：Table NodeID --[column]--> Column NodeID
        - 边：Column NodeID --[SQLtype]--> ColumnType NodeID
        """
        node_id = self._next_node_id()
        col_node = self._create_node(node_id, node_type=COLUMN)
        
        # 添加用于输出格式化的 long_name
        col_node.add_long_name(
            table_name=table.name,
            table_guid=f"{self.db_name}.{table.name}",
            column_name=column.name,
            column_guid=f"{self.db_name}.{table.name}.{column.name}"
        )
        
        self.graph.add_node(col_node)
        col_key = f"{table.name}.{column.name}"
        self._column_nodes[col_key] = col_node
        
        # 创建列名的 Literal 节点
        literal_node = self._create_node(column.name, node_type=LITERAL)
        literal_node.add_long_name(
            table_name=table.name,
            table_guid=f"{self.db_name}.{table.name}",
            column_name=column.name,
            column_guid=f"{self.db_name}.{table.name}.{column.name}"
        )
        self.graph.add_node(literal_node)
        
        # 边：Column --[name]--> Literal
        self.graph.add_edge(col_node, literal_node, label=EDGE_NAME)
        
        # 边：Column --[type]--> Column 标记
        self.graph.add_edge(col_node, self._type_marker_column, label=EDGE_TYPE)
        
        # 边：Table --[column]--> Column
        table_node = self._table_nodes[table.name]
        self.graph.add_edge(table_node, col_node, label=EDGE_COLUMN)
        
        # 处理 SQL 类型
        type_node = self._get_or_create_type_node(column.data_type)
        self.graph.add_edge(col_node, type_node, label=EDGE_SQLTYPE)
        
        return col_node
    
    def _get_or_create_type_node(self, data_type: str) -> Node:
        """
        获取或创建给定数据类型的 ColumnType 节点。
        
        创建：
        - ColumnType NodeID（如果是新的）
        - 类型名的 Literal 节点
        - 边：ColumnType NodeID --[name]--> Literal
        - 边：ColumnType NodeID --[type]--> ColumnType 标记
        """
        if data_type in self._type_nodes:
            return self._type_nodes[data_type]
        
        node_id = self._next_node_id()
        type_node = self._create_node(node_id, node_type=COLUMN_TYPE)
        type_node.set_metadata("data_type", data_type)
        
        self.graph.add_node(type_node)
        self._type_nodes[data_type] = type_node
        
        # 创建类型名的 Literal 节点
        literal_node = self._create_node(data_type, node_type=LITERAL)
        self.graph.add_node(literal_node)
        
        # 添加边
        self.graph.add_edge(type_node, literal_node, label=EDGE_NAME)
        self.graph.add_edge(type_node, self._type_marker_coltype, label=EDGE_TYPE)
        
        return type_node
    
    def _add_pk_constraint_node(self, table: TableInfo) -> Optional[Node]:
        """
        如果表有主键，则添加主键约束节点。
        
        创建：
        - PK Constraint NodeID
        - 边：PK Constraint --[type]--> Constraint 标记
        - 边：PK Constraint --[is_pk]--> Column NodeID
        """
        if not table.primary_key:
            return None
        
        node_id = self._next_node_id()
        pk_node = self._create_node(node_id, node_type=CONSTRAINT)
        pk_node.set_metadata("constraint_type", "PRIMARY_KEY")
        pk_node.set_metadata("table", table.name)
        
        self.graph.add_node(pk_node)
        self._pk_constraint_nodes[table.name] = pk_node
        
        # 边：PK --[type]--> Constraint 标记
        self.graph.add_edge(pk_node, self._type_marker_constraint, label=EDGE_TYPE)
        
        # 边：PK --[is_pk]--> Column
        col_key = f"{table.name}.{table.primary_key}"
        if col_key in self._column_nodes:
            col_node = self._column_nodes[col_key]
            self.graph.add_edge(pk_node, col_node, label=EDGE_IS_PK)
        
        return pk_node
    
    def _add_fk_constraint_node(self, fk: ForeignKeyInfo) -> Node:
        """
        添加外键约束节点。
        
        创建：
        - FK Constraint NodeID
        - 边：FK Constraint --[type]--> Constraint 标记
        - 边：FK Constraint --[fk_source_table]--> 源 Table NodeID
        - 边：FK Constraint --[fk_target_table]--> 目标 Table NodeID
        - 边：FK Constraint --[fk_source_col]--> 源 Column NodeID
        - 边：FK Constraint --[fk_target_col]--> 目标 Column NodeID
        
        注意：fk_source/fk_target 指的是外键关系方向：
        - source = 包含 FK 列的表（引用表）
        - target = 被引用的表（参照表）
        """
        node_id = self._next_node_id()
        fk_node = self._create_node(node_id, node_type=CONSTRAINT)
        fk_node.set_metadata("constraint_type", "FOREIGN_KEY")
        fk_node.set_metadata("constraint_name", fk.constraint_name)
        
        self.graph.add_node(fk_node)
        self._fk_constraint_nodes[fk.constraint_name] = fk_node
        
        # 边：FK --[type]--> Constraint 标记
        self.graph.add_edge(fk_node, self._type_marker_constraint, label=EDGE_TYPE)
        
        # 边：FK --[fk_source_table]--> 源 Table（包含 FK 列的表）
        if fk.source_table in self._table_nodes:
            source_table_node = self._table_nodes[fk.source_table]
            self.graph.add_edge(fk_node, source_table_node, label=EDGE_FK_SOURCE_TABLE)
        
        # 边：FK --[fk_target_table]--> 目标 Table（被引用的表）
        if fk.target_table in self._table_nodes:
            target_table_node = self._table_nodes[fk.target_table]
            self.graph.add_edge(fk_node, target_table_node, label=EDGE_FK_TARGET_TABLE)
        
        # 边：FK --[fk_source_col]--> 源 Column（FK 列）
        source_col_key = f"{fk.source_table}.{fk.source_column}"
        if source_col_key in self._column_nodes:
            source_col_node = self._column_nodes[source_col_key]
            self.graph.add_edge(fk_node, source_col_node, label=EDGE_FK_SOURCE_COL)
        
        # 边：FK --[fk_target_col]--> 目标 Column（被引用的 PK 列）
        target_col_key = f"{fk.target_table}.{fk.target_column}"
        if target_col_key in self._column_nodes:
            target_col_node = self._column_nodes[target_col_key]
            self.graph.add_edge(fk_node, target_col_node, label=EDGE_FK_TARGET_COL)
        
        return fk_node
    
    def _build_graph(self):
        """构建完整的模式图。"""
        # 步骤 1：创建类型标记节点
        self._create_type_marker_nodes()
        
        # 步骤 2：添加所有表及其列
        for table_name, table_info in self.schema.tables.items():
            # 添加表节点
            self._add_table_node(table_info)
            
            # 添加列节点
            for col_name, col_info in table_info.columns.items():
                self._add_column_node(table_info, col_info)
        
        # 步骤 3：添加主键约束
        for table_name, table_info in self.schema.tables.items():
            self._add_pk_constraint_node(table_info)
        
        # 步骤 4：添加外键约束
        for table_name, table_info in self.schema.tables.items():
            for fk in table_info.foreign_keys:
                self._add_fk_constraint_node(fk)
    
    def get_table_node(self, table_name: str) -> Optional[Node]:
        """获取给定表名的 Table NodeID。"""
        return self._table_nodes.get(table_name)
    
    def get_column_node(self, table_name: str, column_name: str) -> Optional[Node]:
        """获取给定 table.column 的 Column NodeID。"""
        col_key = f"{table_name}.{column_name}"
        return self._column_nodes.get(col_key)
    
    def get_all_column_nodes(self) -> Dict[str, Node]:
        """获取所有列节点。"""
        return self._column_nodes.copy()
    
    def get_all_table_nodes(self) -> Dict[str, Node]:
        """获取所有表节点。"""
        return self._table_nodes.copy()
    
    def get_stats(self) -> Dict[str, int]:
        """获取图统计信息。"""
        # 按类型统计节点
        table_count = len(self._table_nodes)
        column_count = len(self._column_nodes)
        type_count = len(self._type_nodes)
        pk_count = len(self._pk_constraint_nodes)
        fk_count = len(self._fk_constraint_nodes)
        
        # 按标签统计边
        edge_counts = {}
        for _, _, data in self.graph.edges(data=True):
            label = data.get('label', 'unknown')
            edge_counts[label] = edge_counts.get(label, 0) + 1
        
        return {
            "total_nodes": self.graph.number_of_nodes(),
            "total_edges": self.graph.number_of_edges(),
            "table_nodes": table_count,
            "column_nodes": column_count,
            "type_nodes": type_count,
            "pk_constraints": pk_count,
            "fk_constraints": fk_count,
            "edges_by_label": edge_counts
        }
    
    def print_stats(self):
        """打印图统计信息。"""
        stats = self.get_stats()
        print(f"\n{'='*50}")
        print(f"模式图统计: {self.db_name}")
        print(f"{'='*50}")
        print(f"总节点数: {stats['total_nodes']}")
        print(f"  - 表节点: {stats['table_nodes']}")
        print(f"  - 列节点: {stats['column_nodes']}")
        print(f"  - 类型节点: {stats['type_nodes']}")
        print(f"  - 主键约束: {stats['pk_constraints']}")
        print(f"  - 外键约束: {stats['fk_constraints']}")
        print(f"\n总边数: {stats['total_edges']}")
        print("按标签统计:")
        for label, count in stats['edges_by_label'].items():
            print(f"  - {label}: {count}")
        print(f"{'='*50}\n")


def build_schema_graph(schema: SchemaInfo, db_name: Optional[str] = None) -> nx.DiGraph:
    """
    构建模式图的便捷函数。
    
    Args:
        schema: SchemaInfo 对象
        db_name: 可选的数据库名
        
    Returns:
        NetworkX 有向图
    """
    builder = SchemaGraph(schema, db_name)
    return builder.graph
