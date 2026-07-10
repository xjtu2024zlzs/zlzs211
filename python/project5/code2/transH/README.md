# 双入口质量追溯系统

基于 TransH 的全生命周期质量追溯系统 Python 项目骨架。当前阶段仅在本地 Python 中跑通算法闭环，不包含前端、MySQL、若依微服务版集成代码或具体 FastAPI 服务实现。

## 双入口模式

业务数据按入口分离，算法流程共用。

| 入口 | 说明 |
|------|------|
| `bearing` | 轴承故障追溯：接收故障类型、故障位置、诊断置信度、RCA 文本、故障推演文本、RCA 置信度等，输出轴承生命周期阶段疑似原因 |
| `hydraulic` | 液压系统故障追溯：接收 C001-C012 部件诊断结果，输出液压系统生命周期阶段疑似原因 |

共用能力：知识图谱三元组构建、TransH 训练、Top6 疑似原因推理、综合重排序、阶段归因、原始/故障增强图谱 JSON、追溯报告 JSON。

### 液压部件（统一命名）

| 编码 | 名称 |
|------|------|
| C001 | 液压泵总成 |
| C002 | 方向控制阀 |
| C003 | 作动筒 |
| C004 | 溢流阀 |
| C005 | 蓄能器 |
| C006 | 冷却器 |
| C007 | 过滤器 |
| C008 | 液压油箱 |
| C009 | 单向阀 |
| C010 | 节流阀 |
| C011 | 管路总成（须含管路设计参数） |
| C012 | 卸荷阀 |

部件定义见 `config/domain_schema.json` 中 `hydraulic.components`。

## 当前阶段目标

1. 准备 `bearing` / `hydraulic` 业务数据与三元组 CSV
2. 构建知识图谱并导出图谱 JSON
3. 训练或加载 TransH 模型（`outputs/{domain}/models/transh_model.pt`）
4. 故障输入 → Top6 推理 → 软先验重排序 → 阶段归因 → 追溯报告 JSON

依赖安装：`pip install -r requirements.txt`。PyTorch 2.11.0 + CUDA 12.8 GPU 版需单独安装，不在 `requirements.txt` 中。

## 后续与若依微服务版集成

将本仓库算法封装为独立推理服务（HTTP / 消息队列），由若依微服务调用；若依负责权限、流程与展示，Python 侧负责图谱、TransH 与报告生成。

## 目录结构

```
data/bearing|hydraulic|common/
config/
outputs/bearing|hydraulic/{models,graphs,reports}/
scripts/
src/{data,kg,models,reasoning,utils}/
services/   # 预留，暂不实现
tests/
```

## 第一阶段运行流程

1. `src/data`：双入口数据读取与校验（含 C011 管路设计参数）
2. `src/kg`：三元组构建、原始/故障增强图谱 JSON
3. `src/models`：TransH 数据集、训练、模型存取
4. `src/reasoning`：Top6、软先验重排序（`config/fault_prior.json`）、阶段归因（`config/stage_mapping.json`）
5. `scripts`：本地一键脚本
6. `tests`：单元测试与端到端样例
