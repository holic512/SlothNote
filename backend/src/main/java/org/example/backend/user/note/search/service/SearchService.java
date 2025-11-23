package org.example.backend.user.note.search.service;

import org.example.backend.user.note.search.dto.SearchResultDto;

import java.util.List;

public interface SearchService {
    List<SearchResultDto> search(Long userId, String q);
}