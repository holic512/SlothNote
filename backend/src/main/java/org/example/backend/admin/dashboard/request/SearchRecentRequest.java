package org.example.backend.admin.dashboard.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SearchRecentRequest {
    private String category; // user, note, folder, comment, favoriteNote, favoriteFolder, todo
    private String q;
    private Long userId;
    private Boolean isDeleted;
    @Min(1)
    private Integer pageNum;
    @Min(1)
    private Integer pageSize;
}