package org.example.backend.admin.repository;

import org.example.backend.common.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminCommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
}