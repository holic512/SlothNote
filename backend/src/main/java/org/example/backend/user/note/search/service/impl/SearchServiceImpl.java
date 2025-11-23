package org.example.backend.user.note.search.service.impl;

import org.example.backend.common.domain.Note;
import org.example.backend.user.note.search.dto.SearchResultDto;
import org.example.backend.user.note.search.repository.SearchNoteInfoRep;
import org.example.backend.user.note.search.repository.SearchNoteRepM;
import org.example.backend.user.note.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    private final SearchNoteInfoRep infoRep;
    private final SearchNoteRepM noteRepM;

    @Autowired
    public SearchServiceImpl(SearchNoteInfoRep infoRep, SearchNoteRepM noteRepM) {
        this.infoRep = infoRep;
        this.noteRepM = noteRepM;
    }

    @Override
    public List<SearchResultDto> search(Long userId, String q) {
        List<SearchResultDto> meta = infoRep.searchByTitleOrSummary(userId, q);
        List<Note> contentMatches = noteRepM.findByContentLike(q);
        Map<Long, SearchResultDto> map = new LinkedHashMap<>();
        for (SearchResultDto s : meta) {
            map.put(s.getNoteId(), new SearchResultDto(s.getNoteId(), s.getTitle(), s.getSummary()));
        }
        for (Note note : contentMatches) {
            Long id = note.getNoteId();
            SearchResultDto s = map.getOrDefault(id, new SearchResultDto(id, null, null));
            String c = note.getContent();
            String snippet = buildSnippet(c, q);
            s.setSnippet(snippet);
            map.put(id, s);
        }
        return new ArrayList<>(map.values());
    }

    private String buildSnippet(String content, String q) {
        if (content == null || q == null) return null;
        String lc = content.toLowerCase();
        String lq = q.toLowerCase();
        int idx = lc.indexOf(lq);
        if (idx < 0) return null;
        int start = Math.max(0, idx - 40);
        int end = Math.min(content.length(), idx + q.length() + 60);
        String pre = start > 0 ? "..." : "";
        String post = end < content.length() ? "..." : "";
        return pre + content.substring(start, end) + post;
    }
}