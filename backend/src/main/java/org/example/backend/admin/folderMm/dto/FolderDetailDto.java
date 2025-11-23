package org.example.backend.admin.folderMm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FolderDetailDto {
    private Long id;
    private Long userId;
    private String folderName;
    private Long parentId;
    private String description;
    private String folderAvatar;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer isDeleted;
}