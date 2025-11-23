package org.example.backend.admin.favoriteMm.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateFavoriteNoteRequest {
    @NotNull
    private Long id;
    private Long userId;
    private Long noteId;
    private Long favoriteFolderId;
    private Boolean favoriteStatus;
    private String noteRemark;
    private Boolean isDeleted;
}