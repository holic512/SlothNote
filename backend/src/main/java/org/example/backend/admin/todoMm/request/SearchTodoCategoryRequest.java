package org.example.backend.admin.todoMm.request;

import lombok.Data;

@Data
public class SearchTodoCategoryRequest {
    private String q;
    private Long userId;
    private Integer type;
    private Boolean isDeleted;
    private int pageNum;
    private int pageSize;
}