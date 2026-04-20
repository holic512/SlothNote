package org.example.backend.user.ai.repository;

import org.example.backend.common.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, Long> {

    List<AiChatMessage> findBySessionIdAndUserIdOrderByCreatedAtAsc(Long sessionId, Long userId);

    List<AiChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    List<AiChatMessage> findTop20BySessionIdAndUserIdOrderByCreatedAtDesc(Long sessionId, Long userId);

    Optional<AiChatMessage> findByIdAndUserId(Long id, Long userId);
}
