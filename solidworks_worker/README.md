# SolidWorks 管路建模 Worker

该本地 HTTP Worker 供平台调用，用于根据设计变量生成管路 CAD 模型文件。

## 启动方式

双击启动：

```text
start_pipe_worker.bat
```

管路 CAD 建模服务地址为：

```text
http://127.0.0.1:18080/api/pipe-model
```

本 Worker 也提供线缆管路 SolidWorks 建模接口：

```text
POST http://127.0.0.1:18080/api/cable-routing/model
```

线缆管路求解算法已拆分到 `python/project2` 服务：

```text
GET  http://127.0.0.1:9721/api/cable-routing/algorithms
GET  http://127.0.0.1:9721/api/cable-routing/defaults
POST http://127.0.0.1:9721/api/cable-routing/solve
```

当前默认算法为 `lp_bend_3d`，入口文件为 `python/project2/pip_path/LP_Bend_3D.py`。后续新增算法时，应在 `python/project2/app/main.py` 的算法注册表中增加算法项；SolidWorks Worker 只接收已求解的 `solveResult` 并生成模型。

线缆管路默认参数与平台前端保持一致：

- 网格范围：`12 x 12 x 8`
- 网格单元格：`50 mm/格`
- 管道外径：`9.53 mm`
- 管道内径：`7.73 mm`
- 圆角弯管半径：`20 mm`
- 障碍物：3 个实心立方体
- 空间壁板：底板、左侧板、后侧板，组成半包围结构
- 管路颜色：默认红、蓝、绿三色区分

`/api/cable-routing/model` 会根据算法服务返回的路径点生成彩色预览图、空心圆角管 STL、路径 JSON 和 SolidWorks 导入脚本；在 `PIPE_WORKER_RUN_SOLIDWORKS=1` 时会继续调用 SolidWorks COM 自动化保存为同一个 `SLDPRT` 零件文件，并尝试导出 STEP。

## 输出文件

Worker 会先生成管路几何文件，然后通过 SolidWorks COM 自动化生成原生零件模型。

正常情况下会输出：

- `pipe_native.SLDPRT`
- `pipe_model.step`
- `pipe_model.x_t`
- `pipe_model.stl`
- `pipe_centerline.csv`
- `pipe_preview.png`

其中 `pipe_model.step` 是 ANSYS 当前优先使用的中性几何文件，`pipe_native.SLDPRT` 作为第二优先级兜底。
`pipe_centerline.csv` 会随 CAD 任务保存，ANSYS Worker 会用它识别管道内壁并施加内压。

## 管道截面约定

平台提交的管径参数包括：

```text
pipeDiameter / pipe_outer_diameter_mm      管道外径，单位 mm
pipeInnerDiameter / pipe_inner_diameter_mm 管道内径，单位 mm
```

SolidWorks Worker 会先按外径扫掠管路外形，再通过抽壳或内孔扫掠切除生成真实空心管。若 SolidWorks 无法生成内孔，Worker 会返回失败，不会把实心模型导出给 ANSYS。

STL 仅用于前端预览，也会按外径和内径生成空心网格；工程仿真仍以 STEP/SLDPRT 为准。

## 注意事项

`start_pipe_worker.bat` 默认设置 `PIPE_WORKER_RUN_SOLIDWORKS=1`，表示必须调用 SolidWorks 生成原生 CAD 模型。

如果 SolidWorks 没有生成 `pipe_native.SLDPRT`，Worker 会返回失败，不会把 STL 预览结果当作真正 CAD 成功。

如果 STEP 或 Parasolid 导出失败，Worker 会在日志中记录导出失败原因；后端会优先尝试 STEP，再尝试把 `SLDPRT` 传给 ANSYS。
