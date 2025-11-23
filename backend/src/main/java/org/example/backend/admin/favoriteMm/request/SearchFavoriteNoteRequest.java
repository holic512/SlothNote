package org.example.backend.admin.favoriteMm.request;

import lombok.Data;

@Data
public class SearchFavoriteNoteRequest {
    private String q;
    private Long userId;
    private Long noteId;
    private Long favoriteFolderId;
    private Boolean favoriteStatus;
    private Boolean isDeleted;
    private int pageNum;
    private int pageSize;
}