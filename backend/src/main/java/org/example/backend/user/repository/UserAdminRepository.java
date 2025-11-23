package org.example.backend.user.repository;

import org.example.backend.common.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAdminRepository extends JpaRepository<Admin, Long> {
}