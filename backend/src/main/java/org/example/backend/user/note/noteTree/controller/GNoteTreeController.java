/**
 * File Name: GNoteTreeController.java
 * Description: 该控制器用于处理用户笔记树的 GET 请求，主要负责查询用户的文件夹和笔记结构。
 *
 * @Author: holic512
 * Created Date: 2024-10-16
 * Version: 1.0
 * Usage:
 * - 通过 `GNoteTreeController` 可以查询用户的文件夹和笔记树结构。
 * - 主要用于前端展示用户的文件夹和笔记列表，支持按父文件夹 ID 进行查询。
 * - 通常在用户界面中展示文件夹树、笔记列表等场景中使用。
 */
package org.example.backend.user.note.noteTree.controller;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.note.noteTree.enums.GetUNTContextEnum;
import org.example.backend.user.note.noteTree.pojo.NoteTreeDto;
import org.example.backend.user.note.noteTree.service.GetNoteTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

import static org.example.backend.user.note.noteTree.enums.GetUNTContextEnum.*;

@RestController
@RequestMapping("/user/noteTree")
public class GNoteTreeController {

    private final GetNoteTreeService getNoteTreeService;

    @Autowired
    GNoteTreeController(GetNoteTreeService getNoteTreeService) {
        this.getNoteTreeService = getNoteTreeService;
    }

    /**
     * 获取用户的全部 笔记树 - 递归
     */
    @GetMapping("/UserAll")
    public ResponseEntity<Object> GetUserAllNoteTree() {
        // 获取用户id
        Long user_id = (Long) StpKit.USER.getSession().get("id");

        List<NoteTreeDto> result = getNoteTreeService.getNoteTreeListByNoteId(user_id, 0);

        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("OK")
                .data(result)
                .build()
        );
    }


    /**
     * 根据用户的笔记Id 获取 文件夹Id
     */
    @GetMapping("/folderIdByNoteId")
    public ResponseEntity<Object> getFolderIdByNoteId(@RequestParam Long noteId) {

        Long folderId = getNoteTreeService.getFolderIdByNoteId(noteId);

        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("查询成功")
                .data(folderId)
                .build());
    }

    /**
     * 获取用户的 笔记简介
     */
    @GetMapping("NoteDescription")
    public ResponseEntity<Object> getNoteDescription(@RequestParam Long noteId) {
        // 获取用户id
        Long user_id = (Long) StpKit.USER.getSession().get("id");

        Pair<GetUNTContextEnum, String> result = getNoteTreeService.getNoteDescription(user_id, noteId);

        if (result.a == GetUNTContextEnum.SUCCESS)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("查询成功")
                    .data(result.b)
                    .build());
        else if (result.a == GetUNTContextEnum.USER_NOTE_ID_MISMATCH)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("用户id与笔记id不匹配")
                    .build());
        else return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("网络出现问题")
                    .build());
    }

    /**
     * 获取用户的 文件夹简介
     */
    @GetMapping("folderDescription")
    public ResponseEntity<Object> getFolderDescription(@RequestParam Long noteId) {
        // 获取用户id
        Long user_id = (Long) StpKit.USER.getSession().get("id");

        Pair<GetUNTContextEnum, String> result = getNoteTreeService.getFolderDescription(user_id, noteId);

        if (result.a == GetUNTContextEnum.SUCCESS)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("查询成功")
                    .data(result.b)
                    .build());
        else if (result.a == GetUNTContextEnum.USER_NOTE_ID_MISMATCH)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("用户id与笔记id不匹配")
                    .build());
        else return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("网络出现问题")
                    .build());
    }


    /**
     * 获取 用户-笔记的 创建时间和更新时间
     */
    @GetMapping("NoteCreatedAtAndUpdatedAt")
    public ResponseEntity<Object> getNoteCreatedAtAndUpdatedAtAndSaveAt(@RequestParam Long noteId) {
        // 获取用户id
        Long user_id = (Long) StpKit.USER.getSession().get("id");

        Pair<GetUNTContextEnum, HashMap<String, String>> result = getNoteTreeService.getNoteCreatedAtAndUpdatedAt(user_id, noteId);

        if (result.a == GetUNTContextEnum.SUCCESS)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("查询成功")
                    .data(result.b)
                    .build());
        else if (result.a == GetUNTContextEnum.USER_NOTE_ID_MISMATCH)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("用户id与笔记id不匹配")
                    .build());
        else return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("网络出现问题")
                    .build());
    }


    /**
     * 获取 用户-笔记的 保存时间
     */
    @GetMapping("NoteSaveAt")
    public ResponseEntity<Object> getNoteSaveAt(@RequestParam Long noteId) {
        // 获取用户id
        Long user_id = (Long) StpKit.USER.getSession().get("id");

        Pair<GetUNTContextEnum, String> result = getNoteTreeService.getNoteSaveAt(user_id, noteId);

        if (result.a == GetUNTContextEnum.SUCCESS)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("查询成功")
                    .data(result.b)
                    .build());
        else if (result.a == GetUNTContextEnum.USER_NOTE_ID_MISMATCH)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("用户id与笔记id不匹配")
                    .build());
        else if (result.a == UNEDITED)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("笔记还未开始编辑")
                    .build());

        else return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("网络出现问题")
                    .build());
    }

    /**
     * 获取 用户-文件夹的 创建时间和更新时间
     */
    @GetMapping("folderCreatedAtAndUpdatedAt")
    public ResponseEntity<Object> getFolderCreatedAtAndUpdatedAt(@RequestParam Long folderId) {
        Long user_id = (Long) StpKit.USER.getSession().get("id");

        Pair<GetUNTContextEnum, HashMap<String, String>> result = getNoteTreeService.getFolderCreatedAtAndUpdatedAt(user_id, folderId);
        if (result.a == GetUNTContextEnum.SUCCESS)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("查询成功")
                    .data(result.b)
                    .build());
        else if (result.a == GetUNTContextEnum.USER_NOTE_ID_MISMATCH)
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message("用户id与文件夹Id不匹配")
                    .build());
        else return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("网络出现问题")
                    .build());
    }


}

