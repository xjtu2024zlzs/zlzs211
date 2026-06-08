"""
从FAA数据集的target表中提取唯一列和值，用于生成训练数据
输出格式包含表名元数据，支持表名上下文的序列化
"""
import json
import pandas as pd
import os
from pathlib import Path

def extract_table_name(filename):
    """
    从文件名提取表名，去掉dossier_前缀
    例如：dossier_aircraft_dossier.csv -> aircraft_dossier
    """
    table_name = filename.replace('.csv', '')
    if table_name.startswith('dossier_'):
        table_name = table_name[8:]  # 去掉 'dossier_' 前缀
    return table_name

def generate_faa_unique_columns():
    """从FAA target表生成unique_columns.json，包含表名元数据"""
    
    # 项目根目录
    project_root = Path(__file__).parent.parent.parent.parent.parent
    target_dir = project_root / "data" / "magneto_faa" / "target-tables"
    output_dir = project_root / "data" / "unique_columns"
    
    print(f"读取目标表目录: {target_dir}")
    print(f"输出目录: {output_dir}")
    
    if not target_dir.exists():
        print(f"错误: 目录不存在 {target_dir}")
        return
    
    # 新格式：包含表名元数据
    unique_columns_with_table = {}
    table_stats = []
    
    # 读取所有target表
    for filename in sorted(os.listdir(target_dir)):
        if not filename.endswith('.csv'):
            continue
            
        table_name = extract_table_name(filename)
        file_path = target_dir / filename
        
        print(f"\n处理表: {table_name}")
        
        try:
            df = pd.read_csv(file_path, low_memory=False)
            column_count = 0
            
            for column in df.columns:
                # 提取唯一值（最多50个）
                value_counts = df[column].dropna().value_counts()
                if len(value_counts) > 50:
                    value_counts = value_counts.head(50)
                
                unique_values = [str(v) for v in value_counts.index.tolist()]
                
                # 存储格式：列名为key，包含表名和值
                unique_columns_with_table[column] = {
                    "table": table_name,
                    "values": unique_values
                }
                
                column_count += 1
                print(f"  - {table_name}.{column}: {len(unique_values)} 个唯一值")
            
            table_stats.append({
                "table": table_name,
                "columns": column_count,
                "rows": len(df)
            })
            
        except Exception as e:
            print(f"  错误: 无法读取 {filename}: {e}")
            continue
    
    # 保存结果
    output_dir.mkdir(parents=True, exist_ok=True)
    output_file = output_dir / "faa_unique_columns.json"
    
    with open(output_file, "w", encoding='utf-8') as f:
        json.dump(unique_columns_with_table, f, indent=2, ensure_ascii=False)
    
    # 打印统计信息
    print("\n" + "="*60)
    print("数据提取完成!")
    print("="*60)
    print(f"\n总计:")
    print(f"  - 表数量: {len(table_stats)}")
    print(f"  - 列数量: {len(unique_columns_with_table)}")
    print(f"\n各表统计:")
    for stat in table_stats:
        print(f"  - {stat['table']}: {stat['columns']} 列, {stat['rows']} 行")
    print(f"\n输出文件: {output_file}")
    print(f"文件大小: {output_file.stat().st_size / 1024:.2f} KB")
    
    return unique_columns_with_table

if __name__ == "__main__":
    generate_faa_unique_columns()
