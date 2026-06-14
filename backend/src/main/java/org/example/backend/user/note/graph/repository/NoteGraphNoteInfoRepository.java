/**
 * @file NoteGraphNoteInfoRepository
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 查询知识星图所需的用户笔记元数据。
 * @logic 1. 获取用户未删除笔记列表；2. 校验引用目标是否属于当前用户；3. 保持图谱节点来源为 note_info。
 * @dependencies Entity: NoteInfo, SpringDataJpa
 * @index_tags 笔记元数据, 图谱节点, note_info, JPA
 * @author holic512
 */
package org.example.backend.user.note.graph.repository;

import org.example.backend.common.entity.NoteInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface NoteGraphNoteInfoRepository extends JpaRepository<NoteInfo, Long> {

    List<NoteInfo> findByUserIdAndIsDeleted(Long userId, Integer isDeleted);

    List<NoteInfo> findByUserIdAndIdInAndIsDeleted(Long userId, Collection<Long> ids, Integer isDeleted);
}
