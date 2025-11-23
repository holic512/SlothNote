package org.example.backend.user.note.favoriteNote.controller;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.note.favoriteNote.dto.FavoriteFolderDto;
import org.example.backend.user.note.favoriteNote.dto.FavoriteNoteDto;
import org.example.backend.user.note.favoriteNote.enums.FavoriteStatusEnum;
import org.example.backend.user.note.favoriteNote.service.FavoriteNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/user/note/favorite")
public class FavoriteNoteController {

    private final FavoriteNoteService favoriteService;

    @Autowired
    public FavoriteNoteController(FavoriteNoteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping("/folders")
    public ResponseEntity<Object> folders() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        List<FavoriteFolderDto> list = favoriteService.listFolders(userId);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(list)
                .build());
    }

    @GetMapping("/notes")
    public ResponseEntity<Object> notes(@RequestParam(required = false) Long folderId) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        List<FavoriteNoteDto> list = favoriteService.listFavorites(userId, folderId);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(list)
                .build());
    }

    @PostMapping("/add")
    public ResponseEntity<Object> add(@RequestBody HashMap<String, String> body) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        Long noteId = Long.parseLong(body.get("noteId"));
        Long folderId = null;
        String fs = body.get("folderId");
        if (fs != null && !fs.trim().isEmpty()) {
            Long parsed = Long.parseLong(fs);
            folderId = parsed == 0 ? 0L : parsed;
        }
        String remark = body.getOrDefault("noteRemark", null);
        Pair<FavoriteStatusEnum, Long> r = favoriteService.addFavorite(userId, noteId, folderId, remark);
        FavoriteStatusEnum s = r.a;
        if (s == FavoriteStatusEnum.SUCCESS) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("OK")
                    .data(r.b)
                    .build());
        }
        int status = (s == FavoriteStatusEnum.ALREADY_EXISTS) ? 409 : (s == FavoriteStatusEnum.NOTE_NOT_FOUND ? 404 : 403);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(status)
                .message(s.name())
                .build());
    }

    @PostMapping("/folders/add")
    public ResponseEntity<Object> addFolder(@RequestBody HashMap<String, String> body) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        String name = body.get("folderName");
        Pair<FavoriteStatusEnum, Long> r = favoriteService.addFolder(userId, name);
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(r.b)
                .build());
    }
}