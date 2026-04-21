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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @Value("${ai.openai.api-key}")
    private String apiKey;

    @Value("${ai.openai.base-url}")
    private String apiBaseUrl;

    @Value("${ai.openai.model}")
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

    public AiToolPlanPreviewDto previewToolPlan(Long userId, ChatRequest request) {
        if (request == null || request.getText() == null || request.getText().trim().isEmpty()) {
            return new AiToolPlanPreviewDto("none", "{}", false, false, "本次无需调用工具。");
        }
        ToolPlan plan = planToolUse(buildPreviewMessages(userId, request), request.getText().trim());
        if (plan == null || "none".equals(plan.tool())) {
            return new AiToolPlanPreviewDto("none", "{}", false, false, "本次无需调用工具。");
        }
        String argumentsJson;
        try {
            argumentsJson = objectMapper.writeValueAsString(plan.arguments() == null ? objectMapper.createObjectNode() : plan.arguments());
        } catch (Exception e) {
            argumentsJson = "{}";
        }
        boolean writeTool = userAiToolService.isWriteTool(plan.tool());
        return new AiToolPlanPreviewDto(
                plan.tool(),
                argumentsJson,
                writeTool,
                writeTool,
                userAiToolService.summarizePlan(plan.tool(), plan.arguments(), request)
        );
    }

    public SseEmitter chat(Long userId, ChatRequest request) {
        String text = request.getText() == null ? "" : request.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("text cannot be empty");
        }

        log.info("AI chat start: userId={}, sessionId={}, type={}, textLength={}, contextNoteCount={}",
                userId,
                request.getSessionId(),
                request.getMessageType(),
                text.length(),
                request.getContextNoteIds() == null ? 0 : request.getContextNoteIds().size());
        log.debug("AI chat request detail: selectedTextLength={}, contextNoteIds={}, textPreview={}",
                request.getSelectedText() == null ? 0 : request.getSelectedText().trim().length(),
                request.getContextNoteIds(),
                previewText(text));

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

        Thread streamThread = new Thread(
                () -> streamAiResponse(emitter, stopFlag, session, userId, request, assistantMessage, userMessage),
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

            Map<String, Object> payload = buildAiRequestPayload(
                    userId,
                    session.getId(),
                    request,
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
            headers.set("Authorization", "Bearer " + apiKey);

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.execute(resolveChatCompletionsUrl(), HttpMethod.POST, httpRequest -> {
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

    private Map<String, Object> buildAiRequestPayload(Long userId,
                                                      Long sessionId,
                                                      ChatRequest request,
                                                      Long currentUserMessageId,
                                                      Long currentAssistantMessageId,
                                                      Consumer<Map<String, Object>> progressEmitter) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("stream", true);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);
        requestBody.put("messages", buildMessagesWithTools(userId, sessionId, request, currentUserMessageId, currentAssistantMessageId, progressEmitter));
        return requestBody;
    }

    private List<Map<String, String>> buildMessagesWithTools(Long userId,
                                                             Long sessionId,
                                                             ChatRequest request,
                                                             Long currentUserMessageId,
                                                             Long currentAssistantMessageId,
                                                             Consumer<Map<String, Object>> progressEmitter) {
        List<Map<String, String>> messages = buildMessages(userId, sessionId, request, true, currentUserMessageId, currentAssistantMessageId);
        log.debug("AI base messages prepared: sessionId={}, count={}, summary={}",
                sessionId, messages.size(), summarizeMessageList(messages));
        Set<String> executedPlans = new HashSet<>();
        if (shouldRefreshCurrentNoteContext(request)) {
            appendLatestCurrentNoteSnapshot(messages, userId, request, currentAssistantMessageId, progressEmitter, true);
        }
        emitStatus(progressEmitter, currentAssistantMessageId, "planning", "正在规划是否需要调用工具");
        for (int i = 0; i < MAX_TOOL_STEPS; i++) {
            ToolPlan plan = i == 0 ? resolveRequestedWritePlan(request, messages) : null;
            if (plan == null) {
                plan = planToolUse(messages, request.getText().trim());
            }
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
            emitStatus(progressEmitter, currentAssistantMessageId, mapToolStatus(plan.tool()), "正在执行工具：" + plan.tool());
            emitToolCall(progressEmitter, currentAssistantMessageId, plan.tool(), plan.arguments(), request, userAiToolService.isWriteTool(plan.tool()));
            String toolResult = executeToolPlan(userId, plan, request);
            if (toolResult == null || toolResult.isBlank()) {
                log.warn("AI tool execution returned empty: sessionId={}, step={}, tool={}", sessionId, i + 1, plan.tool());
                emitToolResult(progressEmitter, currentAssistantMessageId, plan.tool(), false, "工具没有返回结果");
                break;
            }
            log.debug("AI tool execution result: sessionId={}, step={}, tool={}, resultPreview={}",
                    sessionId, i + 1, plan.tool(), previewText(toolResult));
            emitToolResult(progressEmitter, currentAssistantMessageId, plan.tool(), true, previewText(toolResult));
            messages.add(Map.of(
                    "role", "system",
                    "content", "以下是工具调用结果，请仅在相关时引用，并明确说明这是你基于工具检索得到的信息：\n" + toolResult
            ));
            if (userAiToolService.isWriteTool(plan.tool())) {
                appendLatestCurrentNoteSnapshot(messages, userId, request, currentAssistantMessageId, progressEmitter, false);
            }
        }
        emitStatus(progressEmitter, currentAssistantMessageId, "generating_answer", "正在生成最终回答");
        return messages;
    }

    private List<Map<String, String>> buildMessages(Long userId,
                                                    Long sessionId,
                                                    ChatRequest request,
                                                    boolean includeToolGuide,
                                                    Long currentUserMessageId,
                                                    Long currentAssistantMessageId) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(request.getMessageType(), includeToolGuide)));

        List<ContextNoteDto> contextNotes = getContextNotes(userId, sessionId);
        if (!contextNotes.isEmpty()) {
            String contextBlock = buildContextBlock(contextNotes);
            log.debug("AI context block prepared: sessionId={}, noteCount={}, preview={}",
                    sessionId, contextNotes.size(), previewText(contextBlock));
            messages.add(Map.of("role", "system", "content", contextBlock));
        }

        if (request.getCurrentNoteId() != null) {
            messages.add(Map.of("role", "system", "content", buildCurrentNoteHint(userId, request)));
        }

        if (request.getSelectedText() != null && !request.getSelectedText().isBlank()) {
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

    private String buildSystemPrompt(String messageType, boolean includeToolGuide) {
        String normalizedType = messageType == null || messageType.isBlank() ? "chat" : messageType;
        String base = """
                你是 SlothNote 的 AI 助手。
                回答要求：
                1. 语气自然、简洁、有人味，不要客服腔、不要模板化开场。
                2. 先直接回答问题，再补充建议或下一步。
                3. 输出必须兼容 Markdown，标题、列表、代码块、表格都使用标准 Markdown 语法。
                4. 如果回答引用了用户选择的笔记内容，要明确说明“根据你选择的笔记，我理解到……”，不要假装这些内容来自你自身记忆。
                5. 不要输出原始 HTML。
                6. 优先使用最新上下文；如果当前笔记可能已变更，要以最新工具结果和最新笔记快照为准，不要重复引用过期内容。
                7. 如果你打算让用户把内容插入笔记，优先把可直接插入的内容放进标准 fenced code block。
                8. 当用户要求你直接修改当前笔记时：
                   - 如果已经授权写入，就直接执行并明确说明改了什么。
                   - 如果还没授权，不要反复兜圈子；用 1 到 2 句说明限制，并给出唯一清晰的下一步。
                9. 当用户要求“重新获取当前笔记内容”或问题明显依赖当前最新正文时，应优先依据最新当前笔记快照回答，不要被旧的 selectedText 干扰。
                """;
        if (includeToolGuide) {
            base = base + """
                    
                    可用工具：
                    - search_user_notes：搜索当前用户名下的笔记列表。
                    - read_note：读取当前用户某篇笔记的标题、摘要和正文片段。
                    - get_current_note：读取当前正在编辑的笔记上下文。
                    - replace_selected_text：替换当前选中文本。
                    - append_to_current_note：向当前笔记末尾追加内容。
                    - insert_after_selected_text：在当前选中文本后插入内容。
                    - update_current_note_title：更新当前笔记标题。
                    - save_current_note_summary：更新当前笔记摘要。
                    - update_current_note_cover：更新当前笔记封面。
                    工具使用原则：
                    - 只有当用户明确要求查找、列出、回忆某篇笔记内容，或当前上下文不足以回答时，才考虑工具。
                    - 当用户询问“当前笔记现在是什么内容”“重新获取当前笔记”等，优先使用 get_current_note。
                    - 当用户明确要求修改当前打开笔记，且当前笔记 ID 已提供时，可以使用写工具。
                    - 不要臆造 noteId；写工具只能针对当前打开笔记。
                    - 不要为了普通闲聊或已知问题滥用工具。
                    """;
        }
        return switch (normalizedType) {
            case "explain" -> base + "\n当前任务：解释用户给出的内容，优先用简洁的分点说明概念、作用和上下文。";
            case "polish" -> base + "\n当前任务：润色用户给出的内容，先给优化后的文本，再简短说明优化点。";
            case "summary" -> base + "\n当前任务：概括用户给出的内容，直接输出一段清晰的 Markdown 摘要，控制在 200 字左右。";
            case "agent_update_summary" -> base + "\n当前任务：基于当前打开笔记生成更适合作为简介的摘要，并优先通过工具直接写入当前笔记摘要。";
            case "agent_generate_summary_to_note" -> base + "\n当前任务：阅读当前打开笔记，生成清晰摘要，并优先通过工具直接写入当前笔记摘要。";
            case "agent_update_title" -> base + "\n当前任务：根据当前打开笔记内容拟定更准确简洁的标题，并优先通过工具直接更新当前笔记标题。";
            case "agent_update_cover" -> base + "\n当前任务：根据当前打开笔记内容，在可用封面选项中挑选最匹配的封面，并优先通过工具直接更新当前笔记封面。";
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
        builder.append("- 可用写权限: ").append(Boolean.TRUE.equals(request.getAllowCurrentNoteWrite()) ? "已允许修改当前笔记" : "未允许修改当前笔记");
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

    private List<Map<String, String>> buildPreviewMessages(Long userId, ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemPrompt(request.getMessageType(), true)));
        if (request.getContextNoteIds() != null && !request.getContextNoteIds().isEmpty()) {
            List<NoteInfo> notes = aiNoteContextRepository.findByIdInAndUserIdAndIsDeleted(request.getContextNoteIds(), userId, 0);
            List<ContextNoteDto> previewNotes = notes.stream()
                    .map(note -> new ContextNoteDto(note.getId(), note.getNoteTitle(), note.getNoteSummary(), note.getNoteAvatar() == null ? null : new String(note.getNoteAvatar())))
                    .toList();
            if (!previewNotes.isEmpty()) {
                messages.add(Map.of("role", "system", "content", buildContextBlock(previewNotes)));
            }
        }
        if (request.getCurrentNoteId() != null) {
            messages.add(Map.of("role", "system", "content", buildCurrentNoteHint(userId, request)));
        }
        if (request.getSelectedText() != null && !request.getSelectedText().isBlank()) {
            messages.add(Map.of("role", "system", "content",
                    "用户当前额外选中的精确文本如下，请优先结合这段文本回答：\n\n```text\n" + shrink(request.getSelectedText().trim(), MAX_SELECTED_TEXT_CHARS) + "\n```"));
        }
        messages.add(Map.of("role", "user", "content", request.getText().trim()));
        return messages;
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
                            4. {"tool":"get_current_note","arguments":{"maxChars":1500}}
                            5. {"tool":"replace_selected_text","arguments":{"replacement":"替换后的纯文本内容"}}
                            6. {"tool":"append_to_current_note","arguments":{"content":"要追加的内容，可以包含 fenced code block"}}
                            7. {"tool":"insert_after_selected_text","arguments":{"content":"要插入的纯文本内容"}}
                            8. {"tool":"update_current_note_title","arguments":{"title":"新的标题"}}
                            9. {"tool":"save_current_note_summary","arguments":{"summary":"新的摘要"}}
                            10. {"tool":"update_current_note_cover","arguments":{"cover":"1-001"}}
                            规则：
                            - 如果现有上下文已经足够，输出 {"tool":"none"}。
                            - 只有当用户要搜索、列出、定位、回忆笔记内容时，才考虑工具。
                            - 如果用户要求重新获取当前笔记、核对当前笔记最新内容、继续基于刚修改后的笔记操作，优先使用 get_current_note。
                            - 只有当用户明确要求修改当前打开笔记时，才考虑写工具。
                            - replace_selected_text 和 insert_after_selected_text 只能在已有 selectedText 时使用。
                            - update_current_note_cover 只能使用可用封面选项或 none。
                            - 写工具只能针对当前打开笔记，不要输出其他 noteId。
                            - 不要臆造 noteId。
                            - 输出必须是单个 JSON 对象。
                            """),
                    Map.of("role", "user", "content", "用户问题：" + userQuestion),
                    Map.of("role", "user", "content", "已有消息上下文摘要：" + summarizeMessagesForPlanner(messages))
            ));

            String content = callAiForSingleMessage(requestBody);
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

    private ToolPlan resolveRequestedWritePlan(ChatRequest request, List<Map<String, String>> messages) {
        if (!Boolean.TRUE.equals(request.getAllowCurrentNoteWrite())) {
            return null;
        }
        if (request.getPlannedToolName() == null || request.getPlannedToolName().isBlank()) {
            return null;
        }
        if (!userAiToolService.isWriteTool(request.getPlannedToolName())) {
            return null;
        }
        try {
            JsonNode arguments = request.getPlannedToolArgumentsJson() == null || request.getPlannedToolArgumentsJson().isBlank()
                    ? objectMapper.createObjectNode()
                    : objectMapper.readTree(request.getPlannedToolArgumentsJson());
            return new ToolPlan(request.getPlannedToolName(), arguments);
        } catch (Exception e) {
            log.warn("AI requested write plan parse failed: {}", e.getMessage());
            return null;
        }
    }

    private String executeToolPlan(Long userId, ToolPlan plan, ChatRequest request) {
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
            if (UserAiToolService.GET_CURRENT_NOTE.equals(plan.tool())) {
                Map<String, Object> data = userAiToolService.getCurrentNote(userId, request, plan.arguments().path("maxChars").asInt(1500));
                return "工具 get_current_note 返回结果：\n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            }
            if (userAiToolService.isWriteTool(plan.tool()) && !Boolean.TRUE.equals(request.getAllowCurrentNoteWrite())) {
                return "工具 " + plan.tool() + " 未执行：当前请求未授权修改当前笔记。";
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
        }
        return null;
    }

    private boolean shouldRefreshCurrentNoteContext(ChatRequest request) {
        if (request == null || request.getCurrentNoteId() == null) {
            return false;
        }
        if (isAgentMessageType(request.getMessageType())) {
            return true;
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

    private boolean isAgentMessageType(String messageType) {
        return messageType != null && messageType.startsWith("agent_");
    }

    private void appendLatestCurrentNoteSnapshot(List<Map<String, String>> messages,
                                                 Long userId,
                                                 ChatRequest request,
                                                 Long assistantMessageId,
                                                 Consumer<Map<String, Object>> progressEmitter,
                                                 boolean initialSync) {
        if (request == null || request.getCurrentNoteId() == null) {
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
            emitToolResult(progressEmitter, assistantMessageId, UserAiToolService.GET_CURRENT_NOTE, true, initialSync ? "已获取当前笔记最新内容" : "已同步更新后的最新笔记内容");
        } catch (Exception e) {
            log.warn("AI current note sync failed: noteId={}, error={}", request.getCurrentNoteId(), e.getMessage());
            emitToolResult(progressEmitter, assistantMessageId, UserAiToolService.GET_CURRENT_NOTE, false, "当前笔记最新内容同步失败：" + e.getMessage());
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
        payload.put("summary", userAiToolService.summarizePlan(tool, arguments, request));
        payload.put("arguments", arguments == null ? Map.of() : objectMapper.convertValue(arguments, Map.class));
        emitter.accept(payload);
    }

    private void emitToolResult(Consumer<Map<String, Object>> emitter,
                                Long assistantMessageId,
                                String tool,
                                boolean success,
                                String summary) {
        if (emitter == null) {
            return;
        }
        emitter.accept(Map.of(
                "type", "tool_result",
                "assistantMessageId", assistantMessageId,
                "tool", tool,
                "success", success,
                "summary", summary
        ));
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

    private String callAiForSingleMessage(Map<String, Object> requestBody) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        log.debug("AI single-call request: payloadChars={}, payloadPreview={}", jsonBody.length(), previewText(jsonBody));
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.execute(resolveChatCompletionsUrl(), HttpMethod.POST, httpRequest -> {
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

    private String resolveChatCompletionsUrl() {
        String normalized = apiBaseUrl == null ? "" : apiBaseUrl.trim();
        if (normalized.isEmpty()) {
            throw new IllegalStateException("ai.openai.base-url is blank");
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
