package org.example.backend.user.repository;

import org.example.backend.common.entity.NoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNoteInfoRepository extends JpaRepository<NoteInfo, Long> {
}