"""
Similarity Flooding 多表模式匹配器

结合了：
1. Magneto 基于嵌入的初始映射
2. Valentine 的 Similarity Flooding 图传播算法
"""

import math
import os
import sys
from typing import Dict, List, Optional, Tuple, Set

# 强制离线模式，避免联网检查 HuggingFace Hub（模型已缓存在本地）
os.environ.setdefault("HF_HUB_OFFLINE", "1")
os.environ.setdefault("TRANSFORMERS_OFFLINE", "1")

import pandas as pd
import torch
from sentence_transformers import SentenceTransformer
from transformers import AutoTokenizer

import networkx as nx

# 添加项目根目录到路径
project_path = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, project_path)

from algorithms.similarity_flooding.node import Node
from algorithms.similarity_flooding.node_pair import NodePair
from algorithms.similarity_flooding.propagation_graph import PropagationGraph
from algorithms.similarity_flooding.schema_graph import SchemaGraph
from algorithms.similarity_flooding.sql_schema_parser import (
    SchemaInfo, parse_schema, load_schema_from_json
)
from algorithms.similarity_flooding import (
    COLUMN, LITERAL, COLUMN_TYPE,
    EDGE_NAME, EDGE_TYPE
)

# 尝试导入 Magneto 组件
try:
    from magneto.bp_reranker import arrange_bipartite_matches
    from magneto.column_encoder import ColumnEncoder
    from magneto.utils.embedding_utils import compute_cosine_similarity_simple
    from magneto.utils.utils import clean_df, get_samples
    HAS_MAGNETO = True
except ImportError:
    HAS_MAGNETO = False
    print("警告: 未找到 Magneto，使用备选相似度计算方法。")


# 默认嵌入模型
DEFAULT_MODELS = {
    "mpnet": "sentence-transformers/all-mpnet-base-v2",
    "roberta": "sentence-transformers/all-roberta-large-v1",
    "e5": "intfloat/e5-base",
    "minilm": "sentence-transformers/all-MiniLM-L6-v2"
}


class SimilarityFloodingMatcher:
    """
    Similarity Flooding 匹配器，结合了：
    1. Magneto 基于嵌入的相似度作为初始映射
    2. Valentine 的 Similarity Flooding 进行约束传播
    3. Magneto 的匈牙利算法进行最终一对一匹配
    
    通过外键关系传播相似度，解决单表匹配的局限性。
    """
    
    def __init__(
        self,
        source_schema: SchemaInfo,
        target_schema: SchemaInfo,
        embedding_model: str = "mpnet",
        encoding_mode: str = "header_values_default",
        sampling_mode: str = "mixed",
        sampling_size: int = 10,
        topk: int = 20,
        coeff_policy: str = "inverse_average",
        formula: str = "formula_c",
        fk_weight_multiplier: float = 2.0,
        max_iterations: int = 100,
        convergence_threshold: float = 1e-4,
        use_bp_reranker: bool = True,
        subgraph_hop: int = 1,
    ):
        """
        初始化 Similarity Flooding 匹配器。
        
        Args:
            source_schema: 源数据库的 SchemaInfo
            target_schema: 目标数据库的 SchemaInfo
            embedding_model: 嵌入模型名称 (mpnet, roberta 等)
            encoding_mode: Magneto 的列编码模式
            sampling_mode: 值采样模式
            sampling_size: 采样值数量
            topk: 考虑的候选数量
            coeff_policy: 传播系数策略 ('inverse_average' 或 'inverse_product')
            formula: SF 公式 ('basic', 'formula_a', 'formula_b', 'formula_c')
            fk_weight_multiplier: 外键相关边的权重乘数
            max_iterations: 最大不动点迭代次数
            convergence_threshold: 不动点收敛阈值
            use_bp_reranker: 是否使用匈牙利算法进行最终匹配
            subgraph_hop: 子图提取跳数（默认 1）。
                为每对匹配表构建仅包含中心表及 N-hop 外键邻居的子 schema，
                避免完整 schema 的噪声传播。设为 0 或 None 禁用子图提取。
        """
        self.source_schema = source_schema
        self.target_schema = target_schema
        
        # 嵌入参数
        self.embedding_model_name = embedding_model
        self.encoding_mode = encoding_mode
        self.sampling_mode = sampling_mode
        self.sampling_size = sampling_size
        self.topk = topk
        
        # SF 参数
        self.coeff_policy = coeff_policy
        self.formula = formula
        self.fk_weight_multiplier = fk_weight_multiplier
        self.max_iterations = max_iterations
        self.convergence_threshold = convergence_threshold
        self.use_bp_reranker = use_bp_reranker
        self.subgraph_hop = subgraph_hop
        
        # 初始化组件
        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
        self._init_embedding_model()
        
        # 模式图（延迟到 get_matches 中根据匹配表对按需构建）
        self.source_graph_builder = None
        self.target_graph_builder = None
        self.source_graph = None
        self.target_graph = None
        
        # 内部状态
        self._initial_map: Dict[NodePair, float] = {}
        self._column_embeddings_cache: Dict[str, torch.Tensor] = {}
    
    def clear_match_cache(self):
        """
        清理上一次匹配的缓存数据，准备处理新的表对。
        
        保留：
        - 完整 schema（可复用）
        - embedding model（可复用）
        
        清除：
        - 初始映射
        - 列嵌入缓存
        - 子图 schema graph（下次匹配会根据新表对重建）
        """
        self._initial_map = {}
        self._column_embeddings_cache = {}
        self.source_graph_builder = None
        self.target_graph_builder = None
        self.source_graph = None
        self.target_graph = None
        # 不打印日志，避免并行时输出混乱
    
    def _init_embedding_model(self):
        """初始化语义相似度的嵌入模型。"""
        model_path = DEFAULT_MODELS.get(self.embedding_model_name, self.embedding_model_name)
        
        # 检查是否为本地路径
        if os.path.exists(self.embedding_model_name):
            base_key = "mpnet"  # 默认基础模型
            base_model_path = DEFAULT_MODELS[base_key]
            self.model = SentenceTransformer(base_model_path, device=str(self.device))
            state_dict = torch.load(self.embedding_model_name, map_location=self.device)
            self.model.load_state_dict(state_dict)
            print(f"已加载微调模型: {self.embedding_model_name}")
        else:
            self.model = SentenceTransformer(model_path, device=str(self.device))
            print(f"已加载模型 '{model_path}'，设备: {self.device}")
        
        self.model.eval()
        self.tokenizer = AutoTokenizer.from_pretrained(
            DEFAULT_MODELS.get(self.embedding_model_name.split("/")[-1].split("_")[0], 
                              DEFAULT_MODELS["mpnet"])
        )
    
    def _encode_text(self, text: str) -> torch.Tensor:
        """将单个文本字符串编码为嵌入向量。"""
        with torch.no_grad():
            embedding = self.model.encode(
                text,
                convert_to_tensor=True,
                show_progress_bar=False,
                device=self.device
            )
        return embedding
    
    def _encode_column(self, df: pd.DataFrame, column: str, 
                       table_name: Optional[str] = None) -> torch.Tensor:
        """
        使用 Magneto 的编码策略对列进行编码。
        
        Args:
            df: 包含该列的 DataFrame
            column: 列名
            table_name: 可选的表名（用于上下文）
            
        Returns:
            列嵌入向量
        """
        cache_key = f"{table_name or 'unknown'}.{column}"
        if cache_key in self._column_embeddings_cache:
            return self._column_embeddings_cache[cache_key]
        
        # 获取采样值
        values = df[column].dropna().astype(str).tolist()
        if len(values) > self.sampling_size:
            import random
            values = random.sample(values, self.sampling_size)
        
        # 检测数据类型
        from magneto.utils.utils import detect_column_type
        data_type = detect_column_type(df[column])
        
        # 构建列表示
        if self.encoding_mode == "header_values_default":
            sep = self.tokenizer.sep_token
            cls = self.tokenizer.cls_token
            text = f"{cls}{column}{sep}{data_type}{sep}{sep.join(values[:self.sampling_size])}"
        elif self.encoding_mode == "header_values_prefix":
            text = f"header:{column} datatype:{data_type} values:{', '.join(values[:self.sampling_size])}"
        else:
            text = column
        
        embedding = self._encode_text(text)
        self._column_embeddings_cache[cache_key] = embedding
        return embedding
    
    def _compute_literal_similarity(
        self,
        source_df: pd.DataFrame,
        target_df: pd.DataFrame,
        source_table: str,
        target_table: str
    ) -> Dict[Tuple[str, str], float]:
        """
        计算列字面量之间基于嵌入的相似度。
        
        Args:
            source_df: 源表 DataFrame
            target_df: 目标表 DataFrame
            source_table: 源表名
            target_table: 目标表名
            
        Returns:
            字典，映射 (source_col, target_col) -> 相似度
        """
        similarities = {}
        
        # 编码所有源列
        source_embeddings = {}
        for col in source_df.columns:
            source_embeddings[col] = self._encode_column(source_df, col, source_table)
        
        # 编码所有目标列
        target_embeddings = {}
        for col in target_df.columns:
            target_embeddings[col] = self._encode_column(target_df, col, target_table)
        
        # 计算余弦相似度
        for s_col, s_emb in source_embeddings.items():
            for t_col, t_emb in target_embeddings.items():
                sim = torch.nn.functional.cosine_similarity(
                    s_emb.unsqueeze(0), t_emb.unsqueeze(0)
                ).item()
                similarities[(s_col, t_col)] = max(0.0, sim)  # 裁剪负相似度
        
        return similarities
    
    def calculate_initial_mapping(
        self,
        source_dfs: Dict[str, pd.DataFrame],
        target_dfs: Dict[str, pd.DataFrame]
    ):
        """
        使用 Magneto 基于嵌入的相似度计算初始映射。
        
        初始映射规则:
        - Literal Node 对（列/表名）：使用嵌入相似度
        - Type Node 对（相同类型）：1.0
        - 其他节点对：0.0
        
        Args:
            source_dfs: 源表 DataFrame 字典 {table_name: df}
            target_dfs: 目标表 DataFrame 字典 {table_name: df}
        """
        self._initial_map = {}
        
        # 预计算所有表对的列嵌入相似度
        column_similarities: Dict[str, Dict[Tuple[str, str], float]] = {}
        
        for s_table, s_df in source_dfs.items():
            for t_table, t_df in target_dfs.items():
                key = f"{s_table}:{t_table}"
                column_similarities[key] = self._compute_literal_similarity(
                    s_df, t_df, s_table, t_table
                )
        
        # 遍历所有节点对
        for n1 in self.source_graph.nodes():
            for n2 in self.target_graph.nodes():
                node_pair = NodePair(n1, n2)
                
                # 默认相似度
                sim = 0.0
                
                # 情况 1：两者都是 Literal 节点（名称）
                if n1.node_type == LITERAL and n2.node_type == LITERAL:
                    # 检查是否为列名字面量
                    if n1.long_name and n2.long_name:
                        s_table = n1.long_name[0]
                        t_table = n2.long_name[0]
                        s_col = n1.long_name[2]
                        t_col = n2.long_name[2]
                        
                        # 列名字面量
                        if s_col and t_col:
                            key = f"{s_table}:{t_table}"
                            if key in column_similarities:
                                sim = column_similarities[key].get((s_col, t_col), 0.0)
                        # 表名字面量（使用名称字符串相似度）
                        elif not s_col and not t_col:
                            # 简单表名比较
                            sim = self._compute_name_similarity(n1.name, n2.name)
                    else:
                        # 类型名字面量（如 "VARCHAR", "INTEGER"）
                        sim = 1.0 if n1.name == n2.name else 0.0
                
                # 情况 2：两者都是相同类型的 ColumnType 节点
                elif n1.node_type == COLUMN_TYPE and n2.node_type == COLUMN_TYPE:
                    type1 = n1.get_metadata("data_type")
                    type2 = n2.get_metadata("data_type")
                    if type1 and type2 and type1 == type2:
                        sim = 1.0
                
                # 情况 3：类型标记节点（Table, Column, ColumnType, Constraint）
                elif n1.node_type == "TypeMarker" and n2.node_type == "TypeMarker":
                    sim = 1.0 if n1.name == n2.name else 0.0
                
                self._initial_map[node_pair] = sim
    
    def _compute_name_similarity(self, name1: str, name2: str) -> float:
        """使用模糊匹配计算简单名称相似度（用于辅助表名匹配）。"""
        from fuzzywuzzy import fuzz
        return fuzz.ratio(name1.lower(), name2.lower()) / 100.0
    
    def _build_subgraphs(self, source_tables: Set[str], target_tables: Set[str]):
        """
        根据当前匹配的表对，提取子 schema 并构建模式图。
        
        如果 subgraph_hop > 0，会从完整 schema 中提取仅包含
        中心表及其 N-hop 外键邻居的子 schema，然后构建精简的模式图。
        这样传播图的节点数大幅减少，避免不相关结构的噪声传播。
        
        如果 subgraph_hop == 0 或 None，则使用完整 schema 构建模式图。
        
        Args:
            source_tables: 当前匹配涉及的源表名集合
            target_tables: 当前匹配涉及的目标表名集合
        """
        from algorithms.similarity_flooding.sql_schema_parser import extract_subschema
        
        if self.subgraph_hop and self.subgraph_hop > 0:
            sub_source_schema = extract_subschema(
                self.source_schema, source_tables, self.subgraph_hop
            )
            sub_target_schema = extract_subschema(
                self.target_schema, target_tables, self.subgraph_hop
            )
            print(f"子图提取 (hop={self.subgraph_hop}): "
                  f"源 {len(self.source_schema.tables)} 表 -> {len(sub_source_schema.tables)} 表, "
                  f"目标 {len(self.target_schema.tables)} 表 -> {len(sub_target_schema.tables)} 表")
        else:
            sub_source_schema = self.source_schema
            sub_target_schema = self.target_schema
            print(f"子图提取已禁用，使用完整 schema: "
                  f"源 {len(sub_source_schema.tables)} 表, "
                  f"目标 {len(sub_target_schema.tables)} 表")
        
        self.source_graph_builder = SchemaGraph(sub_source_schema, self.source_schema.name)
        self.target_graph_builder = SchemaGraph(sub_target_schema, self.target_schema.name)
        self.source_graph = self.source_graph_builder.graph
        self.target_graph = self.target_graph_builder.graph
    
    @staticmethod
    def _get_euclidean_residual(prev_map: Dict, next_map: Dict) -> float:
        """计算两个映射之间的欧几里得距离。"""
        keys = set(prev_map) | set(next_map)
        return math.sqrt(
            sum((prev_map.get(k, 0) - next_map.get(k, 0)) ** 2 for k in keys)
        )
    
    def _get_next_map(
        self, 
        prev_map: Dict[NodePair, float], 
        p_graph: nx.DiGraph, 
        formula: str
    ) -> Dict[NodePair, float]:
        """
        计算相似度映射的下一次迭代。
        
        Args:
            prev_map: 上一次迭代的相似度映射
            p_graph: 传播图
            formula: 使用的 SF 公式
            
        Returns:
            下一次迭代的相似度映射
        """
        next_map = {}
        max_val = 0.0
        init_map = self._initial_map
        
        for n in p_graph.nodes():
            if formula == 'formula_a':
                map_sim = init_map.get(n, 0.0)
            elif formula == 'formula_b':
                map_sim = 0.0
            else:
                map_sim = prev_map.get(n, 0.0)
            
            # 通过入边传播
            for e in p_graph.in_edges(n):
                w = p_graph.get_edge_data(e[0], e[1]).get('weight', 0)
                
                if formula in ('formula_a', 'basic'):
                    map_sim += w * prev_map.get(e[0], 0.0)
                elif formula == 'formula_b':
                    map_sim += w * init_map.get(e[0], 0.0)
                else:  # formula_c
                    map_sim += init_map.get(e[0], 0.0) + w * (
                        prev_map.get(e[0], 0.0) + init_map.get(e[0], 0.0)
                    )
            
            if map_sim > max_val:
                max_val = map_sim
            next_map[n] = map_sim
        
        # 归一化
        if max_val > 0:
            inv_max = 1.0 / max_val
            for k in next_map:
                next_map[k] *= inv_max
        
        return next_map
    
    def fixpoint_computation(self) -> Dict[NodePair, float]:
        """
        执行不动点计算以传播相似度。
        
        Returns:
            收敛后的最终相似度映射
        """
        # 构建传播图
        p_graph_builder = PropagationGraph(
            self.source_graph, 
            self.target_graph, 
            self.coeff_policy,
            self.fk_weight_multiplier
        )
        p_graph = p_graph_builder.construct_graph()
        
        print(f"传播图: {p_graph.number_of_nodes()} 个节点, "
              f"{p_graph.number_of_edges()} 条边")
        
        def iterate(prev_map, formula, num_iter):
            for i in range(num_iter):
                next_map = self._get_next_map(prev_map, p_graph, formula)
                residual = self._get_euclidean_residual(prev_map, next_map)
                if residual <= self.convergence_threshold:
                    print(f"在第 {i+1} 次迭代收敛，残差: {residual:.6f}")
                    return next_map
                prev_map = next_map.copy()
            return prev_map
        
        if self.formula == 'basic':
            return iterate(self._initial_map.copy(), self.formula, self.max_iterations)
        
        if self.formula == 'formula_a':
            return iterate(self._initial_map.copy(), self.formula, self.max_iterations)
        
        if self.formula == 'formula_b':
            first = self._get_next_map({}, p_graph, self.formula)
            return iterate(first.copy(), self.formula, self.max_iterations - 1)
        
        if self.formula == 'formula_c':
            start = self._get_next_map(self._initial_map.copy(), p_graph, 'formula_b')
            return iterate(start.copy(), self.formula, self.max_iterations - 1)
        
        raise ValueError(f"未知公式: {self.formula}")
    
    def filter_column_matches(
        self, 
        similarity_map: Dict[NodePair, float]
    ) -> Dict[NodePair, float]:
        """
        过滤相似度映射，仅保留列到列的匹配。
        
        Args:
            similarity_map: 不动点计算的完整相似度映射
            
        Returns:
            仅包含列匹配的过滤后映射
        """
        filtered = {}
        
        for node_pair, sim in similarity_map.items():
            n1 = node_pair.node1
            n2 = node_pair.node2
            
            # 检查两个节点是否都是 Column 类型 NodeID
            # Column NodeID 有指向 Column 类型标记的边
            is_col1 = False
            is_col2 = False
            
            for _, target, data in self.source_graph.out_edges(n1, data=True):
                if data.get('label') == EDGE_TYPE and target.name == COLUMN:
                    is_col1 = True
                    break
            
            for _, target, data in self.target_graph.out_edges(n2, data=True):
                if data.get('label') == EDGE_TYPE and target.name == COLUMN:
                    is_col2 = True
                    break
            
            if is_col1 and is_col2:
                filtered[node_pair] = sim
        
        return filtered
    
    def format_output(
        self, 
        column_matches: Dict[NodePair, float]
    ) -> Dict[Tuple[Tuple[str, str], Tuple[str, str]], float]:
        """
        将匹配结果格式化为 Valentine 输出格式。
        
        Args:
            column_matches: 过滤后的列匹配
            
        Returns:
            Valentine 格式: {((src_table, src_col), (tgt_table, tgt_col)): score}
        """
        output = {}
        
        for node_pair, sim in column_matches.items():
            n1 = node_pair.node1
            n2 = node_pair.node2
            
            if n1.long_name and n2.long_name:
                s_table, _, s_col, _ = n1.long_name
                t_table, _, t_col, _ = n2.long_name
                
                if s_table and s_col and t_table and t_col:
                    key = ((s_table, s_col), (t_table, t_col))
                    output[key] = float(sim)
        
        return output
    
    def get_matches(
        self,
        source_dfs: Dict[str, pd.DataFrame],
        target_dfs: Dict[str, pd.DataFrame],
    ) -> Dict[Tuple[Tuple[str, str], Tuple[str, str]], float]:
        """
        主入口：使用 Similarity Flooding 获取列匹配。
        
        Args:
            source_dfs: 源表 DataFrame {table_name: df}
            target_dfs: 目标表 DataFrame {table_name: df}
            
        Returns:
            Valentine 格式的匹配结果
        """
        print("\n" + "="*60)
        print("Similarity Flooding 匹配器")
        print("="*60)
        
        # 步骤 0：根据当前表对提取子 schema 并构建模式图
        source_tables = set(source_dfs.keys())
        target_tables = set(target_dfs.keys())
        print(f"\n步骤 0: 构建子图 (源表: {source_tables}, 目标表: {target_tables})...")
        self._build_subgraphs(source_tables, target_tables)
        
        # 打印模式图统计
        print("\n源模式图:")
        self.source_graph_builder.print_stats()
        print("\n目标模式图:")
        self.target_graph_builder.print_stats()
        
        # 步骤 1：使用 Magneto 嵌入计算初始映射
        print("\n步骤 1: 使用 Magneto 嵌入计算初始映射...")
        self.calculate_initial_mapping(source_dfs, target_dfs)
        print(f"初始映射: {len(self._initial_map)} 个节点对")
        
        # 步骤 2：执行不动点计算
        print("\n步骤 2: 运行不动点计算...")
        final_map = self.fixpoint_computation()
        print(f"最终映射: {len(final_map)} 个节点对")
        
        # 步骤 3：仅过滤列匹配
        print("\n步骤 3: 过滤列匹配...")
        column_matches = self.filter_column_matches(final_map)
        print(f"列匹配数: {len(column_matches)}")
        
        # 步骤 4：格式化输出
        print("\n步骤 4: 格式化输出...")
        matches = self.format_output(column_matches)
        
        # 按分数排序
        matches = dict(sorted(matches.items(), key=lambda x: -x[1]))
        print(f"格式化后匹配数: {len(matches)}")
        
        print("\n" + "="*60)
        print("Similarity Flooding 完成！")
        print("="*60 + "\n")
        
        return matches
    
    def get_matches_with_hungarian(
        self,
        source_dfs: Dict[str, pd.DataFrame],
        target_dfs: Dict[str, pd.DataFrame],
        source_table_name: str = "source",
        target_table_name: str = "target"
    ):
        """
        使用匈牙利算法获取一对一匹配结果。
        
        该方法在 SF 传播后应用 Magneto 的二分图重排器。
        
        Args:
            source_dfs: 源表 DataFrame
            target_dfs: 目标表 DataFrame
            source_table_name: 输出中的源表名
            target_table_name: 输出中的目标表名
            
        Returns:
            优先一对一匹配的 Valentine MatcherResults
        """
        # 获取 SF 匹配结果
        matches = self.get_matches(source_dfs, target_dfs)
        
        if not matches:
            from valentine import MatcherResults
            return MatcherResults({})
        
        if self.use_bp_reranker and HAS_MAGNETO:
            # 获取二分匹配所需的 DataFrame
            # 合并所有源/目标列
            source_cols = set()
            target_cols = set()
            for ((s_tbl, s_col), (t_tbl, t_col)), _ in matches.items():
                source_cols.add(s_col)
                target_cols.add(t_col)
            
            # 为重排器创建虚拟 DataFrame
            source_df = pd.DataFrame(columns=list(source_cols))
            target_df = pd.DataFrame(columns=list(target_cols))
            
            # 应用匈牙利算法
            final_matches = arrange_bipartite_matches(
                matches,
                source_df,
                source_table_name,
                target_df,
                target_table_name
            )
            return final_matches
        
        from valentine import MatcherResults
        return MatcherResults(matches)


# 单表对匹配的便捷函数
def match_tables(
    source_schema: SchemaInfo,
    target_schema: SchemaInfo,
    source_df: pd.DataFrame,
    target_df: pd.DataFrame,
    source_table: str,
    target_table: str,
    **kwargs
) -> Dict[Tuple[Tuple[str, str], Tuple[str, str]], float]:
    """
    匹配单对表的便捷函数。
    
    Args:
        source_schema: 源 schema 信息
        target_schema: 目标 schema 信息
        source_df: 源表 DataFrame
        target_df: 目标表 DataFrame
        source_table: 源表名
        target_table: 目标表名
        **kwargs: SimilarityFloodingMatcher 的其他参数
        
    Returns:
        Valentine 格式的匹配结果
    """
    matcher = SimilarityFloodingMatcher(
        source_schema, target_schema, **kwargs
    )
    
    return matcher.get_matches(
        {source_table: source_df},
        {target_table: target_df}
    )


# ============================================================================
# ConstraintBoostMatcher - 单向约束增强匹配器
# ============================================================================

class ConstraintBoostMatcher:
    """
    单向约束增强匹配器。
    
    替代 SF 的双向洪泛迭代，采用更高效的单向 boost 策略：
    1. 预计算所有列的嵌入向量（模型只加载一次）
    2. 对每个表对计算余弦相似度矩阵
    3. 利用 FK/PK 关系单向 boost 相关列的分数（只加不减）
    4. 匈牙利算法提取一对一匹配
    
    核心思想：如果 FK/PK 列对的初始匹配分数超过阈值，
    说明两张表很可能对应，则 boost 同表对其他列的分数。
    永远不减分，保证下限 >= 纯 Magneto 基线。
    """

    def __init__(
        self,
        source_schemas: Dict[str, SchemaInfo],
        target_schema: SchemaInfo,
        model: SentenceTransformer = None,
        tokenizer=None,
        embedding_model: str = "mpnet",
        encoding_mode: str = "header_values_default",
        sampling_size: int = 10,
        boost_threshold: float = 0.9,
        boost_alpha: float = 0.15,
        subgraph_hop: int = 1,
        pg_host: str = "localhost",
        pg_port: int = 5433,
        pg_dbname: str = "magneto_vectors",
        pg_user: str = "magneto",
        pg_password: str = "magneto123",
        dataset_name: str = "magneto_faa",
    ):
        """
        Args:
            source_schemas: 源数据库 schema 字典 {db_name: SchemaInfo}
            target_schema: 目标数据库 SchemaInfo
            model: 预加载的 SentenceTransformer（外部传入，避免重复加载）
            tokenizer: 预加载的分词器
            embedding_model: 嵌入模型名称（仅在 model=None 时用于自动加载）
            encoding_mode: 列编码模式
            sampling_size: 值采样数量
            boost_threshold: FK/PK 锚点触发 boost 的最低相似度阈值
            boost_alpha: boost 增量系数（new = old + alpha * anchor_score）
            subgraph_hop: 子图提取跳数（用于获取 FK 关系）
            pg_host: pgvector 主机地址
            pg_port: pgvector 端口
            pg_dbname: pgvector 数据库名
            pg_user: pgvector 用户名
            pg_password: pgvector 密码
            dataset_name: 数据集标识（用于 pgvector 中区分不同数据集的嵌入）
        """
        self.source_schemas = source_schemas
        self.target_schema = target_schema
        self.embedding_model_name = embedding_model
        self.encoding_mode = encoding_mode
        self.sampling_size = sampling_size
        self.boost_threshold = boost_threshold
        self.boost_alpha = boost_alpha
        self.subgraph_hop = subgraph_hop
        self.dataset_name = dataset_name

        self.device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

        # 使用外部传入的模型或自动加载
        if model is not None:
            self.model = model
        else:
            model_path = DEFAULT_MODELS.get(embedding_model, embedding_model)
            self.model = SentenceTransformer(model_path, device=str(self.device))
        self.model.eval()

        if tokenizer is not None:
            self.tokenizer = tokenizer
        else:
            self.tokenizer = AutoTokenizer.from_pretrained(
                DEFAULT_MODELS.get(embedding_model, DEFAULT_MODELS["mpnet"])
            )

        # pgvector 向量存储
        self._vector_store = None
        self._pg_config = {
            "host": pg_host, "port": pg_port, "dbname": pg_dbname,
            "user": pg_user, "password": pg_password,
        }

        # 嵌入缓存：{table_name.column_name: tensor}
        self._embedding_cache: Dict[str, torch.Tensor] = {}

    # ------------------------------------------------------------------
    # 预计算嵌入
    # ------------------------------------------------------------------

    def _get_vector_store(self):
        """延迟初始化 pgvector 连接。"""
        if self._vector_store is None:
            from algorithms.similarity_flooding.vector_store import VectorStore
            self._vector_store = VectorStore(**self._pg_config)
        return self._vector_store

    def precompute_embeddings(
        self,
        all_dfs: Dict[str, pd.DataFrame],
        verbose: bool = True,
    ):
        """
        一次性编码所有表的所有列，使用 pgvector 持久化存储。

        如果 pgvector 中已有该 dataset/model/mode 的嵌入，直接加载到内存；
        否则批量编码后写入 pgvector 并填充内存缓存。

        Args:
            all_dfs: {table_name: DataFrame} 包含所有源表和目标表
            verbose: 是否打印进度
        """
        store = self._get_vector_store()
        model_key = self.embedding_model_name

        # 尝试从 pgvector 加载
        if store.has_embeddings(self.dataset_name, model_key, self.encoding_mode):
            if verbose:
                count = store.get_embedding_count(
                    self.dataset_name, model_key, self.encoding_mode
                )
                print(f"\n从 pgvector 加载嵌入: {count} 条 "
                      f"(dataset={self.dataset_name}, model={model_key})")
            self._embedding_cache = store.load_embeddings(
                self.dataset_name, model_key, self.encoding_mode, self.device
            )
            if verbose:
                print(f"已加载 {len(self._embedding_cache)} 个列嵌入到内存\n")
            return

        total_cols = sum(len(df.columns) for df in all_dfs.values())

        if verbose:
            print(f"\n预计算嵌入: {len(all_dfs)} 张表, {total_cols} 列...")

        texts = []
        keys = []

        from magneto.column_encoder import ColumnEncoder
        encoder = ColumnEncoder(
            self.tokenizer,
            encoding_mode=self.encoding_mode,
            sampling_mode="mixed",
            n_samples=self.sampling_size,
        )

        for table_name, df in all_dfs.items():
            for col in df.columns:
                cache_key = f"{table_name}.{col}"
                if cache_key in self._embedding_cache:
                    continue

                text = encoder.encode(df, col, table_name=table_name)
                texts.append(text)
                keys.append(cache_key)

        if texts:
            with torch.no_grad():
                embeddings = self.model.encode(
                    texts,
                    convert_to_tensor=True,
                    show_progress_bar=verbose,
                    device=self.device,
                    batch_size=64,
                )
            for i, key in enumerate(keys):
                self._embedding_cache[key] = embeddings[i]

        if verbose:
            print(f"预计算完成: 缓存 {len(self._embedding_cache)} 个列嵌入")

        # 写入 pgvector
        store.store_embeddings(
            self.dataset_name, model_key, self.encoding_mode,
            self._embedding_cache,
        )
        if verbose:
            print()

    # ------------------------------------------------------------------
    # 核心匹配逻辑
    # ------------------------------------------------------------------

    def match_table_pair(
        self,
        src_table: str,
        tgt_table: str,
        src_df: pd.DataFrame,
        tgt_df: pd.DataFrame,
        src_schema_name: str = None,
    ) -> Dict[Tuple[Tuple[str, str], Tuple[str, str]], float]:
        """
        匹配单个表对：嵌入相似度 + FK 约束 boost。
        
        Args:
            src_table: 源表名（不含数据库前缀）
            tgt_table: 目标表名
            src_df: 源表 DataFrame
            tgt_df: 目标表 DataFrame
            src_schema_name: 源 schema 名称（如 "plm"）用于查找 FK
            
        Returns:
            Valentine 格式匹配结果
        """
        src_cols = list(src_df.columns)
        tgt_cols = list(tgt_df.columns)

        if not src_cols or not tgt_cols:
            return {}

        # 1. 构建相似度矩阵（从缓存查嵌入）
        sim_matrix = {}
        for sc in src_cols:
            s_key = f"{src_table}.{sc}"
            s_emb = self._embedding_cache.get(s_key)
            if s_emb is None:
                continue
            for tc in tgt_cols:
                t_key = f"{tgt_table}.{tc}"
                t_emb = self._embedding_cache.get(t_key)
                if t_emb is None:
                    continue
                sim = torch.nn.functional.cosine_similarity(
                    s_emb.unsqueeze(0), t_emb.unsqueeze(0)
                ).item()
                sim_matrix[(sc, tc)] = max(0.0, sim)

        # 2. 应用 FK/PK 约束 boost
        sim_matrix = self._apply_constraint_boost(
            sim_matrix, src_table, tgt_table,
            src_cols, tgt_cols, src_schema_name
        )

        # 3. 转换为 Valentine 格式
        matches = {}
        for (sc, tc), score in sim_matrix.items():
            key = ((src_table, sc), (tgt_table, tc))
            matches[key] = score

        # 4. 匈牙利算法一对一匹配
        if HAS_MAGNETO and matches:
            source_df_dummy = pd.DataFrame(columns=src_cols)
            target_df_dummy = pd.DataFrame(columns=tgt_cols)
            matches = arrange_bipartite_matches(
                matches, source_df_dummy, src_table,
                target_df_dummy, tgt_table
            )
            if hasattr(matches, 'items'):
                matches = dict(matches)

        return matches

    def _apply_constraint_boost(
        self,
        sim_matrix: Dict[Tuple[str, str], float],
        src_table: str,
        tgt_table: str,
        src_cols: List[str],
        tgt_cols: List[str],
        src_schema_name: str = None,
    ) -> Dict[Tuple[str, str], float]:
        """
        单向约束 boost：利用 FK/PK 锚点提升同表对其他列的分数。
        
        算法：
        1. 收集当前表对涉及的 FK/PK 列
        2. 在相似度矩阵中找到高分锚点（FK/PK列的最佳匹配 > threshold）
        3. 对同表对的所有其他列对施加 boost
        4. 永远不减分
        
        Args:
            sim_matrix: 原始相似度矩阵 {(src_col, tgt_col): score}
            src_table: 源表名
            tgt_table: 目标表名
            src_cols: 源列名列表
            tgt_cols: 目标列名列表
            src_schema_name: 源 schema 名称
            
        Returns:
            boost 后的相似度矩阵
        """
        from algorithms.similarity_flooding.sql_schema_parser import extract_subschema

        # 收集锚点列（PK 和 FK 列）
        anchor_cols_src = set()
        anchor_cols_tgt = set()

        # 从源 schema 提取 PK/FK 列
        if src_schema_name and src_schema_name in self.source_schemas:
            src_schema = self.source_schemas[src_schema_name]
            sub_src = extract_subschema(src_schema, {src_table}, self.subgraph_hop)
            if src_table in sub_src.tables:
                table_info = sub_src.tables[src_table]
                if table_info.primary_key:
                    anchor_cols_src.add(table_info.primary_key)
                for fk in table_info.foreign_keys:
                    if fk.source_table == src_table:
                        anchor_cols_src.add(fk.source_column)
            # 也收集其他表引用到本表的 FK 列
            for t_name, t_info in sub_src.tables.items():
                for fk in t_info.foreign_keys:
                    if fk.target_table == src_table:
                        anchor_cols_src.add(fk.target_column)

        # 从目标 schema 提取 PK/FK 列
        sub_tgt = extract_subschema(self.target_schema, {tgt_table}, self.subgraph_hop)
        if tgt_table in sub_tgt.tables:
            table_info = sub_tgt.tables[tgt_table]
            if table_info.primary_key:
                anchor_cols_tgt.add(table_info.primary_key)
            for fk in table_info.foreign_keys:
                if fk.source_table == tgt_table:
                    anchor_cols_tgt.add(fk.source_column)
        for t_name, t_info in sub_tgt.tables.items():
            for fk in t_info.foreign_keys:
                if fk.target_table == tgt_table:
                    anchor_cols_tgt.add(fk.target_column)

        # 在双侧 PK/FK 列的笛卡尔积内寻找高分锚点对
        anchor_scores = []
        for sc in anchor_cols_src:
            if sc not in src_cols:
                continue
            for tc in anchor_cols_tgt:
                if tc not in tgt_cols:
                    continue
                score = sim_matrix.get((sc, tc), 0.0)
                if score >= self.boost_threshold:
                    anchor_scores.append(score)

        if not anchor_scores:
            return sim_matrix

        # 计算 boost 增量（取所有锚点的平均分数）
        avg_anchor = sum(anchor_scores) / len(anchor_scores)
        boost_delta = self.boost_alpha * avg_anchor

        # 施加 boost：只加不减
        boosted = {}
        for (sc, tc), score in sim_matrix.items():
            new_score = min(1.0, score + boost_delta)
            boosted[(sc, tc)] = new_score  # 始终 >= 原分数

        return boosted
