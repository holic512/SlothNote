/**
 * File Name: PostUCommentController.java
 * Description: 评论控制器，处理用户评论相关的请求
 * Author: holic512
 * Created Date: 2024-12-22
 * Version: 1.0
 */
package org.example.backend.user.note.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.note.comment.dto.CommentCreateRequest;
import org.example.backend.user.note.comment.dto.CommentReplyRequest;
import org.example.backend.user.note.comment.service.PostCommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论管理", description = "处理用户评论相关的操作")
@RestController
@RequestMapping("/user/comments")
@RequiredArgsConstructor
public class PostUCommentController {

    private final PostCommentService postCommentService;

    @Operation(summary = "创建评论", description = "用户创建新评论")
    @PostMapping("/create")
    public ResponseEntity<Object> createComment(@RequestBody CommentCreateRequest request) {
        // 从上下文获取用户ID
        Long userId = (Long) StpKit.USER.getSession().get("id");

        // 调用服务层创建评论
        postCommentService.createComment(request.getNoteId(), request.getContent(), userId);

        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("发送评论成功")
                .build()
        );
    }

    @Operation(summary = "回复评论", description = "用于用户回复评论")
    @PostMapping("/reply")
    public ApiResponse<Object> replyComment(@RequestBody CommentReplyRequest request) {
        // 从上下文获取用户ID
        Long userId = (Long) StpKit.USER.getSession().get("id");

        postCommentService.replyToComment(request.getParentId(), request.getContent(), userId);

        return new ApiResponse.Builder<>()
                .status(200)
                .message("回复评论成功")
                .build();
    }
}
