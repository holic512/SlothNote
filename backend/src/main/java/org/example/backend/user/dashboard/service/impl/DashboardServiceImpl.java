package org.example.backend.user.dashboard.service.impl;

import org.example.backend.common.entity.NoteInfo;
import org.example.backend.common.entity.TodoInfo;
import org.example.backend.user.dashboard.dto.DashboardOverviewDto;
import org.example.backend.user.dashboard.dto.RecentNoteDto;
import org.example.backend.user.dashboard.dto.TodoStatusCountsDto;
import org.example.backend.user.dashboard.dto.TodoWeekItemDto;
import org.example.backend.user.dashboard.service.DashboardService;
import org.example.backend.user.repository.UserFavoriteNoteInfoRepository;
import org.example.backend.user.repository.UserFolderInfoRepository;
import org.example.backend.user.repository.UserNoteInfoRepository;
import org.example.backend.user.repository.UserTodoInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final UserNoteInfoRepository noteRepo;
    private final UserFolderInfoRepository folderRepo;
    private final UserFavoriteNoteInfoRepository favRepo;
    private final UserTodoInfoRepository todoRepo;

    @Autowired
    public DashboardServiceImpl(UserNoteInfoRepository noteRepo, UserFolderInfoRepository folderRepo, UserFavoriteNoteInfoRepository favRepo, UserTodoInfoRepository todoRepo) {
        this.noteRepo = noteRepo;
        this.folderRepo = folderRepo;
        this.favRepo = favRepo;
        this.todoRepo = todoRepo;
    }

    @Override
    public DashboardOverviewDto getOverview(Long userId) {
        DashboardOverviewDto dto = new DashboardOverviewDto();
        // 简化：全库计数替代用户过滤，后续可在仓库添加 @Query 进行用户过滤
        dto.setNotesTotal(noteRepo.count());
        dto.setFoldersTotal(folderRepo.count());
        dto.setFavoritesTotal(favRepo.count());
        dto.setTodosTotal(todoRepo.count());
        // 统计完成数量（简化：遍历过滤）
        long completed = todoRepo.findAll().stream().filter(t -> t.getStatus() != null && t.getStatus() == 1 && Boolean.FALSE.equals(t.getIsDeleted())).count();
        dto.setTodosCompleted(completed);
        return dto;
    }

    @Override
    public TodoStatusCountsDto getTodoStatus(Long userId) {
        TodoStatusCountsDto dto = new TodoStatusCountsDto();
        List<TodoInfo> all = todoRepo.findAll();
        long incomplete = all.stream().filter(t -> Boolean.FALSE.equals(t.getIsDeleted()) && (t.getStatus() == null || t.getStatus() == 0)).count();
        long completed = all.stream().filter(t -> Boolean.FALSE.equals(t.getIsDeleted()) && (t.getStatus() != null && t.getStatus() == 1)).count();
        long deleted = all.stream().filter(t -> Boolean.TRUE.equals(t.getIsDeleted())).count();
        dto.setIncomplete(incomplete);
        dto.setCompleted(completed);
        dto.setDeleted(deleted);
        return dto;
    }

    @Override
    public List<TodoWeekItemDto> getTodoWeek(Long userId) {
        List<TodoWeekItemDto> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            long cnt = todoRepo.findAll().stream()
                    .filter(t -> Boolean.FALSE.equals(t.getIsDeleted()) && t.getCreatedAt() != null && t.getCreatedAt().toLocalDate().equals(d))
                    .count();
            list.add(new TodoWeekItemDto(d.format(f), cnt));
        }
        return list;
    }

    @Override
    public List<RecentNoteDto> getRecentNotes(Long userId) {
        List<NoteInfo> list = noteRepo.findAll();
        list.sort((a, b) -> {
            LocalDateTime ua = a.getUpdatedAt();
            LocalDateTime ub = b.getUpdatedAt();
            if (ua == null && ub == null) return 0;
            if (ua == null) return 1;
            if (ub == null) return -1;
            return ub.compareTo(ua);
        });
        List<RecentNoteDto> res = new ArrayList<>();
        int n = Math.min(5, list.size());
        for (int i = 0; i < n; i++) {
            NoteInfo ni = list.get(i);
            res.add(new RecentNoteDto(ni.getId(), ni.getNoteTitle(), ni.getUpdatedAt()));
        }
        return res;
    }
}