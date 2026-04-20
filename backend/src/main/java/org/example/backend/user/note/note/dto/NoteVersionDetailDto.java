package org.example.backend.user.note.note.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NoteVersionDetailDto {
    private Long id;
    private Integer versionNo;
    private String contentJson;
    private String contentPreview;
    private String sourceType;
    private LocalDateTime createdAt;
}
