# 课题四 Nacos 配置说明

本目录用于提交课题四相关的 Nacos 配置文件及说明。

## 文件说明

* `project4_nacos_config.zip`：由当前可运行环境的 Nacos 配置中心导出的配置文件，已进行脱敏处理。

## 配置来源

本配置由本地 Nacos 配置中心导出，主要涉及 RuoYi-Cloud 系统运行所需配置。课题四模块主要复用主课题已有 RuoYi-Cloud 服务配置，并在此基础上支持数据文件管理、样本增强、特征融合、故障诊断和根因分析相关功能。

## 涉及配置项

本目录配置可能包含以下内容：

* 数据库连接配置
* Redis 连接配置
* Nacos 注册中心配置
* 服务名称与服务端口
* 网关路由配置
* 文件上传路径配置
* FastAPI / Python 算法服务调用地址
* 第三方接口或算法接口调用地址

## 导入说明

请先启动 Nacos 服务，并确保主课题基础配置已导入 Nacos 配置中心。然后根据需要将 `project4_nacos_config.zip` 中的配置导入或覆盖到 Nacos 配置中心。

建议启动顺序：

1. 启动 Nacos
2. 启动 RuoYiGatewayApplication
3. 启动 RuoYiAuthApplication
4. 启动 RuoYiSystemApplication
5. 启动前端 ruoyi-ui
6. 如有 FastAPI / Python 算法服务，请在后端调用前启动对应算法服务

## 注意事项

* 本目录不上传 Nacos 安装包。
* 本目录不上传 Nacos 的 `data`、`logs` 等运行时文件。
* 配置文件中的数据库密码、Redis 密码、真实服务器地址等敏感信息已脱敏处理。
* 如需在本地运行，请将 `your_mysql_password`、`your_redis_password`、`your-fastapi-host` 等占位符替换为实际环境配置。
* 如果主课题已统一维护部分 Nacos 配置，则本目录配置仅作为课题四运行环境参考。

