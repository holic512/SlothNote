package org.example.backend.admin.repository;

import org.example.backend.common.entity.TodoCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminTodoCategoryRepository extends JpaRepository<TodoCategory, Long>, JpaSpecificationExecutor<TodoCategory> {
}