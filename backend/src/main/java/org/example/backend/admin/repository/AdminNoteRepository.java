package org.example.backend.admin.repository;

import org.example.backend.common.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNoteRepository extends JpaRepository<Note, Long> {
}
