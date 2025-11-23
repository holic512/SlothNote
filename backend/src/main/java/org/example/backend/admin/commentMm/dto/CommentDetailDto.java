package org.example.backend.admin.commentMm.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentDetailDto {
    private Long id;
    private Long noteId;
    private Long userId;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
}