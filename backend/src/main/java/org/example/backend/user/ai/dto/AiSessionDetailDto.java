package org.example.backend.user.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AiSessionDetailDto {
    private List<AiChatMessageDto> messages;
    private List<ContextNoteDto> contextNotes;
}
