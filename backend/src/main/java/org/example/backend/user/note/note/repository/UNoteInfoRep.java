/**
 * File Name: UNoteInfoRep.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-12
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.note.repository;

import org.example.backend.common.entity.NoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UNoteInfoRep extends JpaRepository<NoteInfo, Long> {

    @Query("SELECT n.userId FROM NoteInfo n WHERE n.id = :id")
    Long findUserIdByNoteId(@Param("id") Long id);

    Optional<NoteInfo> findByIdAndUserId(Long id, Long userId);
}
