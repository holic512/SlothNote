package org.example.backend.user.note.search;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.note.search.dto.SearchResultDto;
import org.example.backend.user.note.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/note/search")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public ResponseEntity<Object> search(@RequestParam("q") String q) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        List<SearchResultDto> list = searchService.search(userId, q);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(list)
                .build());
    }
}