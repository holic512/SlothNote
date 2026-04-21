package org.example.backend.user.ai;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.user.ai.dto.ChatRequest;
import org.example.backend.user.ai.dto.ContextNotesRequest;
import org.example.backend.user.ai.dto.StopChatRequest;
import org.example.backend.user.ai.service.UserAiService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/user/ai")
public class UserAiController {

    private final UserAiService userAiService;

    public UserAiController(UserAiService userAiService) {
        this.userAiService = userAiService;
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
