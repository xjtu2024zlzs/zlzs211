# project1 Python/FastAPI 算法服务

本目录是课题一 project1 的内部算法服务目录，供 RuoYi-Cloud 后端 `ruoyi-project1` 调用。前端页面不直接请求本服务。

## 服务定位

- 服务名称：project1 模式匹配算法服务
- 默认端口：`9701`
- 默认 Python 环境：`py310-magneto`
- 主要能力：根据源系统模式和卷宗目标库模式生成候选模式映射结果集。
- 若依职责：任务管理、权限、菜单、状态流转、结果入库、审核、接入执行。
- Python 职责：只做模式匹配计算，不直接写若依业务表。

## 目录结构

```text
python/project1
├─ app/                         FastAPI 服务代码
├─ algorithms/magneto/           Magneto 系列算法代码
├─ data/magneto_cf/              CF 评估和目标表辅助数据
├─ requirements.txt              Python 依赖
├─ .env.example                  环境变量示例，不包含真实密钥
├─ start_project1_algorithm.ps1  启动脚本
└─ stop_project1_algorithm.ps1   停止脚本
```

## 环境变量

复制 `.env.example` 为 `.env`，再按需填写真实配置：

```powershell
Copy-Item .env.example .env
```

常用变量：

```text
DEEPSEEK_API_KEY=
OPENAI_API_KEY=
RUOYI_MYSQL_HOST=127.0.0.1
RUOYI_MYSQL_PORT=3306
RUOYI_MYSQL_DATABASE=ry-cloud
RUOYI_MYSQL_USER=root
RUOYI_MYSQL_PASSWORD=
RUOYI_DOSSIER_TABLE_PREFIX=p1p_dossier_
```

真实 API Key 只放本机 `.env`，不要提交到 Git。

## 启动

在项目根目录执行：

```powershell
cd E:\Desktop\zlzs211\python\project1
.\start_project1_algorithm.ps1
```

如需指定环境或端口：

```powershell
.\start_project1_algorithm.ps1 -CondaEnv py310-magneto -Port 9701
```

停止服务：

```powershell
.\stop_project1_algorithm.ps1
```

## 接口

健康检查：

```text
GET http://127.0.0.1:9701/api/health
```

能力说明：

```text
GET http://127.0.0.1:9701/api/v1/capabilities
```

运行模式匹配：

```text
POST http://127.0.0.1:9701/api/v1/match/run
Content-Type: application/json
```

RuoYi 后端会传入源系统、目标卷宗库、算法参数等 JSON。本服务返回 `resultSets`，每个结果集对应一个方法和变体。

## 结果集规则

一次运行默认请求：

- `Magneto all`
- `Magneto one2one`
- `MagnetoBoost all`
- `MagnetoBoost one2one`
- `MagnetoGPT all`
- `MagnetoGPT one2one`

如果没有配置对应 LLM API Key，服务会跳过 `MagnetoGPT`，只返回 4 个候选结果集，并在 `warnings` 中给出提示。

有 Key 时返回 6 个候选结果集；无 Key 时返回 4 个候选结果集。若依后端会按实际返回数量写入 `p1p_match_result_set`、`p1p_match_result_row`、`p1p_task_metric` 和 `p1p_reviewed_match`。

## 若依调用关系

```text
ruoyi-ui
  -> /project1/matchTask/run/{taskId}
  -> ruoyi-gateway
  -> ruoyi-project1
  -> http://127.0.0.1:9701/api/v1/match/run
```

前端只调用若依 `/project1/...` 接口，不直接调用本 Python 服务。
