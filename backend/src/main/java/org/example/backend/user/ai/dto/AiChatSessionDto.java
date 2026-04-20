package org.example.backend.user.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AiChatSessionDto {
    private Long id;
    private String title;
    private LocalDateTime lastMessageAt;
}
