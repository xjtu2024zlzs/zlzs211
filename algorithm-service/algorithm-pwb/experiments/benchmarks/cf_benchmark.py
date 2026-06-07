"""
FAA 航空数据集 Similarity Flooding 混合匹配基准测试

本基准测试结合了：
1. Magneto 基于嵌入的列匹配
2. Valentine 的 Similarity Flooding 算法进行约束传播
3. 匈牙利算法进行一对一匹配

SF 算法通过模式图边传播相似度，利用外键关系改进多表匹配。
"""

import os
import sys
import re

# 强制离线模式，避免每次初始化 SentenceTransformer 时联网检查 HuggingFace Hub
# 模型首次下载后会缓存在本地，后续无需联网
os.environ["HF_HUB_OFFLINE"] = "1"
os.environ["TRANSFORMERS_OFFLINE"] = "1"

import pandas as pd
import time
import datetime
import pprint
import argparse
from typing import Dict, List, Optional, Tuple
from tqdm import tqdm
from multiprocessing import Pool
from functools import partial

pp = pprint.PrettyPrinter(indent=4, sort_dicts=True)

project_path = os.path.dirname(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
)
sys.path.append(os.path.join(project_path))
sys.path.append(os.path.join(project_path, "algorithms", "magneto"))

BUILTIN_EMBEDDING_MODELS = {"mpnet", "roberta", "e5", "arctic", "minilm"}


def is_finetuned_embedding_model(embedding_model: str) -> bool:
    if not embedding_model or embedding_model in BUILTIN_EMBEDDING_MODELS:
        return False
    return os.path.exists(os.path.join(project_path, "models", embedding_model))


def get_global_unknown_method_names(
    skip_gpt: bool,
    has_neo4j: bool,
    is_finetuned: bool,
) -> List[str]:
    prefix = "MagnetoFT" if is_finetuned else "Magneto"
    methods = [prefix, f"{prefix}Boost"]
    if not skip_gpt:
        methods.append(f"{prefix}GPT")
        if has_neo4j:
            methods.append(f"{prefix}GPT_neo4j2")
    return methods

from experiments.benchmarks.benchmark_utils import (
    compute_mean_ranking_reciprocal_adjusted,
    create_result_file,
    record_result,
    calculate_recall_at_k,
)
from algorithms.magneto.magneto import Magneto
from algorithms.similarity_flooding.sql_schema_parser import (
    SQLSchemaParser, SchemaInfo, parse_schema
)
from algorithms.similarity_flooding.sf_matcher import (
    SimilarityFloodingMatcher, ConstraintBoostMatcher
)
from algorithms.similarity_flooding.schema_graph import SchemaGraph

# Valentine 指标计算
try:
    from valentine import MatcherResults
except ImportError:
    # 如果 valentine 未安装，使用备选实现
    class MatcherResults(dict):
        def get_metrics(self, ground_truth):
            return {
                "Precision": 0, "F1Score": 0, "Recall": 0,
                "PrecisionTop10Percent": 0, "RecallAtSizeofGroundTruth": 0
            }
        def one_to_one(self):
            return MatcherResults({})


def load_sql_schemas(schema_dir: str) -> Dict[str, SchemaInfo]:
    """
    从 init-scripts 目录加载所有 SQL schema 文件

    Args:
        schema_dir: SQL schema 文件所在目录路径

    Returns:
        字典，映射 schema 名称到 SchemaInfo
    """
    schemas = {}

    # 文件前缀到 schema 名称的映射。CF 数据集使用 01_cf_* 命名；
    # 保留旧前缀仅作为本脚本内部兜底，不影响 faa_benchmark_sf.py。
    schema_mapping = {
        "01_cf_plm": "plm",
        "02_cf_erp": "erp",
        "03_cf_mes": "mes",
        "04_cf_qms": "qms",
        "05_cf_mro": "mro",
        "06_cf_dossier": "dossier",
        "01_plm": "plm",
        "02_erp": "erp",
        "03_mes": "mes",
        "04_qms": "qms",
        "04_mro": "mro",
        "05_dossier": "dossier"
    }

    for filename in os.listdir(schema_dir):
        if filename.endswith("_schema.sql"):
            for prefix, name in schema_mapping.items():
                if filename.startswith(prefix):
                    filepath = os.path.join(schema_dir, filename)
                    print(f"加载 schema: {filepath}")
                    with open(filepath, 'r', encoding='utf-8') as f:
                        sql_content = f.read()
                    sql_content = re.sub(r'"([A-Za-z_][A-Za-z0-9_]*)"', r'\1', sql_content)
                    schema = SQLSchemaParser(schema_name=name).parse_sql(sql_content)
                    schemas[name] = schema

                    # 打印 schema 统计信息
                    print(f"  - 表数量: {len(schema.tables)}")
                    total_cols = sum(len(t.columns) for t in schema.tables.values())
                    total_fks = sum(len(t.foreign_keys) for t in schema.tables.values())
                    print(f"  - 列数量: {total_cols}")
                    print(f"  - 外键数量: {total_fks}")
                    break

    return schemas


def get_cf_schema_dir(root: str) -> str:
    """返回 CF 数据集专用 SQL schema 目录。"""
    schema_dir = os.path.join(project_path, root, "cf", "init-scripts")
    if not os.path.isdir(schema_dir):
        raise FileNotFoundError(
            f"CF SQL schema 目录不存在: {schema_dir}。"
            "请确认 data/magneto_cf/cf/init-scripts 已准备完成。"
        )
    return schema_dir


def get_source_schema_for_table(table_name: str, schemas: Dict[str, SchemaInfo]) -> Tuple[str, SchemaInfo]:
    """
    获取包含指定表的源 schema

    Args:
        table_name: 带前缀的表名（如 "plm_aircraft_item_master"）
        schemas: 已加载的 schema 字典

    Returns:
        元组 (schema_name, SchemaInfo)
    """
    # 从表名提取前缀
    prefix = table_name.split("_")[0].lower()

    if prefix in schemas:
        return prefix, schemas[prefix]

    # 备选：搜索所有 schema
    for name, schema in schemas.items():
        pure_table = table_name.replace(f"{prefix}_", "")
        if pure_table in schema.tables:
            return name, schema

    return None, None


def get_matcher(
    method: str,
    embedding_model: str = None,
    mode: str = "header_values_default",
    llm_model: str = None,
    source_schema: SchemaInfo = None,
    target_schema: SchemaInfo = None,
):
    """
    根据方法名返回对应的匹配器实例

    Args:
        method: 匹配器名称 (Magneto, MagnetoSF, MagnetoFT, MagnetoFTSF, MagnetoGPT, MagnetoFTGPT)
        embedding_model: 嵌入模型名称或路径
        mode: 列编码模式
        llm_model: LLM 模型名称
        source_schema: 源 schema（SF 匹配器需要）
        target_schema: 目标 schema（SF 匹配器需要）
    """
    if method == "Magneto":
        return Magneto(encoding_mode=mode, embedding_model=embedding_model)

    elif method == "MagnetoFT":
        model_path = os.path.join(project_path, "models", embedding_model)
        return Magneto(encoding_mode=mode, embedding_model=model_path)

    elif method == "MagnetoSF":
        # Similarity Flooding 匹配器
        if source_schema is None or target_schema is None:
            raise ValueError("MagnetoSF 需要 source_schema 和 target_schema")

        return SimilarityFloodingMatcher(
            source_schema=source_schema,
            target_schema=target_schema,
            embedding_model=embedding_model or "mpnet",
            encoding_mode=mode,
            use_bp_reranker=True,
            subgraph_hop=1,
        )

    elif method == "MagnetoFTSF":
        # 微调版 Similarity Flooding 匹配器
        if source_schema is None or target_schema is None:
            raise ValueError("MagnetoFTSF 需要 source_schema 和 target_schema")

        model_path = os.path.join(project_path, "models", embedding_model)
        return SimilarityFloodingMatcher(
            source_schema=source_schema,
            target_schema=target_schema,
            embedding_model=model_path,
            encoding_mode=mode,
            use_bp_reranker=True,
            subgraph_hop=1,
        )

    elif method == "MagnetoGPT":
        return Magneto(
            encoding_mode=mode,
            embedding_model=embedding_model,
            llm_model=llm_model,
            use_bp_reranker=False,
            use_gpt_reranker=True,
        )

    elif method == "MagnetoFTGPT":
        model_path = os.path.join(project_path, "models", embedding_model)
        return Magneto(
            encoding_mode=mode,
            embedding_model=model_path,
            llm_model=llm_model,
            use_bp_reranker=False,
            use_gpt_reranker=True,
        )

    elif method == "MagnetoGPT_neo4j":
        if source_schema is None or target_schema is None:
            raise ValueError("MagnetoGPT_neo4j 需要 source_schema 和 target_schema")
        return Magneto(
            encoding_mode=mode,
            embedding_model=embedding_model,
            llm_model=llm_model,
            use_bp_reranker=False,
            use_gpt_reranker=True,
            source_schema=source_schema,
            target_schema=target_schema,
        )

    else:
        raise ValueError(f"未知方法: {method}")


def load_faa_mappings(all_mappings_file: str) -> Dict[str, str]:
    """
    加载完整的 FAA 映射信息，构建源表到目标表的映射关系

    Returns:
        dict: {source_table: target_table} 映射字典
    """
    df = pd.read_csv(all_mappings_file)

    # 构建源表到目标表的映射（添加 dossier_ 前缀）
    source_to_target = {}
    for _, row in df.iterrows():
        source_table = row['source_table']
        target_table = row['target_table']

        # 统一添加前缀和后缀
        source_key = f"{row['source_database'].lower()}_{source_table}"
        target_key = f"dossier_{target_table}"

        if source_key not in source_to_target:
            source_to_target[source_key] = set()
        source_to_target[source_key].add(target_key)

    # 转换为主目标表（每个源表取第一个目标表）
    source_to_main_target = {}
    for src, targets in source_to_target.items():
        source_to_main_target[src] = list(targets)[0]

    return source_to_main_target


def process_table_pair_worker(
    args_tuple,
    matcher_name,
    embedding_model,
    MODE,
    llm_model,
    schema_dir,
    studies_path,
    target_path,
    gt_dict
):
    """
    Worker函数：在子进程中处理单个表对

    Args:
        args_tuple: (source_file, target_file, source_schema_name)
        其他参数: 匹配所需的配置

    Returns:
        dict: 包含匹配结果和元信息
    """
    source_file, target_file, source_schema_name = args_tuple

    try:
        # 1. 解析表名
        source_table_base = source_file.replace(".csv", "")
        target_table_name = target_file.replace(".csv", "")
        source_database = source_table_base.split("_")[0].upper()
        source_table_pure = source_table_base.replace(f"{source_database.lower()}_", "")
        target_table_pure = target_table_name.replace("dossier_", "")

        # 2. 加载数据
        df_source = pd.read_csv(os.path.join(studies_path, source_file))
        df_target = pd.read_csv(os.path.join(target_path, target_file))

        # 3. 检查Ground Truth
        table_pair_key = (source_table_base, target_table_name)
        has_gt = table_pair_key in gt_dict
        ground_truth = gt_dict.get(table_pair_key, [])

        # 4. 创建Matcher（每个进程独立加载schema和模型）
        from algorithms.similarity_flooding.sql_schema_parser import parse_schema

        # 根据schema名称找到对应的SQL文件
        schema_file_map = {
            "plm": "01_plm_schema.sql",
            "erp": "02_erp_schema.sql",
            "mes": "03_mes_schema.sql",
            "mro": "04_mro_schema.sql"
        }

        source_schema_file = schema_file_map.get(source_schema_name)
        if not source_schema_file:
            return {
                'source_table': source_table_base,
                'target_table': target_table_name,
                'has_gt': False,
                'ground_truth': [],
                'matches': {},
                'runtime': 0,
                'error': f"未知的源schema: {source_schema_name}"
            }

        source_schema = parse_schema(
            os.path.join(schema_dir, source_schema_file),
            source_schema_name
        )
        target_schema = parse_schema(
            os.path.join(schema_dir, "05_dossier_schema.sql"),
            "dossier"
        )

        matcher = get_matcher(
            matcher_name,
            embedding_model,
            MODE,
            llm_model,
            source_schema=source_schema,
            target_schema=target_schema,
        )

        # 5. 执行匹配
        start_time = time.time()
        source_dfs = {source_table_pure: df_source}
        target_dfs = {target_table_pure: df_target}

        matches_result = matcher.get_matches_with_hungarian(
            source_dfs,
            target_dfs,
            source_table_name=source_table_pure,
            target_table_name=target_table_pure
        )

        runtime = time.time() - start_time

        # 6. 转换结果格式
        if hasattr(matches_result, 'items'):
            matches = dict(matches_result)
        else:
            matches = matches_result

        return {
            'source_table': source_table_base,
            'target_table': target_table_name,
            'has_gt': has_gt,
            'ground_truth': ground_truth,
            'matches': matches,
            'runtime': runtime,
            'error': None
        }

    except Exception as e:
        import traceback
        return {
            'source_table': source_table_base if 'source_table_base' in locals() else source_file,
            'target_table': target_table_name if 'target_table_name' in locals() else target_file,
            'has_gt': False,
            'ground_truth': [],
            'matches': {},
            'runtime': 0,
            'error': f"{str(e)}\n{traceback.format_exc()}"
        }


def run_benchmark_sf_known_table(
    BENCHMARK: str = "faa_aviation_sf",
    DATASET: str = "faa_aviation",
    ROOT: str = "data/magneto_faa",
    MODE: str = "header_values_default",
    embedding_model: str = "mpnet",
    llm_model: str = "none",
    use_sf: bool = True,
    gpt_only: bool = False,
    pg_host: str = "localhost",
    pg_port: int = 5433,
):
    """
    Known Table 模式：已知表映射关系，只评估列匹配
    输出每个表对的匹配结果（多行）

    Args:
        BENCHMARK: 基准名称
        DATASET: 数据集名称
        ROOT: 数据集根目录
        MODE: 列编码模式
        embedding_model: 嵌入模型名称
        llm_model: LLM 模型名称
        use_sf: 是否使用 Similarity Flooding
        gpt_only: 是否仅运行 GPT 模式
    """

    HEADER = [
        "benchmark",
        "dataset",
        "source_database",
        "source_table",
        "target_table",
        "ncols_src",
        "ncols_tgt",
        "nrows_src",
        "nrows_tgt",
        "nmatches",
        "method",
        "runtime",
        "mrr",
        "Recall@20",
        "All_Precision",
        "All_F1Score",
        "All_Recall",
        "All_PrecisionTop10Percent",
        "All_RecallAtSizeofGroundTruth",
        "One2One_Precision",
        "One2One_F1Score",
        "One2One_Recall",
        "One2One_PrecisionTop10Percent",
        "One2One_RecallAtSizeofGroundTruth",
    ]

    results_dir = os.path.join(project_path, "results", "benchmarks", BENCHMARK)
    safe_llm_model = llm_model.replace("/", "_").replace(":", "_")
    safe_embedding_model = embedding_model.replace("/", "_").replace(":", "_").replace("\\", "_")

    sf_suffix = "-sf" if use_sf else ""
    experiment_name = f"{DATASET}-{safe_embedding_model}-{MODE}-{safe_llm_model}-known_table{sf_suffix}"

    results_dir = os.path.join(results_dir, experiment_name)

    if not os.path.exists(results_dir):
        os.makedirs(results_dir)

    result_file = os.path.join(results_dir, f"{experiment_name}.csv")
    print(f"结果文件: {result_file}")

    create_result_file(results_dir, result_file, HEADER)

    # 加载 CF SQL schema
    schema_dir = get_cf_schema_dir(ROOT)
    print(f"\n{'='*60}")
    print("加载 SQL Schema 用于 Similarity Flooding...")
    print(f"{'='*60}\n")

    schemas = load_sql_schemas(schema_dir)
    target_schema = schemas.get("dossier")

    if target_schema is None:
        print("错误: 无法加载 dossier schema!")
        return

    print(f"\n成功加载 {len(schemas)} 个 schema。")

    # 加载映射
    all_mappings_file = os.path.join(ROOT, "ground-truth", "_all_mappings.csv")
    source_to_target = load_faa_mappings(all_mappings_file)

    print(f"\n发现 {len(source_to_target)} 个源表到目标表的映射关系\n")

    studies_path = os.path.join(ROOT, "source-tables")
    target_path = os.path.join(ROOT, "target-tables")
    gt_path = os.path.join(ROOT, "ground-truth")

    # 确定要运行的匹配器
    skip_gpt = llm_model is None or llm_model.lower() == "none"

    if gpt_only:
        matchers = ["MagnetoGPT"]
    elif use_sf:
        if embedding_model in ["mpnet", "roberta", "e5", "arctic", "minilm"]:
            base = ["Magneto", "MagnetoBoost"]
        else:
            base = ["MagnetoFT", "MagnetoBoost"]
        matchers = base if skip_gpt else base + ["MagnetoGPT", "MagnetoGPT_neo4j"]
    else:
        if embedding_model in ["mpnet", "roberta", "e5", "arctic", "minilm"]:
            matchers = ["Magneto"] if skip_gpt else ["Magneto", "MagnetoGPT", "MagnetoGPT_neo4j"]
        else:
            matchers = ["MagnetoFT"] if skip_gpt else ["MagnetoFT", "MagnetoFTGPT"]

    # 处理每个 ground truth 文件
    for gt_file in sorted(os.listdir(gt_path)):
        if not gt_file.endswith(".csv") or gt_file.startswith("_"):
            continue

        print(f"\n{'='*80}")
        print(f"处理 {gt_file}")
        print(f"{'='*80}\n")

        source_table_base = gt_file.replace(".csv", "")

        if source_table_base not in source_to_target:
            print(f"警告: 无法找到 {source_table_base} 的目标表映射，跳过")
            continue

        target_table_name = source_to_target[source_table_base]

        source_file = os.path.join(studies_path, f"{source_table_base}.csv")
        target_file = os.path.join(target_path, f"{target_table_name}.csv")

        if not os.path.exists(source_file):
            print(f"警告: 源文件不存在 {source_file}，跳过")
            continue

        if not os.path.exists(target_file):
            print(f"警告: 目标文件不存在 {target_file}，跳过")
            continue

        df_source = pd.read_csv(source_file)
        df_target = pd.read_csv(target_file)

        gt_df = pd.read_csv(os.path.join(gt_path, gt_file))
        gt_df.dropna(inplace=True)

        ground_truth = list(gt_df.itertuples(index=False, name=None))

        if len(ground_truth) == 0:
            print(f"警告: {gt_file} 没有有效的 ground truth，跳过")
            continue

        source_database = source_table_base.split("_")[0].upper()
        source_table_pure = source_table_base.replace(f"{source_database.lower()}_", "")
        target_table_pure = target_table_name.replace("dossier_", "")

        print(f"源表: {source_table_base} ({df_source.shape[1]} 列, {df_source.shape[0]} 行)")
        print(f"  → 表名上下文: {source_table_pure}")
        print(f"目标表: {target_table_name} ({df_target.shape[1]} 列, {df_target.shape[0]} 行)")
        print(f"  → 表名上下文: {target_table_pure}")
        print(f"Ground Truth: {len(ground_truth)} 个匹配对\n")

        # 获取源 schema
        source_schema_name, source_schema = get_source_schema_for_table(
            source_table_base, schemas
        )

        if source_schema is None:
            print(f"警告: 无法找到 {source_table_base} 的 schema，仅使用 Magneto")
            matchers_for_table = [m for m in matchers if "SF" not in m and "neo4j" not in m]
        else:
            matchers_for_table = matchers
            print(f"使用源 schema: {source_schema_name}")

        for matcher_name in matchers_for_table:
            print(f"\n运行匹配器: {matcher_name}")
            print(f"  - 嵌入模型: {embedding_model}")
            print(f"  - LLM 模型: {llm_model}")

            # 检查是否已运行
            if os.path.exists(result_file):
                df_existing = pd.read_csv(result_file)
                if not df_existing[
                    (df_existing["method"] == matcher_name)
                    & (df_existing["source_table"] == source_table_base)
                ].empty:
                    print(f"  ⏭️  跳过 {matcher_name} - {source_table_base}（结果已存在）")
                    continue

            try:
                start_time = time.time()

                if "SF" in matcher_name:
                    # Similarity Flooding 匹配器
                    matcher = get_matcher(
                        matcher_name,
                        embedding_model,
                        MODE,
                        llm_model,
                        source_schema=source_schema,
                        target_schema=target_schema,
                    )

                    # SF 需要将表数据作为字典提供
                    source_dfs = {source_table_pure: df_source}
                    target_dfs = {target_table_pure: df_target}

                    matches_result = matcher.get_matches_with_hungarian(
                        source_dfs,
                        target_dfs,
                        source_table_name=source_table_pure,
                        target_table_name=target_table_pure
                    )

                    # 转换为字典
                    if hasattr(matches_result, 'items'):
                        matches = dict(matches_result)
                    else:
                        matches = matches_result

                else:
                    # 标准 Magneto / GPT 匹配器
                    if "neo4j" in matcher_name:
                        matcher = get_matcher(
                            matcher_name, embedding_model, MODE, llm_model,
                            source_schema=source_schema,
                            target_schema=target_schema,
                        )
                    else:
                        matcher = get_matcher(
                            matcher_name, embedding_model, MODE, llm_model
                        )
                    matches = matcher.get_matches(
                        df_source, df_target,
                        source_table_name=source_table_pure,
                        target_table_name=target_table_pure
                    )

                end_time = time.time()
                runtime = end_time - start_time

                # 导出完整匹配对
                match_rows = []
                for ((src_table, src_col), (tgt_table, tgt_col)), score in matches.items():
                    match_rows.append({
                        "source_table": src_table,
                        "source_column": src_col,
                        "target_table": tgt_table,
                        "target_column": tgt_col,
                        "score": score,
                    })

                if match_rows:
                    match_df = pd.DataFrame(match_rows)
                    match_file = os.path.join(
                        results_dir,
                        f"{source_table_base}-{matcher_name}-matches.csv",
                    )
                    match_df.to_csv(match_file, index=False)

                # 计算指标
                matches_for_metrics = MatcherResults(matches)

                mrr_score = compute_mean_ranking_reciprocal_adjusted(
                    matches_for_metrics, ground_truth
                )

                recall_at_k = calculate_recall_at_k(matches_for_metrics, ground_truth)

                all_metrics = matches_for_metrics.get_metrics(ground_truth)
                recallAtGT = all_metrics["RecallAtSizeofGroundTruth"]

                print(f"  📊 结果:")
                print(f"     MRR: {mrr_score:.4f}")
                print(f"     RecallAtGT: {recallAtGT:.4f}")
                print(f"     Recall@20: {recall_at_k:.4f}")
                print(f"     运行时间: {runtime:.2f}秒")

                one2one_matches = matches_for_metrics.one_to_one()
                one2one_metrics = one2one_matches.get_metrics(ground_truth)

                # 导出一对一匹配结果
                gt_set = set(ground_truth)
                one2one_rows = []
                for ((src_table, src_col), (tgt_table, tgt_col)), score in one2one_matches.items():
                    one2one_rows.append({
                        "source_table": src_table,
                        "source_column": src_col,
                        "target_table": tgt_table,
                        "target_column": tgt_col,
                        "score": score,
                        "is_correct": (src_col, tgt_col) in gt_set,
                    })

                if one2one_rows:
                    one2one_df = pd.DataFrame(one2one_rows)
                    one2one_file = os.path.join(
                        results_dir,
                        f"{source_table_base}-{matcher_name}-one2one-matches.csv",
                    )
                    one2one_df.to_csv(one2one_file, index=False)

                ncols_src = str(df_source.shape[1])
                ncols_tgt = str(df_target.shape[1])
                nrows_src = str(df_source.shape[0])
                nrows_tgt = str(df_target.shape[0])
                nmatches = len(ground_truth)

                result = [
                    BENCHMARK,
                    DATASET,
                    source_database,
                    source_table_base,
                    target_table_name,
                    ncols_src,
                    ncols_tgt,
                    nrows_src,
                    nrows_tgt,
                    nmatches,
                    matcher_name,
                    runtime,
                    mrr_score,
                    recall_at_k,
                    all_metrics["Precision"],
                    all_metrics["F1Score"],
                    all_metrics["Recall"],
                    all_metrics["PrecisionTop10Percent"],
                    all_metrics["RecallAtSizeofGroundTruth"],
                    one2one_metrics["Precision"],
                    one2one_metrics["F1Score"],
                    one2one_metrics["Recall"],
                    one2one_metrics["PrecisionTop10Percent"],
                    one2one_metrics["RecallAtSizeofGroundTruth"],
                ]

                record_result(result_file, result)

            except Exception as e:
                print(f"  ❌ 运行 {matcher_name} 出错: {str(e)}")
                import traceback
                traceback.print_exc()
                continue

    print(f"\n\n{'='*80}")
    print(f"✅ FAA SF 基准测试完成（Known Table 模式）！")
    print(f"{'='*80}")
    print(f"结果已保存到: {result_file}")


def run_benchmark_sf_unknown_table(
    BENCHMARK: str = "faa_aviation_sf",
    DATASET: str = "faa_aviation",
    ROOT: str = "data/magneto_faa",
    MODE: str = "header_values_default",
    embedding_model: str = "mpnet",
    llm_model: str = "none",
    use_sf: bool = True,
    gpt_only: bool = False,
    pg_host: str = "localhost",
    pg_port: int = 5433,
):
    """
    Unknown Table 模式：未知表映射关系，每个源表匹配所有目标表
    输出整个任务的总体表现（1行汇总结果）

    Args:
        BENCHMARK: 基准名称
        DATASET: 数据集名称
        ROOT: 数据集根目录
        MODE: 列编码模式
        embedding_model: 嵌入模型名称
        llm_model: LLM 模型名称
        use_sf: 是否使用 Similarity Flooding
        gpt_only: 是否仅运行 GPT 模式
    """

    HEADER = [
        "benchmark",
        "dataset",
        "method",
        "total_table_pairs",
        "gt_table_pairs",
        "total_gt_matches",
        "total_runtime_seconds",
        "mrr",
        "Recall@20",
        "All_Precision",
        "All_F1Score",
        "All_Recall",
        "All_PrecisionTop10Percent",
        "All_RecallAtSizeofGroundTruth",
        "One2One_Precision",
        "One2One_F1Score",
        "One2One_Recall",
        "One2One_PrecisionTop10Percent",
        "One2One_RecallAtSizeofGroundTruth",
    ]

    results_dir = os.path.join(project_path, "results", "benchmarks", BENCHMARK)
    safe_llm_model = llm_model.replace("/", "_").replace(":", "_")
    safe_embedding_model = embedding_model.replace("/", "_").replace(":", "_").replace("\\", "_")

    sf_suffix = "-sf" if use_sf else ""
    experiment_name = f"{DATASET}-{safe_embedding_model}-{MODE}-{safe_llm_model}-unknown_table{sf_suffix}"

    results_dir = os.path.join(results_dir, experiment_name)

    if not os.path.exists(results_dir):
        os.makedirs(results_dir)

    result_file = os.path.join(results_dir, f"{experiment_name}.csv")
    print(f"结果文件: {result_file}")

    create_result_file(results_dir, result_file, HEADER)

    # 加载 CF SQL schema
    try:
        schema_dir = get_cf_schema_dir(ROOT)
    except FileNotFoundError as exc:
        print(f"错误: {exc}")
        return

    print(f"\n{'='*60}")
    print(f"加载 SQL Schema: {schema_dir}")
    print(f"{'='*60}\n")

    schemas = load_sql_schemas(schema_dir)
    target_schema = schemas.get("dossier")

    if target_schema is None:
        print("错误: 无法加载 dossier schema!")
        return

    print(f"\n成功加载 {len(schemas)} 个 schema。")

    # 加载所有源表和目标表
    studies_path = os.path.join(ROOT, "source-tables")
    target_path = os.path.join(ROOT, "target-tables")
    gt_path = os.path.join(ROOT, "ground-truth")

    # 加载 ground truth 用于评估（但不用于选择表对）
    all_mappings_file = os.path.join(ROOT, "ground-truth", "_all_mappings.csv")
    ground_truth_df = pd.read_csv(all_mappings_file)

    # 构建 ground truth 字典 {(source_table, target_table): [(src_col, tgt_col), ...]}
    gt_dict = {}
    for _, row in ground_truth_df.iterrows():
        src_table = f"{row['source_database'].lower()}_{row['source_table']}"
        tgt_table = f"dossier_{row['target_table']}"
        key = (src_table, tgt_table)
        if key not in gt_dict:
            gt_dict[key] = []
        gt_dict[key].append((row['source_column'], row['target_column']))

    # 获取所有源表
    source_files = [f for f in os.listdir(studies_path) if f.endswith('.csv')]
    # 获取所有目标表
    target_files = [f for f in os.listdir(target_path) if f.endswith('.csv')]

    print(f"\n{'='*80}")
    print(f"Unknown Table 模式：全表对匹配")
    print(f"{'='*80}")
    print(f"源表数量: {len(source_files)}")
    print(f"目标表数量: {len(target_files)}")
    print(f"总表对数: {len(source_files) * len(target_files)}")
    print(f"{'='*80}\n")

    # 确定要运行的匹配器
    skip_gpt = llm_model is None or llm_model.lower() == "none"

    if gpt_only:
        matchers = ["MagnetoGPT"]
    elif use_sf:
        if embedding_model in ["mpnet", "roberta", "e5", "arctic", "minilm"]:
            base = ["Magneto", "MagnetoBoost"]
        else:
            base = ["MagnetoFT", "MagnetoBoost"]
        matchers = base if skip_gpt else base + ["MagnetoGPT", "MagnetoGPT_neo4j"]
    else:
        if embedding_model in ["mpnet", "roberta", "e5", "arctic", "minilm"]:
            matchers = ["Magneto"] if skip_gpt else ["Magneto", "MagnetoGPT", "MagnetoGPT_neo4j"]
        else:
            matchers = ["MagnetoFT"] if skip_gpt else ["MagnetoFT", "MagnetoFTGPT"]

    total_table_pairs = len(source_files) * len(target_files)

    # 对每个匹配器运行完整评估
    for matcher_name in matchers:
        print(f"\n{'='*80}")
        print(f"运行匹配器: {matcher_name}")
        print(f"{'='*80}\n")

        # 检查是否已运行
        if os.path.exists(result_file):
            df_existing = pd.read_csv(result_file)
            if not df_existing[df_existing["method"] == matcher_name].empty:
                print(f"  ⏭️  跳过 {matcher_name}（已存在）")
                continue

        # 初始化累积变量
        all_matches = {}  # 合并所有有GT表对的匹配结果
        all_ground_truth = []  # 合并所有有GT表对的GT
        total_runtime = 0
        gt_pair_count = 0

        # MagnetoBoost: 模型加载一次 + 预计算嵌入 + 串行快速匹配
        # Magneto: 串行匹配
        if "Boost" in matcher_name:
            # === MagnetoBoost: 预计算嵌入 + 串行约束增强 ===
            from sentence_transformers import SentenceTransformer
            from transformers import AutoTokenizer

            print(f"\n{'='*80}")
            print(f"MagnetoBoost: 预计算嵌入 + 串行约束增强")
            print(f"{'='*80}\n")

            # 1. 加载模型一次
            model_key = embedding_model
            model_map = {
                "mpnet": "sentence-transformers/all-mpnet-base-v2",
                "roberta": "sentence-transformers/all-roberta-large-v1",
                "e5": "intfloat/e5-base",
                "minilm": "sentence-transformers/all-MiniLM-L6-v2",
            }
            model_path = model_map.get(model_key, model_key)

            # 如果是微调模型路径
            if model_key not in model_map and os.path.exists(
                os.path.join(project_path, "models", model_key)
            ):
                import torch
                base_path = model_map["mpnet"]
                boost_model = SentenceTransformer(base_path, device="cuda" if torch.cuda.is_available() else "cpu")
                state_dict = torch.load(os.path.join(project_path, "models", model_key), map_location=boost_model.device)
                boost_model.load_state_dict(state_dict)
            else:
                import torch
                boost_model = SentenceTransformer(model_path, device="cuda" if torch.cuda.is_available() else "cpu")

            boost_model.eval()
            boost_tokenizer = AutoTokenizer.from_pretrained(
                model_map.get(model_key.split("/")[-1].split("_")[0], model_map["mpnet"])
            )

            print(f"模型已加载: {model_path}")

            # 2. 加载所有 DataFrame
            all_source_dfs = {}
            for sf in sorted(source_files):
                tname = sf.replace(".csv", "")
                db_prefix = tname.split("_")[0].upper()
                pure_name = tname.replace(f"{db_prefix.lower()}_", "")
                all_source_dfs[pure_name] = pd.read_csv(os.path.join(studies_path, sf))

            all_target_dfs = {}
            for tf in sorted(target_files):
                tname = tf.replace(".csv", "")
                pure_name = tname.replace("dossier_", "")
                all_target_dfs[pure_name] = pd.read_csv(os.path.join(target_path, tf))

            # 3. 创建 ConstraintBoostMatcher 并预计算嵌入（pgvector 存储）
            # 从 ROOT 推断 dataset_name
            root_base = os.path.basename(ROOT.rstrip("/\\"))
            ds_name = root_base.replace("magneto_", "")

            boost_matcher = ConstraintBoostMatcher(
                source_schemas=schemas,
                target_schema=target_schema,
                model=boost_model,
                tokenizer=boost_tokenizer,
                embedding_model=model_key,
                encoding_mode=MODE,
                pg_host=pg_host,
                pg_port=pg_port,
                dataset_name=ds_name,
            )
            all_dfs = {**all_source_dfs, **all_target_dfs}
            boost_matcher.precompute_embeddings(all_dfs, verbose=True)

            # 4. 串行遍历所有表对
            pair_idx = 0
            for source_file in sorted(source_files):
                source_table_base = source_file.replace(".csv", "")
                source_database = source_table_base.split("_")[0].upper()
                source_table_pure = source_table_base.replace(f"{source_database.lower()}_", "")
                source_schema_name = source_database.lower()

                src_df = all_source_dfs[source_table_pure]

                for target_file in sorted(target_files):
                    target_table_name = target_file.replace(".csv", "")
                    target_table_pure = target_table_name.replace("dossier_", "")
                    tgt_df = all_target_dfs[target_table_pure]

                    pair_idx += 1
                    table_pair_key = (source_table_base, target_table_name)
                    has_gt = table_pair_key in gt_dict
                    ground_truth = gt_dict.get(table_pair_key, [])

                    try:
                        start_time = time.time()
                        matches = boost_matcher.match_table_pair(
                            source_table_pure, target_table_pure,
                            src_df, tgt_df,
                            src_schema_name=source_schema_name,
                        )
                        runtime = time.time() - start_time
                        total_runtime += runtime

                        gt_tag = f" [GT: {len(ground_truth)}]" if has_gt else ""
                        if pair_idx % 50 == 0 or has_gt:
                            print(f"  [{pair_idx}/{total_table_pairs}] "
                                  f"{source_table_base} <-> {target_table_name}"
                                  f"{gt_tag} - {runtime:.3f}s")

                        if has_gt and len(ground_truth) > 0:
                            gt_pair_count += 1
                            all_matches.update(matches)
                            all_ground_truth.extend(ground_truth)

                    except Exception as e:
                        print(f"  [{pair_idx}/{total_table_pairs}] "
                              f"{source_table_base} <-> {target_table_name} - 错误: {e}")

            del boost_model, boost_matcher  # 释放显存

        elif "SF" in matcher_name:
            # === 4进程并行处理 SF 匹配器（保留旧路径做对比实验）===
            print(f"\n{'='*80}")
            print(f"使用 4进程并行 处理 {matcher_name}")
            print(f"{'='*80}\n")

            table_pair_args = []
            for source_file in sorted(source_files):
                source_schema_name = source_file.split("_")[0]
                for target_file in sorted(target_files):
                    table_pair_args.append((source_file, target_file, source_schema_name))

            print(f"准备并行处理 {len(table_pair_args)} 个表对...\n")

            worker_func = partial(
                process_table_pair_worker,
                matcher_name=matcher_name,
                embedding_model=embedding_model,
                MODE=MODE,
                llm_model=llm_model,
                schema_dir=schema_dir,
                studies_path=studies_path,
                target_path=target_path,
                gt_dict=gt_dict
            )

            print("开始4进程并行计算...\n")
            with Pool(processes=4) as pool:
                results = pool.map(worker_func, table_pair_args)

            print(f"\n{'='*80}")
            print(f"并行处理完成，开始合并结果...")
            print(f"{'='*80}\n")

            for result in results:
                if result['error']:
                    print(f"  X {result['source_table']} <-> {result['target_table']}: {result['error'][:100]}")
                    continue
                if result['has_gt'] and len(result['ground_truth']) > 0:
                    gt_pair_count += 1
                    all_matches.update(result['matches'])
                    all_ground_truth.extend(result['ground_truth'])
                    total_runtime += result['runtime']

        else:
            # === 串行处理非SF匹配器（Magneto / MagnetoGPT / MagnetoGPT_neo4j） ===
            needs_schema = "neo4j" in matcher_name
            if not needs_schema:
                base_matcher = get_matcher(matcher_name, embedding_model, MODE, llm_model)

            for source_file in sorted(source_files):
                source_table_base = source_file.replace(".csv", "")
                source_database = source_table_base.split("_")[0].upper()
                source_table_pure = source_table_base.replace(f"{source_database.lower()}_", "")

                df_source = pd.read_csv(os.path.join(studies_path, source_file))

                if needs_schema:
                    src_schema_name = source_database.lower()
                    src_schema = schemas.get(src_schema_name)
                    base_matcher = get_matcher(
                        matcher_name, embedding_model, MODE, llm_model,
                        source_schema=src_schema,
                        target_schema=target_schema,
                    )

                for target_file in sorted(target_files):
                    target_table_name = target_file.replace(".csv", "")
                    target_table_pure = target_table_name.replace("dossier_", "")

                    df_target = pd.read_csv(os.path.join(target_path, target_file))

                    # 检查是否有 ground truth
                    table_pair_key = (source_table_base, target_table_name)
                    has_gt = table_pair_key in gt_dict
                    ground_truth = gt_dict.get(table_pair_key, [])

                    print(f"匹配: {source_table_base} <-> {target_table_name}", end="")
                    if has_gt:
                        print(f" [GT: {len(ground_truth)}]", end="")

                    try:
                        start_time = time.time()
                        matches = base_matcher.get_matches(
                            df_source, df_target,
                            source_table_name=source_table_pure,
                            target_table_name=target_table_pure
                        )

                        runtime = time.time() - start_time
                        total_runtime += runtime

                        print(f" - {runtime:.2f}秒")

                        # 如果有 ground truth，累积到总结果中
                        if has_gt and len(ground_truth) > 0:
                            gt_pair_count += 1
                            all_matches.update(matches)
                            all_ground_truth.extend(ground_truth)

                    except Exception as e:
                        print(f" - 错误: {str(e)}")
                        continue

        # 统一计算指标（将整个任务作为一个大匹配问题）
        print(f"\n{'='*80}")
        print(f"计算整体指标...")
        print(f"{'='*80}\n")

        # 将合并后的字典转换为 MatcherResults 对象
        all_matches_results = MatcherResults(all_matches)

        mrr_score = compute_mean_ranking_reciprocal_adjusted(all_matches_results, all_ground_truth)
        recall_at_k = calculate_recall_at_k(all_matches_results, all_ground_truth)
        all_metrics = all_matches_results.get_metrics(all_ground_truth)
        one2one_matches = all_matches_results.one_to_one()
        one2one_metrics = one2one_matches.get_metrics(all_ground_truth)

        print(f"  📊 整体结果:")
        print(f"     总表对数: {total_table_pairs}")
        print(f"     有GT表对数: {gt_pair_count}")
        print(f"     总GT匹配数: {len(all_ground_truth)}")
        print(f"     总运行时间: {total_runtime:.2f}秒")
        print(f"     MRR: {mrr_score:.4f}")
        print(f"     Recall@20: {recall_at_k:.4f}")
        print(f"     All RecallAtGT: {all_metrics['RecallAtSizeofGroundTruth']:.4f}")
        print(f"     One2One RecallAtGT: {one2one_metrics['RecallAtSizeofGroundTruth']:.4f}")

        # 输出1行结果
        result = [
            BENCHMARK,
            DATASET,
            matcher_name,
            total_table_pairs,
            gt_pair_count,
            len(all_ground_truth),
            total_runtime,
            mrr_score,
            recall_at_k,
            all_metrics["Precision"],
            all_metrics["F1Score"],
            all_metrics["Recall"],
            all_metrics["PrecisionTop10Percent"],
            all_metrics["RecallAtSizeofGroundTruth"],
            one2one_metrics["Precision"],
            one2one_metrics["F1Score"],
            one2one_metrics["Recall"],
            one2one_metrics["PrecisionTop10Percent"],
            one2one_metrics["RecallAtSizeofGroundTruth"],
        ]

        record_result(result_file, result)
        print(f"\n  ✓ {matcher_name} 完成！")

    print(f"\n\n{'='*80}")
    print(f"✅ FAA SF 基准测试完成（Unknown Table 模式）！")
    print(f"{'='*80}")
    print(f"结果已保存到: {result_file}")
    print(f"总表对数: {len(source_files) * len(target_files)}")


def _export_match_csv(matches: dict, results_dir: str, matcher_name: str,
                      match_type: str, source_files: list):
    """导出匹配结果到 CSV 文件。

    Args:
        matches: Valentine 格式匹配字典 {((src_table, src_col), (tgt_table, tgt_col)): score}
        results_dir: 输出目录
        matcher_name: 方法名（如 MagnetoGPT）
        match_type: "all" 或 "one2one"
        source_files: 源文件列表，用于推断 source_database
    """
    table_to_db = {}
    for sf in source_files:
        tname = sf.replace(".csv", "")
        db_prefix = tname.split("_")[0].upper()
        pure_name = tname.replace(f"{db_prefix.lower()}_", "")
        table_to_db[pure_name] = db_prefix

    rows = []
    for ((src_table, src_col), (tgt_table, tgt_col)), score in matches.items():
        rows.append({
            "source_database": table_to_db.get(src_table, "UNKNOWN"),
            "source_table": src_table,
            "source_column": src_col,
            "target_table": tgt_table,
            "target_column": tgt_col,
            "score": round(score, 6),
        })

    rows.sort(key=lambda x: -x["score"])

    out_path = os.path.join(results_dir, f"{matcher_name}-{match_type}-matches.csv")
    match_df = pd.DataFrame(rows)
    match_df.to_csv(out_path, index=False)
    print(f"  导出 {len(rows)} 条匹配 -> {out_path}")


def run_benchmark_global_unknown_table(
    BENCHMARK: str = "cf_sf",
    DATASET: str = "cf",
    ROOT: str = "data/magneto_cf",
    MODE: str = "header_values_default",
    embedding_model: str = "mpnet",
    llm_model: str = "none",
    pg_host: str = "localhost",
    pg_port: int = 5433,
    neo4j_uri: str = "bolt://localhost:7687",
    neo4j_user: str = "neo4j",
    neo4j_password: str = "magneto123",
):
    """
    全局 Top-K Unknown Table 模式：每个源列的候选来自所有目标表。
    所有 5 个方法统一使用全局 top-K。
    """
    import torch
    from sentence_transformers import SentenceTransformer
    from transformers import AutoTokenizer

    HEADER = [
        "benchmark", "dataset", "method",
        "total_source_tables", "gt_table_pairs", "total_gt_matches",
        "total_runtime_seconds", "mrr", "Recall@20",
        "All_Precision", "All_F1Score", "All_Recall",
        "All_PrecisionTop10Percent", "All_RecallAtSizeofGroundTruth",
        "One2One_Precision", "One2One_F1Score", "One2One_Recall",
        "One2One_PrecisionTop10Percent", "One2One_RecallAtSizeofGroundTruth",
    ]

    results_dir = os.path.join(project_path, "results", "benchmarks", BENCHMARK)
    safe_llm = llm_model.replace("/", "_").replace(":", "_")
    safe_emb = embedding_model.replace("/", "_").replace(":", "_").replace("\\", "_")
    experiment_name = f"{DATASET}-{safe_emb}-{MODE}-{safe_llm}-global_unknown"
    results_dir = os.path.join(results_dir, experiment_name)
    os.makedirs(results_dir, exist_ok=True)
    result_file = os.path.join(results_dir, f"{experiment_name}.csv")
    print(f"结果文件: {result_file}")
    create_result_file(results_dir, result_file, HEADER)

    # --- 加载 Schema ---
    try:
        schema_dir = get_cf_schema_dir(ROOT)
    except FileNotFoundError as exc:
        print(f"错误: {exc}")
        return
    print(f"\n加载 SQL Schema: {schema_dir}")
    schemas = load_sql_schemas(schema_dir)
    target_schema = schemas.get("dossier")
    if target_schema is None:
        print("错误: 无法加载 dossier schema!")
        return

    # --- 加载数据 ---
    studies_path = os.path.join(ROOT, "source-tables")
    target_path = os.path.join(ROOT, "target-tables")

    source_files = sorted(f for f in os.listdir(studies_path) if f.endswith('.csv'))
    target_files = sorted(f for f in os.listdir(target_path) if f.endswith('.csv'))

    all_source_dfs = {}
    for sf in source_files:
        tname = sf.replace(".csv", "")
        db_prefix = tname.split("_")[0].upper()
        pure_name = tname.replace(f"{db_prefix.lower()}_", "")
        all_source_dfs[pure_name] = pd.read_csv(os.path.join(studies_path, sf))

    all_target_dfs = {}
    for tf in target_files:
        tname = tf.replace(".csv", "")
        pure_name = tname.replace("dossier_", "")
        all_target_dfs[pure_name] = pd.read_csv(os.path.join(target_path, tf))

    # --- Ground Truth ---
    all_mappings_file = os.path.join(ROOT, "ground-truth", "_all_mappings.csv")
    ground_truth_df = pd.read_csv(all_mappings_file)
    gt_dict = {}
    for _, row in ground_truth_df.iterrows():
        src_table = f"{row['source_database'].lower()}_{row['source_table']}"
        tgt_table = f"dossier_{row['target_table']}"
        src_pure = src_table.replace(f"{row['source_database'].lower()}_", "")
        tgt_pure = tgt_table.replace("dossier_", "")
        key = (src_pure, tgt_pure)
        if key not in gt_dict:
            gt_dict[key] = []
        gt_dict[key].append((row['source_column'], row['target_column']))

    print(f"\n源表: {len(source_files)}, 目标表: {len(target_files)}")
    print(f"Ground Truth 表对: {len(gt_dict)}, 总匹配数: {sum(len(v) for v in gt_dict.values())}")

    # --- 加载模型 + 预计算嵌入（pgvector）---
    model_map = {
        "mpnet": "sentence-transformers/all-mpnet-base-v2",
        "roberta": "sentence-transformers/all-roberta-large-v1",
        "e5": "intfloat/e5-base",
        "arctic": "Snowflake/snowflake-arctic-embed-l-v2.0",
        "minilm": "sentence-transformers/all-MiniLM-L6-v2",
    }
    model_path = model_map.get(embedding_model, embedding_model)
    is_finetuned_model = is_finetuned_embedding_model(embedding_model)
    if is_finetuned_model:
        base_path = model_map["mpnet"]
        st_model = SentenceTransformer(base_path, device="cuda" if torch.cuda.is_available() else "cpu")
        state_dict = torch.load(os.path.join(project_path, "models", embedding_model), map_location=st_model.device)
        st_model.load_state_dict(state_dict)
    else:
        st_model = SentenceTransformer(model_path, device="cuda" if torch.cuda.is_available() else "cpu")
    st_model.eval()
    tokenizer = AutoTokenizer.from_pretrained(
        model_map.get(embedding_model.split("/")[-1].split("_")[0], model_map["mpnet"])
    )

    root_base = os.path.basename(ROOT.rstrip("/\\"))
    ds_name = root_base.replace("magneto_", "")

    from algorithms.similarity_flooding.sf_matcher import ConstraintBoostMatcher
    boost_matcher = ConstraintBoostMatcher(
        source_schemas=schemas, target_schema=target_schema,
        model=st_model, tokenizer=tokenizer,
        embedding_model=embedding_model, encoding_mode=MODE,
        pg_host=pg_host, pg_port=pg_port, dataset_name=ds_name,
    )
    all_dfs = {**all_source_dfs, **all_target_dfs}
    boost_matcher.precompute_embeddings(all_dfs, verbose=True)
    embedding_cache = boost_matcher._embedding_cache

    # --- 初始化 Neo4j ---
    neo4j_graph = None
    try:
        from algorithms.similarity_flooding.schema_graph_neo4j import SchemaGraphNeo4j
        neo4j_graph = SchemaGraphNeo4j(uri=neo4j_uri, user=neo4j_user, password=neo4j_password)
        print("\n加载 Schema 到 Neo4j...")
        for name, schema in schemas.items():
            neo4j_graph.load_schema(schema)
        print("Neo4j Schema 加载完成。")
    except Exception as e:
        print(f"Neo4j 初始化失败（MagnetoGPT_neo4j2 将不可用）: {e}")
        neo4j_graph = None

    # --- 创建 GlobalMatcher ---
    from algorithms.magneto.magneto.global_matcher import GlobalMatcher
    matcher = GlobalMatcher(
        source_schemas=schemas, target_schema=target_schema,
        embedding_cache=embedding_cache,
        llm_model=llm_model if llm_model and llm_model.lower() != "none" else None,
        neo4j_graph=neo4j_graph,
    )

    # --- 确定方法列表 ---
    skip_gpt = llm_model is None or llm_model.lower() == "none"
    matchers_list = get_global_unknown_method_names(
        skip_gpt=skip_gpt,
        has_neo4j=neo4j_graph is not None,
        is_finetuned=is_finetuned_model,
    )

    # --- 逐方法处理 ---
    for matcher_name in matchers_list:
        if os.path.exists(result_file):
            df_existing = pd.read_csv(result_file)
            if not df_existing[df_existing["method"] == matcher_name].empty:
                print(f"\n⏭️  跳过 {matcher_name}（已存在）")
                continue

        print(f"\n{'='*80}")
        print(f"运行方法: {matcher_name} (全局 Top-K)")
        print(f"{'='*80}\n")

        if neo4j_graph and "neo4j2" in matcher_name:
            neo4j_graph.clear_semantic_edges(ds_name)

        all_matches = {}
        all_ground_truth = []
        gt_pair_count = 0
        total_runtime = 0.0

        pbar = tqdm(
            source_files, desc=f"{matcher_name}",
            unit="table",
            bar_format="{l_bar}{bar}| {n_fmt}/{total_fmt} [{elapsed}<{remaining}, {rate_fmt}]",
        )
        for source_file in pbar:
            source_table_base = source_file.replace(".csv", "")
            source_database = source_table_base.split("_")[0].upper()
            source_table_pure = source_table_base.replace(f"{source_database.lower()}_", "")
            src_schema_name = source_database.lower()
            src_df = all_source_dfs[source_table_pure]

            pbar.set_postfix_str(source_table_base, refresh=True)
            start_time = time.time()

            try:
                global_topk = matcher.compute_global_topk(
                    source_table_pure, src_df, all_target_dfs, topk=20)

                if matcher_name in ("Magneto", "MagnetoFT"):
                    matches = matcher.match_magneto(global_topk, source_table_pure)
                elif matcher_name in ("MagnetoBoost", "MagnetoFTBoost"):
                    matches = matcher.match_boost(global_topk, source_table_pure, src_schema_name)
                elif matcher_name in ("MagnetoGPT", "MagnetoFTGPT"):
                    matches = matcher.match_gpt(global_topk, source_table_pure, src_df, all_target_dfs)
                elif matcher_name == "MagnetoGPT_neo4j":
                    matches = matcher.match_gpt_neo4j(global_topk, source_table_pure, src_schema_name, src_df, all_target_dfs)
                elif matcher_name in ("MagnetoGPT_neo4j2", "MagnetoFTGPT_neo4j2"):
                    matches = matcher.match_gpt_neo4j2(global_topk, source_table_pure, src_schema_name, src_df, all_target_dfs, dataset_name=ds_name)
                else:
                    matches = {}

                runtime = time.time() - start_time
                total_runtime += runtime

                for (src_pure, tgt_pure), gt_pairs in gt_dict.items():
                    if src_pure == source_table_pure:
                        relevant = {
                            k: v for k, v in matches.items()
                            if k[1][0] == tgt_pure
                        }
                        if relevant:
                            gt_pair_count += 1
                            all_matches.update(relevant)
                            all_ground_truth.extend(gt_pairs)

            except Exception as e:
                import traceback
                tqdm.write(f"  {source_table_base} - 错误: {e}")
                traceback.print_exc()

        # --- 计算指标 ---
        print(f"\n{'='*80}")
        print(f"计算 {matcher_name} 整体指标...")
        print(f"{'='*80}\n")

        all_matches_results = MatcherResults(all_matches)
        mrr_score = compute_mean_ranking_reciprocal_adjusted(all_matches_results, all_ground_truth)
        recall_at_k = calculate_recall_at_k(all_matches_results, all_ground_truth)
        all_metrics = all_matches_results.get_metrics(all_ground_truth)
        one2one_matches = all_matches_results.one_to_one()
        one2one_metrics = one2one_matches.get_metrics(all_ground_truth)

        if "GPT" in matcher_name:
            _export_match_csv(all_matches, results_dir, matcher_name, "all", source_files)
            _export_match_csv(dict(one2one_matches), results_dir, matcher_name, "one2one", source_files)

        print(f"  总源表: {len(source_files)}")
        print(f"  有GT表对数: {gt_pair_count}")
        print(f"  总GT匹配数: {len(all_ground_truth)}")
        print(f"  运行时间: {total_runtime:.2f}s")
        print(f"  MRR: {mrr_score:.4f}")
        print(f"  Recall@20: {recall_at_k:.4f}")
        print(f"  All RecallAtGT: {all_metrics['RecallAtSizeofGroundTruth']:.4f}")
        print(f"  One2One Recall: {one2one_metrics['Recall']:.4f}")

        result = [
            BENCHMARK, DATASET, matcher_name,
            len(source_files), gt_pair_count, len(all_ground_truth),
            total_runtime, mrr_score, recall_at_k,
            all_metrics["Precision"], all_metrics["F1Score"], all_metrics["Recall"],
            all_metrics["PrecisionTop10Percent"], all_metrics["RecallAtSizeofGroundTruth"],
            one2one_metrics["Precision"], one2one_metrics["F1Score"], one2one_metrics["Recall"],
            one2one_metrics["PrecisionTop10Percent"], one2one_metrics["RecallAtSizeofGroundTruth"],
        ]
        record_result(result_file, result)
        print(f"\n  ✓ {matcher_name} 完成！")

    if neo4j_graph:
        neo4j_graph.close()

    print(f"\n\n{'='*80}")
    print(f"全局 Top-K 基准测试完成！")
    print(f"{'='*80}")
    print(f"结果已保存到: {result_file}")


def run_benchmark(
    BENCHMARK: str = "cf_sf",
    DATASET: str = "cf",
    ROOT: str = "data/magneto_cf",
    MODE: str = "header_values_default",
    embedding_model: str = "mpnet",
    llm_model: str = "none",
    use_sf: bool = True,
    gpt_only: bool = False,
    eval_mode: str = "global_unknown",
    pg_host: str = "localhost",
    pg_port: int = 5433,
):
    """
    运行 FAA 航空数据集基准测试（支持三种模式）
    """
    if eval_mode == "known_table":
        return run_benchmark_sf_known_table(
            BENCHMARK, DATASET, ROOT, MODE,
            embedding_model, llm_model, use_sf, gpt_only,
            pg_host, pg_port
        )
    elif eval_mode == "unknown_table":
        return run_benchmark_sf_unknown_table(
            BENCHMARK, DATASET, ROOT, MODE,
            embedding_model, llm_model, use_sf, gpt_only,
            pg_host, pg_port
        )
    elif eval_mode == "global_unknown":
        return run_benchmark_global_unknown_table(
            BENCHMARK, DATASET, ROOT, MODE,
            embedding_model, llm_model,
            pg_host, pg_port,
        )
    else:
        raise ValueError(f"未知 eval_mode: {eval_mode}。使用 'known_table'、'unknown_table' 或 'global_unknown'")


def main():
    parser = argparse.ArgumentParser(description="运行 CF 成飞数据集 Magneto/ConstraintBoost 基准测试")

    parser.add_argument(
        "--mode",
        type=str,
        help="列序列化模式",
        default="header_values_default",
    )
    parser.add_argument(
        "--embedding_model",
        type=str,
        help="嵌入模型名称或权重路径",
        default="mpnet",
    )
    parser.add_argument(
        "--llm_model",
        type=str,
        help="LLM 重排序器名称",
        default="none",
    )
    parser.add_argument(
        "--use_sf",
        action="store_true",
        default=True,
        help="启用 Similarity Flooding（默认开启）",
    )
    parser.add_argument(
        "--no_sf",
        action="store_true",
        help="禁用 Similarity Flooding（仅运行 Magneto）",
    )
    parser.add_argument(
        "--gpt_only",
        action="store_true",
        help="启用仅 GPT 重排模式",
    )
    parser.add_argument(
        "--eval_mode",
        type=str,
        choices=["known_table", "unknown_table", "global_unknown"],
        default="global_unknown",
        help="评估模式：known_table | unknown_table（按表对） | global_unknown（全局top-K）",
    )
    parser.add_argument(
        "--root",
        type=str,
        default="data/magneto_cf",
        help="CF 数据集根目录（默认 data/magneto_cf）",
    )
    parser.add_argument(
        "--dataset",
        type=str,
        default=None,
        help="数据集名称（默认从 root 推断；CF 建议显式传入 cf）",
    )
    parser.add_argument(
        "--pg_host",
        type=str,
        default="localhost",
        help="pgvector 主机地址（默认 localhost）",
    )
    parser.add_argument(
        "--pg_port",
        type=int,
        default=5433,
        help="pgvector 端口（默认 5433）",
    )

    args = parser.parse_args()

    use_sf = not args.no_sf

    # 从 root 推断 dataset 名称
    dataset = args.dataset
    if dataset is None:
        root_base = os.path.basename(args.root.rstrip("/\\"))
        dataset = root_base.replace("magneto_", "")

    benchmark = f"{dataset}_sf" if use_sf else dataset

    run_benchmark(
        BENCHMARK=benchmark,
        DATASET=dataset,
        ROOT=args.root,
        MODE=args.mode,
        embedding_model=args.embedding_model,
        llm_model=args.llm_model,
        use_sf=use_sf,
        gpt_only=args.gpt_only,
        eval_mode=args.eval_mode,
        pg_host=args.pg_host,
        pg_port=args.pg_port,
    )


if __name__ == "__main__":
    main()
