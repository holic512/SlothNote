/**
 * File Name: CommentViewPojo.java
 * Description: 评论视图对象，用于返回给前端以展示评论内容的标准类
 * Author: holic512
 * Created Date: 2024-12-22
 * Version: 1.0
 * Usage:
 * 用于封装返回给前端的评论信息，包括评论内容、评论时间、评论者信息以及评论下的回复列表等。
 */
package org.example.backend.user.note.comment.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentViewPojo {
    /**
     * 评论ID
     * 唯一标识评论的ID
     */
    Long id;

    /**
     * 评论内容
     * 用户发布的评论文本
     */
    String content;

    /**
     * 评论日期
     * 评论的创建时间
     */
    LocalDateTime date;

    /**
     * 用户ID
     * 发布评论的用户的ID
     */
    Long userId;

    /**
     * 用户名
     * 发布评论的用户的名字
     */
    String username;

    /**
     * 用户头像
     * 发布评论的用户的头像URL
     */
    String avatar;

    /**
     * 评论的回复列表
     * 包含该评论下的所有回复，可能为空
     */
    List<ReplyViewPojo> replies;

    /**
     * 构造方法
     * @param id 评论ID
     * @param content 评论内容
     * @param date 评论日期
     * @param userId 发布评论的用户ID
     */
    public CommentViewPojo(Long id, String content, LocalDateTime date, Long userId) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.userId = userId;
    }
}
