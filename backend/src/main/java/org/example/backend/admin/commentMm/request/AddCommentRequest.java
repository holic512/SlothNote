package org.example.backend.admin.commentMm.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddCommentRequest {
    @NotNull
    private Long noteId;
    @NotNull
    private Long userId;
    @NotBlank
    @Size(min = 1)
    private String content;
    private Long parentId;
}