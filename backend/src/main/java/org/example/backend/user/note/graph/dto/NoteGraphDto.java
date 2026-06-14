/**
 * @file NoteGraphDto
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 封装知识星图接口返回的节点、边和当前笔记上下文。
 * @logic 1. nodes 返回用户未删除笔记；2. edges 返回显式笔记引用；3. currentNoteId 标记图谱焦点。
 * @dependencies DTO: NoteGraphNodeDto/NoteGraphEdgeDto
 * @index_tags 知识星图, 图谱数据, 笔记关系
 * @author holic512
 */
package org.example.backend.user.note.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NoteGraphDto {
    private Long currentNoteId;
    private List<NoteGraphNodeDto> nodes;
    private List<NoteGraphEdgeDto> edges;
}
