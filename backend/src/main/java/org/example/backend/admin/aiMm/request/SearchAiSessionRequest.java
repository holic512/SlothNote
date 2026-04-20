package org.example.backend.admin.aiMm.request;

import lombok.Data;

@Data
public class SearchAiSessionRequest {
    private String q;
    private Long userId;
    private String username;
    private Integer isDeleted;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
