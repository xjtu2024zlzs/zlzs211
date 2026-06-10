# Similarity Flooding Algorithm for PostgreSQL Schema Matching
# Based on Valentine's implementation with extensions for FK constraints

__all__ = [
    "SimilarityFloodingMatcher",
    "SchemaGraph",
    "SQLSchemaParser",
]

# Node type constants (extended from Valentine)
TABLE = "Table"
COLUMN = "Column"
COLUMN_TYPE = "ColumnType"
CONSTRAINT = "Constraint"  # New: for PK/FK constraints
LITERAL = "Literal"  # New: for literal name values

# Edge label constants (extended from Valentine's 4 to 9)
EDGE_NAME = "name"           # NodeID → Literal (table/column name)
EDGE_TYPE = "type"           # NodeID → Type node (Table/Column/ColumnType)
EDGE_COLUMN = "column"       # Table → Column
EDGE_SQLTYPE = "SQLtype"     # Column → ColumnType
EDGE_IS_PK = "is_pk"         # PK Constraint → Column (new)
EDGE_FK_SOURCE_TABLE = "fk_source_table"   # FK Constraint → Source Table (new)
EDGE_FK_TARGET_TABLE = "fk_target_table"   # FK Constraint → Target Table (new)
EDGE_FK_SOURCE_COL = "fk_source_col"       # FK Constraint → Source Column (new)
EDGE_FK_TARGET_COL = "fk_target_col"       # FK Constraint → Target Column (new)

