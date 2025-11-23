package org.example.backend.admin.noteMm.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateNoteRequest {
    @NotNull
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
}