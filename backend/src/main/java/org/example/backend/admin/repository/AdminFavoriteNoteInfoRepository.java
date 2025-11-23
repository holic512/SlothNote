package org.example.backend.admin.repository;

import org.example.backend.common.entity.FavoriteNoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminFavoriteNoteInfoRepository extends JpaRepository<FavoriteNoteInfo, Long>, JpaSpecificationExecutor<FavoriteNoteInfo> {
}