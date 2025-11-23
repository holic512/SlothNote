package org.example.backend.admin.favoriteMm.request;

import lombok.Data;

@Data
public class SearchFavoriteFolderRequest {
    private String q;
    private Long userId;
    private Long parentId;
    private Boolean isDeleted;
    private int pageNum;
    private int pageSize;
}