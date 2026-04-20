package org.example.backend.user.ai.dto;

import lombok.Data;

@Data
public class StopChatRequest {
    private Long assistantMessageId;
    private Long sessionId;
}
