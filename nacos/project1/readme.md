# Project1 Nacos 配置说明

## 文件

- `DEFAULT_GROUP/ruoyi-project1-dev.yml`：课题一服务配置，数字卷宗构件已合入 `ruoyi-project1`。
- `DEFAULT_GROUP/ruoyi-gateway-dev.yml`：网关配置，数字卷宗统一通过 `/project1/dossier/**` 访问。

## 注意

- 数字卷宗不是独立服务，不需要新增 `ruoyi-dossier` 服务。
- 服务名继续使用 `ruoyi-project1`，端口继续使用 `9210`。
- 运行数据库为 `ry-cloud`，本地数据库密码已按 `123456` 配置。
- 本构件没有算法服务，因此不需要提交 Python/FastAPI 配置。
