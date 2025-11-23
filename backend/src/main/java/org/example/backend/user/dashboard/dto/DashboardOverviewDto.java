package org.example.backend.user.dashboard.dto;

import lombok.Data;

@Data
public class DashboardOverviewDto {
    private Long notesTotal;
    private Long foldersTotal;
    private Long favoritesTotal;
    private Long todosTotal;
    private Long todosCompleted;
}