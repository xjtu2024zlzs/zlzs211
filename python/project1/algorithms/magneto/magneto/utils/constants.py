import numpy as np
import pandas as pd

"""
定义空值、主键与二值字段的常见字符串表示，供清洗与类型推断复用。
"""

NULL_REPRESENTATIONS = {
    "not allowed to collect",
    "not reported",
    "unknown",
    "not otherwise specified",
    "nos",
    "not applicable",
    "na",
    "not available",
    "n/a",
    "none",
    "null",
    "",
    " ",
    "missing",
    "unspecified",
    "undetermined",
    "not collected",
    "not recorded",
    "not provided",
    "no data",
    "unavailable",
    "empty",
    "undefined",
    "not defined",
    "other, specify",
    "other",
    "exposure to secondhand smoke history not available",
    "exposure to secondhand smoke history not available.",
    "indeterminate",
    "staging incomplete",
    "no pathologic evidence of distant metastasis",
    "medical record does not state",
    "patient not interviewed",
    "medical record does not state.",
    "patient not interviewed.",
    None,
    np.nan,
    pd.NaT,
    pd.NA,
    pd.NaT,
}

KEY_REPRESENTATIONS = [
    "id",
    "identifier",
    "key",
    "uuid",  # 全局唯一标识符
    "gid",  # 全局 ID
    "sid",  # 系统 ID
    "token",  # 常见于鉴权上下文
    "serial",  # 可代表序列号或编码
    "code",  # 泛指识别用编码
    "hash",  # 基于哈希的唯一值
    "primary_key",  # 数据库中的主键
    "foreign_key",  # 指向其它表主键的外键
    "access_key",  # API 或服务访问密钥
    "unique_id",  # 显式说明唯一性
    "slug",  # 网站友好的路径标识
    "auth_token",  # 登录鉴权令牌
    "apikey",  # API Key 的另一种写法
    "object_id",  # 面向对象数据库常用标识
    "record_id",  # 数据集中某条记录的标识
]

BINARY_VALUES = {
    "yes",
    "no",
    "true",
    "false",
    "t",
    "f",
    "y",
    "n",
    "1",
    "0",
    "1.0",
    "0.0",
    "1.00",
    "0.00",
    "0.",
    "1.",
    "present",
    "absent",
    "positive",
    "negative",
    "detected",
    "not detected",
    "normal",
    "abnormal",
    "enabled",
    "disabled",
    "active",
    "inactive",
    "open",
    "closed",
    "success",
    "failure",
    "on",
    "off",
    "approved",
    "rejected",
    "included",
    "excluded",
    "passed",
    "failed",
    "accepted",
    "denied",
    "smoker",
    "non-smoker",
    "present",
    "not identified",
    "no or minimal exposure to secondhand smoke",
    "no or minimal exposure to secondhand smoke.",
}
