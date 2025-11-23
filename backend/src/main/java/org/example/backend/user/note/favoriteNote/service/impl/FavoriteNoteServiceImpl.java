package org.example.backend.user.note.favoriteNote.service.impl;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.entity.FavoriteFolderInfo;
import org.example.backend.common.entity.FavoriteNoteInfo;
import org.example.backend.user.note.favoriteNote.dto.FavoriteFolderDto;
import org.example.backend.user.note.favoriteNote.dto.FavoriteNoteDto;
import org.example.backend.user.note.favoriteNote.enums.FavoriteStatusEnum;
import org.example.backend.user.note.favoriteNote.service.FavoriteNoteService;
import org.example.backend.user.note.note.repository.UNoteInfoRep;
import org.example.backend.user.repository.UserNoteInfoRepository;
import org.example.backend.user.repository.UserFavoriteFolderInfoRepository;
import org.example.backend.user.repository.UserFavoriteNoteInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteNoteServiceImpl implements FavoriteNoteService {

    private final UserFavoriteFolderInfoRepository folderRepo;
    private final UserFavoriteNoteInfoRepository noteRepo;
    private final UNoteInfoRep noteInfoRepo;
    private final UserNoteInfoRepository userNoteInfoRepository;

    @Autowired
    public FavoriteNoteServiceImpl(UserFavoriteFolderInfoRepository folderRepo, UserFavoriteNoteInfoRepository noteRepo, UNoteInfoRep noteInfoRepo, UserNoteInfoRepository userNoteInfoRepository) {
        this.folderRepo = folderRepo;
        this.noteRepo = noteRepo;
        this.noteInfoRepo = noteInfoRepo;
        this.userNoteInfoRepository = userNoteInfoRepository;
    }

    @Override
    public List<FavoriteFolderDto> listFolders(Long userId) {
        List<FavoriteFolderInfo> list = folderRepo.findByUserIdAndIsDeletedFalse(userId);
        return list.stream().map(f -> new FavoriteFolderDto(f.getId(), f.getFolderName())).collect(Collectors.toList());
    }

    @Override
    public Pair<FavoriteStatusEnum, Long> addFavorite(Long userId, Long noteId, Long folderId, String remark) {
        Long ownerId = noteInfoRepo.findUserIdByNoteId(noteId);
        if (ownerId == null) return new Pair<>(FavoriteStatusEnum.NOTE_NOT_FOUND, null);
        if (!ownerId.equals(userId)) return new Pair<>(FavoriteStatusEnum.NOTE_NOT_OWNED_BY_USER, null);
        if (noteRepo.existsByUserIdAndNoteIdAndIsDeletedFalse(userId, noteId)) return new Pair<>(FavoriteStatusEnum.ALREADY_EXISTS, null);
        FavoriteNoteInfo info = new FavoriteNoteInfo();
        info.setUserId(userId);
        info.setNoteId(noteId);
        info.setFavoriteFolderId(folderId == null ? 0L : folderId);
        info.setNoteRemark(remark);
        info.setFavoriteStatus(true);
        FavoriteNoteInfo saved = noteRepo.save(info);
        return new Pair<>(FavoriteStatusEnum.SUCCESS, saved.getId());
    }

    @Override
    public List<FavoriteNoteDto> listFavorites(Long userId, Long folderId) {
        List<FavoriteNoteInfo> list = noteRepo.findByUserIdAndFavoriteFolderIdAndIsDeletedFalse(userId, folderId == null ? 0L : folderId);
        return list.stream().map(n -> {
            var noteOpt = userNoteInfoRepository.findById(n.getNoteId());
            String icon = null;
            java.time.LocalDateTime updated = null;
            String title = null;
            if (noteOpt.isPresent()) {
                var note = noteOpt.get();
                updated = note.getUpdatedAt();
                title = note.getNoteTitle();
                char[] avatar = note.getNoteAvatar();
                icon = (avatar != null && avatar.length > 0) ? new String(avatar) : null;
            }
            return new FavoriteNoteDto(n.getId(), n.getNoteId(), n.getFavoriteFolderId(), n.getNoteRemark(), updated, icon, title);
        }).collect(Collectors.toList());
    }

    @Override
    public Pair<FavoriteStatusEnum, Long> addFolder(Long userId, String folderName) {
        FavoriteFolderInfo info = new FavoriteFolderInfo();
        info.setUserId(userId);
        info.setFolderName(folderName);
        info.setParentId(0L);
        FavoriteFolderInfo saved = folderRepo.save(info);
        return new Pair<>(FavoriteStatusEnum.SUCCESS, saved.getId());
    }
}