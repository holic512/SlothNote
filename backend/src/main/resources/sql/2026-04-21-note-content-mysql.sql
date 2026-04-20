CREATE TABLE IF NOT EXISTS `note_content`
(
    `note_id`       BIGINT   NOT NULL COMMENT '笔记ID，与 note_info.id 一致',
    `content`       LONGTEXT NOT NULL COMMENT '笔记正文 JSON',
    `last_saved_at` DATETIME NULL COMMENT '最后保存时间',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`note_id`),
    CONSTRAINT `fk_note_content_note` FOREIGN KEY (`note_id`) REFERENCES `note_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='笔记正文表';
