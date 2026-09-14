/**
 * @file UserAiController
 * @project SlothNote
 * @module 用户端 / AI
 * @description 提供当前用户的 AI 会话、流式对话、上下文与全局笔记权限接口。
 * @logic 1. 由登录态解析当前用户；2. 读取或完整更新其 AI 权限；3. 仅在读取权限开启时同步会话笔记上下文。
 * @dependencies Service: UserAiService/UserAiPermissionService, Response: ApiResponse
 * @index_tags 用户AI, 全局权限, 会话, SSE, 笔记上下文
 * @author holic512
 */
package org.example.backend.user.ai;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.user.ai.dto.ChatRequest;
import org.example.backend.user.ai.dto.ContextNotesRequest;
import org.example.backend.user.ai.dto.StopChatRequest;
import org.example.backend.user.ai.dto.UserAiPermissionDto;
import org.example.backend.user.ai.service.UserAiPermissionService;
import org.example.backend.user.ai.service.UserAiService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/user/ai")
public class UserAiController {

    private final UserAiService userAiService;
    private final UserAiPermissionService permissionService;

    public UserAiController(UserAiService userAiService, UserAiPermissionService permissionService) {
        this.userAiService = userAiService;
        this.permissionService = permissionService;
    }

    @GetMapping("/permissions")
    public ResponseEntity<Object> permissions() {
        Long userId = userAiService.currentUserId();
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", permissionService.getPermissions(userId)));
    }

    @PutMapping("/permissions")
    public ResponseEntity<Object> updatePermissions(@RequestBody UserAiPermissionDto request) {
        Long userId = userAiService.currentUserId();
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", permissionService.updatePermissions(userId, request)));
    }

    @GetMapping("/sessions")
    public ResponseEntity<Object> sessions() {
        Long userId = userAiService.currentUserId();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("SUCCESS")
                .data(userAiService.listSessions(userId))
                .build());
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<Object> sessionDetail(@PathVariable Long sessionId) {
        Long userId = userAiService.currentUserId();
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("SUCCESS")
                .data(userAiService.getSessionDetail(userId, sessionId))
                .build());
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Object> deleteSession(@PathVariable Long sessionId) {
        Long userId = userAiService.currentUserId();
        userAiService.deleteSession(userId, sessionId);
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS"));
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Object> deleteAllSessions() {
        Long userId = userAiService.currentUserId();
        userAiService.deleteAllSessions(userId);
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS"));
    }

    @PutMapping("/sessions/{sessionId}/context-notes")
    public ResponseEntity<Object> replaceContextNotes(@PathVariable Long sessionId, @RequestBody ContextNotesRequest request) {
        Long userId = userAiService.currentUserId();
        permissionService.requireToolAccess(permissionService.getPermissions(userId), "read_note");
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("SUCCESS")
                .data(userAiService.replaceContextNotes(userId, sessionId, request.getNoteIds()))
                .build());
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        Long userId = userAiService.currentUserId();
        return userAiService.chat(userId, request);
    }

    @PostMapping("/stop")
    public ResponseEntity<Object> stop(@RequestBody StopChatRequest request) {
        Long userId = userAiService.currentUserId();
        userAiService.stop(userId, request);
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS"));
    }
}
