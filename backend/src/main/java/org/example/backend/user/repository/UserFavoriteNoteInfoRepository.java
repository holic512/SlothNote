package org.example.backend.user.repository;

import org.example.backend.common.entity.FavoriteNoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteNoteInfoRepository extends JpaRepository<FavoriteNoteInfo, Long> {
    boolean existsByUserIdAndNoteIdAndIsDeletedFalse(Long userId, Long noteId);
    java.util.List<FavoriteNoteInfo> findByUserIdAndFavoriteFolderIdAndIsDeletedFalse(Long userId, Long favoriteFolderId);
}