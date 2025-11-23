/**
 * File Name: NTNoteRep.java
 * Description: 该接口用于定义与笔记信息相关的数据访问方法，主要通过 JPA 查询从数据库中获取笔记信息。
 * 特别是用于构建笔记树结构中的笔记节点。
 * Author: holic512
 * Created Date: 2024-10-21
 * Version: 1.0
 * Usage:
 * - 通过 `NTNoteRep` 接口可以查询用户的顶级笔记和指定文件夹下的笔记信息。
 * - 通常在服务层中使用这些方法来获取笔记信息，并将其转换为 `NoteTreeDto` 对象，以便在前端展示笔记树结构。
 */
package org.example.backend.user.note.noteTree.repository;

import org.example.backend.common.entity.NoteInfo;
import org.example.backend.user.note.noteTree.pojo.NoteTreeDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface NTNoteRep extends JpaRepository<NoteInfo, Long> {

    @Query("SELECT n.folderId FROM NoteInfo n WHERE n.id = :id")
    Long findFolderIdById(@Param("id") Long id);


    // 根据用户id 父文件夹 id 获取 未被 拟删除的 笔记 的( id 名称 类别)
    @Query("SELECT new org.example.backend.user.note.noteTree.pojo.NoteTreeDto(n.id,n.noteTitle," +
            "org.example.backend.user.note.noteTree.enums.TreeType.NOTE" +
            ",n.noteAvatar,n.note_cover_url)" +
            "FROM NoteInfo n " +
            "where n.userId = :userId And n.folderId = :folderId AND n.isDeleted = 0 ")
    List<NoteTreeDto> findFolderByUserIdAndFolderId(@Param("userId") Long userId, @Param("folderId") Long folderId);

    // 根目录下的笔记（folderId 为 null）
    @Query("SELECT new org.example.backend.user.note.noteTree.pojo.NoteTreeDto(n.id,n.noteTitle," +
            "org.example.backend.user.note.noteTree.enums.TreeType.NOTE" +
            ",n.noteAvatar,n.note_cover_url)" +
            "FROM NoteInfo n " +
            "where n.userId = :userId And n.folderId IS NULL AND n.isDeleted = 0 ")
    List<NoteTreeDto> findRootNotesByUserId(@Param("userId") Long userId);

    // 根据 笔记Id获取用户Id
    @Query("SELECT n.userId FROM NoteInfo n WHERE n.id = :id")
    Long findUserIdById(@Param("id") Long noteId);

    // 根据 笔记Id获取 笔记简介
    @Query("SELECT n.noteSummary FROM NoteInfo n WHERE n.id = :id")
    String findNoteSummaryById(@Param("id") Long noteId);

    @Query("SELECT n.createdAt FROM NoteInfo n WHERE n.id = :id")
    LocalDateTime findCreatedAtById(@Param("id") Long noteId);

    @Query("SELECT n.updatedAt FROM NoteInfo n WHERE n.id = :id")
    LocalDateTime findUpdatedAtById(@Param("id") Long noteId);

}