package org.example.backend.user.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.common.domain.Note;
import org.example.backend.common.entity.*;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.ai.dto.*;
import org.example.backend.user.ai.repository.*;
import org.example.backend.user.note.search.dto.SearchResultDto;
import org.example.backend.user.note.note.repository.UNoteRepM;
import org.example.backend.user.repository.UserUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class UserAiService {

    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final int MAX_HISTORY_MESSAGE_CHARS = 1200;
    private static final int MAX_SELECTED_TEXT_CHARS = 1200;
    private static final int MAX_CONTEXT_NOTES = 3;
    private static final int MAX_CONTEXT_NOTE_CHARS = 900;
    private static final int MAX_CONTEXT_TOTAL_CHARS = 2200;
    private static final int MAX_TOOL_STEPS = 2;

    @Value("${xfyun.spark.api-key}")
    private String apiKey;

    @Value("${xfyun.spark.api-url}")
    private String apiUrl;

    @Value("${xfyun.spark.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final AiChatSessionNoteRefRepository noteRefRepository;
    private final AiNoteContextRepository aiNoteContextRepository;
    private final UNoteRepM noteRepM;
    private final UserUserRepository userUserRepository;
    private final UserAiToolService userAiToolService;
    private final ConcurrentHashMap<Long, AtomicBoolean> stopFlags = new ConcurrentHashMap<>();

    public UserAiService(AiChatSessionRepository sessionRepository,
                         AiChatMessageRepository messageRepository,
                         AiChatSessionNoteRefRepository noteRefRepository,
                         AiNoteContextRepository aiNoteContextRepository,
                         UNoteRepM noteRepM,
                         UserUserRepository userUserRepository,
                         UserAiToolService userAiToolService) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.noteRefRepository = noteRefRepository;
        this.aiNoteContextRepository = aiNoteContextRepository;
        this.noteRepM = noteRepM;
        this.userUserRepository = userUserRepository;
        this.userAiToolService = userAiToolService;
    }

    @Transactional(readOnly = true)
    public List<AiChatSessionDto> listSessions(Long userId) {
        return sessionRepository.findByUserIdAndIsDeletedOrderByLastMessageAtDesc(userId, 0).stream()
                .map(session -> new AiChatSessionDto(session.getId(), session.getTitle(), session.getLastMessageAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AiSessionDetailDto getSessionDetail(Long userId, Long sessionId) {
        AiChatSession session = requireSession(userId, sessionId);
        List<AiChatMessageDto> messages = messageRepository.findBySessionIdAndUserIdOrderByCreatedAtAsc(session.getId(), userId).stream()
                .map(this::toMessageDto)
                .toList();
        List<ContextNoteDto> contextNotes = getContextNotes(userId, session.getId());
        return new AiSessionDetailDto(messages, contextNotes);
    }

    @Transactional
    public void deleteSession(Long userId, Long sessionId) {
        AiChatSession session = requireSession(userId, sessionId);
        session.setIsDeleted(1);
        sessionRepository.save(session);
    }

    @Transactional
    public void deleteAllSessions(Long userId) {
        List<AiChatSession> sessions = sessionRepository.findByUserIdAndIsDeletedOrderByLastMessageAtDesc(userId, 0);
        for (AiChatSession session : sessions) {
            session.setIsDeleted(1);
        }
        sessionRepository.saveAll(sessions);
    }

    @Transactional
    public List<ContextNoteDto> replaceContextNotes(Long userId, Long sessionId, List<Long> noteIds) {
        AiChatSession session = requireSession(userId, sessionId);
        List<Long> safeIds = noteIds == null ? Collections.emptyList() : noteIds.stream().filter(Objects::nonNull).distinct().toList();
        noteRefRepository.deleteBySessionIdAndUserId(session.getId(), userId);

        List<NoteInfo> notes = safeIds.isEmpty()
                ? Collections.emptyList()
                : aiNoteContextRepository.findByIdInAndUserIdAndIsDeleted(safeIds, userId, 0);
        Map<Long, NoteInfo> noteMap = notes.stream().collect(Collectors.toMap(NoteInfo::getId, note -> note));

        List<AiChatSessionNoteRef> refs = new ArrayList<>();
        int index = 0;
        for (Long noteId : safeIds) {
            if (!noteMap.containsKey(noteId)) {
                continue;
            }
            AiChatSessionNoteRef ref = new AiChatSessionNoteRef();
            ref.setSessionId(session.getId());
            ref.setUserId(userId);
            ref.setNoteId(noteId);
            ref.setSortOrder(index++);
            refs.add(ref);
        }
        noteRefRepository.saveAll(refs);
        return getContextNotes(userId, sessionId);
    }

    public SseEmitter chat(Long userId, ChatRequest request) {
        String text = request.getText() == null ? "" : request.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("text cannot be empty");
        }

        AiChatSession session = resolveOrCreateSession(userId, request);
        List<Long> contextNoteIds = request.getContextNoteIds();
        if (contextNoteIds != null) {
            replaceContextNotes(userId, session.getId(), contextNoteIds);
        }

        AiChatMessage userMessage = saveUserMessage(userId, session, request);
        AiChatMessage assistantMessage = saveAssistantPlaceholder(userId, session, request);

        SseEmitter emitter = new SseEmitter(300000L);
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        stopFlags.put(assistantMessage.getId(), stopFlag);

        new Thread(() -> streamAiResponse(emitter, stopFlag, session, userId, request, assistantMessage, userMessage)).start();
        return emitter;
    }

    @Transactional
    public void stop(Long userId, StopChatRequest request) {
        if (request.getAssistantMessageId() != null) {
            AtomicBoolean stopFlag = stopFlags.get(request.getAssistantMessageId());
            if (stopFlag != null) {
                stopFlag.set(true);
            }
        } else if (request.getSessionId() != null) {
            List<AiChatMessage> messages = messageRepository.findBySessionIdAndUserIdOrderByCreatedAtAsc(request.getSessionId(), userId);
            for (AiChatMessage message : messages) {
                if ("assistant".equals(message.getRole()) && "streaming".equals(message.getStatus())) {
                    AtomicBoolean stopFlag = stopFlags.get(message.getId());
                    if (stopFlag != null) {
                        stopFlag.set(true);
                    }
                }
            }
        }
    }

    private void streamAiResponse(SseEmitter emitter,
                                  AtomicBoolean stopFlag,
                                  AiChatSession session,
                                  Long userId,
                                  ChatRequest request,
                                  AiChatMessage assistantMessage,
                                  AiChatMessage userMessage) {
        StringBuilder fullContent = new StringBuilder();
        try {
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of(
                    "type", "session",
                    "sessionId", session.getId(),
                    "userMessageId", userMessage.getId(),
                    "assistantMessageId", assistantMessage.getId()
            ))));

            Map<String, Object> payload = buildAiRequestPayload(userId, session.getId(), request);
            String jsonBody = objectMapper.writeValueAsString(payload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.execute(apiUrl, HttpMethod.POST, httpRequest -> {
                httpRequest.getHeaders().addAll(headers);
                httpRequest.getBody().write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }, response -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (stopFlag.get()) {
                            break;
                        }
                        if (!line.startsWith("data:")) {
                            continue;
                        }
                        String content = line.substring(5).trim();
                        if (content.isEmpty()) {
                            continue;
                        }
                        if ("[DONE]".equals(content)) {
                            break;
                        }
                        JsonNode jsonData = objectMapper.readTree(content);
                        JsonNode choices = jsonData.path("choices");
                        if (choices.isArray() && !choices.isEmpty()) {
                            JsonNode delta = choices.get(0).path("delta");
                            if (delta.has("content")) {
                                String deltaContent = delta.get("content").asText("");
                                fullContent.append(deltaContent);
                                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of(
                                        "type", "delta",
                                        "assistantMessageId", assistantMessage.getId(),
                                        "content", deltaContent
                                ))));
                            }
                        }
                    }
                }
                return null;
            });

            finalizeAssistantMessage(assistantMessage.getId(), fullContent.toString(), stopFlag.get() ? "stopped" : "completed");
            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of(
                    "type", "done",
                    "assistantMessageId", assistantMessage.getId(),
                    "status", stopFlag.get() ? "stopped" : "completed"
            ))));
            emitter.complete();
        } catch (Exception e) {
            finalizeAssistantMessage(assistantMessage.getId(), fullContent.toString(), "failed");
            try {
                emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(Map.of(
                        "type", "error",
                        "assistantMessageId", assistantMessage.getId(),
                        "message", e.getMessage()
                ))));
            } catch (IOException ignored) {
            }
            emitter.completeWithError(e);
        } finally {
            stopFlags.remove(assistantMessage.getId());
        }
    }

    @Transactional
    protected void finalizeAssistantMessage(Long messageId, String content, String status) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setContentMd(content == null ? "" : content);
            message.setStatus(status);
            messageRepository.save(message);
        });
    }

    private Map<String, Object> buildAiRequestPayload(Long userId, Long sessionId, ChatRequest request) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);
        requestBody.put("messages", buildMessagesWithTools(userId, sessionId, request));
        return requestBody;
    }

    private List<Map<String, String>> buildMessagesWithTools(Long userId, Long sessionId, ChatRequest request) {
        List<Map<String, String>> messages = buildMessages(userId, sessionId, request, true);
        Set<String> executedPlans = new HashSet<>();
        for (int i = 0; i < MAX_TOOL_STEPS; i++) {
            ToolPlan plan = planToolUse(messages, request.getText().trim());
            if (plan == null || "none".equals(plan.tool())) {
                break;
            }
            String planSignature = plan.tool() + ":" + plan.arguments();
            if (!executedPlans.add(planSignature)) {
                break;
            }
            String toolResult = executeToolPlan(userId, plan);
            if (toolResult == null || toolResult.isBlank()) {
                break;
            }
            messages.add(Map.of(
                    "role", "system",
                    "content", "以下是工具调用结果，请仅在相关时引用，并明确说明这是你基于工具检索得到的信息：\n" + toolResult
            ));
        }
        return messages;
    }

    private List<Map<String, String>> buildMessages(Long userId, Long sessionId, ChatRequest request, boolean includeToolGuide) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(request.getMessageType(), includeToolGuide)));

        List<ContextNoteDto> contextNotes = getContextNotes(userId, sessionId);
        if (!contextNotes.isEmpty()) {
            String contextBlock = buildContextBlock(contextNotes);
            messages.add(Map.of("role", "system", "content", contextBlock));
        }

        if (request.getSelectedText() != null && !request.getSelectedText().isBlank()) {
            messages.add(Map.of("role", "system", "content",
                    "用户当前额外选中的精确文本如下，请优先结合这段文本回答：\n\n```text\n" + shrink(request.getSelectedText().trim(), MAX_SELECTED_TEXT_CHARS) + "\n```"));
        }

        List<AiChatMessage> history = messageRepository.findTop20BySessionIdAndUserIdOrderByCreatedAtDesc(sessionId, userId);
        Collections.reverse(history);
        int historyCount = 0;
        for (AiChatMessage historyMessage : history) {
            if ("failed".equals(historyMessage.getStatus()) || historyCount >= MAX_HISTORY_MESSAGES) {
                continue;
            }
            messages.add(Map.of(
                    "role", historyMessage.getRole(),
                    "content", shrink(historyMessage.getContentMd() == null ? "" : historyMessage.getContentMd(), MAX_HISTORY_MESSAGE_CHARS)
            ));
            historyCount++;
        }

        messages.add(Map.of("role", "user", "content", request.getText().trim()));
        return messages;
    }

    private String buildSystemPrompt(String messageType, boolean includeToolGuide) {
        String normalizedType = messageType == null || messageType.isBlank() ? "chat" : messageType;
        String base = """
                你是 SlothNote 的 AI 助手。
                回答要求：
                1. 语气自然、克制，不要客服腔和模板化开场。
                2. 先直接回答问题，再补充建议或可执行步骤。
                3. 输出必须兼容 Markdown，标题、列表、代码块、表格都使用标准 Markdown 语法。
                4. 如果回答引用了用户选择的笔记内容，要明确说明“根据你选择的笔记，我理解到……”，不要假装这些内容来自你自身记忆。
                5. 不要输出原始 HTML。
                6. 优先使用当前上下文，只有在确实需要补充信息时才依赖工具结果。
                """;
        if (includeToolGuide) {
            base = base + """
                    
                    可用工具：
                    - search_user_notes：搜索当前用户名下的笔记列表。
                    - read_note：读取当前用户某篇笔记的标题、摘要和正文片段。
                    工具使用原则：
                    - 只有当用户明确要求查找、列出、回忆某篇笔记内容，或当前上下文不足以回答时，才考虑工具。
                    - 不要为了普通闲聊或已知问题滥用工具。
                    """;
        }
        return switch (normalizedType) {
            case "explain" -> base + "\n当前任务：解释用户给出的内容，优先用简洁的分点说明概念、作用和上下文。";
            case "polish" -> base + "\n当前任务：润色用户给出的内容，先给优化后的文本，再简短说明优化点。";
            case "summary" -> base + "\n当前任务：概括用户给出的内容，直接输出一段清晰的 Markdown 摘要，控制在 200 字左右。";
            default -> base + "\n当前任务：进行自然对话，必要时给出结构化建议。";
        };
    }

    private String buildContextBlock(List<ContextNoteDto> contextNotes) {
        StringBuilder builder = new StringBuilder("以下是用户主动选择的笔记上下文，请仅在相关时引用：\n");
        int totalChars = 0;
        int count = 0;
        for (ContextNoteDto note : contextNotes) {
            if (count >= MAX_CONTEXT_NOTES || totalChars >= MAX_CONTEXT_TOTAL_CHARS) {
                break;
            }
            builder.append("\n## 笔记：").append(note.getTitle() == null ? "未命名笔记" : note.getTitle()).append('\n');
            if (note.getSummary() != null && !note.getSummary().isBlank()) {
                builder.append("- 摘要：").append(note.getSummary()).append('\n');
            }
            Optional<Note> noteOptional = noteRepM.findById(note.getNoteId());
            noteOptional.map(Note::getContent)
                    .map(content -> shrink(content, MAX_CONTEXT_NOTE_CHARS))
                    .filter(content -> !content.isBlank())
                    .ifPresent(content -> {
                        builder.append("- 正文片段：\n```markdown\n").append(content).append("\n```\n");
                    });
            totalChars = builder.length();
            count++;
        }
        return builder.toString();
    }

    private String shrink(String content, int maxChars) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.length() <= maxChars) {
            return trimmed;
        }
        return trimmed.substring(0, maxChars) + "\n...\n";
    }

    @Transactional
    protected AiChatSession resolveOrCreateSession(Long userId, ChatRequest request) {
        if (request.getSessionId() != null) {
            return requireSession(userId, request.getSessionId());
        }
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle(buildSessionTitle(request.getText()));
        session.setLastMessageAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    @Transactional
    protected AiChatMessage saveUserMessage(Long userId, AiChatSession session, ChatRequest request) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(session.getId());
        message.setUserId(userId);
        message.setRole("user");
        message.setMessageType(normalizeMessageType(request.getMessageType()));
        message.setContentMd(request.getText().trim());
        message.setStatus("completed");
        session.setLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);
        if ("新对话".equals(session.getTitle())) {
            session.setTitle(buildSessionTitle(request.getText()));
            sessionRepository.save(session);
        }
        return messageRepository.save(message);
    }

    @Transactional
    protected AiChatMessage saveAssistantPlaceholder(Long userId, AiChatSession session, ChatRequest request) {
        AiChatMessage message = new AiChatMessage();
        message.setSessionId(session.getId());
        message.setUserId(userId);
        message.setRole("assistant");
        message.setMessageType(normalizeMessageType(request.getMessageType()));
        message.setContentMd("");
        message.setStatus("streaming");
        session.setLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);
        return messageRepository.save(message);
    }

    private String normalizeMessageType(String messageType) {
        if (messageType == null || messageType.isBlank()) {
            return "chat";
        }
        return messageType;
    }

    private String buildSessionTitle(String text) {
        if (text == null || text.isBlank()) {
            return "新对话";
        }
        String oneLine = text.trim().replaceAll("\\s+", " ");
        return oneLine.length() > 24 ? oneLine.substring(0, 24) + "..." : oneLine;
    }

    private AiChatSession requireSession(Long userId, Long sessionId) {
        return sessionRepository.findByIdAndUserIdAndIsDeleted(sessionId, userId, 0)
                .orElseThrow(() -> new IllegalArgumentException("session not found"));
    }

    private List<ContextNoteDto> getContextNotes(Long userId, Long sessionId) {
        List<AiChatSessionNoteRef> refs = noteRefRepository.findBySessionIdAndUserIdOrderBySortOrderAscCreatedAtAsc(sessionId, userId);
        if (refs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> noteIds = refs.stream().map(AiChatSessionNoteRef::getNoteId).distinct().toList();
        Map<Long, NoteInfo> noteMap = aiNoteContextRepository.findByIdInAndUserIdAndIsDeleted(noteIds, userId, 0).stream()
                .collect(Collectors.toMap(NoteInfo::getId, note -> note));

        List<ContextNoteDto> result = new ArrayList<>();
        for (AiChatSessionNoteRef ref : refs) {
            NoteInfo noteInfo = noteMap.get(ref.getNoteId());
            if (noteInfo == null) {
                continue;
            }
            result.add(new ContextNoteDto(
                    noteInfo.getId(),
                    noteInfo.getNoteTitle(),
                    noteInfo.getNoteSummary(),
                    noteInfo.getNoteAvatar() == null ? null : new String(noteInfo.getNoteAvatar())
            ));
        }
        return result;
    }

    private AiChatMessageDto toMessageDto(AiChatMessage message) {
        return new AiChatMessageDto(
                message.getId(),
                message.getRole(),
                message.getMessageType(),
                message.getContentMd(),
                message.getStatus(),
                message.getCreatedAt()
        );
    }

    private ToolPlan planToolUse(List<Map<String, String>> messages, String userQuestion) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("stream", false);
            requestBody.put("temperature", 0.1);
            requestBody.put("max_tokens", 256);
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", """
                            你是一个工具规划器。你只输出 JSON，不输出解释。
                            你的任务是判断是否需要调用工具来帮助回答用户问题。
                            可选输出：
                            1. {"tool":"none"}
                            2. {"tool":"search_user_notes","arguments":{"query":"关键词","limit":5}}
                            3. {"tool":"read_note","arguments":{"noteId":123,"maxChars":1200}}
                            规则：
                            - 如果现有上下文已经足够，输出 {"tool":"none"}。
                            - 只有当用户要搜索、列出、定位、回忆笔记内容时，才考虑工具。
                            - 不要臆造 noteId。
                            - 输出必须是单个 JSON 对象。
                            """),
                    Map.of("role", "user", "content", "用户问题：" + userQuestion),
                    Map.of("role", "user", "content", "已有消息上下文摘要：" + summarizeMessagesForPlanner(messages))
            ));

            String content = callAiForSingleMessage(requestBody);
            if (content == null || content.isBlank()) {
                return null;
            }
            String json = extractJson(content);
            JsonNode node = objectMapper.readTree(json);
            String tool = node.path("tool").asText("none");
            JsonNode arguments = node.path("arguments");
            return new ToolPlan(tool, arguments);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String executeToolPlan(Long userId, ToolPlan plan) {
        try {
            if (UserAiToolService.SEARCH_USER_NOTES.equals(plan.tool())) {
                String query = plan.arguments().path("query").asText("");
                int limit = plan.arguments().path("limit").asInt(5);
                List<SearchResultDto> results = userAiToolService.searchUserNotes(userId, query, limit);
                if (results.isEmpty()) {
                    return "工具 search_user_notes 返回：未找到匹配笔记。";
                }
                StringBuilder builder = new StringBuilder("工具 search_user_notes 返回结果：\n");
                int index = 1;
                for (SearchResultDto result : results) {
                    builder.append(index++).append(". [noteId=").append(result.getNoteId()).append("] ")
                            .append(result.getTitle() == null ? "未命名笔记" : result.getTitle()).append('\n');
                    if (result.getSummary() != null && !result.getSummary().isBlank()) {
                        builder.append("   摘要：").append(result.getSummary()).append('\n');
                    }
                    if (result.getSnippet() != null && !result.getSnippet().isBlank()) {
                        builder.append("   片段：").append(result.getSnippet()).append('\n');
                    }
                }
                return builder.toString();
            }
            if (UserAiToolService.READ_NOTE.equals(plan.tool())) {
                long noteId = plan.arguments().path("noteId").asLong(0);
                int maxChars = plan.arguments().path("maxChars").asInt(1200);
                ContextNoteDto note = userAiToolService.readNoteMeta(userId, noteId);
                String content = userAiToolService.readNoteContent(userId, noteId, maxChars);
                if (note == null) {
                    return "工具 read_note 返回：未找到该笔记，或当前用户无权访问。";
                }
                StringBuilder builder = new StringBuilder("工具 read_note 返回结果：\n");
                builder.append("- noteId: ").append(note.getNoteId()).append('\n');
                builder.append("- 标题: ").append(note.getTitle() == null ? "未命名笔记" : note.getTitle()).append('\n');
                if (note.getSummary() != null && !note.getSummary().isBlank()) {
                    builder.append("- 摘要: ").append(note.getSummary()).append('\n');
                }
                builder.append("- 正文片段:\n```markdown\n").append(content == null ? "" : content).append("\n```");
                return builder.toString();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String summarizeMessagesForPlanner(List<Map<String, String>> messages) {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (int i = Math.max(0, messages.size() - 6); i < messages.size(); i++) {
            Map<String, String> message = messages.get(i);
            builder.append('[').append(message.get("role")).append("] ")
                    .append(shrink(message.get("content"), 240)).append('\n');
            count++;
            if (count >= 6) {
                break;
            }
        }
        return builder.toString();
    }

    private String callAiForSingleMessage(Map<String, Object> requestBody) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.execute(apiUrl, HttpMethod.POST, httpRequest -> {
            httpRequest.getHeaders().addAll(headers);
            httpRequest.getBody().write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }, response -> {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            return choices.get(0).path("message").path("content").asText(null);
        });
    }

    private String extractJson(String content) {
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end >= start) {
            return content.substring(start, end + 1);
        }
        return "{\"tool\":\"none\"}";
    }

    private record ToolPlan(String tool, JsonNode arguments) {}

    public Long currentUserId() {
        Object sessionId = StpKit.USER.getSession().get("id");
        if (sessionId instanceof Number number) {
            return number.longValue();
        }
        String uid = String.valueOf(StpKit.USER.getLoginId());
        User user = userUserRepository.findByUid(uid);
        if (user == null) {
            throw new IllegalArgumentException("user not found");
        }
        StpKit.USER.getSession().set("id", user.getId());
        return user.getId();
    }
}
