# Nacos 配置文件提交说明

各课题如新增或修改后端微服务配置，需要同步提供对应的 Nacos 配置文件。

## 1. 课题配置文件

各课题需要提供本课题对应的 `yml` 配置文件，命名格式如下：

```text
project1_dev.yml
project2_dev.yml
project3_dev.yml
project4_dev.yml
project5_dev.yml
```

例如课题三需要提供：

```text
project3_dev.yml
```

该文件中应包含本课题新增服务所需的数据库连接、Redis 配置、服务端口、服务名称、第三方接口地址、FastAPI 调用地址等配置信息。

## 2. RuoYi 默认配置文件

如果各课题修改了 RuoYi-Cloud 默认配置文件，也需要一并提供修改后的配置文件，并说明修改内容。

常见默认配置文件包括：

```text
application-dev.yml
ruoyi-gateway-dev.yml
ruoyi-auth-dev.yml
ruoyi-system-dev.yml
```

如涉及其他默认配置文件，也需要同步提交。


