/**
 * @file NoteReferenceRepository
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 提供 note_reference 表的引用关系查询与同步写入能力。
 * @logic 1. 按 sourceNoteId 删除旧引用；2. 按用户读取图谱边；3. 支持查询当前笔记的出入引用。
 * @dependencies Entity: NoteReference, SpringDataJpa
 * @index_tags 笔记引用, 图谱边, JPA, note_reference
 * @author holic512
 */
package org.example.backend.user.note.graph.repository;

import org.example.backend.common.entity.NoteReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NoteReferenceRepository extends JpaRepository<NoteReference, Long> {

    @Transactional
    void deleteByUserIdAndSourceNoteId(Long userId, Long sourceNoteId);

    List<NoteReference> findByUserId(Long userId);

    List<NoteReference> findByUserIdAndSourceNoteId(Long userId, Long sourceNoteId);

    List<NoteReference> findByUserIdAndTargetNoteId(Long userId, Long targetNoteId);
}
