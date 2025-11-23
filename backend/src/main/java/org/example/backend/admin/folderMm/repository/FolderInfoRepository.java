package org.example.backend.admin.folderMm.repository;

import org.example.backend.common.entity.FolderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FolderInfoRepository extends JpaRepository<FolderInfo, Long>, JpaSpecificationExecutor<FolderInfo> {
    boolean existsByUserIdAndParentIdAndFolderName(Long userId, Long parentId, String folderName);
}