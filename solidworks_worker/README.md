# SolidWorks 管路建模 Worker

该本地 HTTP Worker 供平台调用，用于根据设计变量生成管路 CAD 模型文件。

## 启动方式

双击启动：

```text
start_pipe_worker.bat
```

服务地址为：

```text
http://127.0.0.1:18080/api/pipe-model
```

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
