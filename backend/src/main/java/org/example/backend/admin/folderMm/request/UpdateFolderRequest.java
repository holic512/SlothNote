package org.example.backend.admin.folderMm.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFolderRequest {
    @NotNull(message = "id cannot be null")
    private Long id;

    private Long userId;
    private String folderName;
    private Long parentId;
    private String description;

    @Size(max = 4, message = "folderAvatar length must be <= 4")
    private String folderAvatar;

    private Integer isDeleted;
}