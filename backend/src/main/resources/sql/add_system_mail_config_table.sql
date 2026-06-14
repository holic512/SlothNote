CREATE TABLE IF NOT EXISTS `system_mail_config`
(
    `id`                BIGINT       NOT NULL COMMENT '单例配置主键，固定为1',
    `host`              VARCHAR(255)          DEFAULT NULL COMMENT 'SMTP服务器地址',
    `port`              INT          NOT NULL DEFAULT 465 COMMENT 'SMTP端口',
    `username`          VARCHAR(255)          DEFAULT NULL COMMENT 'SMTP登录用户名',
    `password`          VARCHAR(1024)         DEFAULT NULL COMMENT 'SMTP登录密码或授权码',
    `protocol`          VARCHAR(32)  NOT NULL DEFAULT 'smtps' COMMENT '邮件协议',
    `default_encoding`  VARCHAR(64)  NOT NULL DEFAULT 'UTF-8' COMMENT '默认编码',
    `from_address`      VARCHAR(255)          DEFAULT NULL COMMENT '发件人邮箱地址',
    `from_name`         VARCHAR(128)          DEFAULT NULL COMMENT '发件人显示名称',
    `smtp_auth`         TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用SMTP认证',
    `ssl_enable`        TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用SSL',
    `starttls_enable`   TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否启用STARTTLS',
    `starttls_required` TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '是否要求STARTTLS',
    `enabled`           TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否启用邮箱服务',
    `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='系统邮箱配置表';

INSERT INTO `system_mail_config`
(`id`, `host`, `port`, `username`, `password`, `protocol`, `default_encoding`, `from_address`, `from_name`, `smtp_auth`, `ssl_enable`, `starttls_enable`, `starttls_required`, `enabled`)
VALUES (1, NULL, 465, NULL, NULL, 'smtps', 'UTF-8', NULL, NULL, 1, 1, 1, 1, 0)
ON DUPLICATE KEY UPDATE `id` = `id`;
