/**
 * File Name: UCommentRep.java
 * Description: 评论相关的数据库操作接口，提供对评论和回复的查询
 * Author: holic512
 * Created Date: 2024-12-22
 * Version: 1.0
 * Usage:
 * 提供对评论（Comment）和回复（Reply）的数据库查询操作，包括获取根评论、用户昵称、子评论以及笔记ID等。
 */
package org.example.backend.user.note.comment.repository;

import org.example.backend.common.entity.Comment;
import org.example.backend.user.note.comment.dto.CommentViewPojo;
import org.example.backend.user.note.comment.dto.ReplyViewPojo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface UCommentRep extends JpaRepository<Comment, Long> {

    /**
     * 获取指定笔记下的全部根评论（即没有父评论的评论），且未被删除
     * @param note_id 笔记ID
     * @return 根评论列表
     */
    @Query("select new org.example.backend.user.note.comment.dto.CommentViewPojo(c.id,c.content,c.createdAt,c.userId) from Comment c where " +
            "c.noteId = :note_id and c.parentId IS NULL and c.isDeleted = false")
    List<CommentViewPojo> findCommentViewById(@Param("note_id") Long note_id);

    /**
     * 获取指定用户ID集合下用户的昵称
     * @param userIds 用户ID集合
     * @return 用户ID与昵称的对应列表
     */
    @Query("SELECT Up.user_id,Up.nickname FROM UserProfile Up WHERE Up.user_id IN :userIds")
    List<Object[]> findUsernamesByUserIds(@Param("userIds") Set<Long> userIds);

    /**
     * 获取指定父评论ID的子评论（即回复），且未被删除
     * @param parentId 父评论ID
     * @return 子评论（回复）列表
     */
    @Query("select new org.example.backend.user.note.comment.dto.ReplyViewPojo(c.id,c.content,c.createdAt,c.userId) from Comment c where " +
            "c.parentId = :parentId and c.isDeleted = false")
    List<ReplyViewPojo> findReplyByParentId(@Param("parentId") Long parentId);

    /**
     * 获取指定评论ID对应的笔记ID
     * @param id 评论ID
     * @return 对应的笔记ID
     */
    @Query("SELECT c.noteId FROM Comment c WHERE c.id = :id")
    Long findNoteIdById(@Param("id") Long id);

}
