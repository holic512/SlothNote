package org.example.backend.user.dashboard;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.dashboard.dto.DashboardOverviewDto;
import org.example.backend.user.dashboard.dto.RecentNoteDto;
import org.example.backend.user.dashboard.dto.TodoStatusCountsDto;
import org.example.backend.user.dashboard.dto.TodoWeekItemDto;
import org.example.backend.user.dashboard.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/dashboard")
public class DashboardController {

    private final DashboardService service;

    @Autowired
    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public ResponseEntity<Object> overview() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        DashboardOverviewDto dto = service.getOverview(userId);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(dto)
                .build());
    }

    @GetMapping("/todoStatus")
    public ResponseEntity<Object> todoStatus() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        TodoStatusCountsDto dto = service.getTodoStatus(userId);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(dto)
                .build());
    }

    @GetMapping("/todoWeek")
    public ResponseEntity<Object> todoWeek() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        List<TodoWeekItemDto> list = service.getTodoWeek(userId);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(list)
                .build());
    }

    @GetMapping("/recentNotes")
    public ResponseEntity<Object> recentNotes() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        List<RecentNoteDto> list = service.getRecentNotes(userId);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(list)
                .build());
    }
}