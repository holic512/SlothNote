package org.example.backend.user.dashboard.dto;

import lombok.Data;

@Data
public class TodoWeekItemDto {
    private String date;
    private Long count;

    public TodoWeekItemDto(String date, Long count) {
        this.date = date;
        this.count = count;
    }
}