package org.example.backend.user.note.note.service;

import org.example.backend.common.domain.Note;
import org.example.backend.user.note.note.dto.NoteVersionDetailDto;
import org.example.backend.user.note.note.dto.NoteVersionDto;

import java.util.List;

public interface NoteVersionService {
    void createVersionIfChanged(Long userId, Long noteId, String contentJson, String sourceType);

    List<NoteVersionDto> listVersions(Long userId, Long noteId);

    NoteVersionDetailDto getVersionDetail(Long userId, Long noteId, Long versionId);

    Note restoreVersion(Long userId, Long noteId, Long versionId);
}
