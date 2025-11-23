package org.example.backend.user.repository;

import org.example.backend.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserUserRepository extends JpaRepository<User, Long> {
    User findByUid(String uid);
}