package org.example.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "note_version")
public class NoteVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false)
    private Long noteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "content_json", nullable = false, columnDefinition = "LONGTEXT")
    private String contentJson;

    @Column(name = "content_preview", length = 500)
    private String contentPreview;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
