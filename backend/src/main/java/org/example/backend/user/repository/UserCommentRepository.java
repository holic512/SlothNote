package org.example.backend.user.repository;

import org.example.backend.common.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCommentRepository extends JpaRepository<Comment, Long> {
}