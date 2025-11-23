package org.example.backend.admin.todoMm.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TodoCategoryDto {
    private Long id;
    private Long userId;
    private Integer type;
    private String name;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}