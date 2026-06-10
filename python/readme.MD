# Python 算法服务开发说明

## 1. FastAPI 端口分配

本项目中，各课题如需调用 Python 算法，统一采用 FastAPI 进行接口封装。为避免端口冲突，各课题 FastAPI 服务端口统一分配如下：

| 课题  | 服务目录              | FastAPI 端口 |
| --- | ----------------- | ---------: |
| 课题一 | `python/project1` |       9701 |
| 课题二 | `python/project2` |       9721 |
| 课题三 | `python/project3` |       9741 |
| 课题四 | `python/project4` |       9761 |
| 课题五 | `python/project5` |       9781 |

各课题原则上使用一个 FastAPI 主服务，如有需要端口号向后沿伸。
```text
如课题五第一个端口为9781，需要使用第二个算法服务则为9782
```


---

## 2. Python 文件存放位置

所有 Python 算法服务统一放在项目主目录下的 `python` 文件夹中。

目录结构如下：

```text
项目主目录
│
├── ruoyi-gateway
├── ruoyi-auth
├── ruoyi-modules
├── ruoyi-ui
│
└── python
    ├── project1
    ├── project2
    ├── project3
    ├── project4
    └── project5
```

各课题的 Python 算法代码、FastAPI 接口代码、模型文件说明、启动脚本和依赖文件均放在对应课题目录下。

例如课题五代码应放在：

```text
python/project5
```

---

## 3. 各课题目录基本要求

每个课题目录中至少应包含以下内容：

```text
projectX
│
├── app/                  # FastAPI 接口及算法代码
├── requirements.txt      # Python 依赖文件，集成会为每个课题建立独立虚拟环境
└── README.md             # 当前课题算法服务说明
```


## 4. requirements.txt 要求

每个课题必须提供 `requirements.txt`，用于说明该课题 Python 算法运行所需依赖。

生成方式如下：

```bash
pip freeze > requirements.txt
```

部署时可通过以下命令安装依赖：

```bash
pip install -r requirements.txt
```

如果算法使用 PyTorch、TensorFlow、CUDA、OpenCV、Neo4j 等特殊依赖，应在本课题 `README.md` 中单独说明版本要求。

---

## 5. 各课题 README.md 基本内容

每个课题目录下应包含一个简单的 `README.md`，至少说明以下内容：

```text
1. 当前课题 FastAPI 服务端口
2. 启动方式
3. 主要接口路径
4. requirements.txt 依赖说明
5. 算法输入数据格式
6. 算法输出结果格式
7. 是否需要模型文件
8. 是否需要 GPU 或特殊环境
```

示例：

```text
课题五 FastAPI 服务端口：9781
健康检查接口：GET /health
算法运行接口：POST /api/project5/xxx
依赖安装：pip install -r requirements.txt
启动方式：uvicorn app.main:app --host 0.0.0.0 --port 9781
```

---

## 6. 基本约定

1. 各课题 Python 算法代码统一放在 `python/project1` 至 `python/project5` 中；
2. 各课题必须提供 `requirements.txt`；
3. 各课题应提供基本 `README.md` 说明；
4. 各课题使用指定 FastAPI 端口，不得使用默认端口；
5. 前端原则上不直接调用 Python 服务，由 RuoYi 后端统一调用 FastAPI 接口；
6. 不建议将 Python 算法代码放入 `ruoyi-ui`、`ruoyi-gateway`、`ruoyi-auth` 或 `ruoyi-modules` 中；
