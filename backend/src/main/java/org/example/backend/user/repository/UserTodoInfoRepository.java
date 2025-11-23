package org.example.backend.user.repository;

import org.example.backend.common.entity.TodoInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTodoInfoRepository extends JpaRepository<TodoInfo, Long> {
}