"""
PostgreSQL 模式提取的 SQL DDL 解析器

解析 CREATE TABLE 和 ALTER TABLE 语句以提取：
- 表、列、数据类型
- 主键（PK）
- 外键（FK）
"""

import re
import json
from pathlib import Path
from typing import Dict, List, Optional, Tuple
from dataclasses import dataclass, field, asdict


@dataclass
class ColumnInfo:
    """列元数据。"""
    name: str
    data_type: str
    is_primary_key: bool = False
    is_nullable: bool = True
    comment: Optional[str] = None


@dataclass
class ForeignKeyInfo:
    """外键约束元数据。"""
    constraint_name: str
    source_table: str
    source_column: str
    target_table: str
    target_column: str


@dataclass
class TableInfo:
    """表元数据。"""
    name: str
    columns: Dict[str, ColumnInfo] = field(default_factory=dict)
    primary_key: Optional[str] = None
    foreign_keys: List[ForeignKeyInfo] = field(default_factory=list)
    comment: Optional[str] = None


@dataclass
class SchemaInfo:
    """完整的模式元数据。"""
    name: str
    tables: Dict[str, TableInfo] = field(default_factory=dict)
    
    def to_dict(self) -> dict:
        """转换为字典用于 JSON 序列化。"""
        return {
            "name": self.name,
            "tables": {
                tname: {
                    "name": table.name,
                    "columns": {
                        cname: {
                            "name": col.name,
                            "data_type": col.data_type,
                            "is_primary_key": col.is_primary_key,
                            "is_nullable": col.is_nullable,
                            "comment": col.comment
                        }
                        for cname, col in table.columns.items()
                    },
                    "primary_key": table.primary_key,
                    "foreign_keys": [
                        {
                            "constraint_name": fk.constraint_name,
                            "source_table": fk.source_table,
                            "source_column": fk.source_column,
                            "target_table": fk.target_table,
                            "target_column": fk.target_column
                        }
                        for fk in table.foreign_keys
                    ],
                    "comment": table.comment
                }
                for tname, table in self.tables.items()
            }
        }
    
    def to_json(self, indent: int = 2) -> str:
        """转换为 JSON 字符串。"""
        return json.dumps(self.to_dict(), indent=indent, ensure_ascii=False)
    
    def save_json(self, filepath: str):
        """保存到 JSON 文件。"""
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(self.to_json())


class SQLSchemaParser:
    """
    PostgreSQL DDL 语句解析器。
    
    提取模式结构，包括：
    - 表及其列
    - 数据类型
    - 主键约束
    - 外键约束
    """
    
    # SQL 解析的正则表达式模式
    CREATE_TABLE_PATTERN = re.compile(
        r'CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(\w+)\s*\((.*?)\);',
        re.IGNORECASE | re.DOTALL
    )
    
    COLUMN_PATTERN = re.compile(
        r'^\s*(\w+)\s+([\w\(\),\s]+?)(?:\s+(PRIMARY\s+KEY|NOT\s+NULL|UNIQUE|DEFAULT\s+[^,]+))*\s*(?:,|$)',
        re.IGNORECASE | re.MULTILINE
    )
    
    ALTER_FK_PATTERN = re.compile(
        r'ALTER\s+TABLE\s+(\w+)\s+ADD\s+CONSTRAINT\s+(\w+)\s+FOREIGN\s+KEY\s*\((\w+)\)\s*REFERENCES\s+(\w+)\s*\((\w+)\)',
        re.IGNORECASE
    )
    
    COMMENT_TABLE_PATTERN = re.compile(
        r"COMMENT\s+ON\s+TABLE\s+(\w+)\s+IS\s+'([^']+)'",
        re.IGNORECASE
    )
    
    COMMENT_COLUMN_PATTERN = re.compile(
        r"COMMENT\s+ON\s+COLUMN\s+(\w+)\.(\w+)\s+IS\s+'([^']+)'",
        re.IGNORECASE
    )
    
    def __init__(self, schema_name: str = "default"):
        """
        初始化解析器。
        
        Args:
            schema_name: 被解析模式的名称
        """
        self.schema_name = schema_name
        self.schema = SchemaInfo(name=schema_name)
    
    def parse_file(self, filepath: str) -> SchemaInfo:
        """
        解析 SQL 文件。
        
        Args:
            filepath: SQL 文件路径
            
        Returns:
            包含解析后模式的 SchemaInfo 对象
        """
        with open(filepath, 'r', encoding='utf-8') as f:
            sql_content = f.read()
        return self.parse_sql(sql_content)
    
    def parse_sql(self, sql_content: str) -> SchemaInfo:
        """
        解析 SQL 内容。
        
        Args:
            sql_content: SQL DDL 语句字符串
            
        Returns:
            包含解析后模式的 SchemaInfo 对象
        """
        # 移除 SQL 注释
        sql_content = self._remove_comments(sql_content)
        
        # 解析 CREATE TABLE 语句
        self._parse_create_tables(sql_content)
        
        # 解析外键的 ALTER TABLE
        self._parse_foreign_keys(sql_content)
        
        # 解析 COMMENT 语句
        self._parse_comments(sql_content)
        
        return self.schema
    
    def _remove_comments(self, sql: str) -> str:
        """移除 SQL 注释。"""
        # 移除单行注释 (-- ...)
        sql = re.sub(r'--[^\n]*', '', sql)
        # 移除多行注释 (/* ... */)
        sql = re.sub(r'/\*.*?\*/', '', sql, flags=re.DOTALL)
        return sql
    
    def _parse_create_tables(self, sql: str):
        """解析 CREATE TABLE 语句。"""
        for match in self.CREATE_TABLE_PATTERN.finditer(sql):
            table_name = match.group(1).lower()
            columns_str = match.group(2)
            
            table = TableInfo(name=table_name)
            
            # 解析列
            self._parse_columns(table, columns_str)
            
            self.schema.tables[table_name] = table
    
    def _parse_columns(self, table: TableInfo, columns_str: str):
        """从 CREATE TABLE 解析列定义。"""
        # 按逗号分割，但注意类型中的括号
        lines = []
        current_line = ""
        paren_depth = 0
        
        for char in columns_str:
            if char == '(':
                paren_depth += 1
            elif char == ')':
                paren_depth -= 1
            elif char == ',' and paren_depth == 0:
                lines.append(current_line.strip())
                current_line = ""
                continue
            current_line += char
        
        if current_line.strip():
            lines.append(current_line.strip())
        
        for line in lines:
            line = line.strip()
            if not line:
                continue
            
            # 跳过约束定义
            if line.upper().startswith(('CONSTRAINT', 'PRIMARY KEY', 'FOREIGN KEY', 'UNIQUE', 'CHECK')):
                # 检查 PRIMARY KEY (col1, col2) 语法
                pk_match = re.match(r'PRIMARY\s+KEY\s*\(([^)]+)\)', line, re.IGNORECASE)
                if pk_match:
                    pk_cols = [c.strip() for c in pk_match.group(1).split(',')]
                    if len(pk_cols) == 1:
                        table.primary_key = pk_cols[0].lower()
                        if pk_cols[0].lower() in table.columns:
                            table.columns[pk_cols[0].lower()].is_primary_key = True
                continue
            
            # 解析列定义
            parts = line.split()
            if len(parts) < 2:
                continue
            
            col_name = parts[0].lower()
            
            # 跳过看起来像保留字的
            if col_name.upper() in ('CONSTRAINT', 'PRIMARY', 'FOREIGN', 'UNIQUE', 'CHECK', 'INDEX'):
                continue
            
            # 提取数据类型（可能包含大小如 VARCHAR(50)）
            data_type = parts[1].upper()
            if len(parts) > 2 and parts[2].startswith('('):
                # 处理像 "DECIMAL (10,2)" 这样的情况
                data_type += parts[2]
            
            # 规范化数据类型
            data_type = self._normalize_data_type(data_type)
            
            # 检查同一行是否有 PRIMARY KEY
            is_pk = 'PRIMARY KEY' in line.upper()
            is_nullable = 'NOT NULL' not in line.upper() and not is_pk
            
            column = ColumnInfo(
                name=col_name,
                data_type=data_type,
                is_primary_key=is_pk,
                is_nullable=is_nullable
            )
            
            table.columns[col_name] = column
            
            if is_pk:
                table.primary_key = col_name
    
    def _normalize_data_type(self, data_type: str) -> str:
        """规范化数据类型为标准形式。"""
        # 移除大小规格以便比较
        base_type = re.sub(r'\([^)]*\)', '', data_type).strip()
        
        # 映射常见类型
        type_mapping = {
            'INT': 'INTEGER',
            'INT4': 'INTEGER',
            'INT8': 'BIGINT',
            'FLOAT4': 'REAL',
            'FLOAT8': 'DOUBLE PRECISION',
            'BOOL': 'BOOLEAN',
            'SERIAL': 'INTEGER',  # SERIAL 是自增 INTEGER
            'BIGSERIAL': 'BIGINT',
        }
        
        normalized = type_mapping.get(base_type, base_type)
        return normalized
    
    def _parse_foreign_keys(self, sql: str):
        """解析 ALTER TABLE ... ADD CONSTRAINT ... FOREIGN KEY 语句。"""
        for match in self.ALTER_FK_PATTERN.finditer(sql):
            source_table = match.group(1).lower()
            constraint_name = match.group(2).lower()
            source_column = match.group(3).lower()
            target_table = match.group(4).lower()
            target_column = match.group(5).lower()
            
            if source_table in self.schema.tables:
                fk = ForeignKeyInfo(
                    constraint_name=constraint_name,
                    source_table=source_table,
                    source_column=source_column,
                    target_table=target_table,
                    target_column=target_column
                )
                self.schema.tables[source_table].foreign_keys.append(fk)
    
    def _parse_comments(self, sql: str):
        """解析 COMMENT ON TABLE/COLUMN 语句。"""
        # 表注释
        for match in self.COMMENT_TABLE_PATTERN.finditer(sql):
            table_name = match.group(1).lower()
            comment = match.group(2)
            if table_name in self.schema.tables:
                self.schema.tables[table_name].comment = comment
        
        # 列注释
        for match in self.COMMENT_COLUMN_PATTERN.finditer(sql):
            table_name = match.group(1).lower()
            column_name = match.group(2).lower()
            comment = match.group(3)
            if table_name in self.schema.tables:
                if column_name in self.schema.tables[table_name].columns:
                    self.schema.tables[table_name].columns[column_name].comment = comment
    
    def get_all_foreign_keys(self) -> List[ForeignKeyInfo]:
        """获取所有表的所有外键。"""
        all_fks = []
        for table in self.schema.tables.values():
            all_fks.extend(table.foreign_keys)
        return all_fks
    
    def get_table_columns(self, table_name: str) -> Dict[str, ColumnInfo]:
        """获取指定表的列。"""
        table_name = table_name.lower()
        if table_name in self.schema.tables:
            return self.schema.tables[table_name].columns
        return {}


def parse_multiple_sql_files(
    file_paths: List[str],
    schema_names: Optional[List[str]] = None
) -> Dict[str, SchemaInfo]:
    """
    解析多个 SQL 文件为独立的模式。
    
    Args:
        file_paths: SQL 文件路径列表
        schema_names: 可选的模式名称列表（默认为文件名）
        
    Returns:
        模式名称到 SchemaInfo 的字典映射
    """
    if schema_names is None:
        schema_names = [Path(fp).stem for fp in file_paths]
    
    schemas = {}
    for fp, name in zip(file_paths, schema_names):
        parser = SQLSchemaParser(schema_name=name)
        schemas[name] = parser.parse_file(fp)
    
    return schemas


def load_schema_from_json(filepath: str) -> SchemaInfo:
    """
    从 JSON 文件加载模式。
    
    Args:
        filepath: JSON 文件路径
        
    Returns:
        SchemaInfo 对象
    """
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    schema = SchemaInfo(name=data['name'])
    
    for tname, tdata in data['tables'].items():
        table = TableInfo(
            name=tdata['name'],
            primary_key=tdata.get('primary_key'),
            comment=tdata.get('comment')
        )
        
        for cname, cdata in tdata['columns'].items():
            col = ColumnInfo(
                name=cdata['name'],
                data_type=cdata['data_type'],
                is_primary_key=cdata.get('is_primary_key', False),
                is_nullable=cdata.get('is_nullable', True),
                comment=cdata.get('comment')
            )
            table.columns[cname] = col
        
        for fkdata in tdata.get('foreign_keys', []):
            fk = ForeignKeyInfo(
                constraint_name=fkdata['constraint_name'],
                source_table=fkdata['source_table'],
                source_column=fkdata['source_column'],
                target_table=fkdata['target_table'],
                target_column=fkdata['target_column']
            )
            table.foreign_keys.append(fk)
        
        schema.tables[tname] = table
    
    return schema


# 快速解析的便捷函数
def parse_schema(sql_file: str, schema_name: Optional[str] = None) -> SchemaInfo:
    """
    快速解析 SQL 文件。
    
    Args:
        sql_file: SQL 文件路径
        schema_name: 可选的模式名称（默认为文件名）
        
    Returns:
        SchemaInfo 对象
    """
    if schema_name is None:
        schema_name = Path(sql_file).stem
    
    parser = SQLSchemaParser(schema_name=schema_name)
    return parser.parse_file(sql_file)


def extract_subschema(
    schema: SchemaInfo,
    center_tables: set,
    hop: int = 1
) -> SchemaInfo:
    """
    从完整 schema 中提取以指定表为中心的子 schema。
    
    沿外键关系向外扩展 hop 跳，收集所有相关表，
    生成仅包含这些表及其内部外键关系的精简 SchemaInfo。
    
    这对于 Similarity Flooding 算法非常重要：使用完整 schema
    构建的传播图包含大量不相关节点，导致噪声传播淹没正确信号。
    通过子图提取，可以将传播图规模从 O(N_all^2) 降至 O(N_sub^2)，
    显著提高匹配精度。
    
    Args:
        schema: 完整的 SchemaInfo 对象
        center_tables: 中心表名集合（当前匹配涉及的表）
        hop: 沿外键关系向外扩展的跳数（默认 1）
             - 0 或 None: 不提取，返回完整 schema 的副本
             - 1: 包含中心表 + 直接外键邻居
             - 2: 包含 1-hop 邻居的邻居，依此类推
        
    Returns:
        仅包含相关表及其内部外键关系的新 SchemaInfo
    
    Example:
        >>> full_schema = parse_schema("05_dossier_schema.sql", "dossier")
        >>> # 匹配 aircraft_dossier 时，只保留它和 1-hop 外键邻居
        >>> sub = extract_subschema(full_schema, {"aircraft_dossier"}, hop=1)
        >>> print(list(sub.tables.keys()))
        ['aircraft_dossier', 'type_certificate_dossier', 'powerplant_dossier', ...]
    """
    # hop=0 或 None 时不做提取，返回完整 schema
    if not hop or hop <= 0:
        return schema
    
    # 只保留 schema 中实际存在的中心表
    center_tables = {t for t in center_tables if t in schema.tables}
    if not center_tables:
        return schema
    
    # 预构建外键邻接表（双向），加速多跳查找
    fk_neighbors: Dict[str, set] = {}
    for table_name, table_info in schema.tables.items():
        if table_name not in fk_neighbors:
            fk_neighbors[table_name] = set()
        for fk in table_info.foreign_keys:
            # 正向：source_table -> target_table
            if fk.source_table not in fk_neighbors:
                fk_neighbors[fk.source_table] = set()
            fk_neighbors[fk.source_table].add(fk.target_table)
            # 反向：target_table -> source_table
            if fk.target_table not in fk_neighbors:
                fk_neighbors[fk.target_table] = set()
            fk_neighbors[fk.target_table].add(fk.source_table)
    
    # BFS 扩展 N 跳
    relevant_tables = set(center_tables)
    frontier = set(center_tables)
    
    for _ in range(hop):
        next_frontier = set()
        for table in frontier:
            neighbors = fk_neighbors.get(table, set())
            for neighbor in neighbors:
                if neighbor in schema.tables and neighbor not in relevant_tables:
                    next_frontier.add(neighbor)
        relevant_tables |= next_frontier
        frontier = next_frontier
        if not frontier:
            break
    
    # 构建子 schema
    sub_schema = SchemaInfo(name=f"{schema.name}_sub")
    
    for table_name in relevant_tables:
        if table_name not in schema.tables:
            continue
        
        original_table = schema.tables[table_name]
        
        # 复制表和列信息
        new_table = TableInfo(
            name=original_table.name,
            primary_key=original_table.primary_key,
            comment=original_table.comment,
        )
        for col_name, col_info in original_table.columns.items():
            new_table.columns[col_name] = ColumnInfo(
                name=col_info.name,
                data_type=col_info.data_type,
                is_primary_key=col_info.is_primary_key,
                is_nullable=col_info.is_nullable,
                comment=col_info.comment,
            )
        
        # 只保留两端都在相关表集合内的外键
        for fk in original_table.foreign_keys:
            if fk.source_table in relevant_tables and fk.target_table in relevant_tables:
                new_table.foreign_keys.append(ForeignKeyInfo(
                    constraint_name=fk.constraint_name,
                    source_table=fk.source_table,
                    source_column=fk.source_column,
                    target_table=fk.target_table,
                    target_column=fk.target_column,
                ))
        
        sub_schema.tables[table_name] = new_table
    
    return sub_schema


def build_column_context(
    schema: SchemaInfo,
    table_name: str,
    column_name: str,
) -> str:
    """
    将列的结构信息（所在表、PK/FK 关系）序列化为自然语言字符串，
    用于注入 LLM 重排的提示词。

    Returns:
        格式: "Table: xxx, Role: PRIMARY KEY, FK references: a -> b.c, Referenced by: d.e"
        无信息的字段统一显示 None。
    """
    table_info = schema.tables.get(table_name)
    if table_info is None:
        return f"Table: {table_name}, Role: None, FK references: None, Referenced by: None"

    col_info = table_info.columns.get(column_name)

    # Role
    if col_info and col_info.is_primary_key:
        role = "PRIMARY KEY"
    elif table_info.primary_key and table_info.primary_key == column_name:
        role = "PRIMARY KEY"
    else:
        role = "None"

    # FK references: 本表以该列作为外键引出的关系
    fk_refs = []
    for fk in table_info.foreign_keys:
        if fk.source_column == column_name:
            fk_refs.append(f"{fk.source_column} -> {fk.target_table}.{fk.target_column}")

    # Referenced by: 其他表通过外键引用该列
    referenced_by = []
    for other_name, other_table in schema.tables.items():
        for fk in other_table.foreign_keys:
            if fk.target_table == table_name and fk.target_column == column_name:
                referenced_by.append(f"{fk.source_table}.{fk.source_column}")

    fk_str = ", ".join(fk_refs) if fk_refs else "None"
    ref_str = ", ".join(referenced_by) if referenced_by else "None"

    return f"Table: {table_name}, Role: {role}, FK references: {fk_str}, Referenced by: {ref_str}"
