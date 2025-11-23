package org.example.backend.admin.repository;

import org.example.backend.common.domain.Note;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminNoteRepository extends MongoRepository<Note, Long> {
}