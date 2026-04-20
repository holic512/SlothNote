/**
 * File Name: PUNoteService.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-12
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.note.service;

import org.example.backend.common.domain.Note;

public interface PUNoteService {
    String SaveNote(Long id, Note note);

    Note restoreVersion(Long userId, Long noteId, Long versionId);
}
