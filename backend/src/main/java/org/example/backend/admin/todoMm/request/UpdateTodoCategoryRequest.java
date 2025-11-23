package org.example.backend.admin.todoMm.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTodoCategoryRequest {
    @NotNull
    private Long id;
    private Long userId;
    private Integer type;
    private String name;
    private Boolean isDeleted;
}