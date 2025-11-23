package org.example.backend.user.repository;

import org.example.backend.common.entity.FavoriteFolderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteFolderInfoRepository extends JpaRepository<FavoriteFolderInfo, Long> {
    java.util.List<FavoriteFolderInfo> findByUserIdAndIsDeletedFalse(Long userId);
}