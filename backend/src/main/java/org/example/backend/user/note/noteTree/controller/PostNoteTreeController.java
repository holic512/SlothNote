/**
 * File Name: PNoteTreeController.java
 * Description: Todo
 * Author: lv
 * Created Date: 2024-10-28
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.noteTree.controller;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.note.noteTree.service.PNoteTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/user/noteTree")
public class PostNoteTreeController {


    private final PNoteTreeService pNoteTreeService;

    @Autowired
    public PostNoteTreeController(PNoteTreeService pNoteTreeService) {
        this.pNoteTreeService = pNoteTreeService;
    }

    // 新建 笔记
    @PostMapping("/note")
    public ResponseEntity<Object> addNotes(@RequestBody Map<String, String> requestBody) {

        // 获取前端传来的文件夹ID
        String folderIdStr = requestBody.get("FolderId");

        // 验证文件夹ID是否为null或空
        if (folderIdStr == null || folderIdStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("FolderId cannot be null or empty");
        }

        Long folderId = Long.parseLong(folderIdStr);

        // 获取user id
        long id = (long) StpKit.USER.getSession().get("id");

        // 执行服务层
        pNoteTreeService.addNote(folderId, id);

        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("添加笔记成功")
                .build());
    }

    // 新建文件夹
    @PostMapping("/folder")
    public ResponseEntity<Object> addFolder(@RequestBody Map<String, String> requestBody) {

        // 获取前端传来的文件夹ID
        String folderIdStr = requestBody.get("FolderId");

        // 验证文件夹ID是否为null或空
        if (folderIdStr == null || folderIdStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("FolderId cannot be null or empty");
        }

        Long folderId = Long.parseLong(folderIdStr);

        // 获取user id
        long id = (long) StpKit.USER.getSession().get("id");

        // 执行服务层
        pNoteTreeService.addFolder(folderId, id);

        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("添加文件夹成功")
                .build());
    }

    // 删除笔记（假删除）
    @PostMapping("/deleteNote")
    public ResponseEntity<Object> deleteNote(@RequestBody Map<String, String> requestBody) {
        // 获取前端传来的笔记ID
        String noteIdStr = requestBody.get("NoteId");

        // 验证笔记ID是否为null或空
        if (noteIdStr == null || noteIdStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("NoteId cannot be null or empty");
        }

        Long noteId = Long.parseLong(noteIdStr);

        // 获取user id
        long userId = (long) StpKit.USER.getSession().get("id");

        // 执行服务层的删除笔记方法（伪删除）
        boolean success = pNoteTreeService.deleteNote(noteId, userId);

        if (success) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("删除笔记成功")
                    .build());
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse.Builder<>()
                    .status(400)
                    .message("删除笔记失败")
                    .build());
        }
    }

    // 删除文件夹（假删除）
    @PostMapping("/deleteFolder")
    public ResponseEntity<Object> deleteFolder(@RequestBody Map<String, String> requestBody) {
        // 获取前端传来的文件夹ID
        String folderIdStr = requestBody.get("FolderId");

        // 验证文件夹ID是否为null或空
        if (folderIdStr == null || folderIdStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("FolderId cannot be null or empty");
        }

        Long folderId = Long.parseLong(folderIdStr);

        // 获取user id
        long userId = (long) StpKit.USER.getSession().get("id");

        // 执行服务层的删除文件夹方法（伪删除）
        boolean success = pNoteTreeService.deleteFolder(folderId, userId);

        if (success) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("删除文件夹成功")
                    .build());
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse.Builder<>()
                    .status(400)
                    .message("删除文件夹失败")
                    .build());
        }
    }
}
