package org.example.backend.admin.repository;

import org.example.backend.common.entity.NoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminNoteInfoRepository extends JpaRepository<NoteInfo, Long>, JpaSpecificationExecutor<NoteInfo> {
}