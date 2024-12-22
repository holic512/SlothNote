/**
 * File Name: GetCommentService.java
 * Description: 评论服务接口，提供获取评论相关操作的方法
 * Author: holic512
 * Created Date: 2024-12-22
 * Version: 1.0
 * Usage:
 * 提供获取用户评论的服务方法，主要用于查询指定笔记下的评论内容。
 */
package org.example.backend.user.note.comment.service;

import org.example.backend.user.note.comment.dto.CommentViewPojo;

import java.util.List;

public interface GetCommentService {

    /**
     * 获取指定笔记下的所有评论
     * @param user_id 用户ID，用于权限验证和数据隔离
     * @param note_id 笔记ID，用于查询该笔记的评论
     * @return 评论列表
     */
    List<CommentViewPojo> GetComments(Long user_id, Long note_id);
}
