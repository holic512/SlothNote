package org.example.backend.user.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AiToolDefinitionDto {
    private String name;
    private String description;
}
