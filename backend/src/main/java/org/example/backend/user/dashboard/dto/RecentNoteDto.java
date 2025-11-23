package org.example.backend.user.dashboard.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecentNoteDto {
    private Long id;
    private String title;
    private LocalDateTime updatedAt;

    public RecentNoteDto(Long id, String title, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.updatedAt = updatedAt;
    }
}