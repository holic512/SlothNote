CREATE TABLE IF NOT EXISTS `system_ai_config`
(
    `id`                 BIGINT       NOT NULL COMMENT '单例配置主键，固定为1',
    `provider_name`      VARCHAR(64)  NOT NULL DEFAULT 'openai-compatible' COMMENT 'AI供应商标识',
    `base_url`           VARCHAR(500)          DEFAULT NULL COMMENT 'OpenAI兼容接口基础地址',
    `api_key`            VARCHAR(1024)         DEFAULT NULL COMMENT 'AI服务API Key',
    `model`              VARCHAR(128)          DEFAULT NULL COMMENT '模型名称',
    `temperature`        DOUBLE       NOT NULL DEFAULT 0.7 COMMENT '回答生成温度',
    `max_tokens`         INT          NOT NULL DEFAULT 4096 COMMENT '回答最大Token数',
    `planner_temperature` DOUBLE      NOT NULL DEFAULT 0.1 COMMENT '工具规划温度',
    `planner_max_tokens` INT          NOT NULL DEFAULT 256 COMMENT '工具规划最大Token数',
    `enabled`            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否启用AI服务',
    `created_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统AI配置表';

INSERT INTO `system_ai_config`
(`id`, `provider_name`, `base_url`, `api_key`, `model`, `temperature`, `max_tokens`, `planner_temperature`, `planner_max_tokens`, `enabled`)
VALUES (1, 'openai-compatible', NULL, NULL, NULL, 0.7, 4096, 0.1, 256, 0)
ON DUPLICATE KEY UPDATE `id` = `id`;
