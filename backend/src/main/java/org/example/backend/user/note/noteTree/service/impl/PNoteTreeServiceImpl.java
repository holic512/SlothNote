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
import org.springframework.transaction.annotation.Transactional;

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
        if (parentId == null || parentId == 0) {
            noteInfo.setFolderId(null);
        } else {
            noteInfo.setFolderId(parentId);
        }
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
    
    @Override
    @Transactional
    public boolean moveNote(Long noteId, Long targetFolderId, Long userId) {
        try {
            // 1. 验证笔记是否存在
            Optional<NoteInfo> noteOptional = ntNoteRep.findById(noteId);
            if (!noteOptional.isPresent()) {
                return false; // 笔记不存在
            }
            
            NoteInfo note = noteOptional.get();
            
            // 2. 验证笔记是否属于该用户
            if (!note.getUserId().equals(userId)) {
                return false; // 笔记不属于该用户
            }
            
            // 3. 如果目标文件夹ID为0或null，表示移动到根目录
            if (targetFolderId == null || targetFolderId == 0) {
                note.setFolderId(null);
                ntNoteRep.save(note);
                return true;
            }
            
            // 4. 验证目标文件夹是否存在
            Optional<FolderInfo> targetFolderOptional = ntFolderRep.findById(targetFolderId);
            if (!targetFolderOptional.isPresent()) {
                return false; // 目标文件夹不存在
            }
            
            FolderInfo targetFolder = targetFolderOptional.get();
            
            // 5. 验证目标文件夹是否属于该用户
            if (!targetFolder.getUserId().equals(userId)) {
                return false; // 目标文件夹不属于该用户
            }
            
            // 6. 验证目标文件夹是否未被删除
            if (targetFolder.getIsDeleted() == 1) {
                return false; // 目标文件夹已被删除
            }
            
            // 7. 验证笔记当前是否未被删除
            if (note.getIsDeleted() == 1) {
                return false; // 笔记已被删除
            }
            
            // 8. 更新笔记的文件夹ID
            note.setFolderId(targetFolderId);
            ntNoteRep.save(note);
            
            return true;
        } catch (Exception e) {
            // 记录异常并返回失败
            System.err.println("移动笔记失败: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    @Transactional
    public boolean moveFolder(Long folderId, Long targetFolderId, Long userId) {
        try {
            // 1. 验证要移动的文件夹是否存在
            Optional<FolderInfo> folderOptional = ntFolderRep.findById(folderId);
            if (!folderOptional.isPresent()) {
                return false; // 文件夹不存在
            }
            
            FolderInfo folder = folderOptional.get();
            
            // 2. 验证要移动的文件夹是否属于该用户
            if (!folder.getUserId().equals(userId)) {
                return false; // 文件夹不属于该用户
            }
            
            // 3. 如果目标文件夹ID为0或null，表示移动到根目录（文件夹根用0表示）
            if (targetFolderId == null || targetFolderId == 0) {
                folder.setParentId(0L);
                ntFolderRep.save(folder);
                return true;
            }
            
            // 4. 验证目标文件夹是否存在
            Optional<FolderInfo> targetFolderOptional = ntFolderRep.findById(targetFolderId);
            if (!targetFolderOptional.isPresent()) {
                return false; // 目标文件夹不存在
            }
            
            FolderInfo targetFolder = targetFolderOptional.get();
            
            // 5. 验证目标文件夹是否属于该用户
            if (!targetFolder.getUserId().equals(userId)) {
                return false; // 目标文件夹不属于该用户
            }
            
            // 6. 验证是否未被删除
            if (folder.getIsDeleted() == 1 || targetFolder.getIsDeleted() == 1) {
                return false; // 有一个已被删除
            }
            
            // 7. 验证不是将文件夹移动到自己或自己的子文件夹中（防止循环引用）
            if (folderId.equals(targetFolderId) || isDescendant(targetFolderId, folderId)) {
                return false; // 不能移动到自己或自己的子文件夹中
            }
            
            // 8. 更新文件夹的父ID
            folder.setParentId(targetFolderId);
            ntFolderRep.save(folder);
            
            return true;
        } catch (Exception e) {
            // 记录异常并返回失败
            System.err.println("移动文件夹失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 检查一个文件夹是否是另一个文件夹的后代（子文件夹、孙文件夹等）
     *
     * @param folderId 要检查的文件夹ID
     * @param ancestorId 可能的祖先文件夹ID
     * @return 如果folderId是ancestorId的后代，则返回true
     */
    private boolean isDescendant(Long folderId, Long ancestorId) {
        // 如果是根目录，直接返回false
        if (folderId == 0L) {
            return false;
        }
        
        // 获取当前文件夹的父文件夹ID
        Optional<FolderInfo> folderOpt = ntFolderRep.findById(folderId);
        if (!folderOpt.isPresent()) {
            return false;
        }
        
        FolderInfo folder = folderOpt.get();
        Long parentId = folder.getParentId();
        
        // 如果父ID等于ancestorId，则是后代
        if (parentId.equals(ancestorId)) {
            return true;
        }
        
        // 递归检查父文件夹
        return isDescendant(parentId, ancestorId);
    }
}
