# projectp API Pull 模拟外部源系统服务

本目录是 project1 使用的外部企业源系统模拟适配器。它用 FastAPI 暴露 PLM、ERP、MES、QMS、MRO 五个 API Pull 服务，数据来自 `ry-cloud` 中的 `p1p_ext_*` MySQL 表。

## 服务定位

- 服务名称：projectp API Pull 模拟外部源系统服务
- 默认 Python 环境：`py310-magneto`
- 默认数据库：`127.0.0.1:3306/ry-cloud`
- 主要职责：模拟外部企业系统接口，供 `ruoyi-project1` 调用。
- 边界：前端不得直接请求本服务；RuoYi 后端不得绕过 API 直接读取 `p1p_ext_*` 表。

## 端口

| 系统 | 端口 | schema_key |
| --- | ---: | --- |
| PLM | 9711 | `cf_plm` |
| ERP | 9712 | `cf_erp` |
| MES | 9713 | `cf_mes` |
| QMS | 9714 | `cf_qms` |
| MRO | 9715 | `cf_mro` |

## 依赖安装

```powershell
cd E:\Desktop\zlzs211\python\projectp
conda run -n py310-magneto pip install -r requirements.txt
```

## 数据库环境变量

默认连接本机 `ry-cloud`。服务器部署时按需设置：

```powershell
$env:PROJECTP_MYSQL_HOST = "127.0.0.1"
$env:PROJECTP_MYSQL_PORT = "3306"
$env:PROJECTP_MYSQL_DATABASE = "ry-cloud"
$env:PROJECTP_MYSQL_USER = "root"
$env:PROJECTP_MYSQL_PASSWORD = "password"
```

## 启动

Windows 下不要手动并发执行多个 `conda run ... uvicorn`，否则可能抢占 Conda 临时文件，导致只有部分端口成功监听。推荐先激活环境，启动脚本会解析一次该环境的 `python.exe`，再分别后台启动五个适配器：

```powershell
conda activate py310-magneto
```

本机开发：

```powershell
cd E:\Desktop\zlzs211\python\projectp
.\start_projectp_adapters.ps1
```

服务器对外提供服务：

```powershell
.\start_projectp_adapters.ps1 -HostAddress 0.0.0.0
```

停止：

```powershell
.\stop_projectp_adapters.ps1
```

## 接口

健康检查：

```text
GET http://127.0.0.1:9711/api/health
```

读取模式：

```text
GET http://127.0.0.1:9711/api/schema
```

拉取数据：

```text
GET http://127.0.0.1:9711/api/pull?limit=500
GET http://127.0.0.1:9711/api/pull?limit=3&tables=part_master
POST http://127.0.0.1:9711/api/pull
```

POST 请求示例：

```json
{
  "request_id": "access-20260609-0001",
  "limit_per_table": 500,
  "tables": [
    {
      "source_table": "part_master",
      "columns": ["part_no", "part_cn_name"]
    }
  ]
}
```

## SQL 初始化

先在 `E:\Desktop\zlzs211\sql\project1\projectp` 执行建表和 seed 脚本，再启动服务。
