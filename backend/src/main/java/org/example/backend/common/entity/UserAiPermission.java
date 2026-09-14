/**
 * @file UserAiPermission
 * @project SlothNote
 * @module 用户端 / AI 权限
 * @description 保存一个用户对其全部笔记授予 AI 的全局读取与写入能力。
 * @logic 1. 以 userId 唯一关联用户；2. 默认允许读取全部笔记；3. 默认拒绝所有 AI 写入能力。
 * @dependencies JPA, SQLite: user_ai_permissions
 * @index_tags 用户AI, 全局权限, 笔记读取, 笔记写入
 * @author holic512
 */
package org.example.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_ai_permissions")
public class UserAiPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "can_read_all_notes", nullable = false)
    private Boolean canReadAllNotes = true;

    @Column(name = "can_write_note_content", nullable = false)
    private Boolean canWriteNoteContent = false;

    @Column(name = "can_write_note_title", nullable = false)
    private Boolean canWriteNoteTitle = false;

    @Column(name = "can_write_note_summary", nullable = false)
    private Boolean canWriteNoteSummary = false;

    @Column(name = "can_write_note_cover", nullable = false)
    private Boolean canWriteNoteCover = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
