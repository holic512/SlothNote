package org.example.backend.admin.repository;

import org.example.backend.common.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAdminRepository extends JpaRepository<Admin, Long> {
}