/**
 * File Name: ReplyViewPojo.java
 * Description: 回复视图对象，用于返回给前端显示回复内容的标准类
 * Author: holic512
 * Created Date: 2024-12-22
 * Version: 1.0
 * Usage:
 * 用于封装返回给前端的回复信息，包括回复内容、回复时间、回复者信息等。
 */
package org.example.backend.user.note.comment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReplyViewPojo {
    /**
     * 回复ID
     * 唯一标识回复的ID
     */
    Long id;

    /**
     * 回复内容
     * 用户发布的回复文本
     */
    String content;

    /**
     * 回复日期
     * 回复的创建时间
     */
    LocalDateTime date;

    /**
     * 用户ID
     * 发布回复的用户的ID
     */
    Long userId;

    /**
     * 用户名
     * 发布回复的用户的名字
     */
    String username;

    /**
     * 用户头像
     * 发布回复的用户的头像URL
     */
    String avatar;

    /**
     * 构造方法
     * @param id 回复ID
     * @param content 回复内容
     * @param date 回复日期
     * @param userId 发布回复的用户ID
     */
    public ReplyViewPojo(Long id, String content, LocalDateTime date, Long userId) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.userId = userId;
    }
}
