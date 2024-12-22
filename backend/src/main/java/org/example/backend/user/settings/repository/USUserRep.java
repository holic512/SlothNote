// /**
//  * CreateTime: 2024-08-27
//  * Description: 用户 持久层接口
//  * Version: 1.0
//  * Author: holic512
//  */
// package org.example.backend.user.settings.repository;
//
// import org.example.backend.common.entity.User;
// import org.example.backend.user.settings.dto.AccountInfo;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;
//
// @Repository
// public interface USUserRep extends JpaRepository<User, Long> {
//
//     // 查询 用于用户 设置中 的所需的 id username email
//     @Query("select new org.example.backend.user.settings.dto.AccountInfo(u.id,u.username,u.email) from User u where u.uid = :uid")
//     AccountInfo findAccountInfoDtoByUid(@Param("uid") String uid);
//
// }