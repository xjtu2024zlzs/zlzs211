"""
FAA 基准测试结果汇总脚本

功能：
1. 读取表级详细结果（faa_aviation-***.csv）
2. 生成三个层级的汇总：
   - table_level_summary.csv（表级精简）
   - database_level_summary.csv（数据库级，按PLM/ERP/MES/MRO聚合）
   - task_level_summary.csv（任务级，整体聚合）

使用正确的加权聚合方法（不是简单平均）
"""

import os
import pandas as pd
import argparse
from pathlib import Path


def weighted_average(values, weights):
    """加权平均"""
    total_weight = sum(weights)
    if total_weight == 0:
        return 0.0
    return sum(v * w for v, w in zip(values, weights)) / total_weight


def aggregate_precision_recall_f1(df, precision_col, recall_col):
    """
    从Precision和Recall反推TP, FP, FN，然后重新计算整体指标
    
    公式：
    - TP = Recall × GT_total
    - Precision = TP / (TP + FP) → FP = TP/Precision - TP
    - Recall = TP / (TP + FN) → FN = TP/Recall - TP
    """
    total_tp = 0
    total_fp = 0
    total_fn = 0
    
    for _, row in df.iterrows():
        nmatches = row['nmatches']
        precision = row[precision_col]
        recall = row[recall_col]
        
        # TP = Recall × GT
        tp = recall * nmatches
        
        # FP = TP / Precision - TP
        if precision > 0:
            fp = (tp / precision) - tp
        else:
            fp = 0
        
        # FN = TP / Recall - TP
        if recall > 0:
            fn = (tp / recall) - tp
        else:
            fn = nmatches  # 全部漏检
        
        total_tp += tp
        total_fp += fp
        total_fn += fn
    
    # 重新计算整体指标
    overall_precision = total_tp / (total_tp + total_fp) if (total_tp + total_fp) > 0 else 0
    overall_recall = total_tp / (total_tp + total_fn) if (total_tp + total_fn) > 0 else 0
    overall_f1 = 2 * overall_precision * overall_recall / (overall_precision + overall_recall) if (overall_precision + overall_recall) > 0 else 0
    
    return overall_precision, overall_recall, overall_f1


def generate_table_level_summary(df):
    """
    生成表级汇总（精简版，保留关键指标）
    """
    summary = df[[
        'source_database',
        'source_table',
        'target_table',
        'method',
        'ncols_src',
        'ncols_tgt',
        'nmatches',
        'mrr',
        'Recall@20',
        'All_RecallAtSizeofGroundTruth',
        'One2One_RecallAtSizeofGroundTruth',
        'One2One_F1Score',
        'runtime'
    ]].copy()
    
    # 重命名列使其更简洁
    summary.columns = [
        'source_database',
        'source_table',
        'target_table',
        'method',
        'ncols_src',
        'ncols_tgt',
        'nmatches',
        'mrr',
        'recall_at_20',
        'all_recall_at_gt',
        'one2one_recall_at_gt',
        'one2one_f1',
        'runtime'
    ]
    
    return summary


def generate_database_level_summary(df):
    """
    生成数据库级汇总（PLM/ERP/MES/MRO）
    
    使用加权聚合：
    - MRR, Recall等指标按nmatches加权
    - Precision/Recall/F1重新计算（从TP/FP/FN）
    """
    results = []
    
    for (database, method), group in df.groupby(['source_database', 'method']):
        # 基础统计
        num_tables = len(group)
        total_source_cols = group['ncols_src'].astype(int).sum()
        total_target_cols = group['ncols_tgt'].astype(int).sum()
        total_gt_matches = group['nmatches'].astype(int).sum()
        total_runtime = group['runtime'].sum()
        avg_runtime_per_table = total_runtime / num_tables
        
        # MRR：加权平均（按ground truth数量加权）
        weights = group['nmatches'].values
        weighted_mrr = weighted_average(group['mrr'].values, weights)
        weighted_recall_at_20 = weighted_average(group['Recall@20'].values, weights)
        
        # All 类指标：重新计算
        all_precision, all_recall, all_f1 = aggregate_precision_recall_f1(
            group, 'All_Precision', 'All_Recall'
        )
        all_recall_at_gt = weighted_average(
            group['All_RecallAtSizeofGroundTruth'].values, weights
        )
        
        # One2One 类指标：重新计算
        one2one_precision, one2one_recall, one2one_f1 = aggregate_precision_recall_f1(
            group, 'One2One_Precision', 'One2One_Recall'
        )
        one2one_recall_at_gt = weighted_average(
            group['One2One_RecallAtSizeofGroundTruth'].values, weights
        )
        
        results.append({
            'source_database': database,
            'method': method,
            'num_tables': num_tables,
            'total_source_columns': total_source_cols,
            'total_target_columns': total_target_cols,
            'total_gt_matches': total_gt_matches,
            'weighted_avg_mrr': round(weighted_mrr, 4),
            'weighted_avg_recall_at_20': round(weighted_recall_at_20, 4),
            'overall_all_precision': round(all_precision, 4),
            'overall_all_recall': round(all_recall, 4),
            'overall_all_f1': round(all_f1, 4),
            'overall_all_recall_at_gt': round(all_recall_at_gt, 4),
            'overall_one2one_precision': round(one2one_precision, 4),
            'overall_one2one_recall': round(one2one_recall, 4),
            'overall_one2one_f1': round(one2one_f1, 4),
            'overall_one2one_recall_at_gt': round(one2one_recall_at_gt, 4),
            'total_runtime_seconds': round(total_runtime, 2),
            'avg_runtime_per_table': round(avg_runtime_per_table, 2)
        })
    
    return pd.DataFrame(results)


def generate_task_level_summary(df):
    """
    生成任务级汇总（整个FAA匹配任务）
    
    跨所有数据库的全局聚合
    """
    results = []
    
    for method in df['method'].unique():
        group = df[df['method'] == method]
        
        # 统计数据库数量
        num_databases = group['source_database'].nunique()
        num_tables = len(group)
        total_source_cols = group['ncols_src'].astype(int).sum()
        total_target_cols = group['ncols_tgt'].astype(int).sum()
        total_gt_matches = group['nmatches'].astype(int).sum()
        total_runtime = group['runtime'].sum()
        avg_runtime_per_table = total_runtime / num_tables
        
        # 加权聚合
        weights = group['nmatches'].values
        weighted_mrr = weighted_average(group['mrr'].values, weights)
        weighted_recall_at_20 = weighted_average(group['Recall@20'].values, weights)
        
        # All 类指标
        all_precision, all_recall, all_f1 = aggregate_precision_recall_f1(
            group, 'All_Precision', 'All_Recall'
        )
        all_recall_at_gt = weighted_average(
            group['All_RecallAtSizeofGroundTruth'].values, weights
        )
        
        # One2One 类指标
        one2one_precision, one2one_recall, one2one_f1 = aggregate_precision_recall_f1(
            group, 'One2One_Precision', 'One2One_Recall'
        )
        one2one_recall_at_gt = weighted_average(
            group['One2One_RecallAtSizeofGroundTruth'].values, weights
        )
        
        results.append({
            'benchmark': group['benchmark'].iloc[0],
            'dataset': group['dataset'].iloc[0],
            'method': method,
            'num_databases': num_databases,
            'total_tables': num_tables,
            'total_source_columns': total_source_cols,
            'total_target_columns': total_target_cols,
            'total_gt_matches': total_gt_matches,
            'weighted_avg_mrr': round(weighted_mrr, 4),
            'weighted_avg_recall_at_20': round(weighted_recall_at_20, 4),
            'overall_all_precision': round(all_precision, 4),
            'overall_all_recall': round(all_recall, 4),
            'overall_all_f1': round(all_f1, 4),
            'overall_all_recall_at_gt': round(all_recall_at_gt, 4),
            'overall_one2one_precision': round(one2one_precision, 4),
            'overall_one2one_recall': round(one2one_recall, 4),
            'overall_one2one_f1': round(one2one_f1, 4),
            'overall_one2one_recall_at_gt': round(one2one_recall_at_gt, 4),
            'total_runtime_seconds': round(total_runtime, 2),
            'total_runtime_minutes': round(total_runtime / 60, 2),
            'avg_runtime_per_table_seconds': round(avg_runtime_per_table, 2)
        })
    
    return pd.DataFrame(results)


def generate_summary(result_csv_path):
    """
    从原始结果CSV生成三层汇总
    
    Args:
        result_csv_path: 原始结果文件路径（如 faa_aviation-***.csv）
    """
    print(f"读取结果文件: {result_csv_path}")
    
    # 检查文件是否存在
    if not os.path.exists(result_csv_path):
        print(f"错误: 文件不存在 {result_csv_path}")
        return
    
    # 读取原始结果
    df = pd.read_csv(result_csv_path)
    
    print(f"\n原始数据:")
    print(f"  - 表对数量: {len(df)}")
    print(f"  - 方法数量: {df['method'].nunique()}")
    print(f"  - 数据库: {df['source_database'].unique()}")
    
    # 创建 _summary 文件夹
    result_dir = os.path.dirname(result_csv_path)
    summary_dir = os.path.join(result_dir, "_summary")
    os.makedirs(summary_dir, exist_ok=True)
    
    print(f"\n生成汇总文件夹: {summary_dir}")
    
    # 1. 表级汇总（精简版）
    print("\n[1/3] 生成表级汇总...")
    table_summary = generate_table_level_summary(df)
    table_summary_file = os.path.join(summary_dir, "table_level_summary.csv")
    table_summary.to_csv(table_summary_file, index=False)
    print(f"  [OK] 保存到: {table_summary_file}")
    print(f"  - 包含 {len(table_summary)} 条记录")
    
    # 2. 数据库级汇总
    print("\n[2/3] 生成数据库级汇总（加权聚合）...")
    database_summary = generate_database_level_summary(df)
    database_summary_file = os.path.join(summary_dir, "database_level_summary.csv")
    database_summary.to_csv(database_summary_file, index=False)
    print(f"  [OK] 保存到: {database_summary_file}")
    print(f"  - 包含 {len(database_summary)} 条记录（{len(database_summary)//len(df['method'].unique())} 个数据库 x {len(df['method'].unique())} 个方法）")
    
    # 3. 任务级汇总
    print("\n[3/3] 生成任务级汇总（全局聚合）...")
    task_summary = generate_task_level_summary(df)
    task_summary_file = os.path.join(summary_dir, "task_level_summary.csv")
    task_summary.to_csv(task_summary_file, index=False)
    print(f"  [OK] 保存到: {task_summary_file}")
    print(f"  - 包含 {len(task_summary)} 条记录（{len(df['method'].unique())} 个方法）")
    
    # 打印数据库级汇总预览
    print(f"\n" + "="*80)
    print("数据库级汇总预览:")
    print("="*80)
    print(database_summary[['source_database', 'method', 'num_tables', 'total_gt_matches', 
                            'weighted_avg_mrr', 'overall_one2one_f1']].to_string(index=False))
    
    # 打印任务级汇总预览
    print(f"\n" + "="*80)
    print("任务级汇总预览:")
    print("="*80)
    print(task_summary[['method', 'total_tables', 'total_gt_matches', 
                        'weighted_avg_mrr', 'overall_one2one_f1', 
                        'total_runtime_minutes']].to_string(index=False))
    
    print(f"\n" + "="*80)
    print("[SUCCESS] 汇总完成！")
    print("="*80)
    print(f"汇总文件保存在: {summary_dir}")


def main():
    parser = argparse.ArgumentParser(description="生成 FAA 基准测试结果的多层级汇总")
    
    parser.add_argument(
        "--result_file",
        type=str,
        help="原始结果CSV文件路径（如 results/benchmarks/faa_aviation/faa_aviation-***/faa_aviation-***.csv）",
        required=False
    )
    parser.add_argument(
        "--result_dir",
        type=str,
        help="结果目录路径（脚本会自动找到CSV文件）",
        required=False
    )
    
    args = parser.parse_args()
    
    # 确定结果文件路径
    if args.result_file:
        result_csv_path = args.result_file
    elif args.result_dir:
        # 在目录中查找主结果CSV文件（不包括单表的matches文件）
        result_dir = args.result_dir
        dir_name = os.path.basename(result_dir)
        
        # 主结果文件名应该与目录名相同
        expected_file = dir_name + ".csv"
        result_csv_path = os.path.join(result_dir, expected_file)
        
        if not os.path.exists(result_csv_path):
            # 如果找不到，尝试查找所有CSV（排除matches文件）
            csv_files = [f for f in os.listdir(result_dir) 
                        if f.endswith('.csv') 
                        and not '-matches.csv' in f 
                        and not f.startswith('_')]
            
            if len(csv_files) == 0:
                print(f"错误: 在 {result_dir} 中未找到主结果CSV文件")
                return
            elif len(csv_files) > 1:
                print(f"警告: 找到多个CSV文件，使用第一个: {csv_files[0]}")
            
            result_csv_path = os.path.join(result_dir, csv_files[0])
    else:
        print("错误: 必须提供 --result_file 或 --result_dir")
        parser.print_help()
        return
    
    # 生成汇总
    generate_summary(result_csv_path)


if __name__ == "__main__":
    main()

