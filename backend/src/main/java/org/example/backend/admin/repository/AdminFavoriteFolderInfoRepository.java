package org.example.backend.admin.repository;

import org.example.backend.common.entity.FavoriteFolderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminFavoriteFolderInfoRepository extends JpaRepository<FavoriteFolderInfo, Long>, JpaSpecificationExecutor<FavoriteFolderInfo> {
}