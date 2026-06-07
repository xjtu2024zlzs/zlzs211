import re

import mmh3
import numpy as np
import pandas as pd
from sklearn.feature_extraction.text import TfidfVectorizer
from valentine import MatcherResults
from dateutil.parser import parse

from magneto.utils.constants import (
    BINARY_VALUES,
    KEY_REPRESENTATIONS,
    NULL_REPRESENTATIONS,
)

"""
工具函数集合：包含列清洗、取样、类型判定、以及与 Valentine 接口的互操作。
"""

PHI_FRACTION = 0.6180339887  # φ - 1
np.random.seed(42)


def convert_to_valentine_format(matched_columns, source_table, target_table):
    valentine_format = {}
    for source_column, matches in matched_columns.items():
        for target_column, score in matches:
            key = (source_table, source_column), (target_table, target_column)
            valentine_format[key] = score
    if isinstance(valentine_format, MatcherResults):
        return valentine_format
    return MatcherResults(valentine_format)


def common_prefix(strings):
    if not strings:
        return ""

    # 将字符串排序后，只需比较首尾即可找到公共前缀
    strings.sort()
    first = strings[0]
    last = strings[-1]

    i = 0
    while i < len(first) and i < len(last) and first[i] == last[i]:
        i += 1

    return first[:i]


def common_ngrams(strings, threshold=0.3):
    most_common_ngrams = {}

    for n in range(3, 9):
        vectorizer = TfidfVectorizer(analyzer="char", ngram_range=(n, n))

        tfidf_matrix = vectorizer.fit_transform(strings)

        scores = tfidf_matrix.sum(axis=0)

        ngram_scores = [
            (ngram, scores[0, idx]) for ngram, idx in vectorizer.vocabulary_.items()
        ]

        filtered_ngrams = [ngram for ngram in ngram_scores if ngram[1] > threshold]

        most_common_ngrams[n] = sorted(
            filtered_ngrams, key=lambda x: x[1], reverse=True
        )

    return most_common_ngrams


def preprocess_string(s):
    # 移除非字母数字字符并转成小写
    return re.sub(r"[^a-zA-Z0-9]", "", s).lower()


def is_null_value(value):
    if isinstance(value, str):
        value = value.lower()
    return value in NULL_REPRESENTATIONS


def is_binary_value(value):
    if isinstance(value, str):
        value = value.lower()
    return value in BINARY_VALUES


def remove_invalid_characters(input_string):
    # 将非字母、数字和空白字符全部替换为空格
    pattern = r"[^a-zA-Z0-9\s]"
    cleaned_string = re.sub(pattern, " ", input_string)
    return cleaned_string


def split_camel_case(input_string):
    # 在驼峰命名的大小写交界处插入空格以拆分单词
    split_string = re.sub(r"(?<=[a-z])(?=[A-Z])", " ", input_string)
    return split_string


def clean_column_name(col_name):
    # 依次执行去空格、拆驼峰、转小写与清除非法字符
    col_name = col_name.strip()
    col_name = split_camel_case(col_name)
    col_name = col_name.lower()
    col_name = remove_invalid_characters(col_name)
    # 将多个连续空格压缩为单一空格
    col_name = re.sub(r"\s+", " ", col_name)
    return col_name


def clean_element(x):
    if is_null_value(x):
        return None
    if isinstance(x, str):
        val = split_camel_case(x)
        val = remove_invalid_characters(val.strip().lower())

        if val != "":
            return val
        else:
            return None
    return x


def clean_df(df):
    df = df.apply(lambda col: col.apply(clean_element))

    return df


def detect_column_type(col, key_threshold=0.8, numeric_threshold=0.90):
    # 优先尝试将列转为数值类型（整数/浮点）
    temp_col = pd.to_numeric(col, errors="coerce")
    if not temp_col.isnull().all():
        return "numerical"

    if "gene" in col.name.lower():
        # TODO：实现更鲁棒的语义识别方式
        return "gene"

    if "date" in col.name.lower():
        # TODO：实现更鲁棒的语义识别方式
        return "date"

    unique_values = col.dropna().unique()
    if len(unique_values) / len(col) > key_threshold and col.dtype not in [
        np.float64,
        np.float32,
        np.float16,
    ]:
        # 若唯一值比例极高，则视为主键
        return "key"

    if len(unique_values) == 0:
        return "unknown"

    col_name = col.name.lower()
    if any(
        col_name.startswith(rep) or col_name.endswith(rep)
        for rep in KEY_REPRESENTATIONS
    ):
        return "key"

    if col.dtype in [np.float64, np.int64]:
        return "numerical"

    numeric_unique_values = pd.Series(pd.to_numeric(unique_values, errors="coerce"))
    numeric_unique_values = numeric_unique_values.dropna()

    if not numeric_unique_values.empty:
        if len(numeric_unique_values) / len(unique_values) > numeric_threshold:
            if len(numeric_unique_values) > 2:
                return "numerical"
            else:
                unique_values_as_int = set(map(int, unique_values))
                if unique_values_as_int.issubset({0, 1}):
                    return "binary"
                else:
                    return "numerical"

    if len(unique_values) == 2 and all(is_binary_value(val) for val in unique_values):
        return "binary"
    else:
        return "categorical"

    raise ValueError(f"Could not detect type for column {col.name}")


def get_type2columns_map(df):
    # TODO：可扩展更多语义类型
    types2columns_map = {}
    types2columns_map["key"] = []
    types2columns_map["numerical"] = []
    types2columns_map["categorical"] = []
    types2columns_map["binary"] = []
    types2columns_map["gene"] = []
    types2columns_map["date"] = []
    types2columns_map["Unknown"] = []

    for col in df.columns:
        col_type = detect_column_type(df[col])
        types2columns_map[col_type].append(col)

    return types2columns_map


def fibonacci_hash(x):
    result = (x * PHI_FRACTION) % 1  # Take fractional part
    return result


def get_samples(values, n=15, mode="priority_sampling"):
    """
    根据多种策略从 pandas Series 中抽取样本。

    参数:
        values: 待抽样的 Series
        n: 返回样本数量，默认 15
        mode: 抽样策略，可选 'random'、'frequent'、'mixed'、'weighted'、
              'priority_sampling'、'consistent_sampling'
            - random: 在唯一值集合中做完全随机抽样
            - frequent: 仅保留出现频率最高的值
            - mixed: 高频样本与多样样本各取一半
            - weighted: 依据频次进行加权采样
            - priority_sampling: 用频次与哈希的组合构造优先级
            - consistent_sampling: 利用哈希输出稳定的均匀采样

    返回:
        由样本值字符串组成的列表
    """
    unique_values = values.dropna().unique()
    total_unique = len(unique_values)

    # 若唯一值数量不足 n，则直接全部返回
    if total_unique <= n:
        return sorted([str(val) for val in unique_values])

    if mode == "random":
        # 完全随机采样
        random_indices = np.random.choice(total_unique, size=n, replace=False)
        sampled_values = unique_values[random_indices]
        tokens = sorted(sampled_values)

    elif mode == "frequent":
        # 仅保留出现频次最高的值
        value_counts = values.dropna().value_counts()
        tokens = value_counts.head(n).index.tolist()
        tokens.sort()

    elif mode == "mixed":
        # 同时包含高频样本与分散样本
        n_frequent = n // 2
        value_counts = values.dropna().value_counts()
        most_frequent_values = value_counts.head(n_frequent).index.tolist()

        # 计算均匀间隔，提升样本多样性
        n_diverse = n - n_frequent
        spacing_interval = max(1, total_unique // n_diverse)
        diverse_values = unique_values[::spacing_interval][:n_diverse]

        # 合并并去重
        tokens = sorted(set(map(str, most_frequent_values + list(diverse_values))))

    elif mode == "weighted":
        # 按频次构建权重的采样
        value_counts = values.dropna().value_counts(sort=False)
        weights = value_counts / value_counts.sum()
        sampled_indices = np.random.choice(
            total_unique, size=n, replace=False, p=weights
        )
        sampled_values = unique_values[sampled_indices]
        tokens = sampled_values

    elif mode == "priority_sampling":
        value_counts = values.dropna().value_counts(sort=False)

        # 利用频次与哈希共同确定优先级
        priorities = pd.Series(
            {
                val: freq / fibonacci_hash(mmh3.hash(str(val), 42))
                for val, freq in value_counts.items()
            }
        )

        # 选择优先级最高的若干元素
        sampled_values = priorities.nlargest(n).index.tolist()
        tokens = sampled_values

    elif mode == "consistent_sampling":
        value_counts = values.dropna().value_counts(sort=False)

        priorities = pd.Series(
            {
                val: fibonacci_hash(mmh3.hash(str(val), 42))
                for val in value_counts.keys()
            }
        )

        # 依据哈希优先级挑选前 n 个值
        sampled_values = priorities.nlargest(n).index.tolist()
        tokens = sampled_values

    else:
        raise ValueError(
            f"Unsupported mode: {mode}. Use 'random', 'frequent', 'mixed','weighted', 'priority_sampling' or 'consistent_sampling'"
        )

    return [str(token) for token in tokens]


def is_date(string, fuzzy=False):
    """
    判断字符串是否可解析为日期。

    :param string: 待检测的字符串
    :param fuzzy: 若为 True，则忽略未知标记
    """
    try:
        parse(str(string), fuzzy=fuzzy)
        return True
    except Exception:
        return False
