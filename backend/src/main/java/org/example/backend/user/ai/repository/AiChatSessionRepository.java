package org.example.backend.user.ai.repository;

import org.example.backend.common.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long>, JpaSpecificationExecutor<AiChatSession> {

    List<AiChatSession> findByUserIdAndIsDeletedOrderByLastMessageAtDesc(Long userId, Integer isDeleted);

    Optional<AiChatSession> findByIdAndUserIdAndIsDeleted(Long id, Long userId, Integer isDeleted);
}
