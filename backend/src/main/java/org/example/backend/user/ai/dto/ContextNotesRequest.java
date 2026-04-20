package org.example.backend.user.ai.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContextNotesRequest {
    private List<Long> noteIds;
}
