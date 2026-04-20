package org.example.backend.user.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {
    private Long sessionId;
    private String text;
    private String messageType;
    private String selectedText;
    private List<Long> contextNoteIds;
}
