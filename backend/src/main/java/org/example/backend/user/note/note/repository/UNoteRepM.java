/**
 * File Name: UNoteRepM.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-12
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.note.repository;

import org.example.backend.common.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UNoteRepM extends JpaRepository<Note, Long> {
}
