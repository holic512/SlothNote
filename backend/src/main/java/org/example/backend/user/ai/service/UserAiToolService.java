package org.example.backend.user.ai.service;

import org.example.backend.common.domain.Note;
import org.example.backend.common.entity.NoteInfo;
import org.example.backend.user.ai.dto.AiToolDefinitionDto;
import org.example.backend.user.ai.dto.ContextNoteDto;
import org.example.backend.user.note.note.repository.UNoteRepM;
import org.example.backend.user.note.search.dto.SearchResultDto;
import org.example.backend.user.note.search.repository.SearchNoteInfoRep;
import org.example.backend.user.note.search.service.SearchService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserAiToolService {

    public static final String SEARCH_USER_NOTES = "search_user_notes";
    public static final String READ_NOTE = "read_note";

    private final SearchService searchService;
    private final SearchNoteInfoRep searchNoteInfoRep;
    private final UNoteRepM noteRepM;

    public UserAiToolService(SearchService searchService,
                             SearchNoteInfoRep searchNoteInfoRep,
                             UNoteRepM noteRepM) {
        this.searchService = searchService;
        this.searchNoteInfoRep = searchNoteInfoRep;
        this.noteRepM = noteRepM;
    }

    public List<AiToolDefinitionDto> listTools() {
        return List.of(
                new AiToolDefinitionDto(SEARCH_USER_NOTES, "搜索当前用户名下的笔记列表，返回标题、摘要和匹配片段"),
                new AiToolDefinitionDto(READ_NOTE, "读取当前用户某篇笔记的标题、摘要和正文片段")
        );
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
        String content = note.get().getContent().trim();
        if (content.length() <= safeMaxChars) {
            return content;
        }
        return content.substring(0, safeMaxChars) + "\n...\n";
    }
}
