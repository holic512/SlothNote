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
        return ResponseEntity.ok(new ApiResponse<>(200, "SUCCESS", userAiService.previewToolPlan(userId, request)));
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
