from magneto.utils.utils import detect_column_type, get_samples

"""
ColumnEncoder 负责将“列名 + 示例取值”序列化为模型输入字符串，
不同模式强调不同信息量，便于在实验中做对比。
"""

modes = [
    "header_values_default",
    "header_values_prefix",
    "header_values_repeat",
    "header_values_verbose",
    "header_only",
    "header_values_verbose_notype",
    "header_values_columnvaluepair_notype",
    "header_header_values_repeat_notype",
    "header_values_default_notype",
]

sampling_modes = [
    "random",
    "frequent",
    "mixed",
    "weighted",
    "priority_sampling",
    "consistent_sampling",
]


class ColumnEncoder:
    def __init__(
        self,
        tokenizer,
        encoding_mode="header_values_repeat",
        sampling_mode="mixed",
        n_samples=10,
    ):
        self._tokenizer = tokenizer
        self.cls_token = getattr(tokenizer, "cls_token", "")
        self.sep_token = getattr(tokenizer, "sep_token", "")
        self.eos_token = getattr(tokenizer, "eos_token", "")

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

        if encoding_mode not in self._serialization_methods:
            raise ValueError(
                f"Unsupported encoding mode: {encoding_mode}. Supported modes are: {list(self._serialization_methods.keys())}"
            )
        if sampling_mode not in sampling_modes:
            raise ValueError(
                f"Unsupported sampling mode: {sampling_mode}. Supported modes are: {sampling_modes}"
            )

        self.encoding_mode = encoding_mode
        self.sampling_mode = sampling_mode
        self.n_samples = n_samples

    def encode(self, df, col, table_name=None):
        """
        使用指定的序列化模式，将列名、类型与样本值拼装成模型可读的字符串。
        
        Args:
            df: DataFrame
            col: 列名
            table_name: 表名（可选，用于带表名上下文的序列化）
        """
        header = col
        tokens = get_samples(df[col], n=self.n_samples, mode=self.sampling_mode)
        data_type = detect_column_type(df[col])
        
        # 如果使用表名上下文的序列化方法
        if 'with_table' in self.encoding_mode:
            return self._serialization_methods[self.encoding_mode](
                table_name, header, data_type, tokens
            )
        else:
            return self._serialization_methods[self.encoding_mode](
                header, data_type, tokens
            )

    def _serialize_header_values_verbose(self, header, data_type, tokens):
        """包含“列名、类型、示例值”三部分的详细描述。"""
        return (
            f"{self.cls_token}"
            f"Column: {header}{self.sep_token}"
            f"Type: {data_type}{self.sep_token}"
            f"Values: {self.sep_token.join(tokens)}{self.sep_token}"
        )

    def _serialize_header_values_default(self, header, data_type, tokens):
        """默认格式：使用分隔符串联列名、类型与示例值。"""
        return (
            f"{self.cls_token}"
            f"{header}{self.sep_token}"
            f"{data_type}{self.sep_token}"
            f"{self.sep_token.join(tokens)}"
        )

    def _serialize_header_values_prefix(self, header, data_type, tokens):
        """为每个字段加上显式标签（header/datatype/values），增强语义提示。"""
        return (
            f"{self.cls_token}"
            f"header:{header}{self.sep_token}"
            f"datatype:{data_type}{self.sep_token}"
            f"values:{', '.join(tokens)}"
        )

    def _serialize_header_values_repeat(self, header, data_type, tokens):
        """将列名重复多次，突出列名的重要性。"""
        repeated_header = self.sep_token.join([header] * 5)
        return (
            f"{self.cls_token}"
            f"{repeated_header}{self.sep_token}"
            f"{data_type}{self.sep_token}"
            f"{self.sep_token.join(tokens)}"
        )

    def _serialize_header_only(self, header, data_type, tokens):
        """仅保留列名，以测试“无示例值”时的效果。"""
        return f"{self.cls_token}" f"{header}" f"{self.eos_token}"

    def _serialize_header_values_verbose_notype(self, header, data_type, tokens):
        """移除类型，只保留列名 + 示例值的详细描述。"""
        return (
            f"{self.cls_token}"
            f"Column: {header}{self.sep_token}"
            f"Values: {self.sep_token.join(tokens)}{self.sep_token}"
            f"{self.eos_token}"
        )

    def _serialize_header_values_columnvaluepair_notype(
        self, header, data_type, tokens
    ):
        """把“列名:值”成对拼接，突出每个样本与列名的对应关系。"""
        tokens = [f"{header}:{token}" for token in tokens]
        return (
            f"{self.cls_token}"
            f"Column: {header}{self.sep_token}"
            f"Values: {self.sep_token.join(tokens)}{self.sep_token}"
            f"{self.eos_token}"
        )

    def _serialize_header_values_repeat_notype(self, header, data_type, tokens):
        """重复列名但不包含类型，对比类型信息的重要性。"""
        repeated_header = self.sep_token.join([header] * 5)
        return (
            f"{self.cls_token}"
            f"{repeated_header}{self.sep_token}"
            f"{data_type}{self.sep_token}"
            f"{self.sep_token.join(tokens)}"
        )

    def _serialize_header_values_default_notype(self, header, data_type, tokens):
        """最精简版本：列名加示例值，中间仅用分隔符区分。"""
        return (
            f"{self.cls_token}"
            f"{header}{self.sep_token}"
            f"{self.sep_token.join(tokens)}"
        )
    
    def _serialize_header_values_verbose_with_table(self, table_name, column_name, data_type, tokens):
        """
        带表名上下文的详细序列化（inference时使用）
        格式: Table: {table} [SEP] Column: {col} [SEP] Type: {type} [SEP] Values: ...
        """
        if table_name:
            return (
                f"{self.cls_token}"
                f"Table: {table_name}{self.sep_token}"
                f"Column: {column_name}{self.sep_token}"
                f"Type: {data_type}{self.sep_token}"
                f"Values: {self.sep_token.join(tokens)}{self.sep_token}"
            )
        else:
            # 如果没有表名，退化为普通verbose模式
            return self._serialize_header_values_verbose(column_name, data_type, tokens)
    
    def _serialize_header_values_repeat_with_table(self, table_name, column_name, data_type, tokens):
        """
        带表名上下文的重复序列化，强调表名和列名
        格式: {table}x3 [SEP] {col}x3 [SEP] Type [SEP] Values
        """
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
            # 如果没有表名，退化为普通repeat模式
            return self._serialize_header_values_repeat(column_name, data_type, tokens)
