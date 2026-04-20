package org.example.backend.user.note.note.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NoteVersionDto {
    private Long id;
    private Integer versionNo;
    private String contentPreview;
    private String sourceType;
    private LocalDateTime createdAt;
}
