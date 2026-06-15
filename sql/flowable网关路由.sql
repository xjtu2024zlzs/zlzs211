-- 修复 Spring Cloud Gateway 4.0 / Spring Cloud 2022 的网关配置键。
-- 旧配置 spring.cloud.gateway.server.webflux.routes 在当前版本不会生效。
UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(
    `content`,
    '    gateway:\n      server:\n        webflux:\n          discovery:\n            locator:\n              lowerCaseServiceId: true\n              enabled: true\n          routes:',
    '    gateway:\n      discovery:\n        locator:\n          lowerCaseServiceId: true\n          enabled: true\n      routes:'
)
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `content` LIKE '%gateway:%server:%webflux:%routes:%';

-- 补充设计任务服务路由。前端 /designtask/** 请求需要转发到 ruoyi-designtask1。
UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(
    `content`,
    '\n\n# 安全配置',
    '\n            # 设计任务服务\n            - id: ruoyi-designtask1\n              uri: lb://ruoyi-designtask1\n              predicates:\n                - Path=/designtask/**\n              filters:\n                - StripPrefix=1\n\n# 安全配置'
)
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `content` NOT LIKE '%id: ruoyi-designtask1%';

-- 补充工作流服务路由。工作流中心 /flowable/** 请求需要转发到 ruoyi-flowable。
UPDATE `ry-config`.`config_info`
SET `content` = REPLACE(
    `content`,
    '\n\n# 安全配置',
    '\n            # 工作流服务\n            - id: ruoyi-flowable\n              uri: lb://ruoyi-flowable\n              predicates:\n                - Path=/flowable/**\n              filters:\n                - StripPrefix=1\n\n# 安全配置'
)
WHERE `data_id` = 'ruoyi-gateway-dev.yml'
  AND `group_id` = 'DEFAULT_GROUP'
  AND `content` NOT LIKE '%id: ruoyi-flowable%';

-- 如果上面两个 UPDATE 只命中其中一个，最终 ruoyi-gateway-dev.yml 的 routes 下应至少包含：
-- - id: ruoyi-designtask1
--   uri: lb://ruoyi-designtask1
--   predicates:
--     - Path=/designtask/**
--   filters:
--     - StripPrefix=1
-- - id: ruoyi-flowable
--   uri: lb://ruoyi-flowable
--   predicates:
--     - Path=/flowable/**
--   filters:
--     - StripPrefix=1
