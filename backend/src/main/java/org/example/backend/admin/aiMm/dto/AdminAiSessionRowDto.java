package org.example.backend.admin.aiMm.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminAiSessionRowDto {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String title;
    private Integer isDeleted;
    private Integer messageCount;
    private Integer contextNoteCount;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
