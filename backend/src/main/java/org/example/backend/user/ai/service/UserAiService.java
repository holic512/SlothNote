/**
 * @file UserAiService
 * @project SlothNote
 * @module 用户端 / AI 助手
 * @description 处理用户 AI 会话、上下文笔记、工具规划和流式回答。
 * @logic 1. 管理 AI 会话与消息；2. 组合笔记上下文和工具调用；3. 从数据库读取系统 AI 配置后请求 OpenAI 兼容接口。
 * @dependencies Service: AiConfigService/UserAiToolService, Repository: AiChatSessionRepository/AiChatMessageRepository
 * @index_tags 用户AI, SSE, OpenAI兼容, 数据库配置, 工具调用
 * @author holic512
 */
package org.example.backend.user.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.backend.common.config.ai.AiConfigService;
import org.example.backend.common.domain.Note;
import org.example.backend.common.entity.*;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.ai.dto.*;
import org.example.backend.user.ai.repository.*;
import org.example.backend.user.note.search.dto.SearchResultDto;
import org.example.backend.user.note.note.repository.UNoteRepM;
import org.example.backend.user.repository.UserUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class UserAiService {

    private static final Logger log = LoggerFactory.getLogger(UserAiService.class);

    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final int MAX_HISTORY_MESSAGE_CHARS = 1200;
    private static final int MAX_SELECTED_TEXT_CHARS = 1200;
    private static final int MAX_CONTEXT_NOTES = 3;
    private static final int MAX_CONTEXT_NOTE_CHARS = 900;
    private static final int MAX_CONTEXT_TOTAL_CHARS = 2200;
    private static final int MAX_CURRENT_NOTE_CHARS = 1500;
    private static final int MAX_TOOL_STEPS = 2;
    private static final int DEBUG_PREVIEW_CHARS = 240;
    private static final String TOOL_EXECUTION_FAILURE_PREFIX = "工具未执行：";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiConfigService aiConfigService;
    private final AiChatSessionRepository sessionRepository;
    private final AiChatMessageRepository messageRepository;
    private final AiChatSessionNoteRefRepository noteRefRepository;
    private final AiNoteContextRepository aiNoteContextRepository;
    private final UNoteRepM noteRepM;
    private final UserUserRepository userUserRepository;
    private final UserAiToolService userAiToolService;
    private final UserAiPermissionService permissionService;
    private final ConcurrentHashMap<Long, AtomicBoolean> stopFlags = new ConcurrentHashMap<>();

    public UserAiService(AiConfigService aiConfigService,
                         AiChatSessionRepository sessionRepository,
                         AiChatMessageRepository messageRepository,
                         AiChatSessionNoteRefRepository noteRefRepository,
                         AiNoteContextRepository aiNoteContextRepository,
                         UNoteRepM noteRepM,
                         UserUserRepository userUserRepository,
                         UserAiToolService userAiToolService,
                         UserAiPermissionService permissionService) {
        this.aiConfigService = aiConfigService;
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.noteRefRepository = noteRefRepository;
        this.aiNoteContextRepository = aiNoteContextRepository;
        this.noteRepM = noteRepM;
        this.userUserRepository = userUserRepository;
        this.userAiToolService = userAiToolService;
        this.permissionService = permissionService;
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

        UserAiPermissionDto permissions = permissionService.getPermissions(userId);
        if (!Boolean.TRUE.equals(permissions.getCanReadAllNotes())) {
            request.setContextNoteIds(Collections.emptyList());
            request.setSelectedText(null);
            request.setCurrentNoteTitle(null);
            request.setCurrentNoteCover(null);
        }

        log.info("AI chat start: userId={}, sessionId={}, textLength={}, contextNoteCount={}",
                userId,
                request.getSessionId(),
                text.length(),
                request.getContextNoteIds() == null ? 0 : request.getContextNoteIds().size());
        log.debug("AI chat request detail: selectedTextLength={}, contextNoteIds={}, textPreview={}",
                request.getSelectedText() == null ? 0 : request.getSelectedText().trim().length(),
                request.getContextNoteIds(),
                previewText(text));

        AiChatSession session = resolveOrCreateSession(userId, request);
        List<Long> contextNoteIds = request.getContextNoteIds();
        if (contextNoteIds != null && Boolean.TRUE.equals(permissions.getCanReadAllNotes())) {
            replaceContextNotes(userId, session.getId(), contextNoteIds);
        }

        AiChatMessage userMessage = saveUserMessage(userId, session, request);
        AiChatMessage assistantMessage = saveAssistantPlaceholder(userId, session, request);

        SseEmitter emitter = new SseEmitter(300000L);
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        stopFlags.put(assistantMessage.getId(), stopFlag);

        Thread streamThread = new Thread(
                () -> streamAiResponse(emitter, stopFlag, session, userId, request, permissions, assistantMessage, userMessage),
                "ai-chat-" + assistantMessage.getId()
        );
        streamThread.start();
        return emitter;
    }

    @Transactional
    public void stop(Long userId, StopChatRequest request) {
        log.info("AI chat stop requested: userId={}, sessionId={}, assistantMessageId={}",
                userId, request.getSessionId(), request.getAssistantMessageId());
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
                                  UserAiPermissionDto permissions,
                                  AiChatMessage assistantMessage,
                                  AiChatMessage userMessage) {
        StringBuilder fullContent = new StringBuilder();
        try {
            sendEvent(emitter, Map.of(
                    "type", "session",
                    "sessionId", session.getId(),
                    "userMessageId", userMessage.getId(),
                    "assistantMessageId", assistantMessage.getId()
            ));
            log.debug("AI session event sent: sessionId={}, userMessageId={}, assistantMessageId={}",
                    session.getId(), userMessage.getId(), assistantMessage.getId());

            AiConfigService.RuntimeConfig aiConfig = aiConfigService.requireEnabledConfig();
            Map<String, Object> payload = buildAiRequestPayload(
                    aiConfig,
                    userId,
                    session.getId(),
                    request,
                    permissions,
                    userMessage.getId(),
                    assistantMessage.getId(),
                    progressPayload -> {
                        try {
                            sendEvent(emitter, progressPayload);
                        } catch (IOException ioException) {
                            throw new RuntimeException(ioException);
                        }
                    }
            );
            String jsonBody = objectMapper.writeValueAsString(payload);
            log.debug("AI upstream request prepared: sessionId={}, assistantMessageId={}, payloadChars={}, messageCount={}, payloadPreview={}",
                    session.getId(), assistantMessage.getId(), jsonBody.length(),
                    ((List<?>) payload.getOrDefault("messages", Collections.emptyList())).size(),
                    previewText(jsonBody));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiConfig.apiKey());

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.execute(resolveChatCompletionsUrl(aiConfig.baseUrl()), HttpMethod.POST, httpRequest -> {
                httpRequest.getHeaders().addAll(headers);
                httpRequest.getBody().write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }, response -> {
                log.info("AI upstream connected: sessionId={}, assistantMessageId={}, statusCode={}, contentType={}",
                        session.getId(),
                        assistantMessage.getId(),
                        response.getStatusCode(),
                        response.getHeaders().getContentType());
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                    String line;
                    int lineCount = 0;
                    int deltaCount = 0;
                    while ((line = reader.readLine()) != null) {
                        lineCount++;
                        if (stopFlag.get()) {
                            log.warn("AI stream interrupted by stop flag: sessionId={}, assistantMessageId={}, lineCount={}, contentLength={}",
                                    session.getId(), assistantMessage.getId(), lineCount, fullContent.length());
                            break;
                        }
                        log.debug("AI upstream raw line: sessionId={}, assistantMessageId={}, lineNo={}, line={}",
                                session.getId(), assistantMessage.getId(), lineCount, previewText(line));
                        if (!line.startsWith("data:")) {
                            continue;
                        }
                        String content = line.substring(5).trim();
                        if (content.isEmpty()) {
                            continue;
                        }
                        if ("[DONE]".equals(content)) {
                            log.debug("AI upstream done marker received: sessionId={}, assistantMessageId={}, lineNo={}",
                                    session.getId(), assistantMessage.getId(), lineCount);
                            break;
                        }
                        JsonNode jsonData = objectMapper.readTree(content);
                        JsonNode choices = jsonData.path("choices");
                        if (choices.isArray() && !choices.isEmpty()) {
                            JsonNode delta = choices.get(0).path("delta");
                            if (delta.has("content")) {
                                String deltaContent = delta.get("content").asText("");
                                fullContent.append(deltaContent);
                                deltaCount++;
                                log.debug("AI upstream delta: sessionId={}, assistantMessageId={}, deltaIndex={}, deltaLength={}, deltaPreview={}",
                                        session.getId(), assistantMessage.getId(), deltaCount, deltaContent.length(), previewText(deltaContent));
                                sendEvent(emitter, Map.of(
                                        "type", "delta",
                                        "assistantMessageId", assistantMessage.getId(),
                                        "content", deltaContent
                                ));
                            } else {
                                log.debug("AI upstream non-content delta: sessionId={}, assistantMessageId={}, payload={}",
                                        session.getId(), assistantMessage.getId(), previewText(content));
                            }
                        } else {
                            log.warn("AI upstream choices missing: sessionId={}, assistantMessageId={}, payload={}",
                                    session.getId(), assistantMessage.getId(), previewText(content));
                        }
                    }
                    log.debug("AI upstream stream finished: sessionId={}, assistantMessageId={}, lineCount={}, deltaCount={}, contentLength={}",
                            session.getId(), assistantMessage.getId(), lineCount, deltaCount, fullContent.length());
                }
                return null;
            });

            finalizeAssistantMessage(assistantMessage.getId(), fullContent.toString(), stopFlag.get() ? "stopped" : "completed");
            log.info("AI chat completed: sessionId={}, assistantMessageId={}, status={}, contentLength={}",
                    session.getId(), assistantMessage.getId(), stopFlag.get() ? "stopped" : "completed", fullContent.length());
            sendEvent(emitter, Map.of(
                    "type", "done",
                    "assistantMessageId", assistantMessage.getId(),
                    "status", stopFlag.get() ? "stopped" : "completed"
            ));
            emitter.complete();
        } catch (Exception e) {
            log.error("AI chat failed: sessionId={}, assistantMessageId={}, error={}",
                    session.getId(), assistantMessage.getId(), e.getMessage(), e);
            finalizeAssistantMessage(assistantMessage.getId(), fullContent.toString(), "failed");
            try {
                sendEvent(emitter, Map.of(
                        "type", "error",
                        "assistantMessageId", assistantMessage.getId(),
                        "message", e.getMessage()
                ));
            } catch (IOException ignored) {
            }
            emitter.complete();
        } finally {
            stopFlags.remove(assistantMessage.getId());
        }
    }

    private void sendEvent(SseEmitter emitter, Map<String, Object> payload) throws IOException {
        emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(payload)));
    }

    @Transactional
    protected void finalizeAssistantMessage(Long messageId, String content, String status) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setContentMd(content == null ? "" : content);
            message.setStatus(status);
            messageRepository.save(message);
        });
    }

    private Map<String, Object> buildAiRequestPayload(AiConfigService.RuntimeConfig aiConfig,
                                                      Long userId,
                                                      Long sessionId,
                                                      ChatRequest request,
                                                      UserAiPermissionDto permissions,
                                                      Long currentUserMessageId,
                                                      Long currentAssistantMessageId,
                                                      Consumer<Map<String, Object>> progressEmitter) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.model());
        requestBody.put("stream", true);
        requestBody.put("temperature", aiConfig.temperature());
        requestBody.put("max_tokens", aiConfig.maxTokens());
        requestBody.put("messages", buildMessagesWithTools(aiConfig, userId, sessionId, request, permissions, currentUserMessageId, currentAssistantMessageId, progressEmitter));
        return requestBody;
    }

    private List<Map<String, String>> buildMessagesWithTools(AiConfigService.RuntimeConfig aiConfig,
                                                             Long userId,
                                                             Long sessionId,
                                                             ChatRequest request,
                                                             UserAiPermissionDto permissions,
                                                             Long currentUserMessageId,
                                                             Long currentAssistantMessageId,
                                                             Consumer<Map<String, Object>> progressEmitter) {
        List<Map<String, String>> messages = buildMessages(userId, sessionId, request, permissions, true, currentUserMessageId, currentAssistantMessageId);
        log.debug("AI base messages prepared: sessionId={}, count={}, summary={}",
                sessionId, messages.size(), summarizeMessageList(messages));
        Set<String> executedPlans = new HashSet<>();
        if (shouldRefreshCurrentNoteContext(request, permissionService.getPermissions(userId))) {
            appendLatestCurrentNoteSnapshot(messages, userId, request, currentAssistantMessageId, progressEmitter, true);
        }
        emitStatus(progressEmitter, currentAssistantMessageId, "planning", "正在规划是否需要调用工具");
        for (int i = 0; i < MAX_TOOL_STEPS; i++) {
            UserAiPermissionDto plannerPermissions = permissionService.getPermissions(userId);
            ToolPlan plan = planToolUse(aiConfig, messages, request.getText().trim(), plannerPermissions);
            if (plan == null || "none".equals(plan.tool())) {
                log.debug("AI tool planning skipped: sessionId={}, step={}, plan={}", sessionId, i + 1, plan);
                break;
            }
            String planSignature = plan.tool() + ":" + plan.arguments();
            if (!executedPlans.add(planSignature)) {
                log.warn("AI tool planning repeated and stopped: sessionId={}, step={}, plan={}", sessionId, i + 1, planSignature);
                break;
            }
            log.info("AI tool planning selected: sessionId={}, step={}, tool={}, arguments={}",
                    sessionId, i + 1, plan.tool(), plan.arguments());
            boolean writeTool = userAiToolService.isWriteTool(plan.tool());
            Long targetNoteId = resolveToolTargetNoteId(plan.arguments(), request, writeTool);
            if (!permissionService.canUseTool(plannerPermissions, plan.tool())) {
                String deniedMessage = "AI 权限未允许执行工具：" + plan.tool();
                log.warn("AI tool plan denied by current permissions: sessionId={}, step={}, tool={}",
                        sessionId, i + 1, plan.tool());
                emitToolResult(progressEmitter, currentAssistantMessageId, plan.tool(), targetNoteId, writeTool, false, deniedMessage);
                messages.add(Map.of("role", "system", "content", "工具未执行：" + deniedMessage));
                break;
            }
            emitStatus(progressEmitter, currentAssistantMessageId, mapToolStatus(plan.tool()), "正在执行工具：" + plan.tool());
            emitToolCall(progressEmitter, currentAssistantMessageId, plan.tool(), plan.arguments(), request, writeTool);
            String toolResult = executeToolPlan(userId, plan, request);
            if (toolResult == null || toolResult.isBlank() || toolResult.startsWith(TOOL_EXECUTION_FAILURE_PREFIX)) {
                String failureMessage = toolResult == null || toolResult.isBlank()
                        ? "工具没有返回结果"
                        : toolResult.substring(TOOL_EXECUTION_FAILURE_PREFIX.length());
                log.warn("AI tool execution failed: sessionId={}, step={}, tool={}, reason={}",
                        sessionId, i + 1, plan.tool(), failureMessage);
                emitToolResult(progressEmitter, currentAssistantMessageId, plan.tool(), targetNoteId, writeTool, false, failureMessage);
                messages.add(Map.of("role", "system", "content", "工具未执行：" + failureMessage));
                break;
            }
            log.debug("AI tool execution result: sessionId={}, step={}, tool={}, resultPreview={}",
                    sessionId, i + 1, plan.tool(), previewText(toolResult));
            emitToolResult(progressEmitter, currentAssistantMessageId, plan.tool(), targetNoteId, writeTool, true, previewText(toolResult));
            messages.add(Map.of(
                    "role", "system",
                    "content", "以下是工具调用结果，请仅在相关时引用，并明确说明这是你基于工具检索得到的信息：\n" + toolResult
            ));
            if (userAiToolService.isWriteTool(plan.tool())
                    && Boolean.TRUE.equals(permissionService.getPermissions(userId).getCanReadAllNotes())) {
                appendLatestCurrentNoteSnapshot(messages, userId, request, currentAssistantMessageId, progressEmitter, false);
            }
        }
        emitStatus(progressEmitter, currentAssistantMessageId, "generating_answer", "正在生成最终回答");
        return messages;
    }

    private List<Map<String, String>> buildMessages(Long userId,
                                                    Long sessionId,
                                                    ChatRequest request,
                                                    UserAiPermissionDto permissions,
                                                    boolean includeToolGuide,
                                                    Long currentUserMessageId,
                                                    Long currentAssistantMessageId) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(permissions, includeToolGuide)));

        List<ContextNoteDto> contextNotes = Boolean.TRUE.equals(permissions.getCanReadAllNotes())
                ? getContextNotes(userId, sessionId)
                : Collections.emptyList();
        if (!contextNotes.isEmpty()) {
            String contextBlock = buildContextBlock(contextNotes);
            log.debug("AI context block prepared: sessionId={}, noteCount={}, preview={}",
                    sessionId, contextNotes.size(), previewText(contextBlock));
            messages.add(Map.of("role", "system", "content", contextBlock));
        }

        if (Boolean.TRUE.equals(permissions.getCanReadAllNotes()) && request.getCurrentNoteId() != null) {
            messages.add(Map.of("role", "system", "content", buildCurrentNoteHint(userId, request)));
        }

        if (Boolean.TRUE.equals(permissions.getCanReadAllNotes()) && request.getSelectedText() != null && !request.getSelectedText().isBlank()) {
            log.debug("AI selected text included: sessionId={}, length={}, preview={}",
                    sessionId, request.getSelectedText().trim().length(), previewText(request.getSelectedText().trim()));
            messages.add(Map.of("role", "system", "content",
                    "用户当前额外选中的精确文本如下，请优先结合这段文本回答：\n\n```text\n" + shrink(request.getSelectedText().trim(), MAX_SELECTED_TEXT_CHARS) + "\n```"));
        }

        List<AiChatMessage> history = messageRepository.findTop20BySessionIdAndUserIdOrderByCreatedAtDesc(sessionId, userId);
        Collections.reverse(history);
        int historyCount = 0;
        for (AiChatMessage historyMessage : history) {
            if ("failed".equals(historyMessage.getStatus())
                    || "streaming".equals(historyMessage.getStatus())
                    || Objects.equals(historyMessage.getId(), currentUserMessageId)
                    || Objects.equals(historyMessage.getId(), currentAssistantMessageId)
                    || historyCount >= MAX_HISTORY_MESSAGES) {
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

    private String buildSystemPrompt(UserAiPermissionDto permissions, boolean includeToolGuide) {
        String base = """
                你是 SlothNote 的 AI 助手。
                回答要求：
                1. 语气自然、简洁、有人味，不要客服腔、不要模板化开场。
                2. 先直接回答问题，再补充建议或下一步。
                3. 输出必须兼容 Markdown，标题、列表、代码块、表格都使用标准 Markdown 语法。
                4. 如果回答引用了用户选择的笔记内容，要明确说明“根据你选择的笔记，我理解到……”，不要假装这些内容来自你自身记忆。
                5. 不要输出原始 HTML。
                6. 优先使用最新上下文；如果当前笔记可能已变更，要以最新工具结果和最新笔记快照为准，不要重复引用过期内容。
                7. 当用户要求直接修改笔记时，只有在可用工具中存在对应写入工具时才执行；否则简洁说明该功能需要在设置中开启。
                8. 当用户要求“重新获取当前笔记内容”或问题明显依赖当前最新正文时，应优先依据最新当前笔记快照回答，不要被旧的 selectedText 干扰。
                """;
        if (includeToolGuide) {
            base = base + buildAllowedToolGuide(permissions);
        }
        return base + "\n当前任务：进行自然对话，必要时给出结构化建议。";
    }

    private String buildAllowedToolGuide(UserAiPermissionDto permissions) {
        StringBuilder guide = new StringBuilder("\n已授权的 AI 工具与常见处理流程：\n");
        if (Boolean.TRUE.equals(permissions.getCanReadAllNotes())) {
            guide.append("- 查询或阅读笔记：用户要求查找、列出或回忆笔记时，可搜索并读取其名下笔记。\n")
                    .append("- 当前笔记上下文：需要核对最新正文、标题、简介或封面时，可读取当前打开笔记。\n");
        }
        if (Boolean.TRUE.equals(permissions.getCanWriteNoteContent())) {
            guide.append("- 正文编辑：用户明确要求替换选区、在选区后插入或追加内容时，可编辑当前打开笔记。\n");
        }
        if (Boolean.TRUE.equals(permissions.getCanWriteNoteTitle())) {
            guide.append("- 标题优化：用户明确要求修改当前笔记标题时，可直接更新标题。\n");
        }
        if (Boolean.TRUE.equals(permissions.getCanWriteNoteSummary())) {
            guide.append("- 简介整理：用户明确要求生成并保存当前笔记简介时，可直接更新简介。\n");
        }
        if (Boolean.TRUE.equals(permissions.getCanWriteNoteCover())) {
            guide.append("- 封面调整：用户明确要求更换当前笔记封面时，可从可用封面中选择并更新。\n");
        }
        guide.append("工具使用原则：只在用户明确需要笔记数据或修改笔记时使用；不得臆造 noteId；写工具只能作用于当前打开且属于该用户的笔记。");
        return guide.toString();
    }

    private String buildPlannerPrompt(UserAiPermissionDto permissions) {
        StringBuilder prompt = new StringBuilder("""
                你是一个工具规划器。你只输出单个 JSON 对象，不输出解释。
                你的任务是判断是否需要调用已授权工具帮助回答用户问题。
                可选输出始终包含：{"tool":"none"}
                """);
        if (Boolean.TRUE.equals(permissions.getCanReadAllNotes())) {
            prompt.append("- {\"tool\":\"search_user_notes\",\"arguments\":{\"query\":\"关键词\",\"limit\":5}}\n")
                    .append("- {\"tool\":\"read_note\",\"arguments\":{\"noteId\":123,\"maxChars\":1200}}\n")
                    .append("- {\"tool\":\"get_current_note\",\"arguments\":{\"maxChars\":1500}}\n");
        }
        if (Boolean.TRUE.equals(permissions.getCanWriteNoteContent())) {
            prompt.append("- {\"tool\":\"replace_selected_text\",\"arguments\":{\"replacement\":\"替换后的内容\"}}\n")
                    .append("- {\"tool\":\"append_to_current_note\",\"arguments\":{\"content\":\"要追加的内容\"}}\n")
                    .append("- {\"tool\":\"insert_after_selected_text\",\"arguments\":{\"content\":\"要插入的内容\"}}\n");
        }
        if (Boolean.TRUE.equals(permissions.getCanWriteNoteTitle())) {
            prompt.append("- {\"tool\":\"update_current_note_title\",\"arguments\":{\"title\":\"新的标题\"}}\n");
        }
        if (Boolean.TRUE.equals(permissions.getCanWriteNoteSummary())) {
            prompt.append("- {\"tool\":\"save_current_note_summary\",\"arguments\":{\"summary\":\"新的摘要\"}}\n");
        }
        if (Boolean.TRUE.equals(permissions.getCanWriteNoteCover())) {
            prompt.append("- {\"tool\":\"update_current_note_cover\",\"arguments\":{\"cover\":\"1-001\"}}\n");
        }
        prompt.append("""
                规则：
                - 现有上下文足够或普通闲聊时，输出 {"tool":"none"}。
                - 仅可选择上方列出的工具，绝不能使用未授权工具。
                - 只有用户明确要求修改当前打开笔记时才选择写工具。
                - replace_selected_text 和 insert_after_selected_text 只能在已有 selectedText 时使用。
                - update_current_note_cover 只能使用可用封面选项或 none。
                - 不要臆造 noteId；写工具不能指定其他笔记。
                """);
        return prompt.toString();
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

    private String buildCurrentNoteHint(Long userId, ChatRequest request) {
        StringBuilder builder = new StringBuilder("当前正在编辑的笔记上下文（服务端最新快照）：\n");
        builder.append("- noteId: ").append(request.getCurrentNoteId()).append('\n');
        try {
            Map<String, Object> currentNote = userAiToolService.getCurrentNote(userId, request, MAX_CURRENT_NOTE_CHARS);
            if (currentNote.get("title") instanceof String title && !title.isBlank()) {
                builder.append("- 标题: ").append(title.trim()).append('\n');
            }
            if (currentNote.get("summary") instanceof String summary && !summary.isBlank()) {
                builder.append("- 摘要: ").append(summary.trim()).append('\n');
            }
            builder.append("- 当前封面: ")
                    .append(currentNote.get("cover") instanceof String cover && !cover.isBlank() ? cover.trim() : "无")
                    .append('\n');
            if (currentNote.get("content") instanceof String content && !content.isBlank()) {
                builder.append("- 最新正文片段:\n```text\n")
                        .append(content)
                        .append("\n```\n");
            }
        } catch (Exception e) {
            log.warn("AI current note hint fallback: noteId={}, error={}", request.getCurrentNoteId(), e.getMessage());
            if (request.getCurrentNoteTitle() != null && !request.getCurrentNoteTitle().isBlank()) {
                builder.append("- 标题: ").append(request.getCurrentNoteTitle().trim()).append('\n');
            }
            builder.append("- 当前封面: ")
                    .append(request.getCurrentNoteCover() == null || request.getCurrentNoteCover().isBlank() ? "无" : request.getCurrentNoteCover().trim())
                    .append('\n');
        }
        builder.append("- 可用封面选项: 1-001 到 1-014，以及 2-001 到 2-006；移除封面可用 none\n");
        if (request.getSelectedText() != null && !request.getSelectedText().isBlank()) {
            builder.append("- 当前选中文本（仅作选区参考，可能早于最新正文状态）: ```text\n")
                    .append(shrink(request.getSelectedText().trim(), MAX_SELECTED_TEXT_CHARS))
                    .append("\n```\n");
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
        message.setMessageType("chat");
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
        message.setMessageType("chat");
        message.setContentMd("");
        message.setStatus("streaming");
        session.setLastMessageAt(LocalDateTime.now());
        sessionRepository.save(session);
        return messageRepository.save(message);
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

    private ToolPlan planToolUse(AiConfigService.RuntimeConfig aiConfig,
                                 List<Map<String, String>> messages,
                                 String userQuestion,
                                 UserAiPermissionDto permissions) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", aiConfig.model());
            requestBody.put("stream", false);
            requestBody.put("temperature", aiConfig.plannerTemperature());
            requestBody.put("max_tokens", aiConfig.plannerMaxTokens());
            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", buildPlannerPrompt(permissions)),
                    Map.of("role", "user", "content", "用户问题：" + userQuestion),
                    Map.of("role", "user", "content", "已有消息上下文摘要：" + summarizeMessagesForPlanner(messages))
            ));

            String content = callAiForSingleMessage(aiConfig, requestBody);
            if (content == null || content.isBlank()) {
                log.warn("AI tool planner returned empty content");
                return null;
            }
            String json = extractJson(content);
            log.debug("AI tool planner raw response: content={}, extractedJson={}", previewText(content), previewText(json));
            JsonNode node = objectMapper.readTree(json);
            String tool = node.path("tool").asText("none");
            JsonNode arguments = node.path("arguments");
            return new ToolPlan(tool, arguments);
        } catch (Exception e) {
            log.error("AI tool planner failed: userQuestion={}, error={}", previewText(userQuestion), e.getMessage(), e);
            return null;
        }
    }

    private String executeToolPlan(Long userId,
                                   ToolPlan plan,
                                   ChatRequest request) {
        try {
            permissionService.requireToolAccess(permissionService.getPermissions(userId), plan.tool());
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
            if (UserAiToolService.GET_CURRENT_NOTE.equals(plan.tool())) {
                Map<String, Object> data = userAiToolService.getCurrentNote(userId, request, plan.arguments().path("maxChars").asInt(1500));
                return "工具 get_current_note 返回结果：\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            }
            if (UserAiToolService.REPLACE_SELECTED_TEXT.equals(plan.tool())) {
                Map<String, Object> result = userAiToolService.replaceSelectedText(userId, request, plan.arguments().path("replacement").asText(""));
                return "工具 replace_selected_text 返回结果：\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            }
            if (UserAiToolService.APPEND_TO_CURRENT_NOTE.equals(plan.tool())) {
                Map<String, Object> result = userAiToolService.appendToCurrentNote(userId, request, plan.arguments().path("content").asText(""));
                return "工具 append_to_current_note 返回结果：\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            }
            if (UserAiToolService.INSERT_AFTER_SELECTED_TEXT.equals(plan.tool())) {
                Map<String, Object> result = userAiToolService.insertAfterSelectedText(userId, request, plan.arguments().path("content").asText(""));
                return "工具 insert_after_selected_text 返回结果：\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            }
            if (UserAiToolService.UPDATE_CURRENT_NOTE_TITLE.equals(plan.tool())) {
                Map<String, Object> result = userAiToolService.updateCurrentNoteTitle(userId, request, plan.arguments().path("title").asText(""));
                return "工具 update_current_note_title 返回结果：\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            }
            if (UserAiToolService.SAVE_CURRENT_NOTE_SUMMARY.equals(plan.tool())) {
                Map<String, Object> result = userAiToolService.saveCurrentNoteSummary(userId, request, plan.arguments().path("summary").asText(""));
                return "工具 save_current_note_summary 返回结果：\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            }
            if (UserAiToolService.UPDATE_CURRENT_NOTE_COVER.equals(plan.tool())) {
                Map<String, Object> result = userAiToolService.updateCurrentNoteCover(userId, request, plan.arguments().path("cover").asText(""));
                return "工具 update_current_note_cover 返回结果：\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
            }
            log.warn("AI tool execution got unsupported tool: userId={}, tool={}", userId, plan.tool());
        } catch (Exception e) {
            log.error("AI tool execution failed: userId={}, tool={}, arguments={}, error={}",
                    userId, plan.tool(), plan.arguments(), e.getMessage(), e);
            return TOOL_EXECUTION_FAILURE_PREFIX + (e.getMessage() == null ? "工具执行失败" : e.getMessage());
        }
        return TOOL_EXECUTION_FAILURE_PREFIX + "不支持的工具：" + plan.tool();
    }

    private boolean shouldRefreshCurrentNoteContext(ChatRequest request, UserAiPermissionDto permissions) {
        if (request == null || request.getCurrentNoteId() == null || !Boolean.TRUE.equals(permissions.getCanReadAllNotes())) {
            return false;
        }
        String text = request.getText() == null ? "" : request.getText().trim();
        if (text.isEmpty()) {
            return false;
        }
        return text.contains("当前笔记")
                || text.contains("这篇笔记")
                || text.contains("这篇文章")
                || text.contains("重新获取")
                || text.contains("重新读取")
                || text.contains("最新内容")
                || text.contains("帮我修改")
                || text.contains("改为")
                || text.contains("替换")
                || text.contains("插入")
                || text.contains("追加")
                || text.contains("标题")
                || text.contains("简介")
                || text.contains("摘要")
                || text.contains("封面");
    }

    private void appendLatestCurrentNoteSnapshot(List<Map<String, String>> messages,
                                                 Long userId,
                                                 ChatRequest request,
                                                 Long assistantMessageId,
                                                 Consumer<Map<String, Object>> progressEmitter,
                                                 boolean initialSync) {
        if (request == null
                || request.getCurrentNoteId() == null
                || !permissionService.canUseTool(permissionService.getPermissions(userId), UserAiToolService.GET_CURRENT_NOTE)) {
            return;
        }
        try {
            emitStatus(
                    progressEmitter,
                    assistantMessageId,
                    "reading_note",
                    initialSync ? "正在同步当前笔记最新内容" : "正在校验更新后的最新笔记内容"
            );
            emitToolCall(
                    progressEmitter,
                    assistantMessageId,
                    UserAiToolService.GET_CURRENT_NOTE,
                    objectMapper.createObjectNode().put("maxChars", MAX_CURRENT_NOTE_CHARS),
                    request,
                    false
            );
            Map<String, Object> currentNote = userAiToolService.getCurrentNote(userId, request, MAX_CURRENT_NOTE_CHARS);
            String snapshot = formatCurrentNoteSnapshot(currentNote, initialSync);
            messages.add(Map.of("role", "system", "content", snapshot));
            emitToolResult(progressEmitter, assistantMessageId, UserAiToolService.GET_CURRENT_NOTE, request.getCurrentNoteId(), false, true, initialSync ? "已获取当前笔记最新内容" : "已同步更新后的最新笔记内容");
        } catch (Exception e) {
            log.warn("AI current note sync failed: noteId={}, error={}", request.getCurrentNoteId(), e.getMessage());
            emitToolResult(progressEmitter, assistantMessageId, UserAiToolService.GET_CURRENT_NOTE, request.getCurrentNoteId(), false, false, "当前笔记最新内容同步失败：" + e.getMessage());
        }
    }

    private String formatCurrentNoteSnapshot(Map<String, Object> currentNote, boolean initialSync) {
        StringBuilder builder = new StringBuilder(initialSync
                ? "以下是当前笔记的最新同步结果，请优先以这份内容为准：\n"
                : "以下是写入后重新同步到的当前笔记最新结果，请覆盖之前可能过期的理解：\n");
        builder.append("- noteId: ").append(currentNote.get("noteId")).append('\n');
        if (currentNote.get("title") instanceof String title && !title.isBlank()) {
            builder.append("- 标题: ").append(title.trim()).append('\n');
        }
        if (currentNote.get("summary") instanceof String summary && !summary.isBlank()) {
            builder.append("- 摘要: ").append(summary.trim()).append('\n');
        }
        builder.append("- 当前封面: ")
                .append(currentNote.get("cover") instanceof String cover && !cover.isBlank() ? cover.trim() : "无")
                .append('\n');
        if (initialSync && currentNote.get("selectedText") instanceof String selectedText && !selectedText.isBlank()) {
            builder.append("- 当前选中文本: ```text\n")
                    .append(shrink(selectedText.trim(), MAX_SELECTED_TEXT_CHARS))
                    .append("\n```\n");
        }
        if (currentNote.get("content") instanceof String content && !content.isBlank()) {
            builder.append("- 最新正文片段:\n```text\n")
                    .append(content)
                    .append("\n```\n");
        }
        return builder.toString();
    }

    private void emitStatus(Consumer<Map<String, Object>> emitter, Long assistantMessageId, String status, String label) {
        if (emitter == null) {
            return;
        }
        emitter.accept(Map.of(
                "type", "status",
                "assistantMessageId", assistantMessageId,
                "status", status,
                "label", label
        ));
    }

    private void emitToolCall(Consumer<Map<String, Object>> emitter,
                              Long assistantMessageId,
                              String tool,
                              JsonNode arguments,
                              ChatRequest request,
                              boolean writeTool) {
        if (emitter == null) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "tool_call");
        payload.put("assistantMessageId", assistantMessageId);
        payload.put("tool", tool);
        payload.put("writeTool", writeTool);
        Long targetNoteId = resolveToolTargetNoteId(arguments, request, writeTool);
        if (targetNoteId != null) {
            payload.put("noteId", targetNoteId);
        }
        payload.put("summary", userAiToolService.summarizePlan(tool, arguments, request));
        payload.put("arguments", arguments == null ? Map.of() : objectMapper.convertValue(arguments, Map.class));
        emitter.accept(payload);
    }

    private void emitToolResult(Consumer<Map<String, Object>> emitter,
                                Long assistantMessageId,
                                String tool,
                                Long noteId,
                                boolean writeTool,
                                boolean success,
                                String summary) {
        if (emitter == null) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "tool_result");
        payload.put("assistantMessageId", assistantMessageId);
        payload.put("tool", tool);
        payload.put("writeTool", writeTool);
        payload.put("success", success);
        payload.put("summary", summary);
        if (noteId != null) {
            payload.put("noteId", noteId);
        }
        emitter.accept(payload);
    }

    private Long resolveToolTargetNoteId(JsonNode arguments, ChatRequest request, boolean writeTool) {
        if (writeTool) {
            return request == null ? null : request.getCurrentNoteId();
        }
        if (arguments != null && arguments.hasNonNull("noteId")) {
            return arguments.get("noteId").asLong();
        }
        return request == null ? null : request.getCurrentNoteId();
    }

    private String mapToolStatus(String tool) {
        if (UserAiToolService.SEARCH_USER_NOTES.equals(tool)) {
            return "searching_notes";
        }
        if (UserAiToolService.READ_NOTE.equals(tool) || UserAiToolService.GET_CURRENT_NOTE.equals(tool)) {
            return "reading_note";
        }
        if (userAiToolService.isWriteTool(tool)) {
            return "editing_note";
        }
        return "planning";
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

    private String callAiForSingleMessage(AiConfigService.RuntimeConfig aiConfig, Map<String, Object> requestBody) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfig.apiKey());
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        log.debug("AI single-call request: payloadChars={}, payloadPreview={}", jsonBody.length(), previewText(jsonBody));
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.execute(resolveChatCompletionsUrl(aiConfig.baseUrl()), HttpMethod.POST, httpRequest -> {
            httpRequest.getHeaders().addAll(headers);
            httpRequest.getBody().write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }, response -> {
            log.debug("AI single-call response metadata: statusCode={}, contentType={}",
                    response.getStatusCode(), response.getHeaders().getContentType());
            JsonNode root = objectMapper.readTree(response.getBody());
            log.debug("AI single-call raw response: {}", previewText(root.toString()));
            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                return null;
            }
            return choices.get(0).path("message").path("content").asText(null);
        });
    }

    private String resolveChatCompletionsUrl(String apiBaseUrl) {
        String normalized = apiBaseUrl == null ? "" : apiBaseUrl.trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("AI Base URL 未配置");
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/v1")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/v1/chat/completions";
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

    private String previewText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= DEBUG_PREVIEW_CHARS) {
            return normalized;
        }
        return normalized.substring(0, DEBUG_PREVIEW_CHARS) + "...";
    }

    private String summarizeMessageList(List<Map<String, String>> messages) {
        return messages.stream()
                .map(message -> "[" + message.getOrDefault("role", "?") + "] " + previewText(message.getOrDefault("content", "")))
                .collect(Collectors.joining(" | "));
    }

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
