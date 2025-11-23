/**
 * File Name: UserProfile.java
 * Description: 用户扩展资料实体类，对应 user_profiles 表
 * Author: holic512
 * Created Date: 2024-09-20
 * Version: 1.0
 * Usage:
 * - 存储用户的个人资料，包括昵称、性别、年龄、简介、联系方式等
 * - 与 users 表一对一关联（user_id）
 */

package org.example.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    /**
     * 主键 ID，自增
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * users 表主键，与用户一对一
     * user_id 具有唯一约束
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 用户昵称
     */
    @Column(name = "nickname", nullable = false)
    private String nickname;

    /**
     * 用户性别
     * 值建议参考 UserGenderEnum（male / female / secret）
     */
    @Column(name = "gender", nullable = false)
    private String gender;

    /**
     * 年龄，可为空
     */
    @Column(name = "age")
    private Integer age;

    /**
     * 用户个性签名 / 简介
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * 联系方式，如手机号 / 微信号 / 邮箱
     */
    @Column(name = "contact_info")
    private String contactInfo;

    /**
     * 用户头像：
     */
    @Column(name = "avatar", length = 255)
    private String avatar;

    /**
     * 创建时间（自动生成）
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间（自动更新）
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;


    // ============================
    // 构造方法
    // ============================

    /**
     * JPA 默认构造方法
     */
    public UserProfile() {
    }

    /**
     * 创建用户资料时的简化构造方法
     */
    public UserProfile(Long userId, String nickname, String gender) {
        this.userId = userId;
        this.nickname = nickname;
        this.gender = gender;
    }


    // ============================
    // 生命周期自动方法
    // ============================

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

