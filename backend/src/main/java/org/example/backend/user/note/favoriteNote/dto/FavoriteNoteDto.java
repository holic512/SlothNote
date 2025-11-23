package org.example.backend.user.note.favoriteNote.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FavoriteNoteDto {
    private Long id;
    private Long noteId;
    private Long folderId;
    private String noteRemark;
    private LocalDateTime updatedAt;
    private String icon;
    private String title;

    public FavoriteNoteDto(Long id, Long noteId, Long folderId, String noteRemark) {
        this.id = id;
        this.noteId = noteId;
        this.folderId = folderId;
        this.noteRemark = noteRemark;
    }

    public FavoriteNoteDto(Long id, Long noteId, Long folderId, String noteRemark, LocalDateTime updatedAt, String icon, String title) {
        this.id = id;
        this.noteId = noteId;
        this.folderId = folderId;
        this.noteRemark = noteRemark;
        this.updatedAt = updatedAt;
        this.icon = icon;
        this.title = title;
    }
}