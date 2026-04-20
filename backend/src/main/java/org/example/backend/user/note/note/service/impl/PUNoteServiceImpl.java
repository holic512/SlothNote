/**
 * File Name: PUNoteServiceImpl.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-12
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.note.service.impl;

import org.bson.codecs.jsr310.LocalDateTimeCodec;
import org.example.backend.common.domain.Note;
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

    @Autowired
    public PUNoteServiceImpl(UNoteInfoRep uNoteInfoRep, UNoteRepM uNoteRepM, NoteVersionService noteVersionService) {
        this.uNoteInfoRep = uNoteInfoRep;
        this.uNoteRepM = uNoteRepM;
        this.noteVersionService = noteVersionService;
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

        // 更新 保存时间
        note.setLastSavedAt(LocalDateTime.now());

        // 执行保存
        uNoteRepM.save(note);
        noteVersionService.createVersionIfChanged(userId, noteId, note.getContent(), "SAVE");
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
        return noteVersionService.restoreVersion(userId, noteId, versionId);
    }
}
