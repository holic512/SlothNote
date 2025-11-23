package org.example.backend.admin.dashboard.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTodoRequest {
    @NotNull
    private Long userId;
    private Long categoryId;
    @NotBlank
    private String title;
    private String description;
    private Integer status;
}