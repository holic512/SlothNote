CREATE TABLE comments
(
    id         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID，主键',
    note_id    BIGINT UNSIGNED NOT NULL COMMENT '关联笔记ID，外键，指向笔记表',
    user_id    BIGINT  NOT NULL COMMENT '关联用户ID，外键，指向用户表',
    content    TEXT   NOT NULL COMMENT '评论内容，用户发表的评论文本',
    parent_id  BIGINT UNSIGNED COMMENT '回复的父评论ID，外键，指向同表中的评论ID，如果是根评论则为NULL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评论创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '评论更新时间，每次更新时自动修改',
    is_deleted BOOLEAN   DEFAULT FALSE COMMENT '软删除标识，若为TRUE表示评论已删除，若为FALSE表示评论有效',
    FOREIGN KEY (note_id) REFERENCES test.note_info (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (parent_id) REFERENCES comments (id) -- 对应父评论
) COMMENT '用于存储用户笔记的评论';
