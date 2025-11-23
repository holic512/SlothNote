DROP TABLE IF EXISTS `comments`;
DROP TABLE IF EXISTS `favorite_note_info`;
DROP TABLE IF EXISTS `favorite_folder_info`;
DROP TABLE IF EXISTS `note_info`;
DROP TABLE IF EXISTS `folder_info`;
DROP TABLE IF EXISTS `todo_info`;
DROP TABLE IF EXISTS `todo_category`;
DROP TABLE IF EXISTS `user_profiles`;
DROP TABLE IF EXISTS `admins`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键，用户ID',
    `uid`         VARCHAR(50)  NOT NULL COMMENT '业务唯一UID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255) NOT NULL COMMENT '密码Hash',
    `email`       VARCHAR(100) NOT NULL COMMENT '邮箱',
    `status`      INT          NOT NULL COMMENT '用户状态码',
    `has_profile` INT COMMENT '是否已完善资料',
    `is_deleted`  INT          NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_uid` (`uid`),
    UNIQUE KEY `uk_users_username` (`username`),
    UNIQUE KEY `uk_users_email` (`email`),
    KEY `idx_users_status_is_deleted` (`status`, `is_deleted`),
    KEY `idx_users_created_at` (`created_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户表';

CREATE TABLE `admins`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键，管理员ID',
    `username`   VARCHAR(50)  NOT NULL COMMENT '管理员用户名',
    `password`   VARCHAR(255) NOT NULL COMMENT '密码Hash',
    `email`      VARCHAR(100) NOT NULL COMMENT '邮箱',
    `is_deleted` INT          NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_admins_username` (`username`),
    UNIQUE KEY `uk_admins_email` (`email`),
    KEY `idx_admins_created_at` (`created_at`),
    KEY `idx_admins_is_deleted` (`is_deleted`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='管理员表';

CREATE TABLE `folder_info`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键，文件夹ID',
    `user_id`       BIGINT       NOT NULL COMMENT '用户ID',
    `folder_name`   VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    `parent_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '父文件夹ID，0为根',
    `description`   VARCHAR(255) COMMENT '文件夹简介',
    `folder_avatar` CHAR(4) COMMENT '文件夹头像emoji',
    `is_deleted`    INT          NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_folder_user_id` (`user_id`),
    KEY `idx_folder_parent_id` (`parent_id`),
    KEY `idx_folder_user_deleted` (`user_id`, `is_deleted`),
    KEY `idx_folder_parent_deleted` (`parent_id`, `is_deleted`),
    CONSTRAINT `fk_folder_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='文件夹表';

CREATE TABLE `note_info`
(
    `id`             BIGINT   NOT NULL AUTO_INCREMENT COMMENT '自增主键，笔记ID',
    `user_id`        BIGINT   NOT NULL COMMENT '用户ID',
    `folder_id`      BIGINT COMMENT '所属文件夹ID',
    `note_title`     VARCHAR(255) COMMENT '笔记标题',
    `note_summary`   VARCHAR(255) COMMENT '笔记摘要',
    `note_avatar`    CHAR(4) COMMENT '笔记头像emoji',
    `note_cover_url` VARCHAR(255) COMMENT '封面URL',
    `note_password`  VARCHAR(255) COMMENT '访问密码',
    `note_type`      INT      NOT NULL COMMENT '笔记类型',
    `is_deleted`     INT      NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_note_user_id` (`user_id`),
    KEY `idx_note_folder_id` (`folder_id`),
    KEY `idx_note_user_deleted` (`user_id`, `is_deleted`),
    KEY `idx_note_folder_deleted` (`folder_id`, `is_deleted`),
    KEY `idx_note_type` (`note_type`),
    CONSTRAINT `fk_note_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_note_folder` FOREIGN KEY (`folder_id`) REFERENCES `folder_info` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='笔记表';

CREATE TABLE `favorite_folder_info`
(
    `id`                          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键，收藏文件夹ID',
    `user_id`                     BIGINT       NOT NULL COMMENT '用户ID',
    `folder_name`                 VARCHAR(255) NOT NULL COMMENT '收藏文件夹名称',
    `parent_id`                   BIGINT       NOT NULL DEFAULT 0 COMMENT '父文件夹ID，0为根',
    `favorite_folder_description` VARCHAR(255) COMMENT '收藏文件夹简介',
    `is_deleted`                  TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at`                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`                  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_fav_folder_user_id` (`user_id`),
    KEY `idx_fav_folder_parent_id` (`parent_id`),
    KEY `idx_fav_folder_user_deleted` (`user_id`, `is_deleted`),
    CONSTRAINT `fk_fav_folder_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='收藏文件夹表';

CREATE TABLE `favorite_note_info`
(
    `id`                 BIGINT     NOT NULL AUTO_INCREMENT COMMENT '自增主键，收藏记录ID',
    `user_id`            BIGINT     NOT NULL COMMENT '用户ID',
    `note_id`            BIGINT     NOT NULL COMMENT '笔记ID',
    `favorite_folder_id` BIGINT     NOT NULL DEFAULT 0 COMMENT '所属收藏文件夹ID',
    `favorite_status`    TINYINT(1) NOT NULL DEFAULT 1 COMMENT '收藏状态，1已收藏，0取消',
    `note_remark`        VARCHAR(255) COMMENT '收藏备注',
    `is_deleted`         TINYINT(1) NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at`         DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_fav_note_user_id` (`user_id`),
    KEY `idx_fav_note_note_id` (`note_id`),
    KEY `idx_fav_note_folder_id` (`favorite_folder_id`),
    KEY `idx_fav_note_user_status` (`user_id`, `favorite_status`),
    KEY `idx_fav_note_user_deleted` (`user_id`, `is_deleted`),
    CONSTRAINT `fk_fav_note_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_fav_note_note` FOREIGN KEY (`note_id`) REFERENCES `note_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='收藏笔记表';

CREATE TABLE `todo_category`
(
    `id`         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '自增主键，分类ID',
    `user_id`    BIGINT           NOT NULL COMMENT '用户ID',
    `type`       INT              NOT NULL COMMENT '分类类型',
    `name`       VARCHAR(255)     NOT NULL COMMENT '分类名称',
    `is_deleted` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_todo_category_user_id` (`user_id`),
    KEY `idx_todo_category_user_deleted_type` (`user_id`, `is_deleted`, `type`),
    CONSTRAINT `fk_todo_category_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='待办分类表';

CREATE TABLE `todo_info`
(
    `id`          BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '自增主键，待办ID',
    `user_id`     BIGINT           NOT NULL COMMENT '用户ID',
    `category_id` BIGINT UNSIGNED  NULL COMMENT '分类ID，允许为空表示未分类',
    `title`       VARCHAR(255) COMMENT '标题',
    `description` VARCHAR(255) COMMENT '描述',
    `start_date`  DATETIME COMMENT '开始时间',
    `due_date`    DATETIME COMMENT '截止时间',
    `status`      INT                       DEFAULT 0 COMMENT '状态',
    `is_deleted`  TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_todo_info_user_id` (`user_id`),
    KEY `idx_todo_info_category_id` (`category_id`),
    KEY `idx_todo_info_user_deleted_status` (`user_id`, `is_deleted`, `status`),
    KEY `idx_todo_info_category_deleted` (`category_id`, `is_deleted`),
    CONSTRAINT `fk_todo_info_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_todo_info_category` FOREIGN KEY (`category_id`) REFERENCES `todo_category` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='待办信息表';

CREATE TABLE `user_profiles`
(
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '自增主键，资料ID',
    `user_id`      BIGINT       NOT NULL COMMENT '用户ID，一对一',
    `nickname`     VARCHAR(255) NOT NULL COMMENT '昵称',
    `gender`       VARCHAR(255) NOT NULL COMMENT '性别',
    `age`          INT COMMENT '年龄',
    `bio`          TEXT COMMENT '个性签名/简介',
    `contact_info` VARCHAR(255) COMMENT '联系方式',
    `avatar`       VARCHAR(255) COMMENT '头像',
    `is_deleted`   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_profiles_user_id` (`user_id`),
    KEY `idx_user_profiles_nickname` (`nickname`),
    KEY `idx_user_profiles_user_deleted` (`user_id`, `is_deleted`),
    CONSTRAINT `fk_user_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='用户扩展资料表';

CREATE TABLE `comments`
(
    `id`         BIGINT     NOT NULL AUTO_INCREMENT COMMENT '自增主键，评论ID',
    `note_id`    BIGINT     NOT NULL COMMENT '笔记ID',
    `user_id`    BIGINT     NOT NULL COMMENT '作者用户ID',
    `content`    TEXT       NOT NULL COMMENT '评论内容',
    `parent_id`  BIGINT     NULL COMMENT '父评论ID',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '伪删除标志，0未删，1已删',
    `created_at` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_comments_note_id` (`note_id`),
    KEY `idx_comments_user_id` (`user_id`),
    KEY `idx_comments_parent_id` (`parent_id`),
    KEY `idx_comments_note_deleted` (`note_id`, `is_deleted`),
    CONSTRAINT `fk_comments_note` FOREIGN KEY (`note_id`) REFERENCES `note_info` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_comments_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_comments_parent` FOREIGN KEY (`parent_id`) REFERENCES `comments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='评论表';