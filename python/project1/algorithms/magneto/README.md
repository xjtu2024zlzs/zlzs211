# Magneto

Magneto 是一个创新框架，通过协同小型预训练语言模型（SLM）与大型语言模型（LLM），在成本可控的前提下显著提升模式匹配（Schema Matching）效果。

## 安装

可直接从 [PyPI](https://pypi.org/project/magneto-python/) 获取最新稳定版：

```
pip install magneto-python
```

## 使用示例
安装完成后，可按如下方式调用独立版本：

```python
from magneto import Magneto
import pandas as pd

source = pd.DataFrame({"column_1": ["a1", "b1", "c1"], "col_2": ["a2", "b2", "c2"]})
target = pd.DataFrame({"column_1a": ["a1", "b1", "c1"], "col2": ["a2", "b2", "c2"]})

mode = "header_values_verbose"
mag = Magneto(encoding_mode=mode)
matches = mag.get_matches(source, target)

print(matches)
```

更多示例请参见我们的 [GitHub 仓库](https://github.com/VIDA-NYU/data-integration-eval/tree/main/algorithms/magneto)。