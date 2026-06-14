/**
 * @file PUNoteServiceImpl
 * @project SlothNote
 * @module 用户端 / 笔记保存服务
 * @description 处理用户笔记正文保存、历史版本恢复与引用关系同步。
 * @logic 1. 校验笔记存在与用户归属；2. 保存 note_content 并创建版本；3. 从 Tiptap JSON 同步 note_reference 出边。
 * @dependencies Repository: UNoteInfoRep/UNoteRepM, Service: NoteVersionService/NoteReferenceService
 * @index_tags 笔记保存, 历史版本, 引用同步, note_content, note_reference
 * @author holic512
 */
package org.example.backend.user.note.note.service.impl;

import org.example.backend.common.domain.Note;
import org.example.backend.user.note.graph.service.NoteReferenceService;
import org.example.backend.user.note.note.repository.UNoteInfoRep;
import org.example.backend.user.note.note.repository.UNoteRepM;
import org.example.backend.user.note.note.service.NoteVersionService;
import org.example.backend.user.note.note.service.PUNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PUNoteServiceImpl implements PUNoteService {

    private final UNoteInfoRep uNoteInfoRep;
    private final UNoteRepM uNoteRepM;
    private final NoteVersionService noteVersionService;
    private final NoteReferenceService noteReferenceService;

    @Autowired
    public PUNoteServiceImpl(UNoteInfoRep uNoteInfoRep,
                             UNoteRepM uNoteRepM,
                             NoteVersionService noteVersionService,
                             NoteReferenceService noteReferenceService) {
        this.uNoteInfoRep = uNoteInfoRep;
        this.uNoteRepM = uNoteRepM;
        this.noteVersionService = noteVersionService;
        this.noteReferenceService = noteReferenceService;
    }

    /**
     * 保存笔记
     *
     * @param userId 当前用户ID
     * @param note   需要保存的笔记内容
     * @return 返回保存结果，成功时返回 "success"，失败时返回 null
     */
    @Override
    public String SaveNote(Long userId, Note note) {
        if (note == null || note.getNoteId() == null) {
            return null;
        }
        Long noteId = note.getNoteId();

        // 判断当前笔记是否存在
        if (!uNoteInfoRep.existsById(noteId)) {
            return null; // 如果笔记不存在，返回 null
        }

        // 查询笔记所有权是否属于当前用户
        Long ownerId = uNoteInfoRep.findUserIdByNoteId(noteId);
        if (!ownerId.equals(userId)) {
            return null; // 如果当前用户不是笔记所有者，返回 null
        }

        Note persisted = uNoteRepM.findById(noteId).orElseGet(() -> {
            Note created = new Note();
            created.setNoteId(noteId);
            return created;
        });
        persisted.setContent(note.getContent() == null ? "" : note.getContent());
        persisted.setLastSavedAt(LocalDateTime.now());

        uNoteRepM.save(persisted);
        noteVersionService.createVersionIfChanged(userId, noteId, persisted.getContent(), "SAVE");
        noteReferenceService.syncReferencesFromContent(userId, noteId, persisted.getContent());
        return "success"; // 保存成功
    }

    @Override
    public Note restoreVersion(Long userId, Long noteId, Long versionId) {
        if (!uNoteInfoRep.existsById(noteId)) {
            return null;
        }
        Long ownerId = uNoteInfoRep.findUserIdByNoteId(noteId);
        if (!ownerId.equals(userId)) {
            return null;
        }
        Note restored = noteVersionService.restoreVersion(userId, noteId, versionId);
        if (restored != null) {
            noteReferenceService.syncReferencesFromContent(userId, noteId, restored.getContent());
        }
        return restored;
    }
}
