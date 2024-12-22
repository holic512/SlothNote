package org.example.backend.user.note.comment.service.impl;

import org.example.backend.common.entity.Comment;
import org.example.backend.user.note.comment.repository.UCommentRep;
import org.example.backend.user.note.comment.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostCommentServiceImpl implements PostCommentService {

    private final UCommentRep commentRep;

    @Autowired
    public PostCommentServiceImpl(UCommentRep commentRep) {
        this.commentRep = commentRep;
    }

    @Override
    @Transactional
    public void createComment(Long noteId, String content, Long userId) {
        Comment comment = new Comment();
        comment.setNoteId(noteId);
        comment.setContent(content);
        comment.setUserId(userId);

        commentRep.save(comment);
    }

    @Override
    public void replyToComment(Long parentCommentId, String content, Long userId) {
        // 获取父id 的笔记id
        Long noteId = commentRep.findNoteIdById(parentCommentId);

        Comment comment = new Comment();
        comment.setNoteId(noteId);
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setParentId(parentCommentId);

        commentRep.save(comment);
    }
} 