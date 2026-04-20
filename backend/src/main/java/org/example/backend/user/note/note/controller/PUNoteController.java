/**
 * File Name: PUNoteController.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-12
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.note.controller;

import org.example.backend.common.domain.Note;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.note.note.service.PUNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user/note")
public class PUNoteController {

    private final PUNoteService puNoteService;

    @Autowired
    public PUNoteController(PUNoteService puNoteService) {
        this.puNoteService = puNoteService;
    }

    /**
     * 保存笔记
     *
     * @param note 需要保存的笔记内容
     * @return ResponseEntity 返回操作结果及HTTP状态码
     */
    @PostMapping("SaveNote")
    public ResponseEntity<Object> SaveNote(@RequestBody Note note) {
        // 获取用户 id 判断笔记所有权
        Long userId = (Long) StpKit.USER.getSession().get("id");

        // 执行保存操作
        String result = puNoteService.SaveNote(userId, note);

        if ("success".equals(result)) {
            // 返回成功响应，HTTP状态码200
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("笔记保存成功")
                    .build());
        } else {
            // 返回失败响应，HTTP状态码403，表示权限问题
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(403)
                    .message("笔记权限不足或笔记不存在")
                    .build());
        }
    }

    @PostMapping("RestoreVersion")
    public ResponseEntity<Object> restoreVersion(@RequestBody Map<String, Object> body) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        Long noteId = Long.valueOf(body.get("noteId").toString());
        Long versionId = Long.valueOf(body.get("versionId").toString());
        Note restored = puNoteService.restoreVersion(userId, noteId, versionId);
        if (restored == null) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("恢复失败，历史版本不存在或权限不足")
                    .build());
        }
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("恢复历史版本成功")
                .data(restored)
                .build());
    }
}
