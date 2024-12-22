package org.example.backend.user.note.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建评论请求")
public class CommentCreateRequest {

    @Schema(description = "笔记ID",  requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "笔记ID不能为空")
    private Long noteId;

    @Schema(description = "评论内容", required = true)
    @NotBlank(message = "评论内容不能为空")
    private String content;
} 