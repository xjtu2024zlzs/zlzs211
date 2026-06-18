# 课题二 FastAPI 代理模型求解服务

## 服务信息

- 目录：`python/project2`
- 端口：`9721`
- 模型注册表：`models/model_registry.json`
- 当前模型：`models/aero_pipe_kriging.pkl`

## 启动方式

```bash
cd python/project2
pip install -r requirements.txt
uvicorn app.main:app --host 127.0.0.1 --port 9721
```

## 接口

```text
GET  /health
GET  /api/models/active
POST /api/surrogate/optimize
```

## 替换模型

将新的 `.pkl` 文件放入 `models/`，修改 `model_registry.json` 中的 `activeModel`，然后重启服务即可。
