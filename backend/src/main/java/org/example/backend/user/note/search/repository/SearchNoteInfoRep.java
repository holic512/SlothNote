package org.example.backend.user.note.search.repository;

import org.example.backend.common.entity.NoteInfo;
import org.example.backend.user.note.search.dto.SearchResultDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SearchNoteInfoRep extends JpaRepository<NoteInfo, Long> {
    @Query("SELECT new org.example.backend.user.note.search.dto.SearchResultDto(n.id, n.noteTitle, n.noteSummary) " +
            "FROM NoteInfo n WHERE n.userId = :userId AND n.isDeleted = 0 AND " +
            "(LOWER(n.noteTitle) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(n.noteSummary) LIKE LOWER(CONCAT('%', :q, '%')))" )
    List<SearchResultDto> searchByTitleOrSummary(@Param("userId") Long userId, @Param("q") String q);
}