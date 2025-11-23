package org.example.backend.admin.noteMm.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddNoteRequest {
    @NotNull
    private Long userId;
    private Long folderId;
    @NotBlank
    private String noteTitle;
    private String noteSummary;
    private String noteAvatar;
    private String noteCoverUrl;
    private String notePassword;
    @NotNull
    private Integer noteType;
}