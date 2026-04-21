package org.example.backend.user.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.backend.common.domain.Note;
import org.example.backend.common.entity.NoteInfo;
import org.example.backend.user.ai.dto.AiToolDefinitionDto;
import org.example.backend.user.ai.dto.ChatRequest;
import org.example.backend.user.ai.dto.ContextNoteDto;
import org.example.backend.user.note.note.repository.UNoteInfoRep;
import org.example.backend.user.note.note.repository.UNoteRepM;
import org.example.backend.user.note.note.service.NoteVersionService;
import org.example.backend.user.note.noteTree.enums.PutNoteTitleEnum;
import org.example.backend.user.note.noteTree.enums.PutUNTContextEnum;
import org.example.backend.user.note.noteTree.service.PutNoteTreeService;
import org.example.backend.user.note.noteCover.enums.PatchContextEnum;
import org.example.backend.user.note.noteCover.service.PatchUNNoteCoverService;
import org.example.backend.user.note.search.dto.SearchResultDto;
import org.example.backend.user.note.search.repository.SearchNoteInfoRep;
import org.example.backend.user.note.search.service.SearchService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserAiToolService {

    public static final String SEARCH_USER_NOTES = "search_user_notes";
    public static final String READ_NOTE = "read_note";
    public static final String GET_CURRENT_NOTE = "get_current_note";
    public static final String REPLACE_SELECTED_TEXT = "replace_selected_text";
    public static final String APPEND_TO_CURRENT_NOTE = "append_to_current_note";
    public static final String INSERT_AFTER_SELECTED_TEXT = "insert_after_selected_text";
    public static final String UPDATE_CURRENT_NOTE_TITLE = "update_current_note_title";
    public static final String SAVE_CURRENT_NOTE_SUMMARY = "save_current_note_summary";
    public static final String UPDATE_CURRENT_NOTE_COVER = "update_current_note_cover";

    private static final List<String> NOTE_COVER_OPTIONS = List.of(
            "1-001", "1-002", "1-003", "1-004", "1-005", "1-006", "1-007",
            "1-008", "1-009", "1-010", "1-011", "1-012", "1-013", "1-014",
            "2-001", "2-002", "2-003", "2-004", "2-005", "2-006"
    );

    private static final Pattern FENCED_BLOCK_PATTERN = Pattern.compile("(?s)```([\\w-]*)\\n(.*?)\\n```");

    private final SearchService searchService;
    private final SearchNoteInfoRep searchNoteInfoRep;
    private final UNoteRepM noteRepM;
    private final UNoteInfoRep uNoteInfoRep;
    private final PutNoteTreeService putNoteTreeService;
    private final PatchUNNoteCoverService patchUNNoteCoverService;
    private final NoteVersionService noteVersionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserAiToolService(SearchService searchService,
                             SearchNoteInfoRep searchNoteInfoRep,
                             UNoteRepM noteRepM,
                             UNoteInfoRep uNoteInfoRep,
                             PutNoteTreeService putNoteTreeService,
                             PatchUNNoteCoverService patchUNNoteCoverService,
                             NoteVersionService noteVersionService) {
        this.searchService = searchService;
        this.searchNoteInfoRep = searchNoteInfoRep;
        this.noteRepM = noteRepM;
        this.uNoteInfoRep = uNoteInfoRep;
        this.putNoteTreeService = putNoteTreeService;
        this.patchUNNoteCoverService = patchUNNoteCoverService;
        this.noteVersionService = noteVersionService;
    }

    public List<AiToolDefinitionDto> listTools() {
        return List.of(
                new AiToolDefinitionDto(SEARCH_USER_NOTES, "搜索当前用户名下的笔记列表，返回标题、摘要和匹配片段"),
                new AiToolDefinitionDto(READ_NOTE, "读取当前用户某篇笔记的标题、摘要和正文片段"),
                new AiToolDefinitionDto(GET_CURRENT_NOTE, "读取当前正在编辑的笔记标题、摘要、正文片段和选中文本"),
                new AiToolDefinitionDto(REPLACE_SELECTED_TEXT, "将当前打开笔记中的已选中文本替换为新内容"),
                new AiToolDefinitionDto(APPEND_TO_CURRENT_NOTE, "向当前打开笔记末尾追加内容"),
                new AiToolDefinitionDto(INSERT_AFTER_SELECTED_TEXT, "在当前打开笔记的已选中文本后插入新内容"),
                new AiToolDefinitionDto(UPDATE_CURRENT_NOTE_TITLE, "更新当前打开笔记的标题"),
                new AiToolDefinitionDto(SAVE_CURRENT_NOTE_SUMMARY, "更新当前打开笔记的摘要"),
                new AiToolDefinitionDto(UPDATE_CURRENT_NOTE_COVER, "更新当前打开笔记的封面")
        );
    }

    public boolean isWriteTool(String tool) {
        return REPLACE_SELECTED_TEXT.equals(tool)
                || APPEND_TO_CURRENT_NOTE.equals(tool)
                || INSERT_AFTER_SELECTED_TEXT.equals(tool)
                || UPDATE_CURRENT_NOTE_TITLE.equals(tool)
                || SAVE_CURRENT_NOTE_SUMMARY.equals(tool)
                || UPDATE_CURRENT_NOTE_COVER.equals(tool);
    }

    public String summarizePlan(String tool, JsonNode arguments, ChatRequest request) {
        if (tool == null || tool.isBlank() || "none".equals(tool)) {
            return "本次无需调用工具。";
        }
        return switch (tool) {
            case SEARCH_USER_NOTES -> "准备搜索用户名下的相关笔记。";
            case READ_NOTE -> "准备读取一篇已有笔记的标题、摘要和正文片段。";
            case GET_CURRENT_NOTE -> "准备读取当前打开笔记的上下文。";
            case REPLACE_SELECTED_TEXT -> "准备替换当前笔记中的选中文本。";
            case APPEND_TO_CURRENT_NOTE -> "准备向当前笔记末尾追加内容。";
            case INSERT_AFTER_SELECTED_TEXT -> "准备在当前选中文本后插入内容。";
            case UPDATE_CURRENT_NOTE_TITLE -> "准备修改当前笔记标题。";
            case SAVE_CURRENT_NOTE_SUMMARY -> "准备修改当前笔记摘要。";
            case UPDATE_CURRENT_NOTE_COVER -> "准备修改当前笔记封面。";
            default -> "准备执行工具：" + tool;
        } + buildTargetSuffix(arguments, request);
    }

    public List<SearchResultDto> searchUserNotes(Long userId, String query, Integer limit) {
        int safeLimit = limit == null ? 5 : Math.max(1, Math.min(limit, 10));
        return searchService.search(userId, query, safeLimit);
    }

    public ContextNoteDto readNoteMeta(Long userId, Long noteId) {
        NoteInfo noteInfo = searchNoteInfoRep.findByIdInAndUserIdAndIsDeleted(List.of(noteId), userId, 0).stream()
                .findFirst()
                .orElse(null);
        if (noteInfo == null) {
            return null;
        }
        return new ContextNoteDto(
                noteInfo.getId(),
                noteInfo.getNoteTitle(),
                noteInfo.getNoteSummary(),
                noteInfo.getNoteAvatar() == null ? null : new String(noteInfo.getNoteAvatar())
        );
    }

    public String readNoteContent(Long userId, Long noteId, Integer maxChars) {
        ContextNoteDto noteMeta = readNoteMeta(userId, noteId);
        if (noteMeta == null) {
            return null;
        }
        Optional<Note> note = noteRepM.findById(noteId);
        if (note.isEmpty() || note.get().getContent() == null) {
            return "";
        }
        int safeMaxChars = maxChars == null ? 1200 : Math.max(200, Math.min(maxChars, 3000));
        return shrink(extractPlainText(note.get().getContent()), safeMaxChars);
    }

    public Map<String, Object> getCurrentNote(Long userId, ChatRequest request, Integer maxChars) {
        NoteInfo noteInfo = requireCurrentNoteInfo(userId, request);
        Note note = requireCurrentNoteContent(noteInfo.getId());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("noteId", noteInfo.getId());
        data.put("title", noteInfo.getNoteTitle());
        data.put("summary", noteInfo.getNoteSummary());
        data.put("cover", request.getCurrentNoteCover());
        data.put("availableCoverOptions", NOTE_COVER_OPTIONS);
        data.put("selectedText", request.getSelectedText());
        data.put("content", shrink(extractPlainText(note.getContent()), maxChars == null ? 1500 : maxChars));
        return data;
    }

    public Map<String, Object> appendToCurrentNote(Long userId, ChatRequest request, String content) throws Exception {
        NoteInfo noteInfo = requireCurrentNoteInfo(userId, request);
        Note note = requireCurrentNoteContent(noteInfo.getId());
        ObjectNode root = readOrCreateDoc(note.getContent());
        ArrayNode docContent = root.withArray("content");
        for (JsonNode node : buildNodesFromContent(content)) {
            docContent.add(node);
        }
        persistNote(userId, note, root);
        return buildWriteResult(noteInfo.getId(), noteInfo.getNoteTitle(), "已追加到当前笔记末尾。");
    }

    public Map<String, Object> replaceSelectedText(Long userId, ChatRequest request, String replacement) throws Exception {
        NoteInfo noteInfo = requireCurrentNoteInfo(userId, request);
        String selectedText = safeSelectedText(request);
        Note note = requireCurrentNoteContent(noteInfo.getId());
        ObjectNode root = readOrCreateDoc(note.getContent());
        boolean replaced = replaceFirstOccurrence(root, selectedText, replacement, false);
        if (!replaced) {
            throw new IllegalArgumentException("当前笔记中未找到需要替换的选中文本");
        }
        persistNote(userId, note, root);
        return buildWriteResult(noteInfo.getId(), noteInfo.getNoteTitle(), "已替换当前选中文本。");
    }

    public Map<String, Object> insertAfterSelectedText(Long userId, ChatRequest request, String content) throws Exception {
        NoteInfo noteInfo = requireCurrentNoteInfo(userId, request);
        String selectedText = safeSelectedText(request);
        Note note = requireCurrentNoteContent(noteInfo.getId());
        ObjectNode root = readOrCreateDoc(note.getContent());
        boolean replaced = replaceFirstOccurrence(root, selectedText, "\n" + normalizeToolContent(content), true);
        if (!replaced) {
            throw new IllegalArgumentException("当前笔记中未找到插入位置");
        }
        persistNote(userId, note, root);
        return buildWriteResult(noteInfo.getId(), noteInfo.getNoteTitle(), "已在选中文本后插入新内容。");
    }

    public Map<String, Object> updateCurrentNoteTitle(Long userId, ChatRequest request, String title) {
        NoteInfo noteInfo = requireCurrentNoteInfo(userId, request);
        String safeTitle = shrink(normalizeToolContent(title), 80);
        PutNoteTitleEnum result = putNoteTreeService.putNoteTitle(userId, noteInfo.getId(), safeTitle);
        if (result != PutNoteTitleEnum.UPDATE_SUCCESS) {
            throw new IllegalArgumentException("当前笔记标题更新失败");
        }
        return buildWriteResult(noteInfo.getId(), safeTitle, "已更新当前笔记标题。");
    }

    public Map<String, Object> saveCurrentNoteSummary(Long userId, ChatRequest request, String summary) {
        NoteInfo noteInfo = requireCurrentNoteInfo(userId, request);
        String safeSummary = shrink(normalizeToolContent(summary), 255);
        PutUNTContextEnum result = putNoteTreeService.putNoteDescription(userId, noteInfo.getId(), safeSummary);
        if (result != PutUNTContextEnum.SUCCESS) {
            throw new IllegalArgumentException("当前笔记摘要更新失败");
        }
        return buildWriteResult(noteInfo.getId(), noteInfo.getNoteTitle(), "已更新当前笔记摘要。");
    }

    public Map<String, Object> updateCurrentNoteCover(Long userId, ChatRequest request, String cover) {
        NoteInfo noteInfo = requireCurrentNoteInfo(userId, request);
        String safeCover = normalizeCover(cover);
        PatchContextEnum result = patchUNNoteCoverService.UpNoteCover(userId, noteInfo.getId(), safeCover);
        if (result != PatchContextEnum.SUCCESS) {
            throw new IllegalArgumentException("当前笔记封面更新失败");
        }
        Map<String, Object> resultData = buildWriteResult(noteInfo.getId(), noteInfo.getNoteTitle(), safeCover == null ? "已移除当前笔记封面。" : "已更新当前笔记封面。");
        resultData.put("cover", safeCover);
        return resultData;
    }

    private NoteInfo requireCurrentNoteInfo(Long userId, ChatRequest request) {
        Long noteId = request == null ? null : request.getCurrentNoteId();
        if (noteId == null) {
            throw new IllegalArgumentException("当前没有打开的笔记，无法执行写入工具");
        }
        return uNoteInfoRep.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new IllegalArgumentException("当前笔记不存在或无权访问"));
    }

    private Note requireCurrentNoteContent(Long noteId) {
        return noteRepM.findById(noteId).orElseGet(() -> {
            Note note = new Note();
            note.setNoteId(noteId);
            note.setContent("");
            note.setLastSavedAt(LocalDateTime.now());
            return note;
        });
    }

    private Map<String, Object> buildWriteResult(Long noteId, String title, String message) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("noteId", noteId);
        data.put("title", title);
        data.put("message", message);
        return data;
    }

    private String buildTargetSuffix(JsonNode arguments, ChatRequest request) {
        Long noteId = arguments != null && arguments.hasNonNull("noteId")
                ? arguments.get("noteId").asLong()
                : request == null ? null : request.getCurrentNoteId();
        if (noteId == null) {
            return "";
        }
        return " 目标笔记 ID=" + noteId + "。";
    }

    private String safeSelectedText(ChatRequest request) {
        String selectedText = request == null ? null : request.getSelectedText();
        if (selectedText == null || selectedText.isBlank()) {
            throw new IllegalArgumentException("当前没有选中文本，无法执行该写入工具");
        }
        return selectedText.trim();
    }

    private String normalizeCover(String cover) {
        String normalized = cover == null ? "" : cover.trim();
        if (normalized.isEmpty() || "none".equalsIgnoreCase(normalized) || "null".equalsIgnoreCase(normalized) || "remove".equalsIgnoreCase(normalized)) {
            return null;
        }
        if (!NOTE_COVER_OPTIONS.contains(normalized)) {
            throw new IllegalArgumentException("封面选项不合法");
        }
        return normalized;
    }

    private String normalizeToolContent(String content) {
        return content == null ? "" : content.trim();
    }

    private void persistNote(Long userId, Note note, ObjectNode root) throws Exception {
        String contentJson = objectMapper.writeValueAsString(root);
        note.setContent(contentJson);
        note.setLastSavedAt(LocalDateTime.now());
        noteRepM.save(note);
        noteVersionService.createVersionIfChanged(userId, note.getNoteId(), contentJson, "AI_TOOL");
    }

    private ObjectNode readOrCreateDoc(String contentJson) throws Exception {
        if (contentJson == null || contentJson.isBlank()) {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("type", "doc");
            root.putArray("content");
            return root;
        }
        JsonNode parsed = objectMapper.readTree(contentJson);
        if (parsed instanceof ObjectNode objectNode) {
            if (!objectNode.has("type")) {
                objectNode.put("type", "doc");
            }
            if (!objectNode.has("content") || !objectNode.get("content").isArray()) {
                objectNode.putArray("content");
            }
            return objectNode;
        }
        ObjectNode root = objectMapper.createObjectNode();
        root.put("type", "doc");
        root.putArray("content");
        return root;
    }

    private ArrayNode buildNodesFromContent(String rawContent) {
        ArrayNode nodes = objectMapper.createArrayNode();
        String content = normalizeToolContent(rawContent);
        if (content.isBlank()) {
            return nodes;
        }

        Matcher matcher = FENCED_BLOCK_PATTERN.matcher(content);
        int cursor = 0;
        while (matcher.find()) {
            addParagraphNodes(nodes, content.substring(cursor, matcher.start()));
            nodes.add(createCodeBlockNode(matcher.group(2), matcher.group(1)));
            cursor = matcher.end();
        }
        addParagraphNodes(nodes, content.substring(cursor));
        if (nodes.isEmpty()) {
            nodes.add(createParagraphNode(content));
        }
        return nodes;
    }

    private void addParagraphNodes(ArrayNode nodes, String textBlock) {
        String normalized = textBlock == null ? "" : textBlock.trim();
        if (normalized.isBlank()) {
            return;
        }
        String[] paragraphs = normalized.split("\\n\\s*\\n");
        for (String paragraph : paragraphs) {
            String trimmed = paragraph.trim();
            if (!trimmed.isBlank()) {
                nodes.add(createParagraphNode(trimmed));
            }
        }
    }

    private ObjectNode createParagraphNode(String text) {
        ObjectNode paragraph = objectMapper.createObjectNode();
        paragraph.put("type", "paragraph");
        ArrayNode content = paragraph.putArray("content");
        String[] lines = text.split("\\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isEmpty()) {
                content.add(createTextNode(lines[i]));
            }
            if (i < lines.length - 1) {
                ObjectNode hardBreak = objectMapper.createObjectNode();
                hardBreak.put("type", "hard_break");
                content.add(hardBreak);
            }
        }
        return paragraph;
    }

    private ObjectNode createCodeBlockNode(String text, String language) {
        ObjectNode codeBlock = objectMapper.createObjectNode();
        codeBlock.put("type", "code_block");
        if (language != null && !language.isBlank()) {
            ObjectNode attrs = codeBlock.putObject("attrs");
            attrs.put("language", language.trim());
        }
        ArrayNode content = codeBlock.putArray("content");
        content.add(createTextNode(text == null ? "" : text));
        return codeBlock;
    }

    private ObjectNode createTextNode(String text) {
        ObjectNode textNode = objectMapper.createObjectNode();
        textNode.put("type", "text");
        textNode.put("text", text);
        return textNode;
    }

    private boolean replaceFirstOccurrence(JsonNode node, String target, String replacement, boolean keepTarget) {
        if (node == null || target == null || target.isBlank()) {
            return false;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            if ("text".equals(objectNode.path("type").asText()) && objectNode.has("text")) {
                String text = objectNode.path("text").asText("");
                if (text.contains(target)) {
                    String replacementText = keepTarget ? target + replacement : replacement;
                    objectNode.put("text", text.replaceFirst(Pattern.quote(target), Matcher.quoteReplacement(replacementText)));
                    return true;
                }
            }
            if (objectNode.has("content")) {
                for (JsonNode child : objectNode.withArray("content")) {
                    if (replaceFirstOccurrence(child, target, replacement, keepTarget)) {
                        return true;
                    }
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                if (replaceFirstOccurrence(child, target, replacement, keepTarget)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String extractPlainText(String contentJson) {
        if (contentJson == null || contentJson.isBlank()) {
            return "";
        }
        try {
            JsonNode root = objectMapper.readTree(contentJson);
            StringBuilder builder = new StringBuilder();
            collectText(root, builder);
            return builder.toString().replaceAll("\\n{3,}", "\n\n").trim();
        } catch (Exception e) {
            return contentJson;
        }
    }

    private void collectText(JsonNode node, StringBuilder builder) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            String type = node.path("type").asText("");
            if ("text".equals(type)) {
                builder.append(node.path("text").asText(""));
                return;
            }
            if ("hard_break".equals(type)) {
                builder.append('\n');
                return;
            }
            if ("paragraph".equals(type) || "heading".equals(type) || "blockquote".equals(type) || "list_item".equals(type) || "code_block".equals(type)) {
                for (JsonNode child : node.path("content")) {
                    collectText(child, builder);
                }
                builder.append('\n');
                return;
            }
            for (JsonNode child : node.path("content")) {
                collectText(child, builder);
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectText(child, builder);
            }
        }
    }

    private String shrink(String content, int maxChars) {
        String normalized = Objects.requireNonNullElse(content, "").trim();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        return normalized.substring(0, maxChars) + "\n...\n";
    }
}
