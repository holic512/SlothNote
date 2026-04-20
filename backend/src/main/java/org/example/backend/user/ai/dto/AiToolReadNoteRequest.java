package org.example.backend.user.ai.dto;

import lombok.Data;

@Data
public class AiToolReadNoteRequest {
    private Long noteId;
    private Integer maxChars;
}
