CREATE TABLE IF NOT EXISTS `auth_ticket`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `ticket_id`   VARCHAR(64)  NOT NULL,
    `ticket_type` VARCHAR(32)  NOT NULL,
    `subject_key` VARCHAR(128) NOT NULL,
    `code`        VARCHAR(16)  NOT NULL,
    `payload_json` TEXT        NOT NULL,
    `expire_at`   DATETIME     NOT NULL,
    `used_at`     DATETIME     NULL,
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_ticket_ticket_id` (`ticket_id`),
    KEY `idx_auth_ticket_type_subject` (`ticket_type`, `subject_key`),
    KEY `idx_auth_ticket_expire_at` (`expire_at`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_chat_session`
(
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT       NOT NULL,
    `title`           VARCHAR(255) NOT NULL,
    `last_message_at` DATETIME     NOT NULL,
    `is_deleted`      INT          NOT NULL DEFAULT 0,
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_ai_chat_session_user` (`user_id`, `is_deleted`, `last_message_at`),
    CONSTRAINT `fk_ai_chat_session_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_chat_message`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `session_id`   BIGINT       NOT NULL,
    `user_id`      BIGINT       NOT NULL,
    `role`         VARCHAR(16)  NOT NULL,
    `message_type` VARCHAR(32)  NOT NULL,
    `content_md`   LONGTEXT     NOT NULL,
    `status`       VARCHAR(16)  NOT NULL,
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_ai_chat_message_session` (`session_id`, `created_at`),
    KEY `idx_ai_chat_message_user` (`user_id`, `created_at`),
    CONSTRAINT `fk_ai_chat_message_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_ai_chat_message_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `ai_chat_session_note_ref`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `session_id` BIGINT   NOT NULL,
    `user_id`    BIGINT   NOT NULL,
    `note_id`    BIGINT   NOT NULL,
    `sort_order` INT      NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ai_chat_session_note_ref` (`session_id`, `note_id`),
    KEY `idx_ai_chat_session_note_ref_user` (`user_id`, `session_id`),
    CONSTRAINT `fk_ai_chat_session_note_ref_session` FOREIGN KEY (`session_id`) REFERENCES `ai_chat_session` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_ai_chat_session_note_ref_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_ai_chat_session_note_ref_note` FOREIGN KEY (`note_id`) REFERENCES `note_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `note_version`
(
    `id`              BIGINT    NOT NULL AUTO_INCREMENT,
    `note_id`         BIGINT    NOT NULL,
    `user_id`         BIGINT    NOT NULL,
    `version_no`      INT       NOT NULL,
    `content_json`    LONGTEXT  NOT NULL,
    `content_preview` VARCHAR(500) NULL,
    `source_type`     VARCHAR(32) NOT NULL,
    `created_at`      DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_note_version_note_no` (`note_id`, `version_no`),
    KEY `idx_note_version_note_user` (`note_id`, `user_id`, `created_at`),
    CONSTRAINT `fk_note_version_note` FOREIGN KEY (`note_id`) REFERENCES `note_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_note_version_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
