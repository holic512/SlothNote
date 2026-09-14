package org.example.backend.user.ai.dto;

public class UserAiPermissionDto {
    private Boolean canReadAllNotes;
    private Boolean canWriteNoteContent;
    private Boolean canWriteNoteTitle;
    private Boolean canWriteNoteSummary;
    private Boolean canWriteNoteCover;

    public UserAiPermissionDto() {
    }

    public UserAiPermissionDto(Boolean canReadAllNotes,
                               Boolean canWriteNoteContent,
                               Boolean canWriteNoteTitle,
                               Boolean canWriteNoteSummary,
                               Boolean canWriteNoteCover) {
        this.canReadAllNotes = canReadAllNotes;
        this.canWriteNoteContent = canWriteNoteContent;
        this.canWriteNoteTitle = canWriteNoteTitle;
        this.canWriteNoteSummary = canWriteNoteSummary;
        this.canWriteNoteCover = canWriteNoteCover;
    }

    public Boolean getCanReadAllNotes() { return canReadAllNotes; }
    public void setCanReadAllNotes(Boolean canReadAllNotes) { this.canReadAllNotes = canReadAllNotes; }
    public Boolean getCanWriteNoteContent() { return canWriteNoteContent; }
    public void setCanWriteNoteContent(Boolean canWriteNoteContent) { this.canWriteNoteContent = canWriteNoteContent; }
    public Boolean getCanWriteNoteTitle() { return canWriteNoteTitle; }
    public void setCanWriteNoteTitle(Boolean canWriteNoteTitle) { this.canWriteNoteTitle = canWriteNoteTitle; }
    public Boolean getCanWriteNoteSummary() { return canWriteNoteSummary; }
    public void setCanWriteNoteSummary(Boolean canWriteNoteSummary) { this.canWriteNoteSummary = canWriteNoteSummary; }
    public Boolean getCanWriteNoteCover() { return canWriteNoteCover; }
    public void setCanWriteNoteCover(Boolean canWriteNoteCover) { this.canWriteNoteCover = canWriteNoteCover; }
}
