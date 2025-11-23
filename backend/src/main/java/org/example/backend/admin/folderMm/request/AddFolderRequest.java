package org.example.backend.admin.folderMm.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddFolderRequest {
    @NotNull(message = "userId cannot be null")
    private Long userId;

    @NotBlank(message = "folderName cannot be empty")
    @Size(min = 1, max = 100, message = "folderName length must be 1-100")
    private String folderName;

    @NotNull(message = "parentId cannot be null")
    private Long parentId;

    private String description;

    @Size(max = 4, message = "folderAvatar length must be <= 4")
    private String folderAvatar;
}