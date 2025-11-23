/**
 * CreateTime: 2024-08-27
 * Description: 用户 持久层接口
 * Version: 1.0
 * Author: holic512
 */
package org.example.backend.common.repository;

import jakarta.transaction.Transactional;
import org.example.backend.common.dto.user.UserAuthDto;
import org.example.backend.common.dto.user.UserDetailDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.backend.common.entity.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 判断 email 是否存在
    boolean existsByEmail(String email);

    // 判断 username 是否存在
    boolean existsByUsername(String username);

    // 判断 uid 是否存在
    boolean existsByUid(String uid);

    // 获取用户数目
    long count();

    // 查询 用于用户登录中 的所需的  密码,UID,状态
    @Query("select new org.example.backend.common.dto.user.UserAuthDto(u.id,u.uid,u.password,u.status) from User u where u.username = :username")
    UserAuthDto findAuthDtoByUsername(@Param("username") String username);

    // 查询 用于用户登录中 的所需的 UID,状态
    @Query("select new org.example.backend.common.dto.user.UserAuthDto(u.uid,u.status) from User u where u.email = :email")
    UserAuthDto findAuthDtoByEmail(@Param("email") String email);

    // 获取指定范围内的用户数据 - 管理员后台-用户管理
    @Query("SELECT new org.example.backend.common.dto.user.UserDetailDto(u.id,u.uid, u.username, u.email, u.status) " +
            "FROM User u")
    List<UserDetailDto> findUserInRange(Pageable pageable);

    // 根据 id 批量删除用户
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.id IN :ids")
    void deleteUsersByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.status = :status WHERE u.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") Integer status);

}