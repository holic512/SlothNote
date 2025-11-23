package org.example.backend.admin.folderMm.request;

import lombok.Data;

@Data
public class SearchFolderRequest {
    private String q;
    private Integer isDeleted;
    private Long userId;
    private Long parentId;
    private int pageNum;
    private int pageSize;
}