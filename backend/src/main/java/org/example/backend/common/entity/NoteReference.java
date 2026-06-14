/**
 * @file NoteReference
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 映射笔记之间的显式引用关系，用于构建知识星图与反向链接。
 * @logic 1. sourceNoteId 表示引用发起笔记；2. targetNoteId 表示被引用笔记；3. labelSnapshot 保存引用时的标题快照。
 * @dependencies Table: note_reference, Entity: NoteInfo/User
 * @index_tags 笔记引用, 知识图谱, note_reference, 反向链接
 * @author holic512
 */
package org.example.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "note_reference")
public class NoteReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_note_id", nullable = false)
    private Long sourceNoteId;

    @Column(name = "target_note_id", nullable = false)
    private Long targetNoteId;

    @Column(name = "label_snapshot")
    private String labelSnapshot;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
