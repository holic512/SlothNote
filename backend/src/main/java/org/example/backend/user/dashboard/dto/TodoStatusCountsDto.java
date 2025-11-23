package org.example.backend.user.dashboard.dto;

import lombok.Data;

@Data
public class TodoStatusCountsDto {
    private Long incomplete;
    private Long completed;
    private Long deleted;
}