package org.example.backend.admin.todoMm.request;

import lombok.Data;

@Data
public class SearchTodoRequest {
    private String q;
    private Long userId;
    private Long categoryId;
    private Integer status;
    private Boolean isDeleted;
    private int pageNum;
    private int pageSize;
}