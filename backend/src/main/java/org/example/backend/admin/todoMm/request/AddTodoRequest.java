package org.example.backend.admin.todoMm.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTodoRequest {
    @NotNull
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private Integer status;
}