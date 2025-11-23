package org.example.backend.admin.dashboard.dto;

import lombok.Data;

@Data
public class DashboardMetricsDto {
    private long userCount;
    private long noteCount;
    private long folderCount;
    private long commentCount;
    private long favoriteNoteCount;
    private long favoriteFolderCount;
    private long todoCount;
}