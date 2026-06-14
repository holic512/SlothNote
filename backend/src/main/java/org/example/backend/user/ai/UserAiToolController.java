/**
 * @file UserAiToolController
 * @project SlothNote
 * @module 用户端 / AI 工具
 * @description 提供 AI 工具清单、工具规划预览和笔记检索读取接口。
 * @logic 1. 返回可用工具；2. 调用 AI 规划器预览工具执行方案；3. 提供笔记搜索与读取辅助接口。
 * @dependencies Service: UserAiService/UserAiToolService, Response: ApiResponse
 * @index_tags 用户AI, 工具调用, 规划预览, 笔记检索
 * @author holic512
 */
package org.example.backend.user.ai;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.user.ai.dto.AiToolReadNoteRequest;
import org.example.backend.user.ai.dto.AiToolSearchRequest;
import org.example.backend.user.ai.dto.ChatRequest;
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

    public UserAiToolController(UserAiService userAiService, UserAiToolService userAiToolService) {
        this.userAiService = userAiService;
        this.userAiToolService = userAiToolService;
    }

    @GetMapping
    public ResponseEntity<Object> listTools() {
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", userAiToolService.listTools()));
    }

    @PostMapping("/plan")
    public ResponseEntity<Object> previewPlan(@RequestBody ChatRequest request) {
        Long userId = userAiService.currentUserId();
        try {
            return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", userAiService.previewToolPlan(userId, request)));
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(new ApiResponse<>(400, ex.getMessage()));
        }
    }

    @PostMapping("/search-notes")
    public ResponseEntity<Object> searchNotes(@RequestBody AiToolSearchRequest request) {
        Long userId = userAiService.currentUserId();
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS",
                userAiToolService.searchUserNotes(userId, request.getQuery(), request.getLimit())));
    }

    @PostMapping("/read-note")
    public ResponseEntity<Object> readNote(@RequestBody AiToolReadNoteRequest request) {
        Long userId = userAiService.currentUserId();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("note", userAiToolService.readNoteMeta(userId, request.getNoteId()));
        data.put("content", userAiToolService.readNoteContent(userId, request.getNoteId(), request.getMaxChars()));
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", data));
    }
}
