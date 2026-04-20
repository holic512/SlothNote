package org.example.backend.user.note.search.repository;

import org.example.backend.common.domain.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchNoteRepM extends JpaRepository<Note, Long> {
    @Query("SELECT n FROM Note n WHERE LOWER(n.content) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Note> findByContentLike(@Param("q") String q);
}
