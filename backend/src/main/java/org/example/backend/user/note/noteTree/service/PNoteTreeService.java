package org.example.backend.user.note.noteTree.service;

public interface PNoteTreeService {

    void addNote(Long parentId, Long UserId);

    void addFolder(Long parentId, Long UserId);
    
    /**
     * 删除笔记（伪删除）
     * 
     * @param noteId 要删除的笔记ID
     * @param userId 操作的用户ID
     * @return 操作是否成功
     */
    boolean deleteNote(Long noteId, Long userId);
    
    /**
     * 删除文件夹（伪删除）
     * 
     * @param folderId 要删除的文件夹ID
     * @param userId 操作的用户ID
     * @return 操作是否成功
     */
    boolean deleteFolder(Long folderId, Long userId);
    
    /**
     * 移动笔记到指定文件夹
     *
     * @param noteId 要移动的笔记ID
     * @param targetFolderId 目标文件夹ID
     * @param userId 操作的用户ID
     * @return 操作是否成功
     */
    boolean moveNote(Long noteId, Long targetFolderId, Long userId);
    
    /**
     * 移动文件夹到指定文件夹
     *
     * @param folderId 要移动的文件夹ID
     * @param targetFolderId 目标文件夹ID
     * @param userId 操作的用户ID
     * @return 操作是否成功
     */
    boolean moveFolder(Long folderId, Long targetFolderId, Long userId);
}
