/**
 * @file NoteReferenceService
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 定义笔记引用同步与知识星图查询能力。
 * @logic 1. 保存正文后同步 noteReference 节点；2. 查询用户知识星图；3. 校验引用目标用户归属。
 * @dependencies DTO: NoteGraphDto
 * @index_tags 笔记引用, 知识星图, 引用同步, Tiptap
 * @author holic512
 */
package org.example.backend.user.note.graph.service;

import org.example.backend.user.note.graph.dto.NoteGraphDto;

public interface NoteReferenceService {

    void syncReferencesFromContent(Long userId, Long sourceNoteId, String contentJson);

    NoteGraphDto getGraph(Long userId, Long currentNoteId);
}
