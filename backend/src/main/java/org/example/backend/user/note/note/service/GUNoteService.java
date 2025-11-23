/**
 * File Name: GUNoteService.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-12
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.note.service;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.domain.Note;
import org.example.backend.user.note.note.enums.GContextEnum;

import java.util.Optional;

public interface GUNoteService {

    Pair<GContextEnum, Optional<Note>> GetContext(Long userId, Long noteId);

    String exportHtml(Long userId, Long noteId);
}
