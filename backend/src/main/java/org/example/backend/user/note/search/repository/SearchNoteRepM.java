package org.example.backend.user.note.search.repository;

import org.example.backend.common.domain.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SearchNoteRepM extends MongoRepository<Note, Long> {
    @Query(value = "{ 'content': { $regex: ?0, $options: 'i' } }")
    List<Note> findByContentLike(String q);
}