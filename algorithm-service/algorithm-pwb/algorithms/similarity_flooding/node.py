"""
Node class for Schema Graph representation.
Extended from Valentine's implementation to support multiple node types.
"""


class Node:
    """
    Class for describing a node of a schema graph.
    
    Attributes:
        name: Node identifier (e.g., "NodeID1", "aircraft_dossier", "VARCHAR")
        node_type: Type of node (Table, Column, ColumnType, Constraint, Literal)
        db: Database/schema identifier this node belongs to
        long_name: Tuple for output formatting (table_name, table_guid, column_name, column_guid)
        metadata: Additional metadata for the node
    """

    def __init__(self, name: str, db: str, node_type: str = None):
        """
        Initialize a Node.
        
        Args:
            name: Node name/identifier
            db: Database/schema identifier
            node_type: Optional type classification (Table, Column, etc.)
        """
        self.name = name
        self.db = db
        self.node_type = node_type
        self.long_name = None
        self.metadata = {}

    def add_long_name(self, table_name: str, table_guid: str, 
                      column_name: str = None, column_guid: str = None):
        """
        Add detailed name information for output formatting.
        
        Args:
            table_name: Name of the table
            table_guid: Unique identifier for the table
            column_name: Name of the column (if applicable)
            column_guid: Unique identifier for the column (if applicable)
        """
        self.long_name = (table_name, table_guid, column_name, column_guid)

    def set_metadata(self, key: str, value):
        """Set additional metadata for the node."""
        self.metadata[key] = value

    def get_metadata(self, key: str, default=None):
        """Get metadata value by key."""
        return self.metadata.get(key, default)

    def __eq__(self, other):
        if isinstance(other, Node):
            return self.name == other.name and self.db == other.db
        return False

    def __hash__(self):
        return hash((self.name, self.db))

    def __repr__(self):
        return f"Node({self.name}, db={self.db}, type={self.node_type})"

    def __str__(self):
        return f"{self.name}"

