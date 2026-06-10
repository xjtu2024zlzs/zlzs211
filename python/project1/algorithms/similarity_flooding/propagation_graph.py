"""
Similarity Flooding 传播图（PCG/IPG）构建器

基于 Valentine 的实现，支持外键边的加权传播。
"""

import networkx as nx
from typing import Dict, Tuple

from .node_pair import NodePair
from . import (
    EDGE_FK_SOURCE_TABLE, EDGE_FK_TARGET_TABLE,
    EDGE_FK_SOURCE_COL, EDGE_FK_TARGET_COL, EDGE_IS_PK
)


# 应该获得更高传播权重的外键相关边标签
FK_EDGE_LABELS = {
    EDGE_IS_PK,
    EDGE_FK_SOURCE_TABLE,
    EDGE_FK_TARGET_TABLE,
    EDGE_FK_SOURCE_COL,
    EDGE_FK_TARGET_COL,
}


class PropagationGraph:
    """
    从两个输入模式图构建传播图的类。
    
    传播图（也称为成对连接图 - PCG 或诱导传播图 - IPG）
    是 Similarity Flooding 算法的核心结构。
    
    节点：NodePair (n1, n2)，其中 n1 ∈ graph1，n2 ∈ graph2
    边：连接具有相同标签的原始边对应的 NodePair
    权重：根据策略计算的传播系数
    """

    def __init__(self, graph1: nx.DiGraph, graph2: nx.DiGraph, 
                 policy: str = 'inverse_average',
                 fk_weight_multiplier: float = 2.0):
        """
        初始化传播图构建器。
        
        Args:
            graph1: 源模式图（NetworkX 有向图）
            graph2: 目标模式图（NetworkX 有向图）
            policy: 系数计算策略（'inverse_average' 或 'inverse_product'）
            fk_weight_multiplier: 外键相关边的权重乘数（默认 2.0）
        """
        self.graph1 = graph1
        self.graph2 = graph2
        self.policy = policy
        self.fk_weight_multiplier = fk_weight_multiplier

    @staticmethod
    def __inverse_label_values(labels: Dict[str, float], m: float = 1.0):
        """
        计算传播系数的逆值。
        
        Args:
            labels: 标签 -> 计数的字典映射
            m: 乘数（默认 1.0，inverse_average 使用 2.0）
        """
        for key, value in labels.items():
            labels[key] = m / value

    def __get_edge_weight_multiplier(self, label: str) -> float:
        """
        根据边标签获取权重乘数。
        外键相关的边获得更高的权重。
        
        Args:
            label: 边标签
            
        Returns:
            权重乘数（外键边为 fk_weight_multiplier，其他为 1.0）
        """
        if label in FK_EDGE_LABELS:
            return self.fk_weight_multiplier
        return 1.0

    def __add_propagation_edges(self, c_graph: nx.DiGraph, p_graph: nx.DiGraph, 
                                 node: NodePair, case_in: bool) -> nx.DiGraph:
        """
        使用 inverse_product 策略为节点添加传播边。
        
        Args:
            c_graph: 连接图
            p_graph: 正在构建的传播图
            node: 要处理的 NodePair
            case_in: 如果为 True，处理入边；否则处理出边
            
        Returns:
            更新后的传播图
        """
        if case_in:
            edges = c_graph.in_edges(node)
        else:
            edges = c_graph.out_edges(node)

        labels = {}
        for e in edges:
            edge_data = c_graph.get_edge_data(e[0], e[1])
            label = edge_data.get('label')

            if label in labels:
                labels[label] += 1.0
            else:
                labels[label] = 1.0

        self.__inverse_label_values(labels)

        for e in edges:
            edge_data = c_graph.get_edge_data(e[0], e[1])
            label = edge_data.get('label')
            
            # 应用外键权重乘数
            weight = labels[label] * self.__get_edge_weight_multiplier(label)

            if case_in:
                p_graph.add_edge(e[1], e[0], weight=weight, label=label)
            else:
                p_graph.add_edge(e[0], e[1], weight=weight, label=label)

        return p_graph

    def __construct_connectivity_graph(self) -> nx.DiGraph:
        """
        构建连接图（C_G）。
        
        对于每对边 (e1, e2)，其中：
        - e1 = (a, b) 是 graph1 中标签为 L 的边
        - e2 = (x, y) 是 graph2 中标签为 L 的边
        
        我们添加：
        - NodePair(a, x) 和 NodePair(b, y) 作为节点
        - 从 NodePair(a, x) 到 NodePair(b, y) 的边，标签为 L
        
        这是 Similarity Flooding 的核心：只有标签匹配的边
        才能传播相似度！
        
        Returns:
            连接图（NetworkX 有向图）
        """
        c_g = nx.DiGraph()

        for e1 in self.graph1.edges():
            for e2 in self.graph2.edges():
                l1 = self.graph1.get_edge_data(e1[0], e1[1])
                l2 = self.graph2.get_edge_data(e2[0], e2[1])
                
                # 核心条件：标签必须匹配！
                if l1.get('label') == l2.get('label'):
                    np1 = NodePair(e1[0], e2[0])
                    c_g.add_node(np1)
                    np2 = NodePair(e1[1], e2[1])
                    c_g.add_node(np2)
                    c_g.add_edge(np1, np2, label=l1.get('label'))

        return c_g

    @staticmethod
    def __create_label_dicts(graph1: nx.DiGraph, graph2: nx.DiGraph, 
                             node: NodePair) -> Tuple[Dict, Dict, Dict, Dict]:
        """
        为 inverse_average 策略创建标签频率字典。
        
        Args:
            graph1: 第一个模式图
            graph2: 第二个模式图
            node: 要分析的 NodePair
            
        Returns:
            元组 (in_labels1, in_labels2, out_labels1, out_labels2)
        """
        in_labels1 = {}
        out_labels1 = {}
        in_labels2 = {}
        out_labels2 = {}

        # 统计 graph1 中 node1 的入边标签
        for e in graph1.in_edges(node.node1):
            edge_data = graph1.get_edge_data(e[0], e[1])
            label = edge_data.get('label')
            in_labels1[label] = in_labels1.get(label, 0) + 1.0

        # 统计 graph2 中 node2 的入边标签
        for e in graph2.in_edges(node.node2):
            edge_data = graph2.get_edge_data(e[0], e[1])
            label = edge_data.get('label')
            in_labels2[label] = in_labels2.get(label, 0) + 1.0

        # 统计 graph1 中 node1 的出边标签
        for e in graph1.out_edges(node.node1):
            edge_data = graph1.get_edge_data(e[0], e[1])
            label = edge_data.get('label')
            out_labels1[label] = out_labels1.get(label, 0) + 1.0

        # 统计 graph2 中 node2 的出边标签
        for e in graph2.out_edges(node.node2):
            edge_data = graph2.get_edge_data(e[0], e[1])
            label = edge_data.get('label')
            out_labels2[label] = out_labels2.get(label, 0) + 1.0

        return in_labels1, in_labels2, out_labels1, out_labels2

    def construct_graph(self) -> nx.DiGraph:
        """
        构建传播图。
        
        使用指定策略计算传播系数：
        - inverse_product：基于标签频率的简单逆值
        - inverse_average：两个图标签频率的平均值
        
        Returns:
            带权重边的传播图（NetworkX 有向图）
        """
        c_g = self.__construct_connectivity_graph()
        
        # 初始化相似度传播图
        p_g = nx.DiGraph()

        for n in c_g.nodes():
            p_g.add_node(n)

        if self.policy == 'inverse_product':
            # 简单的逆积策略
            for node in p_g.nodes():
                p_g = self.__add_propagation_edges(c_g, p_g, node, case_in=True)
                p_g = self.__add_propagation_edges(c_g, p_g, node, case_in=False)

        elif self.policy == 'inverse_average':
            # 逆平均策略（推荐）
            for n in p_g.nodes():
                # 确定 node1 属于哪个图
                if n.node1 in self.graph1.nodes():
                    in_labels1, in_labels2, out_labels1, out_labels2 = \
                        self.__create_label_dicts(self.graph1, self.graph2, n)
                else:
                    in_labels1, in_labels2, out_labels1, out_labels2 = \
                        self.__create_label_dicts(self.graph2, self.graph1, n)

                # 合并标签计数
                in_labels = in_labels1.copy()
                out_labels = out_labels1.copy()

                for key in in_labels2:
                    in_labels[key] = in_labels.get(key, 0) + in_labels2[key]

                for key in out_labels2:
                    out_labels[key] = out_labels.get(key, 0) + out_labels2[key]

                # 计算逆值（m=2.0 用于取平均）
                self.__inverse_label_values(in_labels, m=2.0)
                self.__inverse_label_values(out_labels, m=2.0)

                # 添加带权重的传播边
                for e in c_g.in_edges(n):
                    edge_data = c_g.get_edge_data(e[0], e[1])
                    label = edge_data.get('label')
                    weight = in_labels[label] * self.__get_edge_weight_multiplier(label)
                    p_g.add_edge(e[1], e[0], weight=weight, label=label)

                for e in c_g.out_edges(n):
                    edge_data = c_g.get_edge_data(e[0], e[1])
                    label = edge_data.get('label')
                    weight = out_labels[label] * self.__get_edge_weight_multiplier(label)
                    p_g.add_edge(e[0], e[1], weight=weight, label=label)

        else:
            raise ValueError(f"未知策略: {self.policy}。"
                           f"请使用 'inverse_average' 或 'inverse_product'")

        return p_g
