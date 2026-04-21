package org.example.backend.user.ai.dto;

public class AiToolPlanPreviewDto {
    private String tool;
    private String argumentsJson;
    private Boolean requiresConfirmation;
    private Boolean writeTool;
    private String summary;

    public AiToolPlanPreviewDto() {
    }

    public AiToolPlanPreviewDto(String tool, String argumentsJson, Boolean requiresConfirmation, Boolean writeTool, String summary) {
        this.tool = tool;
        this.argumentsJson = argumentsJson;
        this.requiresConfirmation = requiresConfirmation;
        this.writeTool = writeTool;
        this.summary = summary;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getArgumentsJson() {
        return argumentsJson;
    }

    public void setArgumentsJson(String argumentsJson) {
        this.argumentsJson = argumentsJson;
    }

    public Boolean getRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(Boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }

    public Boolean getWriteTool() {
        return writeTool;
    }

    public void setWriteTool(Boolean writeTool) {
        this.writeTool = writeTool;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
