# Project2 服务启动操作手册

本文只记录新服务器部署和启动课题二服务时的具体操作。

## 0. 路径占位说明

后文中的路径需要按服务器实际情况替换：

| 占位 | 含义 | 示例 |
| --- | --- | --- |
| `<PROJECT_ROOT>` | 项目根目录 | `D:\projects\zlzs211-clean` |
| `<SERVER_IP>` | 部署 Worker 的服务器 IP | `192.168.1.20` |
| `<PYTHON_EXE>` | Python 解释器路径 | `<PROJECT_ROOT>\.venv_project2\Scripts\python.exe` |
| `<RUNWB2_BAT>` | ANSYS Workbench 启动脚本 | `D:\Program Files\ANSYS Inc\v221\Framework\bin\Win64\runwb2.bat` |

## 1. 检查服务器基础环境

### 1.1 检查软件

在服务器上确认以下软件可以正常打开：

1. SolidWorks。
2. ANSYS Workbench。
3. ANSYS Mechanical。
4. MySQL。
5. Redis。
6. Nacos。
7. Java。
8. Maven。
9. Node.js。
10. Python 3.10 或 3.11。

### 1.2 检查软件授权

用启动服务的 Windows 用户登录服务器，手动打开：

```text
SolidWorks
ANSYS Workbench
ANSYS Mechanical
```

确认没有以下弹窗：

1. 首次启动配置。
2. 许可证异常。
3. 模板路径异常。
4. 用户目录不可写。

如果这些弹窗没有处理，后面 Worker 自动调用时容易失败。

### 1.3 检查端口

课题二需要以下端口：

| 端口 | 服务 |
| ---: | --- |
| 9721 | Python 代理模型 |
| 18080 | SolidWorks Worker |
| 18081 | ANSYS Worker |

如果 Java 后端和这些 Worker 在同一台服务器，可以只监听 `127.0.0.1`，不需要对外开放端口。

如果 Java 后端和这些 Worker 不在同一台服务器，需要在 Worker 所在服务器防火墙放行：

```text
9721
18080
18081
```

## 2. 启动 Python 代理模型服务

### 2.1 进入项目根目录

```powershell
cd <PROJECT_ROOT>
```

示例：

```powershell
cd D:\projects\zlzs211-clean
```

### 2.2 创建虚拟环境

```powershell
python -m venv .venv_project2
```

### 2.3 启用虚拟环境

```powershell
.\.venv_project2\Scripts\activate
```

### 2.4 进入代理模型目录

```powershell
cd <PROJECT_ROOT>\python\project2
```

示例：

```powershell
cd D:\projects\zlzs211-clean\python\project2
```

### 2.5 安装依赖

```powershell
pip install -r requirements.txt
```

如果失败，先执行：

```powershell
python -m pip install --upgrade pip setuptools wheel
pip install -r requirements.txt
```

### 2.6 检查模型文件

确认以下文件存在：

```text
<PROJECT_ROOT>\python\project2\models\model_registry.json
<PROJECT_ROOT>\python\project2\models\aero_pipe_kriging.pkl
```

如果缺少模型文件，需要先把模型文件复制到 `models` 目录。

### 2.7 启动代理模型服务

Java 后端和 Python 在同一台服务器时：

```powershell
uvicorn app.main:app --host 127.0.0.1 --port 9721
```

Java 后端需要跨服务器访问 Python 时：

```powershell
uvicorn app.main:app --host 0.0.0.0 --port 9721
```

### 2.8 验证代理模型服务

同服务器访问：

```text
http://127.0.0.1:9721/health
```

跨服务器访问：

```text
http://<SERVER_IP>:9721/health
```

正常返回：

```json
{
  "status": "UP",
  "service": "project2-surrogate-solver",
  "port": 9721
}
```

### 2.9 检查当前模型

访问：

```text
http://127.0.0.1:9721/api/models/active
```

需要看到当前模型指向：

```text
aero_pipe_kriging.pkl
```

### 2.10 替换代理模型

需要替换模型时执行：

1. 把新的 `.pkl` 文件放到：

```text
<PROJECT_ROOT>\python\project2\models
```

2. 修改：

```text
<PROJECT_ROOT>\python\project2\models\model_registry.json
```

把 `activeModel` 改成新的模型文件名。

3. 重启 Python 服务。

4. 访问：

```text
http://127.0.0.1:9721/api/models/active
```

确认模型已切换。

## 3. 启动 SolidWorks Worker

### 3.1 修改启动脚本

打开：

```text
<PROJECT_ROOT>\solidworks_worker\start_pipe_worker.bat
```

确认内容包含：

```bat
set PIPE_WORKER_RUN_SOLIDWORKS=1
```

### 3.2 修改 Python 启动方式

如果服务器可以直接识别 `python`，保留：

```bat
python pipe_worker.py
```

如果不能识别 `python`，改成虚拟环境 Python：

```bat
<PYTHON_EXE> pipe_worker.py
```

示例：

```bat
D:\projects\zlzs211-clean\.venv_project2\Scripts\python.exe pipe_worker.py
```

### 3.3 启动 SolidWorks Worker

```powershell
cd <PROJECT_ROOT>\solidworks_worker
.\start_pipe_worker.bat
```

启动成功后窗口应显示：

```text
Pipe SolidWorks worker listening on http://127.0.0.1:18080/api/pipe-model
```

### 3.4 检查 CAD 输出

在平台执行 CAD 建模后，检查目录：

```text
<PROJECT_ROOT>\solidworks_worker\output\task_xx
```

正常应生成：

```text
pipe_native.SLDPRT
pipe_model.step
pipe_model.x_t
pipe_model.stl
pipe_centerline.csv
pipe_preview.png
```

至少要有：

```text
pipe_model.step
pipe_centerline.csv
```

否则 ANSYS 仿真不能继续。

### 3.5 SolidWorks 失败时检查文件

进入当前任务目录：

```text
<PROJECT_ROOT>\solidworks_worker\output\task_xx
```

优先看：

```text
solidworks_native_log.txt
solidworks_vbs_stdout.txt
solidworks_vbs_stderr.txt
```

常见原因：

1. SolidWorks 未授权。
2. SolidWorks 第一次启动弹窗未处理。
3. 当前 Windows 用户无法调用 SolidWorks COM。
4. 输出目录没有写权限。
5. 未生成 `pipe_native.SLDPRT`。

## 4. 启动 ANSYS Worker

### 4.1 修改启动脚本

打开：

```text
<PROJECT_ROOT>\ansys_worker\start_ansys_worker.bat
```

必须修改：

```bat
set "ANSYS_WORKBENCH_CMD=<RUNWB2_BAT>"
```

示例：

```bat
set "ANSYS_WORKBENCH_CMD=D:\Program Files\ANSYS Inc\v221\Framework\bin\Win64\runwb2.bat"
```

不要配置为：

```text
Workbench 2022 R1.lnk
```

不要使用开始菜单快捷方式路径。

### 4.2 检查 ANSYS 参数

建议保留：

```bat
set "ANSYS_MESH_SIZE_MM=3"
set "ANSYS_KEEP_MECHANICAL_OPEN=1"
```

调试阶段建议增加：

```bat
set "ANSYS_MECHANICAL_INTERACTIVE=1"
set "ANSYS_MECHANICAL_RESULT_TIMEOUT=540"
set "ANSYS_IMAGE_EXPORT_WIDTH=1920"
set "ANSYS_IMAGE_EXPORT_HEIGHT=1080"
```

### 4.3 修改 Python 启动方式

如果服务器可以直接识别 `python`，保留：

```bat
python ansys_import_worker.py
```

如果不能识别 `python`，改成虚拟环境 Python：

```bat
<PYTHON_EXE> ansys_import_worker.py
```

示例：

```bat
D:\projects\zlzs211-clean\.venv_project2\Scripts\python.exe ansys_import_worker.py
```

### 4.4 启动 ANSYS Worker

```powershell
cd <PROJECT_ROOT>\ansys_worker
.\start_ansys_worker.bat
```

启动成功后窗口应显示：

```text
ANSYS import worker listening on http://127.0.0.1:18081/api/ansys/import-geometry
```

### 4.5 验证 ANSYS Worker

访问：

```text
http://127.0.0.1:18081/api/ansys/health
```

检查返回内容：

1. `ANSYS_WORKBENCH_CMD` 是否为新服务器路径。
2. 解析后的 Workbench 启动文件是否存在。
3. Worker 是否正常。

### 4.6 检查 ANSYS 输出

平台执行 ANSYS 仿真后，检查：

```text
<PROJECT_ROOT>\ansys_worker\output\task_xx
```

或模型子目录：

```text
<PROJECT_ROOT>\ansys_worker\output\task_xx\demo_simulation_model
<PROJECT_ROOT>\ansys_worker\output\task_xx\fsi_simulation_model
```

正常应生成：

```text
pipe_import.wbpj
mechanical_setup.py
mechanical_result.json
equivalent_stress.png
workbench_steps.txt
```

`mechanical_result.json` 中 `status` 为 `SUCCESS`，才表示 Mechanical 求解完成。

### 4.7 ANSYS 失败时检查文件

优先看：

```text
workbench_steps.txt
mechanical_trace.txt
ansys_stdout.txt
ansys_stderr.txt
progress.json
mechanical_result.json
```

判断方法：

| 现象 | 重点检查 |
| --- | --- |
| Worker 直接报不可用 | `ANSYS_WORKBENCH_CMD` 路径 |
| Workbench 打不开 | ANSYS 安装和授权 |
| 没有 Mechanical 窗口 | `ANSYS_MECHANICAL_INTERACTIVE` |
| 没有 `mechanical_result.json` | `workbench_steps.txt` 是否到 `Mechanical script started` |
| 找不到压力面 | `pipe_centerline.csv` 是否存在 |
| 图不显示 | `equivalent_stress.png` 是否生成 |

## 5. 修改 Nacos 配置

### 5.1 修改 designtask1 配置

打开 Nacos：

```text
ruoyi-designtask1-dev.yml
```

确认或加入：

```yaml
designtask:
  flowable:
    base-url: http://127.0.0.1:9308
  solidworks:
    worker-url: http://127.0.0.1:18080/api/pipe-model
  ansys:
    worker-url: http://127.0.0.1:18081/api/ansys/import-geometry

design:
  solver:
    surrogate-base-url: http://127.0.0.1:9721
```

### 5.2 同服务器部署填写方式

所有服务在同一台服务器：

```yaml
designtask:
  flowable:
    base-url: http://127.0.0.1:9308
  solidworks:
    worker-url: http://127.0.0.1:18080/api/pipe-model
  ansys:
    worker-url: http://127.0.0.1:18081/api/ansys/import-geometry

design:
  solver:
    surrogate-base-url: http://127.0.0.1:9721
```

### 5.3 分服务器部署填写方式

如果 Worker 在另一台服务器：

```yaml
designtask:
  solidworks:
    worker-url: http://<SERVER_IP>:18080/api/pipe-model
  ansys:
    worker-url: http://<SERVER_IP>:18081/api/ansys/import-geometry

design:
  solver:
    surrogate-base-url: http://<SERVER_IP>:9721
```

如果 Flowable 也在另一台服务器：

```yaml
designtask:
  flowable:
    base-url: http://<SERVER_IP>:9308
```

### 5.4 修改后重启

修改 Nacos 后，至少重启：

```text
ruoyi-designtask1
```

如果修改了网关路由，也重启：

```text
ruoyi-gateway
```

### 5.5 网关是否需要改

一般不需要给 Python、SolidWorks、ANSYS 单独加网关路由。

只需要保证前端能通过网关访问：

```text
ruoyi-designtask1
ruoyi-flowable
```

## 6. 数据库与历史文件路径

### 6.1 新任务

新服务器重新发起任务时，不需要修改历史路径。

### 6.2 旧历史任务

如果数据库里保存了旧电脑路径，例如：

```text
C:\Users\wrr\IdeaProjects\zlzs211-clean\solidworks_worker\output\...
C:\Users\wrr\IdeaProjects\zlzs211-clean\ansys_worker\output\...
```

旧任务的图片和文件可能无法显示。

解决方式二选一：

1. 把旧输出目录复制到新服务器相同路径。
2. 批量更新数据库中的文件路径为新服务器路径。

## 7. 完整启动顺序

按顺序启动：

1. MySQL。
2. Redis。
3. Nacos。
4. `ruoyi-gateway`。
5. `ruoyi-auth`。
6. `ruoyi-system`。
7. `ruoyi-flowable`。
8. `ruoyi-designtask1`。
9. Python 代理模型：

```powershell
cd <PROJECT_ROOT>\python\project2
uvicorn app.main:app --host 127.0.0.1 --port 9721
```

10. SolidWorks Worker：

```powershell
cd <PROJECT_ROOT>\solidworks_worker
.\start_pipe_worker.bat
```

11. ANSYS Worker：

```powershell
cd <PROJECT_ROOT>\ansys_worker
.\start_ansys_worker.bat
```

12. 前端 `ruoyi-ui`。

## 8. 启动后检查清单

### 8.1 Python 代理模型

访问：

```text
http://127.0.0.1:9721/health
http://127.0.0.1:9721/api/models/active
```

### 8.2 SolidWorks Worker

检查启动窗口：

```text
Pipe SolidWorks worker listening on http://127.0.0.1:18080/api/pipe-model
```

执行 CAD 建模后检查：

```text
<PROJECT_ROOT>\solidworks_worker\output\task_xx\pipe_model.step
<PROJECT_ROOT>\solidworks_worker\output\task_xx\pipe_centerline.csv
```

### 8.3 ANSYS Worker

访问：

```text
http://127.0.0.1:18081/api/ansys/health
```

执行仿真后检查：

```text
<PROJECT_ROOT>\ansys_worker\output\task_xx\...\mechanical_result.json
<PROJECT_ROOT>\ansys_worker\output\task_xx\...\equivalent_stress.png
```

### 8.4 平台页面

按顺序验证：

1. 登录平台。
2. 进入课题二任务。
3. 执行代理模型求解。
4. 执行 CAD 建模。
5. 执行 ANSYS 仿真。
6. 检查仿真结果指标和应力图。

## 9. 常见问题处理

### 9.1 代理模型不可用

按顺序检查：

1. `python/project2` 是否启动。
2. `/health` 是否能访问。
3. Nacos 中 `design.solver.surrogate-base-url` 是否正确。
4. 端口 `9721` 是否放行。
5. `model_registry.json` 中的模型文件是否存在。

### 9.2 CAD 建模失败

按顺序检查：

1. SolidWorks 是否能手动打开。
2. 当前用户是否有 SolidWorks 授权。
3. `start_pipe_worker.bat` 是否启动。
4. Nacos 中 `designtask.solidworks.worker-url` 是否正确。
5. 是否生成 `pipe_native.SLDPRT`。
6. 查看 `solidworks_native_log.txt`。

### 9.3 ANSYS Worker 不可用

按顺序检查：

1. `ANSYS_WORKBENCH_CMD` 是否指向真实 `runwb2.bat`。
2. 是否误用了 `.lnk` 快捷方式。
3. `/api/ansys/health` 是否能访问。
4. ANSYS Workbench 是否能手动打开。
5. 当前用户是否有 ANSYS 授权。
6. Nacos 中 `designtask.ansys.worker-url` 是否正确。

### 9.4 ANSYS 没有结果

按顺序检查：

1. `workbench_steps.txt` 是否包含 `Mechanical script started`。
2. 是否生成 `mechanical_result.json`。
3. 是否生成 `equivalent_stress.png`。
4. `pipe_centerline.csv` 是否存在。
5. `ANSYS_MECHANICAL_RESULT_TIMEOUT` 是否过短。

## 10. 必改配置汇总

| 文件或配置 | 必改内容 |
| --- | --- |
| `python/project2` | 建虚拟环境、装依赖、检查模型文件 |
| `solidworks_worker/start_pipe_worker.bat` | 检查 Python 路径，保持 `PIPE_WORKER_RUN_SOLIDWORKS=1` |
| `ansys_worker/start_ansys_worker.bat` | 修改 `ANSYS_WORKBENCH_CMD` |
| Nacos `ruoyi-designtask1-dev.yml` | 修改 Flowable、SolidWorks、ANSYS、代理模型地址 |
| 服务器防火墙 | 跨服务器时放行 `9721`、`18080`、`18081` |
| SolidWorks | 安装、授权、首次手动打开 |
| ANSYS | 安装、授权、确认 `runwb2.bat` 路径 |
| 输出目录 | 确认 `solidworks_worker/output`、`ansys_worker/output` 可写 |
