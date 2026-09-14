/**
 * @file UserAiToolController
 * @project SlothNote
 * @module 用户端 / AI 工具
 * @description 提供受当前用户全局 AI 权限约束的工具清单与笔记检索读取接口。
 * @logic 1. 按当前权限返回工具白名单；2. 在检索或读取前重新校验读取权限；3. 仅访问当前用户拥有的笔记。
 * @dependencies Service: UserAiService/UserAiToolService, Response: ApiResponse
 * @index_tags 用户AI, 工具调用, 权限白名单, 笔记检索
 * @author holic512
 */
package org.example.backend.user.ai;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.user.ai.dto.AiToolReadNoteRequest;
import org.example.backend.user.ai.dto.AiToolSearchRequest;
import org.example.backend.user.ai.dto.UserAiPermissionDto;
import org.example.backend.user.ai.service.UserAiPermissionService;
import org.example.backend.user.ai.service.UserAiService;
import org.example.backend.user.ai.service.UserAiToolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/ai/tools")
public class UserAiToolController {

    private final UserAiService userAiService;
    private final UserAiToolService userAiToolService;
    private final UserAiPermissionService permissionService;

    public UserAiToolController(UserAiService userAiService,
                                UserAiToolService userAiToolService,
                                UserAiPermissionService permissionService) {
        this.userAiService = userAiService;
        this.userAiToolService = userAiToolService;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<Object> listTools() {
        Long userId = userAiService.currentUserId();
        UserAiPermissionDto permissions = permissionService.getPermissions(userId);
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS",
                userAiToolService.listTools(permissionService.allowedToolNames(permissions))));
    }

    @PostMapping("/search-notes")
    public ResponseEntity<Object> searchNotes(@RequestBody AiToolSearchRequest request) {
        Long userId = userAiService.currentUserId();
        permissionService.requireToolAccess(permissionService.getPermissions(userId), UserAiToolService.SEARCH_USER_NOTES);
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS",
                userAiToolService.searchUserNotes(userId, request.getQuery(), request.getLimit())));
    }

    @PostMapping("/read-note")
    public ResponseEntity<Object> readNote(@RequestBody AiToolReadNoteRequest request) {
        Long userId = userAiService.currentUserId();
        permissionService.requireToolAccess(permissionService.getPermissions(userId), UserAiToolService.READ_NOTE);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("note", userAiToolService.readNoteMeta(userId, request.getNoteId()));
        data.put("content", userAiToolService.readNoteContent(userId, request.getNoteId(), request.getMaxChars()));
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", data));
    }
}
