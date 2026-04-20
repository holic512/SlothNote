package org.example.backend.user.ai.repository;

import jakarta.transaction.Transactional;
import org.example.backend.common.entity.AiChatSessionNoteRef;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiChatSessionNoteRefRepository extends JpaRepository<AiChatSessionNoteRef, Long> {

    List<AiChatSessionNoteRef> findBySessionIdAndUserIdOrderBySortOrderAscCreatedAtAsc(Long sessionId, Long userId);

    List<AiChatSessionNoteRef> findBySessionIdOrderBySortOrderAscCreatedAtAsc(Long sessionId);

    @Transactional
    void deleteBySessionIdAndUserId(Long sessionId, Long userId);
}
