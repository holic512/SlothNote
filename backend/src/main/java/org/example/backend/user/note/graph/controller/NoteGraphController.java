/**
 * @file NoteGraphController
 * @project SlothNote
 * @module 用户端 / 笔记知识图谱
 * @description 提供用户知识星图数据接口。
 * @logic 1. 从 Sa-Token 用户会话读取 userId；2. 支持按当前笔记聚焦图谱；3. 返回节点与引用边。
 * @dependencies Service: NoteReferenceService, Response: ApiResponse, Auth: StpKit
 * @index_tags 知识星图接口, 笔记图谱, 引用关系, graph
 * @author holic512
 */
package org.example.backend.user.note.graph.controller;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.note.graph.service.NoteReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/note/graph")
public class NoteGraphController {

    private final NoteReferenceService noteReferenceService;

    @Autowired
    public NoteGraphController(NoteReferenceService noteReferenceService) {
        this.noteReferenceService = noteReferenceService;
    }

    @GetMapping
    public ResponseEntity<Object> graph(@RequestParam(required = false) Long noteId) {
        Long userId = (Long) StpKit.USER.getSession().get("id");

        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("获取知识星图成功")
                .data(noteReferenceService.getGraph(userId, noteId))
                .build());
    }
}
