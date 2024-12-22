/**
 * File Name: NTFolderRep.java
 * Description: 该接口用于定义与文件夹信息相关的数据访问方法，主要通过 JPA 查询从数据库中获取文件夹信息。
 * 特别是用于构建笔记树结构中的文件夹节点。
 * Author: holic512
 * Created Date: 2024-10-16
 * Version: 1.0
 * Usage:
 * - 通过 `NTFolderRep` 接口可以查询用户的顶级文件夹和子文件夹信息。
 * - 通常在服务层中使用这些方法来获取文件夹信息，并将其转换为 `NoteTreeDto` 对象，以便在前端展示笔记树结构。
 */
package org.example.backend.user.note.noteTree.repository;

import org.example.backend.common.entity.FolderInfo;
import org.example.backend.user.note.noteTree.pojo.NoteFolderDto;
import org.example.backend.user.note.noteTree.pojo.NoteTreeDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

public interface NTFolderRep extends JpaRepository<FolderInfo, Long> {

    /**
     * 根据用户 ID 查询特定文件夹的信息。
     *
     * @param userId   用户 ID
     * @param folderId 文件夹 ID
     * @return 返回匹配的文件夹信息，封装在 NoteFolderDto 对象中
     */
    @Query("SELECT new org.example.backend.user.note.noteTree.pojo.NoteFolderDto(f.id, f.userId, f.folderName, f.parentId, f.description, f.createdAt, f.updatedAt) " +
            "FROM FolderInfo f " +
            "WHERE f.userId = :userId AND f.id = :folderId")
    List<NoteFolderDto> findFolderByUserIdAndFolderId(@Param("userId") Long userId, @Param("folderId") Long folderId);


    // 根据用户id 父文件夹 id 获取 未被 拟删除的 文件夹 的( id 名称 类别)
    @Query("SELECT new org.example.backend.user.note.noteTree.pojo.NoteTreeDto(f.id,f.folderName," +
            "org.example.backend.user.note.noteTree.enums.TreeType.FOLDER," +
            "f.folderAvatar)" +
            "FROM FolderInfo f " +
            "where f.userId = :userId And f.parentId = :parentId AND f.isDeleted = 0 ")
    List<NoteTreeDto> findFolderByUserIdAndParentId(@Param("userId") Long userId, @Param("parentId") Long parentId);

    // 查询该文件夹id下的 用户id
    @Query("select f.userId from FolderInfo f where f.id = :id")
    Long findUserIdById(@Param("id") Long id);

    // 根据 文件夹Id获取 文件夹简介
    @Query("select f.description from FolderInfo f where f.id = :id")
    String findDescriptionById(@Param("id") Long id);

    @Query("SELECT n.createdAt FROM NoteInfo n WHERE n.id = :id")
    LocalDateTime findCreatedAtById(@Param("id") Long folderId);

    @Query("SELECT n.updatedAt FROM NoteInfo n WHERE n.id = :id")
    LocalDateTime findUpdatedAtById(@Param("id") Long folderId);

}