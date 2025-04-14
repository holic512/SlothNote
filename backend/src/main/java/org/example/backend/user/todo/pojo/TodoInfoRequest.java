package org.example.backend.user.todo.pojo;

import java.time.LocalDateTime;

public class TodoInfoRequest {
    private String title;
    private String description;
    private Long categoryId;
    private LocalDateTime dueDate;
    private Integer status;

    public TodoInfoRequest() {
    }

    public TodoInfoRequest(String title, String description, Long categoryId, LocalDateTime dueDate, Integer status) {
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.dueDate = dueDate;
        this.status = status;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}