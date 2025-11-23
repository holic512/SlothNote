/**
 * File Name: AdminUserMmService.java
 * Description: 管理员 用户管理 服务类接口
 * Author: holic512
 * Created Date: 2024-09-12
 * Version: 1.0
 * Usage:
 * 管理员 用户管理 服务类接口
 */
package org.example.backend.admin.userMm.service;

import org.example.backend.admin.userMm.enums.AdminUserMmEnum;
import org.example.backend.admin.userMm.request.AddUserRequest;
import org.example.backend.admin.userMm.request.SearchUserRequest;
import org.example.backend.admin.userMm.request.UpdateUserRequest;
import org.example.backend.admin.userMm.dto.AdminUserDetailDto;
import org.example.backend.common.enums.user.UserStatusEnum;
import org.example.backend.common.dto.user.UserDetailDto;

import java.util.List;

public interface AdminUserMmService {

    /**
     * 查询 user表中的总数
     *
     * @return user表中行数
     */
    Long getUserCount();

    /**
     * 查询 初始页面 (0 - count行) 的 用户数据
     *
     * @param count 初始页面(第一页)有多少行
     * @return 带有用户数据的 列表
     */
    List<UserDetailDto> fetchInitialUser(int count);

    /**
     * 查询 范围start-end的 用户数据
     *
     * @param start 偏移量
     * @param end   结束位置
     * @return 带有用户数据的 列表
     */
    List<UserDetailDto> findUserInRange(int start, int end);

    /**
     * 添加 用户 数据
     *
     * @param addUserRequest 前端-用户数据(username,password,email,status)
     * @return 是否添加成功
     */
    AdminUserMmEnum addUser(AddUserRequest addUserRequest);

    /**
     * 批量删除 用户
     *
     * @param userIds 存储用户id的列表
     * @return 是否删除成功
     */
    boolean deleteUsers(List<Long> userIds);

    List<UserDetailDto> searchUsers(SearchUserRequest request);

    long countSearchUsers(SearchUserRequest request);

    AdminUserDetailDto getUserDetail(Long id);

    AdminUserMmEnum updateUser(UpdateUserRequest request);

    boolean deleteUser(Long id);

    boolean batchUpdateStatus(List<Long> userIds, UserStatusEnum status);


}
