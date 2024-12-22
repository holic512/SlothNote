// /**
//  * 文件名称: UserProfileRepository.java
//  * 描述: 用户资料存储库接口，提供对用户资料的查询功能，特别是用户设置所需的字段。
//  * 作者: holic512
//  * 创建日期: 2024-09-20
//  * 版本: 1.0
//  * 用法: 该接口用于通过用户ID查询用户的昵称、性别、年龄、联系方式和简介信息。
//  */
//
// package org.example.backend.user.settings.repository;
//
// import org.example.backend.common.entity.UserProfile;
// import org.example.backend.user.settings.dto.AccountInfo;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
//
// public interface USUserProfileRep extends JpaRepository<UserProfile, Long> {
//
//     // 查询 用于用户 设置中 的所需的  nickname gender age contactInfo bio
//     @Query("select new org.example.backend.user.settings.dto.AccountInfo(u.nickname,u.gender,u.age,u.contactInfo,u.bio) from UserProfile u where u.uid = :uid")
//     AccountInfo findAccountInfoDtoByUid(@Param("uid") String uid);
//
// }
