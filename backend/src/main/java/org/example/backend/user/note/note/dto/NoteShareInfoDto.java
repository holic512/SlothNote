package org.example.backend.user.note.note.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteShareInfoDto {
    private Long noteId;
    private String noteName;
    private List<String> noteLocation;
    private String avatar;
    private String cover;
}
