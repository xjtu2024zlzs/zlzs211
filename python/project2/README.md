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
GET  /api/cable-routing/algorithms
GET  /api/cable-routing/defaults
POST /api/cable-routing/solve
```

## 线缆管路算法

当前默认算法为 `lp_bend_3d`，入口文件为：

```text
pip_path/LP_Bend_3D.py
```

算法服务统一返回平台可识别的 `paths`、长度、弯头数量和状态信息。后续新增算法时，在 `app/main.py` 的 `CABLE_ROUTING_ALGORITHMS` 注册表中增加 `value`、`module` 和 `entry`，前端会通过算法列表接口自动展示。

该算法依赖 `pulp`、`numpy`、`matplotlib`，请通过 `pip install -r requirements.txt` 统一安装。

## 替换模型

将新的 `.pkl` 文件放入 `models/`，修改 `model_registry.json` 中的 `activeModel`，然后重启服务即可。
