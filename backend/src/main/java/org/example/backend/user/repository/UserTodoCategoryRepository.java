package org.example.backend.user.repository;

import org.example.backend.common.entity.TodoCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTodoCategoryRepository extends JpaRepository<TodoCategory, Long> {
}