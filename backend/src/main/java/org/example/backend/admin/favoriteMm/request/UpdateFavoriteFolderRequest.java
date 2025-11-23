package org.example.backend.admin.favoriteMm.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateFavoriteFolderRequest {
    @NotNull
    private Long id;
    private Long userId;
    private String folderName;
    private Long parentId;
    private String favoriteFolderDescription;
    private Boolean isDeleted;
}