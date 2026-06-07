<h2 align="center">Magneto：将小型与大型语言模型结合用于模式匹配</h2>

> 欢迎来到 Magneto！

本仓库包含论文「[Magneto: Combining Small and Large Language Models for Schema Matching](https://arxiv.org/abs/2412.08194)」的全部实现代码。

Magneto 通过协同小型预训练模型（SLM）与大型语言模型（LLM）来提升模式匹配（Schema Matching）效果：前者提供高性价比的候选检索，后者负责精排，兼顾成本与泛化性。

整体流程包含两个阶段：
- **候选检索**：使用 SLM 快速筛出少量高质量候选列，可选配 LLM 微调模型。
- **匹配重排**：调用 LLM 读取候选列的上下文语义，重新打分与排序，方便人工审核。

## 目录引导

本文档包含以下章节：

* [1. 环境准备](#gear-1-环境准备)
* [2. 代码结构](#gear-2-代码结构)
* [3. 运行示例](#gear-3-运行示例)

## :gear: 1. 环境准备

### 🔥 1.1 创建虚拟环境
建议使用虚拟环境隔离依赖，可按下述命令创建并激活：
```bash
python -m venv ./venv
source ./venv/bin/activate
```

### 🔥 1.2 安装依赖

运行下述命令安装项目依赖：
```bash
pip install -r requirements.txt
```

### 🔥 1.3 数据准备

`data` 目录存放所有模式匹配基准数据，请从 [Google Drive](https://drive.google.com/drive/folders/19kCWQI0CWHs1ZW9RQEUSeK6nuXoA-5B7?usp=sharing) 下载并解压到该目录。主要包含：
- **`gdc`**：论文中的 GDC 基准，含 10 份肿瘤分析研究数据，要与 GDC 标准映射。
- **`Valentine-datasets`**：来自 [Valentine](https://delftdata.github.io/valentine/) 的通用模式匹配基准（亦可在 [Zenodo](https://zenodo.org/records/5084605#.YOgWHBMzY-Q) 获取，DOI: 10.5281/zenodo.5084605）。
- **`synthetic`**：利用 `llm-aug` 与 `struct-aug` 生成的合成数据，可直接使用 JSON，也可依据 [代码](https://github.com/VIDA-NYU/data-integration-eval/blob/main/algorithms/magneto/finetune/data_generation/synthetic_data_gen.py) 自行再生成。处理后的唯一列数据位于 `unique_columns` 子目录。

### 🔥 1.4 下载 GDC 微调模型

若要运行 `MagnetoFT` 或 `MagnetoFTGPT`，需要从 [Google Drive](https://drive.google.com/drive/folders/1vlWaTm4rpEH4hs-Kq3mhSfTyffhDEp6P?usp=sharing) 下载已微调模型并放入 `models` 目录。

### 🔥 1.5 配置环境变量
若使用 `MagnetoGPT` 或 `MagnetoFTGPT`，需设置 `OPENAI_API_KEY`：
#### Windows
```bash
set OPENAI_API_KEY=your_openai_api_key_here
```
#### macOS/Linux
```bash
export OPENAI_API_KEY=your_api_key_here
```
如需启用 `LLaMA3.3` 作为 LLM 重排序器，请额外配置 `LLAMA_API_KEY`。

## :gear: 2. 代码结构
> 说明：批量基准及基线方法位于单独仓库 [data-harmonization-benchmark](https://github.com/VIDA-NYU/data-harmonization-benchmark)。

```bash
|-- algorithm
    |-- magneto            # Magneto 主体
        |-- finetune       # 微调流程相关代码
        |-- magneto        # 核心库
    |-- gpt_matcher        # 基于 GPT 的匹配器
        |-- gpt_matcher.py # GPT 匹配核心实现
    |-- topk_metrics.py    # Recall@TopK 指标定义
|-- experiments
    |-- ablations          # 消融实验脚本
        |-- run_bp_gdc.py                      # GDC 数据的二分图消融
        |-- run_bp_valentine.py               # Valentine 数据的二分图消融
        |-- run_encoding_sampling_ablation_gdc.py
        |-- run_encoding_sampling_ablation_valentine.py
        |-- run_multistrategy_ablation_gdc.py
        |-- run_multistrategy_ablation_valentine.py
    |-- benchmark          # 基准实验脚本（批量基线参见外部仓库）
        |-- gdc_benchmark.py                  # GDC 基准
        |-- valentine_benchmark.py            # Valentine 基准
|-- results_visualization  # 各类可视化 notebook
```

## :gear: 3. 运行示例
复现 GDC 基准结果：
```bash
python experiments/benchmarks/gdc_benchmark.py --mode [MODE] --embedding_model [EMBEDDING_MODEL] --llm_model [LLM_MODEL]
```
- `[MODE]`：序列化模式，可选 `header-value-default`、`header-value-repeat`、`header-value-verbose`。
- `[EMBEDDING_MODEL]`：检索用的预训练模型，支持 `mpnet`、`roberta`、`e5`、`arctic`、`minilm`，默认 `mpnet`。
- `[LLM_MODEL]`：重排序用 LLM，支持 `gpt-4o-mini` 或 `llama3.3-70b`。

复现 Valentine 基准：
```bash
python experiments/benchmarks/valentine_benchmark.py --mode [MODE] --dataset [DATASET]
```
其中 `[MODE]` 同 GDC，`[DATASET]` 可选：
- `chembl`
- `magellan`
- `opendata`
- `tpc`
- `wikidata`

如需微调 Magneto 的其他配置，可直接编辑对应的基准脚本。