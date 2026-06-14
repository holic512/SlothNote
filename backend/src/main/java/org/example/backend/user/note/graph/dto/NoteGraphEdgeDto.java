/**
 * @file NoteGraphEdgeDto
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 表示知识星图中的笔记引用边。
 * @logic sourceNoteId 指向 targetNoteId，labelSnapshot 用于显示引用标题快照。
 * @dependencies Entity: NoteReference
 * @index_tags 图谱边, 笔记引用, 知识星图
 * @author holic512
 */
package org.example.backend.user.note.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoteGraphEdgeDto {
    private Long sourceNoteId;
    private Long targetNoteId;
    private String labelSnapshot;
}
