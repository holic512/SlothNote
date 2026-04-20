package org.example.backend.user.ai.repository;

import org.example.backend.common.entity.NoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiNoteContextRepository extends JpaRepository<NoteInfo, Long> {
    List<NoteInfo> findByIdInAndUserIdAndIsDeleted(List<Long> ids, Long userId, Integer isDeleted);
}
