package org.example.backend.admin.repository;

import org.example.backend.common.entity.TodoInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminTodoInfoRepository extends JpaRepository<TodoInfo, Long>, JpaSpecificationExecutor<TodoInfo> {
}