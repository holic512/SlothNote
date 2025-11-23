package org.example.backend.user.dashboard.service;

import org.example.backend.user.dashboard.dto.DashboardOverviewDto;
import org.example.backend.user.dashboard.dto.RecentNoteDto;
import org.example.backend.user.dashboard.dto.TodoStatusCountsDto;
import org.example.backend.user.dashboard.dto.TodoWeekItemDto;

import java.util.List;

public interface DashboardService {
    DashboardOverviewDto getOverview(Long userId);
    TodoStatusCountsDto getTodoStatus(Long userId);
    List<TodoWeekItemDto> getTodoWeek(Long userId);
    List<RecentNoteDto> getRecentNotes(Long userId);
}