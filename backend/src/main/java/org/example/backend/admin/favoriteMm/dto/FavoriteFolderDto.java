package org.example.backend.admin.favoriteMm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteFolderDto {
    private Long id;
    private Long userId;
    private String folderName;
    private Long parentId;
    private String favoriteFolderDescription;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}