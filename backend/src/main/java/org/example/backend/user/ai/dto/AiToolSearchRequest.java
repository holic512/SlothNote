package org.example.backend.user.ai.dto;

import lombok.Data;

@Data
public class AiToolSearchRequest {
    private String query;
    private Integer limit;
}
