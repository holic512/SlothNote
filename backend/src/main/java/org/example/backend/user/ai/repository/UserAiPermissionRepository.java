package org.example.backend.user.ai.repository;

import org.example.backend.common.entity.UserAiPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAiPermissionRepository extends JpaRepository<UserAiPermission, Long> {
    Optional<UserAiPermission> findByUserId(Long userId);
}
