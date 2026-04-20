package org.example.backend.user.note.search.repository;

import org.example.backend.common.domain.Note;
import org.example.backend.common.mapper.NoteMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SearchNoteRepM {

    private final NoteMapper noteMapper;

    public SearchNoteRepM(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    public List<Note> findByContentLike(String q) {
        return noteMapper.findByContentLike(q == null ? "" : q.trim());
    }
}
