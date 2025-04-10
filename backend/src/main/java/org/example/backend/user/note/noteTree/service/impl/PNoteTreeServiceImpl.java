/**
 * File Name: PNoteTreeServiceImpl.java
 * Description: Todo
 * Author: lv
 * Created Date: 2024-10-28
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.note.noteTree.service.impl;

import org.example.backend.common.entity.FolderInfo;
import org.example.backend.common.entity.NoteInfo;
import org.example.backend.common.enums.note.NoteType;
import org.example.backend.user.note.noteTree.repository.NTFolderRep;
import org.example.backend.user.note.noteTree.repository.NTNoteRep;
import org.example.backend.user.note.noteTree.service.PNoteTreeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PNoteTreeServiceImpl implements PNoteTreeService {


    private final NTNoteRep ntNoteRep;
    private final NTFolderRep ntFolderRep;

    @Autowired
    PNoteTreeServiceImpl(NTNoteRep ntNoteRep, NTFolderRep ntFolderRep) {
        this.ntNoteRep = ntNoteRep;
        this.ntFolderRep = ntFolderRep;
    }

    @Override
    public void addNote(Long parentId, Long UserId) {
        // 处理数据
        NoteInfo noteInfo = new NoteInfo();
        noteInfo.setNoteType(NoteType.Normal.getValue());
        noteInfo.setFolderId(parentId);
        noteInfo.setUserId(UserId);

        ntNoteRep.save(noteInfo);
    }

    @Override
    public void addFolder(Long parentId, Long UserId) {

        FolderInfo folderInfo = new FolderInfo();
        folderInfo.setFolderName("新建文件夹");
        folderInfo.setParentId(parentId);
        folderInfo.setUserId(UserId);

        ntFolderRep.save(folderInfo);
    }
    
    @Override
    public boolean deleteNote(Long noteId, Long userId) {
        try {
            // 查询笔记是否存在
            Optional<NoteInfo> noteOptional = ntNoteRep.findById(noteId);
            if (!noteOptional.isPresent()) {
                return false; // 笔记不存在
            }
            
            NoteInfo note = noteOptional.get();
            
            // 检查笔记是否属于该用户
            if (!note.getUserId().equals(userId)) {
                return false; // 笔记不属于该用户
            }
            
            // 执行伪删除（设置删除标记）
            note.setIsDeleted(1); // 1表示已删除
            ntNoteRep.save(note);
            
            return true;
        } catch (Exception e) {
            // 记录异常并返回失败
            System.err.println("删除笔记失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean deleteFolder(Long folderId, Long userId) {
        try {
            // 查询文件夹是否存在
            Optional<FolderInfo> folderOptional = ntFolderRep.findById(folderId);
            if (!folderOptional.isPresent()) {
                return false; // 文件夹不存在
            }
            
            FolderInfo folder = folderOptional.get();
            
            // 检查文件夹是否属于该用户
            if (!folder.getUserId().equals(userId)) {
                return false; // 文件夹不属于该用户
            }
            
            // 执行伪删除（设置删除标记）
            folder.setIsDeleted(1); // 1表示已删除
            ntFolderRep.save(folder);
            
            // 注意：这里没有递归删除子文件夹和笔记
            // 如果需要递归删除，可以在此处添加代码
            
            return true;
        } catch (Exception e) {
            // 记录异常并返回失败
            System.err.println("删除文件夹失败: " + e.getMessage());
            return false;
        }
    }
}
