package org.example.backend.admin.favoriteMm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteNoteDto {
    private Long id;
    private Long userId;
    private Long noteId;
    private Long favoriteFolderId;
    private Boolean favoriteStatus;
    private String noteRemark;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}