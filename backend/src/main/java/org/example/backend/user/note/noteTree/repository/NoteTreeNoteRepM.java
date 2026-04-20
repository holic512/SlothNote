package org.example.backend.user.note.noteTree.repository;

import org.example.backend.common.domain.Note;
import org.example.backend.common.mapper.NoteMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class NoteTreeNoteRepM {

    private final NoteMapper noteMapper;

    public NoteTreeNoteRepM(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    public Optional<Note> findById(Long id) {
        return Optional.ofNullable(noteMapper.selectById(id));
    }
}
