package org.example.backend.admin.noteMm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoteInfoDto {
    private Long id;
    private Long userId;
    private Long folderId;
    private String noteTitle;
    private String noteSummary;
    private String noteAvatar;
    private String noteCoverUrl;
    private String notePassword;
    private Integer noteType;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}