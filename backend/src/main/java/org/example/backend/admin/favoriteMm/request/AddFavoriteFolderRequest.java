package org.example.backend.admin.favoriteMm.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddFavoriteFolderRequest {
    @NotNull
    private Long userId;
    @NotBlank
    @Size(min = 1, max = 255)
    private String folderName;
    @NotNull
    private Long parentId;
    private String favoriteFolderDescription;
}