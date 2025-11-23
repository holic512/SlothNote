/**
 * File Name: FavoriteNoteInfo.java
 * Description: Todo
 * Author: lv
 * Created Date: 2024-12-04
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "favorite_note_info")
public class FavoriteNoteInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 收藏记录唯一ID，自动递增

    @Column(name = "user_id", nullable = false)
    private Long userId; // 用户ID，关联users表

    @Column(name = "note_id", nullable = false)
    private Long noteId; // 笔记ID，关联note_info表

    @Column(name = "favorite_folder_id", nullable = false)
    private Long favoriteFolderId = 0L; // 收藏文件夹ID，0表示根文件夹

    @Column(name = "favorite_status", nullable = false)
    private boolean favoriteStatus = true; // 收藏状态，1表示已收藏，0表示已取消收藏

    @Column(name = "note_remark")
    private String noteRemark; // 用户对收藏笔记的备注

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 收藏时间，默认当前时间

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // 最后更新时间

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // 自动填充创建时间和更新时间
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
