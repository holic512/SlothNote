/**
 * File Name: CommentReplyRequest.java
 * Description: 评论回复请求类，封装用户回复评论时所需的请求数据
 * Author: holic512
 * Created Date: 2024-12-22
 * Version: 1.0
 * Usage:
 * 用于处理用户在评论下回复的请求数据，包含父评论ID和回复内容。
 */
package org.example.backend.user.note.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentReplyRequest {
    /**
     * 父评论ID
     * 用于标识该回复属于哪个父评论
     * 必填字段
     */
    @Schema(description = "父评论ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "父评论ID不能为空")
    private Long parentId;

    /**
     * 回复内容
     * 用户对评论的回复内容
     * 必填字段
     */
    @Schema(description = "回复内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "回复内容不能为空")
    private String content;
}
