package org.example.backend.user.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ContextNoteDto {
    private Long noteId;
    private String title;
    private String summary;
    private String icon;
}
