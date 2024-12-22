/**
 * File Name: Comment.java
 * Description: 评论实体类，用于存储用户对笔记的评论信息，支持多级评论结构。
 * 通过JPA实体映射，该类与数据库中的comments表相对应。
 * Author: holic512
 * Created Date: 2024-01-01
 * Version: 1.0
 * Usage:
 * - 通过Comment对象可以创建、查询、更新和删除评论信息
 * - 支持评论的软删除功能，通过isDeleted字段标记
 * - 支持多级评论结构，通过parentId关联父评论
 */
package org.example.backend.common.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "comments")
public class Comment {
    /**
     * 评论ID，主键，自动递增
     * 用于唯一标识每条评论
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的笔记ID
     * 表示该评论所属的笔记，关联note_info表
     */
    @Column(name = "note_id", nullable = false)
    private Long noteId;

    /**
     * 评论作者的用户ID
     * 关联到users表的主键，标识评论的创建者
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 评论内容
     * 存储用户发表的具体评论文本，使用TEXT类型支持长文本
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 父评论ID
     * 若该评论是回复其他评论，则此字段指向父评论的ID
     * 若为null则表示这是一个顶层评论
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 评论创建时间
     * 系统自动生成，不可更新
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 评论最后更新时间
     * 每次更新评论时自动更新此字段
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 软删除标记
     * true表示评论已删除，false表示评论有效
     * 默认值为false
     */
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    /**
     * 创建评论时的回调方法
     * 自动设置创建时间和更新时间为当前时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 更新评论时的回调方法
     * 自动更新最后修改时间为当前时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}