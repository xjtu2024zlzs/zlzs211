import os
import sys
import pandas as pd
import time
import datetime
import pprint
import argparse

pp = pprint.PrettyPrinter(indent=4, sort_dicts=True)

project_path = os.path.dirname(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
)
sys.path.append(os.path.join(project_path))

from experiments.benchmarks.benchmark_utils import (
    compute_mean_ranking_reciprocal_adjusted,
    create_result_file,
    record_result,
    calculate_recall_at_k,
)
from algorithms.magneto.magneto import Magneto


def get_matcher(
    method, embedding_model=None, mode="header_values_default", llm_model=None
):
    """
    根据方法名返回对应的 Magneto 匹配器
    
    Args:
        method: 匹配器名称 (Magneto, MagnetoGPT, MagnetoFT, MagnetoFTGPT)
        embedding_model: 嵌入模型名称或路径
        mode: 列编码模式
        llm_model: LLM 模型名称
    """
    if method == "Magneto":
        return Magneto(encoding_mode=mode, embedding_model=embedding_model)
    elif method == "MagnetoFT":
        model_path = os.path.join(
            project_path,
            "models",
            embedding_model,
        )
        return Magneto(encoding_mode=mode, embedding_model=model_path)
    elif method == "MagnetoGPT":
        return Magneto(
            encoding_mode=mode,
            embedding_model=embedding_model,
            llm_model=llm_model,
            use_bp_reranker=False,
            use_gpt_reranker=True,
        )
    elif method == "MagnetoFTGPT":
        model_path = os.path.join(
            project_path,
            "models",
            embedding_model,
        )
        return Magneto(
            encoding_mode=mode,
            embedding_model=model_path,
            llm_model=llm_model,
            use_bp_reranker=False,
            use_gpt_reranker=True,
        )


def load_faa_mappings(all_mappings_file):
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
        source_to_main_target[src] = list(targets)[0]  # 取第一个
    
    return source_to_main_target


def run_benchmark_known_table(
    BENCHMARK="faa_aviation",
    DATASET="faa_aviation",
    ROOT="data/magneto_faa",
    MODE="header_values_default",
    embedding_model="mpnet",
    llm_model="ollama/llama3:8b",
    gpt_only=False,
):
    """
    Known Table 模式：已知表映射关系，只评估列匹配
    
    Args:
        BENCHMARK: 基准名称
        DATASET: 数据集名称
        ROOT: 数据集根目录
        MODE: 列编码模式
        embedding_model: 嵌入模型名称
        llm_model: LLM 模型名称
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

    results_dir = os.path.join(
        project_path, "results", "benchmarks", BENCHMARK
    )
    safe_llm_model = llm_model.replace("/", "_").replace(":", "_")
    safe_embedding_model = embedding_model.replace("/", "_").replace(":", "_").replace("\\", "_")

    # 构建实验名称，用作子文件夹名（添加 known_table 后缀）
    experiment_name = (
        DATASET
        + "-"
        + safe_embedding_model
        + "-"
        + MODE
        + "-"
        + safe_llm_model
        + "-known_table"
    )
    
    # 将结果目录指向子文件夹
    results_dir = os.path.join(results_dir, experiment_name)
    
    # 确保子文件夹存在
    if not os.path.exists(results_dir):
        os.makedirs(results_dir)

    result_file = os.path.join(
        results_dir,
        experiment_name + ".csv",
    )
    print(result_file)

    create_result_file(results_dir, result_file, HEADER)

    # 加载源表到目标表的映射关系
    all_mappings_file = os.path.join(ROOT, "ground-truth", "_all_mappings.csv")
    source_to_target = load_faa_mappings(all_mappings_file)
    
    print(f"\n发现 {len(source_to_target)} 个源表到目标表的映射关系\n")

    studies_path = os.path.join(ROOT, "source-tables")
    target_path = os.path.join(ROOT, "target-tables")
    gt_path = os.path.join(ROOT, "ground-truth")

    # 遍历所有 ground-truth 文件
    for gt_file in sorted(os.listdir(gt_path)):
        if not gt_file.endswith(".csv") or gt_file.startswith("_"):
            continue

        print(f"\n{'='*80}")
        print(f"Processing {gt_file}")
        print(f"{'='*80}\n")

        # 从文件名提取源表名（去掉 .csv）
        source_table_base = gt_file.replace(".csv", "")
        
        # 确定目标表
        if source_table_base not in source_to_target:
            print(f"Warning: 无法找到 {source_table_base} 的目标表映射，跳过")
            continue
        
        target_table_name = source_to_target[source_table_base]
        
        # 构建文件路径
        source_file = os.path.join(studies_path, f"{source_table_base}.csv")
        target_file = os.path.join(target_path, f"{target_table_name}.csv")
        
        if not os.path.exists(source_file):
            print(f"Warning: 源文件不存在 {source_file}，跳过")
            continue
        
        if not os.path.exists(target_file):
            print(f"Warning: 目标文件不存在 {target_file}，跳过")
            continue

        # 读取数据
        df_source = pd.read_csv(source_file)
        df_target = pd.read_csv(target_file)

        # 读取 ground truth
        gt_df = pd.read_csv(os.path.join(gt_path, gt_file))
        gt_df.dropna(inplace=True)
        
        # Ground truth 格式: source_column, target_column
        ground_truth = list(gt_df.itertuples(index=False, name=None))

        if len(ground_truth) == 0:
            print(f"Warning: {gt_file} 没有有效的 ground truth，跳过")
            continue

        # 提取源数据库名称（PLM, ERP, MES, MRO）
        source_database = source_table_base.split("_")[0].upper()
        
        # 提取纯净的表名（去掉数据库前缀用于表名上下文）
        # 例如: plm_aircraft_item_master -> aircraft_item_master
        source_table_pure = source_table_base.replace(f"{source_database.lower()}_", "")
        # 例如: dossier_aircraft_dossier -> aircraft_dossier  
        target_table_pure = target_table_name.replace("dossier_", "")

        print(f"源表: {source_table_base} ({df_source.shape[1]} 列, {df_source.shape[0]} 行)")
        print(f"  → 表名上下文: {source_table_pure}")
        print(f"目标表: {target_table_name} ({df_target.shape[1]} 列, {df_target.shape[0]} 行)")
        print(f"  → 表名上下文: {target_table_pure}")
        print(f"Ground Truth: {len(ground_truth)} 个匹配对\n")

        # 确定要运行的匹配器
        # 如果 llm_model 为 none 或 None，则跳过 GPT 匹配器
        skip_gpt = llm_model is None or llm_model.lower() == "none"
        
        if gpt_only:
            matchers = ["GPT"]
        else:
            if embedding_model in ["mpnet", "roberta", "e5", "arctic", "minilm"]:
                matchers = ["Magneto"] if skip_gpt else ["Magneto", "MagnetoGPT"]
            else:
                matchers = ["MagnetoFT"] if skip_gpt else ["MagnetoFT", "MagnetoFTGPT"]

        for matcher_name in matchers:
            print(f"\n运行匹配器: {matcher_name}")
            print(f"  - 嵌入模型: {embedding_model}")
            print(f"  - LLM 模型: {llm_model}")

            # 检查是否已经运行过
            if os.path.exists(result_file):
                df_existing = pd.read_csv(result_file)
                if not df_existing[
                    (df_existing["method"] == matcher_name)
                    & (df_existing["source_table"] == source_table_base)
                ].empty:
                    print(f"  ⏭️  跳过 {matcher_name} - {source_table_base}（结果已存在）")
                    continue

            matcher = get_matcher(matcher_name, embedding_model, MODE, llm_model)

            start_time = time.time()
            # 传递表名上下文
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
                match_rows.append(
                    {
                        "source_table": src_table,
                        "source_column": src_col,
                        "target_table": tgt_table,
                        "target_column": tgt_col,
                        "score": score,
                    }
                )
            if match_rows:
                match_df = pd.DataFrame(match_rows)
                match_file = os.path.join(
                    results_dir,
                    f"{source_table_base}-{matcher_name}-matches.csv",
                )
                match_df.to_csv(match_file, index=False)

            # 计算指标
            mrr_score = compute_mean_ranking_reciprocal_adjusted(
                matches, ground_truth
            )

            recall_at_k = calculate_recall_at_k(matches, ground_truth)

            all_metrics = matches.get_metrics(ground_truth)

            recallAtGT = all_metrics["RecallAtSizeofGroundTruth"]

            print(f"  📊 结果:")
            print(f"     MRR: {mrr_score:.4f}")
            print(f"     RecallAtGT: {recallAtGT:.4f}")
            print(f"     Recall@20: {recall_at_k:.4f}")
            print(f"     运行时间: {runtime:.2f}秒")

            one2one_matches = matches.one_to_one()
            one2one_metrics = one2one_matches.get_metrics(ground_truth)

            # 导出一对一匹配结果
            gt_set = set(ground_truth)
            one2one_rows = []
            for (
                (src_table, src_col),
                (tgt_table, tgt_col),
            ), score in one2one_matches.items():
                one2one_rows.append(
                    {
                        "source_table": src_table,
                        "source_column": src_col,
                        "target_table": tgt_table,
                        "target_column": tgt_col,
                        "score": score,
                        "is_correct": (src_col, tgt_col) in gt_set,
                    }
                )
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

    print(f"\n\n{'='*80}")
    print(f"✅ FAA 基准测试完成（Known Table 模式）！")
    print(f"{'='*80}")
    print(f"结果已保存到: {result_file}")


def run_benchmark_unknown_table(
    BENCHMARK="faa_aviation",
    DATASET="faa_aviation",
    ROOT="data/magneto_faa",
    MODE="header_values_default",
    embedding_model="mpnet",
    llm_model="ollama/llama3:8b",
    gpt_only=False,
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
    
    results_dir = os.path.join(
        project_path, "results", "benchmarks", BENCHMARK
    )
    safe_llm_model = llm_model.replace("/", "_").replace(":", "_")
    safe_embedding_model = embedding_model.replace("/", "_").replace(":", "_").replace("\\", "_")
    
    # 添加 unknown_table 后缀
    experiment_name = (
        DATASET
        + "-"
        + safe_embedding_model
        + "-"
        + MODE
        + "-"
        + safe_llm_model
        + "-unknown_table"
    )
    
    results_dir = os.path.join(results_dir, experiment_name)
    
    if not os.path.exists(results_dir):
        os.makedirs(results_dir)
    
    result_file = os.path.join(results_dir, experiment_name + ".csv")
    print(f"结果文件: {result_file}")
    
    create_result_file(results_dir, result_file, HEADER)
    
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
    # 如果 llm_model 为 none 或 None，则跳过 GPT 匹配器
    skip_gpt = llm_model is None or llm_model.lower() == "none"
    
    if gpt_only:
        matchers = ["GPT"]
    else:
        if embedding_model in ["mpnet", "roberta", "e5", "arctic", "minilm"]:
            matchers = ["Magneto"] if skip_gpt else ["Magneto", "MagnetoGPT"]
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
        
        matcher = get_matcher(matcher_name, embedding_model, MODE, llm_model)
        
        # 遍历所有表对
        for source_file in sorted(source_files):
            source_table_base = source_file.replace(".csv", "")
            source_database = source_table_base.split("_")[0].upper()
            source_table_pure = source_table_base.replace(f"{source_database.lower()}_", "")
            
            df_source = pd.read_csv(os.path.join(studies_path, source_file))
            
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
                
                start_time = time.time()
                matches = matcher.get_matches(
                    df_source, df_target,
                    source_table_name=source_table_pure,
                    target_table_name=target_table_pure
                )
                end_time = time.time()
                runtime = end_time - start_time
                total_runtime += runtime
                
                print(f" - {runtime:.2f}秒")
                
                # 如果有ground truth，累积到总结果中
                if has_gt and len(ground_truth) > 0:
                    gt_pair_count += 1
                    # 合并matches（key已包含表名，直接更新）
                    all_matches.update(matches)
                    # 合并ground_truth
                    all_ground_truth.extend(ground_truth)
        
        # 统一计算指标（将整个任务作为一个大匹配问题）
        print(f"\n{'='*80}")
        print(f"计算整体指标...")
        print(f"{'='*80}\n")
        
        # 将合并后的字典转换为 MatcherResults 对象
        from valentine import MatcherResults
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
    print(f"✅ FAA 基准测试完成（Unknown Table 模式）！")
    print(f"{'='*80}")
    print(f"结果已保存到: {result_file}")
    print(f"总表对数: {len(source_files) * len(target_files)}")


def run_benchmark(
    BENCHMARK="faa_aviation",
    DATASET="faa_aviation",
    ROOT="data/magneto_faa",
    MODE="header_values_default",
    embedding_model="mpnet",
    llm_model="ollama/llama3:8b",
    gpt_only=False,
    eval_mode="known_table",
):
    """
    运行 FAA 航空数据集基准测试（支持两种模式）
    
    Args:
        eval_mode: 评估模式
            - "known_table": 已知表映射，只评估列匹配（快速）
            - "unknown_table": 未知表映射，匹配所有表对（用于GNN）
    """
    if eval_mode == "known_table":
        return run_benchmark_known_table(
            BENCHMARK, DATASET, ROOT, MODE, 
            embedding_model, llm_model, gpt_only
        )
    elif eval_mode == "unknown_table":
        return run_benchmark_unknown_table(
            BENCHMARK, DATASET, ROOT, MODE,
            embedding_model, llm_model, gpt_only
        )
    else:
        raise ValueError(f"Unknown eval_mode: {eval_mode}. Use 'known_table' or 'unknown_table'")


def main():
    parser = argparse.ArgumentParser(description="运行 FAA 航空数据集基准测试")

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
        default="ollama/llama3:8b",
    )
    parser.add_argument(
        "--gpt_only",
        action="store_true",
        help="启用仅 GPT 重排模式",
    )
    parser.add_argument(
        "--eval_mode",
        type=str,
        choices=["known_table", "unknown_table"],
        default="known_table",
        help="评估模式：known_table（已知表映射，快速）或 unknown_table（未知表映射，用于GNN）",
    )
    args = parser.parse_args()

    run_benchmark(
        MODE=args.mode,
        embedding_model=args.embedding_model,
        llm_model=args.llm_model,
        gpt_only=args.gpt_only,
        eval_mode=args.eval_mode,
    )


if __name__ == "__main__":
    main()

