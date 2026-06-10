from magneto import Magneto
import pandas as pd

"""
最小可运行示例：展示如何构造 DataFrame、配置 Magneto，并输出匹配结果。
"""

if __name__ == "__main__":
    # 输入示例：构造两个仅差列名的表
    source = pd.DataFrame({"column_1": ["a1", "b1", "c1"], "col_2": ["a2", "b2", "c2"]})
    target = pd.DataFrame({"column_1a": ["a1", "b1", "c1"], "col2": ["a2", "b2", "c2"]})

    # 推理配置：指定编码模式与本地/云端的检索模型路径
    mode = "header_values_verbose"
    model_path = (
        "/Users/rlopez/Downloads/mpnet-gdc-header_values_verbose-semantic-64-0.5.pth"
    )

    mag = Magneto(encoding_mode=mode, embedding_model=model_path)
    matches = mag.get_matches(source, target)

    print(matches)
