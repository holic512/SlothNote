/**
 * File Name: GetUCommentController.java
 * Description: 用户评论控制器，提供获取用户评论的相关接口
 * Author: holic512
 * Created Date: 2024-12-22
 * Version: 1.0
 * Usage:
 * 提供获取用户某笔记评论的接口，用户可通过该接口查看笔记下的所有评论。
 */
package org.example.backend.user.note.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.note.comment.dto.CommentViewPojo;
import org.example.backend.user.note.comment.service.GetCommentService;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "评论管理", description = "处理用户评论相关的操作")
@RestController
@RequestMapping("/user/comments")
@RequiredArgsConstructor
public class GetUCommentController {

    final private GetCommentService getCommentService;

    /**
     * 获取评论接口
     * @param noteId 笔记ID，用于查询该笔记下的所有评论
     * @return ApiResponse 返回包含评论数据的响应
     */
    @Operation(summary = "获取评论", description = "用户获取当前笔记的全部评论")
    @GetMapping("comments")
    public ApiResponse<Object> GetComments(@Param("noteId") Long noteId) {
        // 从上下文获取用户ID
        Long userId = (Long) StpKit.USER.getSession().get("id");

        // 调用服务类获取评论数据
        List<CommentViewPojo> result = getCommentService.GetComments(userId, noteId);

        // 返回评论数据和操作状态
        return new ApiResponse.Builder<>().status(200).message("成功查询").data(result).build();
    }
}
