package org.example.backend.user.note.comment.service;

/**
 * 评论服务接口
 * 定义评论相关的业务操作
 */
public interface PostCommentService {
    
    /**
     * 创建新评论
     *
     * @param noteId 笔记ID
     * @param content 评论内容
     * @param userId 用户ID
     */
    void createComment(Long noteId, String content, Long userId);

    /**
     * 回复评论
     *
     * @param parentCommentId 父评论ID（即要回复的评论ID）
     * @param content 回复内容
     * @param userId 用户ID
     */
    void replyToComment(Long parentCommentId, String content, Long userId);
} 