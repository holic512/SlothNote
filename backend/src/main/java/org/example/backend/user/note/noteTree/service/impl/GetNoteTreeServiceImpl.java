/**
 * File Name: GNoteTreeImpl.java
 * Description: 该类实现了 `GNoteTreeService` 接口，提供了具体的业务逻辑实现，主要用于查询用户的文件夹和笔记信息，并构建笔记树结构。
 * Author: holic512
 * Created Date: 2024-10-16
 * Version: 1.0
 * Usage:
 * - 通过 `GNoteTreeImpl` 类可以查询用户的文件夹和笔记信息。
 * - 通常在控制器层中调用 `getNoteTreeList` 方法，获取文件夹和笔记的层级结构，并返回给前端进行展示。
 * - 前端可以根据返回的 `NoteTreeDto` 列表构建和展示笔记树结构。
 */
package org.example.backend.user.note.noteTree.service.impl;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.domain.Note;
import org.example.backend.user.note.noteTree.enums.GetUNTContextEnum;
import org.example.backend.user.note.noteTree.enums.TreeType;
import org.example.backend.user.note.noteTree.pojo.NoteTreeDto;
import org.example.backend.user.note.noteTree.repository.NTNoteRep;
import org.example.backend.user.note.noteTree.repository.NTFolderRep;
import org.example.backend.user.note.noteTree.repository.NoteTreeNoteRepM;
import org.example.backend.user.note.noteTree.service.GetNoteTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.time.format.DateTimeFormatter;

import static org.example.backend.user.note.noteTree.enums.GetUNTContextEnum.*;

@Service
public class GetNoteTreeServiceImpl implements GetNoteTreeService {

    private final NTFolderRep folderRep;
    private final NTNoteRep noteRep;
    private final NoteTreeNoteRepM noteRepM;

    @Autowired
    GetNoteTreeServiceImpl(NTFolderRep ntFolderRep, NTNoteRep ntNoteRep, NoteTreeNoteRepM noteTreeNoteRepM) {
        this.folderRep = ntFolderRep;
        this.noteRep = ntNoteRep;
        this.noteRepM = noteTreeNoteRepM;
    }

    @Override
    public Long getFolderIdByNoteId(long NoteId) {
        return noteRep.findFolderIdById(NoteId);
    }


    @Override
    public List<NoteTreeDto> getNoteTreeListByNoteId(long UserId, long ParentId) {
        // 递归查询 - 起点就是 ParentId = 0 (表示根目录) 终点是 此递归 文件夹下 没有 笔记或者文件夹

        // 先查询 根目录的 笔记和文件夹
        List<NoteTreeDto> folderList = folderRep.findFolderByUserIdAndParentId(UserId, ParentId);
        List<NoteTreeDto> noteList = (ParentId == 0)
                ? noteRep.findRootNotesByUserId(UserId)
                : noteRep.findFolderByUserIdAndFolderId(UserId, ParentId);

        // 将查询到的文件夹和笔记合并为一个列表
        List<NoteTreeDto> resultList = new ArrayList<>();
        // 添加文件夹到结果列表
        if (folderList != null && !folderList.isEmpty()) {
            resultList.addAll(folderList);
        }
        // 添加笔记到结果列表
        if (noteList != null && !noteList.isEmpty()) {
            resultList.addAll(noteList);
        }

        // 对每个文件夹进行递归查询，获取其子文件夹和子笔记
        for (NoteTreeDto node : resultList) {
            // 如果是文件夹，则递归查询其子文件夹和笔记
            if (node.getType().equals(TreeType.FOLDER)) {
                // 递归查询当前文件夹的子文件夹和笔记
                List<NoteTreeDto> subTree = getNoteTreeListByNoteId(UserId, node.getId());
                node.setChildren(subTree);  // 将子节点设置为当前文件夹的子节点
            }
        }

        return resultList;
    }

    @Override
    public Pair<GetUNTContextEnum, String> getNoteDescription(Long UserId, Long NoteId) {
        // 判断是否 存在该 UserId 的 NoteId
        Long realUserId = noteRep.findUserIdById(NoteId);
        if (!realUserId.equals(UserId))
            return new Pair<>(USER_NOTE_ID_MISMATCH, null);

        String noteDescription = noteRep.findNoteSummaryById(NoteId);
        return new Pair<>(SUCCESS, noteDescription);
    }

    @Override
    public Pair<GetUNTContextEnum, String> getFolderDescription(Long UserId, Long folderId) {
        // 判断是否 存在该 UserId 的 NoteId
        Long realUserId = folderRep.findUserIdById(folderId);
        if (!realUserId.equals(UserId))
            return new Pair<>(USER_NOTE_ID_MISMATCH, null);

        String noteDescription = folderRep.findDescriptionById(folderId);
        return new Pair<>(SUCCESS, noteDescription);
    }

    @Override
    public Pair<GetUNTContextEnum, HashMap<String, String>> getNoteCreatedAtAndUpdatedAt(Long UserId, Long NoteId) {
        // 判断是否 存在该 UserId 的 NoteId
        Long realUserId = noteRep.findUserIdById(NoteId);
        if (!realUserId.equals(UserId))
            return new Pair<>(USER_NOTE_ID_MISMATCH, null);

        // 定义 存储
        HashMap<String, String> hashMap = new HashMap<>();
        // 定义格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

        // 获取创建时间
        LocalDateTime CreatedAt = noteRep.findCreatedAtById(NoteId);
        String formattedCreatedAt = CreatedAt.format(formatter);
        hashMap.put("CreateAt", formattedCreatedAt);

        // 获取修改时间
        LocalDateTime UpdatedAt = noteRep.findUpdatedAtById(NoteId);
        String formattedUpdatedAt = UpdatedAt.format(formatter);
        hashMap.put("UpdatedAt", formattedUpdatedAt);

        return new Pair<>(SUCCESS, hashMap);
    }

    @Override
    public Pair<GetUNTContextEnum, String> getNoteSaveAt(Long UserId, Long NoteId) {

        // 判断是否 存在该 UserId 的 NoteId
        Long realUserId = noteRep.findUserIdById(NoteId);
        if (!realUserId.equals(UserId)){
            return new Pair<>(USER_NOTE_ID_MISMATCH, null);
        }

        // 执行查询
        Optional<Note> result = noteRepM.findById(NoteId);

        // 如果查询为空 证明笔记还没有编辑
        if (result.isEmpty()) {
            return new Pair<>(UNEDITED, null);
        }

        // 定义格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        String formattedUpdatedAt = result.get().getLastSavedAt().format(formatter);
        return new Pair<>(SUCCESS, formattedUpdatedAt);
    }

    @Override
    public Pair<GetUNTContextEnum, HashMap<String, String>> getFolderCreatedAtAndUpdatedAt(Long UserId, Long folderId) {
        // 判断是否 存在该 UserId 的 folderId
        Long realUserId = folderRep.findUserIdById(folderId);
        if (!realUserId.equals(UserId))
            return new Pair<>(USER_NOTE_ID_MISMATCH, null);

        // 定义 存储
        HashMap<String, String> hashMap = new HashMap<>();
        // 定义格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");

        // 获取创建时间
        LocalDateTime CreatedAt = folderRep.findCreatedAtById(folderId);
        String formattedCreatedAt = CreatedAt.format(formatter);
        hashMap.put("CreateAt", formattedCreatedAt);

        // 获取修改时间
        LocalDateTime UpdatedAt = folderRep.findUpdatedAtById(folderId);
        String formattedUpdatedAt = UpdatedAt.format(formatter);
        hashMap.put("UpdatedAt", formattedUpdatedAt);

        return new Pair<>(SUCCESS, hashMap);
    }
}
