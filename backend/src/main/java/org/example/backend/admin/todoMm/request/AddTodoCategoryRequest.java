package org.example.backend.admin.todoMm.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTodoCategoryRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Integer type;
    @NotBlank
    private String name;
}