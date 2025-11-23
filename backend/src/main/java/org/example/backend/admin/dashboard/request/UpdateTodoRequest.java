package org.example.backend.admin.dashboard.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTodoRequest {
    @NotNull
    private Long id;
    private String title;
    private String description;
    private Integer status;
    private Boolean isDeleted;
}