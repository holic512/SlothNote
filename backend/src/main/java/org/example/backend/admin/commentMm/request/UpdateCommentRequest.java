package org.example.backend.admin.commentMm.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommentRequest {
    @NotNull
    private Long id;
    @Size(min = 1)
    private String content;
    private Boolean isDeleted;
}