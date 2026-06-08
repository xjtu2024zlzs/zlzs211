# AGENTS.md

## 基本要求

- 使用 skills 时必须说明使用了哪些 skills。
- 本项目开发必须以 RuoYi-Cloud 官方文档为准：https://doc.ruoyi.vip/ruoyi-cloud/
- 开发前必须先查看若依官方文档和本仓库现有同类模块，优先仿照若依已有实现。
- 当前功能属于大项目中的若依微服务模块，最终实现必须纳入 RuoYi-Cloud 体系。
- 原有非若依代码仅作为业务逻辑、字段、流程和页面交互参考，不作为最终工程结构。
- 不修改无关模块，不重构与当前需求无关的若依基础能力。
- mysql版本为 8.0.46

## 总体架构

- 若依 Java 后端是主业务后端，负责权限、菜单、任务管理、参数配置、状态流转、结果入库和前端接口。
- 若依 Vue 前端是唯一用户操作入口，所有页面必须集成到 `ruoyi-ui`。
- Python/FastAPI 仅作为内部算法服务，用于模式匹配、模型推理等算法能力。
- FastAPI 不作为主业务后端，不直接承载登录、权限、菜单、用户管理和业务 CRUD 页面接口。
- 前端不得直接请求 FastAPI；前端只能请求若依后端接口。
- 若依后端负责调用 FastAPI，并将算法结果转换为若依标准业务结果。

## 后端约束

- project1 后端代码放在 `ruoyi-modules/ruoyi-project1`。
- 包名使用 `com.ruoyi.project1`。
- 模块结构参照 `ruoyi-modules/ruoyi-system`。
- 服务名、Nacos 配置、网关路由统一使用 `ruoyi-project1`。
- 优先使用若依已有 common 模块、权限注解、日志注解、返回结构、分页结构和 MyBatis XML 写法。
- Controller 返回结构使用 `AjaxResult`、`TableDataInfo` 等若依标准结构。
- 权限控制使用若依权限体系，不绕过 `ruoyi-gateway`、`ruoyi-auth`、Token 和菜单权限体系。
- 如需跨服务调用，优先参考若依已有 Feign、Remote 接口或内部 HTTP 调用模式。
- 若依后端必须负责参数校验、权限校验、任务记录、状态更新、日志记录和结果封装。
- 不得把前端参数简单透传给 Python 服务后原样返回。

## 前端约束

- project1 前端页面放在 `ruoyi-ui/src/views/project1`。
- project1 接口文件放在 `ruoyi-ui/src/api/project1`。
- 页面优先仿照 `ruoyi-ui/src/views/system`、`monitor`、`tool/gen` 等现有后台页面。
- 请求必须使用 `@/utils/request`，不直接使用 axios 或 fetch。
- 权限按钮使用若依 `v-hasPermi`。
- 页面、表格、表单、弹窗、按钮、分页、权限控制等必须参照若依现有后台页面风格。
- 不引入新的 UI 库，不做脱离若依后台管理风格的界面。

## Python/FastAPI 算法服务边界

- Python 算法服务建议放在独立目录，例如 `algorithm-service`。
- Python 算法代码不得混入 `ruoyi-modules/ruoyi-project1` 的 Java 包结构中。
- Python/FastAPI 仅作为内部算法服务，不作为主业务后端。
- 若依负责用户权限、菜单页面、任务管理、状态流转、结果入库和前端接口。
- Python 只负责匹配计算、模型推理、文件解析、转换建议等算法能力。
- 前端不得直接请求 Python 服务；必须通过若依后端调用。
- 长耗时算法必须按任务方式调用，由若依记录状态、日志、异常和最终结果。
- Python 服务不得绕过若依业务层直接写核心业务表。

## 数据库与开发流程

- 业务表结构优先符合若依代码生成器习惯。
- 业务 CRUD 优先考虑若依代码生成器生成基础代码，再在生成结果上按需调整。
- 菜单、按钮权限、前端路由、后端权限标识必须保持一致。
- SQL 初始化脚本、菜单权限脚本放入 `sql` 目录，并说明执行顺序。
- 新增接口、页面、权限、菜单、SQL 时，必须保持若依命名和目录风格。
- 修改范围必须聚焦 project1 模块及其必要接入点。

## 验证要求

- 后端修改后优先执行对应模块编译或测试命令。
- 前端修改后优先执行若依前端已有 lint/build 检查。
- 如因环境缺失无法验证，必须明确说明未验证的命令和原因。