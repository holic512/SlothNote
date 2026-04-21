package org.example.backend.user.ai.dto;

import java.util.List;

public class ChatRequest {
    private Long sessionId;
    private String text;
    private String messageType;
    private String selectedText;
    private List<Long> contextNoteIds;
    private Long currentNoteId;
    private String currentNoteTitle;
    private String currentNoteCover;
    private Boolean allowCurrentNoteWrite;
    private String plannedToolName;
    private String plannedToolArgumentsJson;

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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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

    public Boolean getAllowCurrentNoteWrite() {
        return allowCurrentNoteWrite;
    }

    public void setAllowCurrentNoteWrite(Boolean allowCurrentNoteWrite) {
        this.allowCurrentNoteWrite = allowCurrentNoteWrite;
    }

    public String getPlannedToolName() {
        return plannedToolName;
    }

    public void setPlannedToolName(String plannedToolName) {
        this.plannedToolName = plannedToolName;
    }

    public String getPlannedToolArgumentsJson() {
        return plannedToolArgumentsJson;
    }

    public void setPlannedToolArgumentsJson(String plannedToolArgumentsJson) {
        this.plannedToolArgumentsJson = plannedToolArgumentsJson;
    }
}
