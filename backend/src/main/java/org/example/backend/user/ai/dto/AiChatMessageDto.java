package org.example.backend.user.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AiChatMessageDto {
    private Long id;
    private String role;
    private String messageType;
    private String content;
    private String status;
    private LocalDateTime createdAt;
}
