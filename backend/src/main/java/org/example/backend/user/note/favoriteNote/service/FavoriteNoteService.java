package org.example.backend.user.note.favoriteNote.service;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.user.note.favoriteNote.dto.FavoriteFolderDto;
import org.example.backend.user.note.favoriteNote.dto.FavoriteNoteDto;
import org.example.backend.user.note.favoriteNote.enums.FavoriteStatusEnum;

import java.util.List;

public interface FavoriteNoteService {
    List<FavoriteFolderDto> listFolders(Long userId);
    Pair<FavoriteStatusEnum, Long> addFavorite(Long userId, Long noteId, Long folderId, String remark);
    List<FavoriteNoteDto> listFavorites(Long userId, Long folderId);
    Pair<FavoriteStatusEnum, Long> addFolder(Long userId, String folderName);
}