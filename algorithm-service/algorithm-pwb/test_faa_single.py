"""
FAA 数据集单表快速测试脚本

用途：
1. 验证 Ollama 连接是否正常
2. 快速测试单个表的匹配效果
3. 调试和验证配置

使用方法：
    python test_faa_single.py
"""

import os
import sys
import pandas as pd
import time
from pprint import pprint

# 添加项目路径
project_path = os.path.dirname(os.path.abspath(__file__))
sys.path.append(project_path)

# 设置环境变量，确保 Magneto 内部也能通过 litellm 找到正确的 Ollama 地址
os.environ["OLLAMA_API_BASE"] = os.getenv("OLLAMA_API_BASE", "http://100.73.18.78:11434")

from algorithms.magneto.magneto import Magneto
from experiments.benchmarks.benchmark_utils import (
    compute_mean_ranking_reciprocal_adjusted,
    calculate_recall_at_k,
)


def test_ollama_connection():
    """测试 Ollama 连接"""
    print("\n" + "=" * 80)
    print("🔌 测试 Ollama 连接")
    print("=" * 80)
    
    # 优先读取环境变量，便于在不同服务器/网段切换
    api_base = os.getenv("OLLAMA_API_BASE", "http://100.73.18.78:11434")
    
    try:
        from litellm import completion
        
        response = completion(
            model="ollama/llama3:8b",
            messages=[{"role": "user", "content": "Hello, just testing. Reply with OK."}],
            api_base=api_base,
            max_tokens=10,
        )
        
        print("✅ Ollama 连接成功！")
        print(f"响应: {response.choices[0].message.content}")
        return True
        
    except Exception as e:
        print(f"❌ Ollama 连接失败: {e}")
        print("\n请检查：")
        print("  1. Tailscale 是否连接")
        print("  2. 远程服务器 Ollama 是否运行")
        print(f"  3. IP 地址是否正确 ({api_base})")
        return False


def test_single_table():
    """测试单个表的匹配"""
    print("\n" + "=" * 80)
    print("🧪 测试单表匹配: plm_aircraft_item_master")
    print("=" * 80)
    
    # 数据路径
    ROOT = "data/magneto_faa"
    source_file = os.path.join(ROOT, "source-tables", "plm_aircraft_item_master.csv")
    target_file = os.path.join(ROOT, "target-tables", "dossier_aircraft_dossier.csv")
    gt_file = os.path.join(ROOT, "ground-truth", "plm_aircraft_item_master.csv")
    
    # 检查文件是否存在
    for file in [source_file, target_file, gt_file]:
        if not os.path.exists(file):
            print(f"❌ 文件不存在: {file}")
            return False
    
    # 加载数据
    print("\n📂 加载数据...")
    df_source = pd.read_csv(source_file)
    df_target = pd.read_csv(target_file)
    gt_df = pd.read_csv(gt_file)
    gt_df.dropna(inplace=True)
    ground_truth = list(gt_df.itertuples(index=False, name=None))
    
    print(f"  源表: {df_source.shape[1]} 列, {df_source.shape[0]} 行")
    print(f"  目标表: {df_target.shape[1]} 列, {df_target.shape[0]} 行")
    print(f"  Ground Truth: {len(ground_truth)} 个匹配对")
    
    # 显示 Ground Truth
    print("\n📋 Ground Truth 匹配对:")
    for i, (src_col, tgt_col) in enumerate(ground_truth[:5], 1):
        print(f"  {i}. {src_col:30s} → {tgt_col}")
    if len(ground_truth) > 5:
        print(f"  ... 还有 {len(ground_truth) - 5} 对")
    
    # ========================================
    # 测试 1: Magneto (仅嵌入检索)
    # ========================================
    print("\n" + "-" * 80)
    print("🔍 测试 1: Magneto (mpnet, 无 LLM)")
    print("-" * 80)
    
    matcher_magneto = Magneto(
        encoding_mode="header_values_default",
        embedding_model="mpnet",
    )
    
    start_time = time.time()
    matches_magneto = matcher_magneto.get_matches(df_source, df_target)
    runtime_magneto = time.time() - start_time
    
    mrr_magneto = compute_mean_ranking_reciprocal_adjusted(matches_magneto, ground_truth)
    recall20_magneto = calculate_recall_at_k(matches_magneto, ground_truth)
    metrics_magneto = matches_magneto.get_metrics(ground_truth)
    
    print(f"\n📊 Magneto 结果:")
    print(f"  MRR:        {mrr_magneto:.4f}")
    print(f"  RecallAtGT: {metrics_magneto['RecallAtSizeofGroundTruth']:.4f}")
    print(f"  Recall@20:  {recall20_magneto:.4f}")
    print(f"  运行时间:    {runtime_magneto:.2f} 秒")
    
    # 显示前5个匹配
    print("\n🎯 Top 5 匹配:")
    sorted_matches = sorted(matches_magneto.items(), key=lambda x: x[1], reverse=True)[:5]
    for i, (((_, src_col), (_, tgt_col)), score) in enumerate(sorted_matches, 1):
        is_correct = "✅" if (src_col, tgt_col) in ground_truth else "❌"
        print(f"  {i}. {score:.4f} {is_correct}  {src_col:25s} → {tgt_col}")
    
    # ========================================
    # 测试 2: MagnetoGPT (嵌入 + LLM)
    # ========================================
    print("\n" + "-" * 80)
    print("🤖 测试 2: MagnetoGPT (mpnet + llama3:8b)")
    print("-" * 80)
    
    try:
        matcher_gpt = Magneto(
            encoding_mode="header_values_default",
            embedding_model="mpnet",
            llm_model="ollama/llama3:8b",
            use_bp_reranker=False,
            use_gpt_reranker=True,
        )
        
        start_time = time.time()
        matches_gpt = matcher_gpt.get_matches(df_source, df_target)
        runtime_gpt = time.time() - start_time
        
        mrr_gpt = compute_mean_ranking_reciprocal_adjusted(matches_gpt, ground_truth)
        recall20_gpt = calculate_recall_at_k(matches_gpt, ground_truth)
        metrics_gpt = matches_gpt.get_metrics(ground_truth)
        
        print(f"\n📊 MagnetoGPT 结果:")
        print(f"  MRR:        {mrr_gpt:.4f}")
        print(f"  RecallAtGT: {metrics_gpt['RecallAtSizeofGroundTruth']:.4f}")
        print(f"  Recall@20:  {recall20_gpt:.4f}")
        print(f"  运行时间:    {runtime_gpt:.2f} 秒 ({runtime_gpt/60:.1f} 分钟)")
        
        # 显示前5个匹配
        print("\n🎯 Top 5 匹配:")
        sorted_matches_gpt = sorted(matches_gpt.items(), key=lambda x: x[1], reverse=True)[:5]
        for i, (((_, src_col), (_, tgt_col)), score) in enumerate(sorted_matches_gpt, 1):
            is_correct = "✅" if (src_col, tgt_col) in ground_truth else "❌"
            print(f"  {i}. {score:.4f} {is_correct}  {src_col:25s} → {tgt_col}")
        
        # 对比
        print("\n" + "-" * 80)
        print("📈 对比结果")
        print("-" * 80)
        print(f"  MRR 提升:        {mrr_gpt - mrr_magneto:+.4f} ({(mrr_gpt/mrr_magneto-1)*100:+.1f}%)")
        print(f"  RecallAtGT 提升: {metrics_gpt['RecallAtSizeofGroundTruth'] - metrics_magneto['RecallAtSizeofGroundTruth']:+.4f}")
        print(f"  Recall@20 提升:  {recall20_gpt - recall20_magneto:+.4f}")
        print(f"  时间增加:        {runtime_gpt - runtime_magneto:.1f} 秒 ({(runtime_gpt/runtime_magneto):.1f}x)")
        
    except Exception as e:
        print(f"❌ MagnetoGPT 测试失败: {e}")
        print("\n可能原因:")
        print("  1. Ollama 连接问题")
        print("  2. llama3:8b 模型未安装")
        print("  3. LiteLLM 配置错误")
        return False
    
    print("\n" + "=" * 80)
    print("✅ 单表测试完成！")
    print("=" * 80)
    
    return True


def main():
    print("\n" + "=" * 80)
    print("🚀 FAA 数据集快速测试")
    print("=" * 80)
    print("\n本脚本将:")
    print("  1. 测试 Ollama 连接")
    print("  2. 在单个表上运行 Magneto 和 MagnetoGPT")
    print("  3. 对比结果")
    
    # 测试 Ollama 连接
    if not test_ollama_connection():
        print("\n⚠️  Ollama 连接失败，但会继续测试 Magneto（不使用 LLM）")
        input("\n按 Enter 继续...")
    
    # 测试单表
    test_single_table()
    
    print("\n💡 提示:")
    print("  - 如果结果满意，运行完整基准测试:")
    print("    python experiments\\benchmarks\\faa_benchmark.py --embedding_model mpnet --llm_model ollama/llama3:8b")
    print("\n")


if __name__ == "__main__":
    main()

