"""
NodePair class for Propagation Graph.
Represents a pair of nodes from source and target schema graphs.
"""

from .node import Node


class NodePair:
    """
    Class for describing a map pair in the propagation graph.
    
    A NodePair (n1, n2) represents a potential mapping between:
    - node1: A node from the source schema graph
    - node2: A node from the target schema graph
    
    These pairs form the nodes of the Propagation Graph (PCG/IPG),
    and similarity scores propagate through edges between NodePairs.
    """

    def __init__(self, node1: Node, node2: Node):
        """
        Initialize a NodePair.
        
        Args:
            node1: Node from the source schema graph
            node2: Node from the target schema graph
        """
        self.node1 = node1
        self.node2 = node2

    def __eq__(self, other):
        if isinstance(other, NodePair):
            # NodePairs are equal if they contain the same nodes
            # (order matters - (n1, n2) != (n2, n1))
            return (self.node1 == other.node1 and self.node2 == other.node2)
        return False

    def __hash__(self):
        # Use both node names for hashing to avoid collisions
        return hash((self.node1.name, self.node1.db, 
                     self.node2.name, self.node2.db))

    def __repr__(self):
        return f"NodePair({self.node1}, {self.node2})"

    def __str__(self):
        return f"({self.node1.name}, {self.node2.name})"

