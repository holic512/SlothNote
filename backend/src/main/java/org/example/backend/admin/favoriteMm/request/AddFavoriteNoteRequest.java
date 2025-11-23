package org.example.backend.admin.favoriteMm.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFavoriteNoteRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long noteId;
    @NotNull
    private Long favoriteFolderId;
    private Boolean favoriteStatus;
    private String noteRemark;
}