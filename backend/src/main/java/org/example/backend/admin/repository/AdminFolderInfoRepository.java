package org.example.backend.admin.repository;

import org.example.backend.common.entity.FolderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminFolderInfoRepository extends JpaRepository<FolderInfo, Long> {
}