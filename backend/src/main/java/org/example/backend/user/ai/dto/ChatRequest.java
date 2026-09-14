package org.example.backend.user.ai.dto;

import java.util.List;

public class ChatRequest {
    private Long sessionId;
    private String text;
    private String selectedText;
    private List<Long> contextNoteIds;
    private Long currentNoteId;
    private String currentNoteTitle;
    private String currentNoteCover;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public void setSelectedText(String selectedText) {
        this.selectedText = selectedText;
    }

    public List<Long> getContextNoteIds() {
        return contextNoteIds;
    }

    public void setContextNoteIds(List<Long> contextNoteIds) {
        this.contextNoteIds = contextNoteIds;
    }

    public Long getCurrentNoteId() {
        return currentNoteId;
    }

    public void setCurrentNoteId(Long currentNoteId) {
        this.currentNoteId = currentNoteId;
    }

    public String getCurrentNoteTitle() {
        return currentNoteTitle;
    }

    public void setCurrentNoteTitle(String currentNoteTitle) {
        this.currentNoteTitle = currentNoteTitle;
    }

    public String getCurrentNoteCover() {
        return currentNoteCover;
    }

    public void setCurrentNoteCover(String currentNoteCover) {
        this.currentNoteCover = currentNoteCover;
    }

}
