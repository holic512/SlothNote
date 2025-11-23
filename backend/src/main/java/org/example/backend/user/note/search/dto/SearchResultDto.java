package org.example.backend.user.note.search.dto;

import lombok.Data;

@Data
public class SearchResultDto {
    private Long noteId;
    private String title;
    private String summary;
    private String snippet;

    public SearchResultDto(Long noteId, String title, String summary) {
        this.noteId = noteId;
        this.title = title;
        this.summary = summary;
    }

    public SearchResultDto(Long noteId, String title, String summary, String snippet) {
        this.noteId = noteId;
        this.title = title;
        this.summary = summary;
        this.snippet = snippet;
    }
}