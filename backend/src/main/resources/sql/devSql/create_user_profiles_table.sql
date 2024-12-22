CREATE TABLE user_profiles
(
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键，自增ID',
    user_id      BIGINT       NOT NULL UNIQUE COMMENT '唯一用户ID，外键，引用users表的id',
    nickname     VARCHAR(100) NOT NULL COMMENT '用户昵称，不能为空',
    gender       VARCHAR(50)  NOT NULL COMMENT '用户性别，枚举类型，male, female, other',
    age          INT CHECK (age >= 0)  default 0 COMMENT '用户年龄，必须大于0',
    bio          VARCHAR(255) COMMENT '个性签名，允许为空',
    contact_info VARCHAR(255) COMMENT '联系方式，允许为空',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间，记录profile的创建时间',
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间，记录最近一次修改的时间',

    CONSTRAINT fk_profiles_users_uid FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT ='用户资料表，存储用户的扩展信息';
