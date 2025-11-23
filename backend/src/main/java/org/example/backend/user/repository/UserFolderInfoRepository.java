package org.example.backend.user.repository;

import org.example.backend.common.entity.FolderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFolderInfoRepository extends JpaRepository<FolderInfo, Long> {
}