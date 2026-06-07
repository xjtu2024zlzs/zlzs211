import os
import sys

import pandas as pd
from torch.utils.data import Dataset
from train_utils import sentence_transformer_map
from transformers import AutoTokenizer

project_path = os.getcwd()
sys.path.append(os.path.join(project_path))

from algorithms.magneto.magneto.utils.utils import (
    clean_element,
    detect_column_type,
    get_samples,
)


class CustomDataset(Dataset):
    def __init__(
        self,
        data,
        model_type="mpnet",
        serialization="header_values_verbose",
        augmentation="exact_semantic",
    ):
        self.serialization = serialization
        self.tokenizer = AutoTokenizer.from_pretrained(
            sentence_transformer_map[model_type]
        )
        self.labels = []
        self.items = self._initialize_items(data, augmentation)

        self._serialization_methods = {
            "header_values_default": self._serialize_header_values_default,
            "header_values_prefix": self._serialize_header_values_prefix,
            "header_values_repeat": self._serialize_header_values_repeat,
            "header_values_verbose": self._serialize_header_values_verbose,
            "header_only": self._serialize_header_only,
            "header_values_verbose_notype": self._serialize_header_values_verbose_notype,
            "header_values_columnvaluepair_notype": self._serialize_header_values_columnvaluepair_notype,
            "header_header_values_repeat_notype": self._serialize_header_values_repeat_notype,
            "header_values_default_notype": self._serialize_header_values_default,
            # 新增: 支持表名上下文的序列化方法
            "header_values_verbose_with_table": self._serialize_header_values_verbose_with_table,
            "header_values_repeat_with_table": self._serialize_header_values_repeat_with_table,
        }
        self.cls_token = self.tokenizer.cls_token or ""
        self.sep_token = self.tokenizer.sep_token or ""
        self.eos_token = self.tokenizer.eos_token or ""

    def _initialize_items(self, data, augmentation):
        items = []
        class_id = 0

        for column_key, column_data in data.items():
            # 提取表名（如果有）
            table_name = column_data.get("table", None)
            
            for aug_type, columns in column_data.items():
                if aug_type == "table":  # 跳过元数据字段
                    continue
                    
                if aug_type in augmentation or aug_type == "original":
                    for column_name, values in columns.items():
                        processed_column_name = (
                            column_name.rsplit("_", 1)[0]
                            if aug_type == "exact"
                            else column_name
                        )
                        values = [
                            (
                                clean_element(value)
                                if isinstance(value, str)
                                else str(value)
                            )
                            for value in values
                        ]
                        tokens = get_samples(
                            pd.Series(values), n=10, mode="priority_sampling"
                        )
                        # 将表名也传递给items
                        items.append((table_name, processed_column_name, tokens, class_id))
                        self.labels.append(class_id)
            class_id += 1

        return items

    def __len__(self):
        return len(self.items)

    def __getitem__(self, idx):
        table_name, column_name, values, class_id = self.items[idx]
        text = self._serialize(table_name, column_name, values)
        return text, class_id

    def _serialize(self, table_name, column_name, values):
        """序列化列数据，支持表名上下文"""
        if values:
            col = pd.DataFrame({column_name: values})[column_name]
            data_type = detect_column_type(col)
        else:
            data_type = "unknown"
            values = []

        tokens = [str(token) for token in values]
        
        # 检查是否使用表名上下文的序列化方法
        if 'with_table' in self.serialization:
            return self._serialization_methods[self.serialization](
                table_name, column_name, data_type, tokens
            )
        else:
            # 对于不带表名的序列化，使用列名作为header
            return self._serialization_methods[self.serialization](
                column_name, data_type, tokens
            )

    def _serialize_header_values_verbose(self, header, data_type, tokens):
        """包含列名、推断类型与样本值的详细描述。"""
        return (
            f"{self.cls_token}"
            f"Column: {header}{self.sep_token}"
            f"Type: {data_type}{self.sep_token}"
            f"Values: {self.sep_token.join(tokens)}{self.sep_token}"
        )

    def _serialize_header_values_default(self, header, data_type, tokens):
        """默认模式：列名 + 类型 + 样本值。"""
        return (
            f"{self.cls_token}"
            f"{header}{self.sep_token}"
            f"{data_type}{self.sep_token}"
            f"{self.sep_token.join(tokens)}"
        )

    def _serialize_header_values_prefix(self, header, data_type, tokens):
        """为列名、类型和值分别添加文本前缀，强化语义。"""
        return (
            f"{self.cls_token}"
            f"header:{header}{self.sep_token}"
            f"datatype:{data_type}{self.sep_token}"
            f"values:{', '.join(tokens)}"
        )

    def _serialize_header_values_repeat(self, header, data_type, tokens):
        """重复列名多次，强调列名信号。"""
        repeated_header = self.sep_token.join([header] * 5)
        return (
            f"{self.cls_token}"
            f"{repeated_header}{self.sep_token}"
            f"{data_type}{self.sep_token}"
            f"{self.sep_token.join(tokens)}"
        )

    def _serialize_header_only(self, header, data_type, tokens):
        """仅输出列名，用于评估“无值”时的表现。"""
        return f"{self.cls_token}" f"{header}" f"{self.eos_token}"

    def _serialize_header_values_verbose_notype(self, header, data_type, tokens):
        """去掉类型，仅保留列名与样本值。"""
        return (
            f"{self.cls_token}"
            f"Column: {header}{self.sep_token}"
            f"Values: {self.sep_token.join(tokens)}{self.sep_token}"
            f"{self.eos_token}"
        )

    def _serialize_header_values_columnvaluepair_notype(
        self, header, data_type, tokens
    ):
        tokens = [f"{header}:{token}" for token in tokens]
        return (
            f"{self.cls_token}"
            f"Column: {header}{self.sep_token}"
            f"Values: {self.sep_token.join(tokens)}{self.sep_token}"
            f"{self.eos_token}"
        )

    def _serialize_header_values_repeat_notype(self, header, data_type, tokens):
        """重复列名但不包含类型。"""
        repeated_header = self.sep_token.join([header] * 5)
        return (
            f"{self.cls_token}"
            f"{repeated_header}{self.sep_token}"
            f"{data_type}{self.sep_token}"
            f"{self.sep_token.join(tokens)}"
        )

    def _serialize_header_values_default_notype(self, header, data_type, tokens):
        return (
            f"{self.cls_token}"
            f"{header}{self.sep_token}"
            f"{self.sep_token.join(tokens)}"
        )
    
    def _serialize_header_values_verbose_with_table(self, table_name, column_name, data_type, tokens):
        """带表名上下文的详细序列化，用于多表场景"""
        if table_name:
            return (
                f"{self.cls_token}"
                f"Table: {table_name}{self.sep_token}"
                f"Column: {column_name}{self.sep_token}"
                f"Type: {data_type}{self.sep_token}"
                f"Values: {self.sep_token.join(tokens)}{self.sep_token}"
            )
        else:
            # 如果没有表名，退化为普通的verbose模式
            return self._serialize_header_values_verbose(column_name, data_type, tokens)
    
    def _serialize_header_values_repeat_with_table(self, table_name, column_name, data_type, tokens):
        """带表名上下文的重复序列化，强调表名和列名"""
        if table_name:
            # 重复表名和列名各3次
            repeated_table = self.sep_token.join([table_name] * 3)
            repeated_column = self.sep_token.join([column_name] * 3)
            return (
                f"{self.cls_token}"
                f"{repeated_table}{self.sep_token}"
                f"{repeated_column}{self.sep_token}"
                f"{data_type}{self.sep_token}"
                f"{self.sep_token.join(tokens)}"
            )
        else:
            # 如果没有表名，退化为普通的repeat模式
            return self._serialize_header_values_repeat(column_name, data_type, tokens)
