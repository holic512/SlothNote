package org.example.backend.admin.commentMm.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SearchCommentRequest {
    private String q;
    private Long noteId;
    private Long userId;
    private Boolean isDeleted;
    private Boolean topLevelOnly;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Min(1)
    private Integer pageNum;
    @Min(1)
    private Integer pageSize;
}