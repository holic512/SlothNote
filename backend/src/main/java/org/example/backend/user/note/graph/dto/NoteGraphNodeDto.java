/**
 * @file NoteGraphNodeDto
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 表示知识星图中的笔记节点。
 * @logic 包含笔记 ID、标题、摘要、头像、文件夹 ID、引用度数与当前笔记标记。
 * @dependencies Entity: NoteInfo
 * @index_tags 图谱节点, 笔记节点, 知识星图
 * @author holic512
 */
package org.example.backend.user.note.graph.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NoteGraphNodeDto {
    private Long noteId;
    private String title;
    private String summary;
    private String avatar;
    private Long folderId;
    private Integer degree;
    private Boolean current;
}
