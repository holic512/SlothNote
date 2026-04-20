package org.example.backend.admin.repository;

import org.example.backend.common.domain.Note;
import org.example.backend.common.mapper.NoteMapper;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminNoteRepository {

    private final NoteMapper noteMapper;

    public AdminNoteRepository(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    public Optional<Note> findById(Long id) {
        return Optional.ofNullable(noteMapper.selectById(id));
    }

    public List<Note> findAllById(Collection<Long> ids) {
        return ids == null || ids.isEmpty() ? List.of() : noteMapper.selectBatchIds(ids);
    }

    public Note save(Note note) {
        if (note == null) {
            return null;
        }
        if (noteMapper.selectById(note.getNoteId()) == null) {
            noteMapper.insert(note);
        } else {
            noteMapper.updateById(note);
        }
        return noteMapper.selectById(note.getNoteId());
    }
}
