package org.example.backend.admin.noteMm.request;

import lombok.Data;

@Data
public class SearchNoteRequest {
    private String q;
    private Long userId;
    private Long folderId;
    private Integer noteType;
    private Integer isDeleted;
    private int pageNum;
    private int pageSize;
}