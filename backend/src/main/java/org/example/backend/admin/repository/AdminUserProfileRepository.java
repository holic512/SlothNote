package org.example.backend.admin.repository;

import org.example.backend.common.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserProfileRepository extends JpaRepository<UserProfile, Long> {
}