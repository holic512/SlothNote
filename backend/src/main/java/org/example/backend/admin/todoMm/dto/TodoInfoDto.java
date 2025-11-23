package org.example.backend.admin.todoMm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TodoInfoDto {
    private Long id;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private Integer status;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}