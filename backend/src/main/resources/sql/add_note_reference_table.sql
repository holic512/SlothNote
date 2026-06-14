CREATE TABLE IF NOT EXISTS `note_reference`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键，引用关系ID',
    `user_id`        BIGINT       NOT NULL COMMENT '用户ID',
    `source_note_id` BIGINT       NOT NULL COMMENT '发起引用的笔记ID',
    `target_note_id` BIGINT       NOT NULL COMMENT '被引用的笔记ID',
    `label_snapshot` VARCHAR(255) COMMENT '引用目标标题快照',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_note_reference_source_target` (`source_note_id`, `target_note_id`),
    KEY `idx_note_reference_user_id` (`user_id`),
    KEY `idx_note_reference_source` (`source_note_id`),
    KEY `idx_note_reference_target` (`target_note_id`),
    CONSTRAINT `fk_note_reference_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_note_reference_source` FOREIGN KEY (`source_note_id`) REFERENCES `note_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_note_reference_target` FOREIGN KEY (`target_note_id`) REFERENCES `note_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='笔记引用关系表';
