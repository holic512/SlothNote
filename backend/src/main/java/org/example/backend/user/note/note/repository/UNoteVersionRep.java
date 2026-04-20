package org.example.backend.user.note.note.repository;

import org.example.backend.common.entity.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UNoteVersionRep extends JpaRepository<NoteVersion, Long> {

    List<NoteVersion> findTop20ByNoteIdAndUserIdOrderByVersionNoDesc(Long noteId, Long userId);

    Optional<NoteVersion> findTopByNoteIdAndUserIdOrderByVersionNoDesc(Long noteId, Long userId);

    Optional<NoteVersion> findByIdAndNoteIdAndUserId(Long id, Long noteId, Long userId);
}
