package org.example.backend.admin.aiMm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.user.ai.dto.AiChatMessageDto;
import org.example.backend.user.ai.dto.ContextNoteDto;

import java.util.List;

@Data
@AllArgsConstructor
public class AdminAiSessionDetailDto {
    private AdminAiSessionRowDto session;
    private List<AiChatMessageDto> messages;
    private List<ContextNoteDto> contextNotes;
}
