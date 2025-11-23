/**
 * File Name: Admin.java
 * Description: 管理员信息类
 * Author: holic512
 * Created Date: 2024-09-04
 * Version: 1.0
 * Usage:
 * 用于存储管理员的 授权信息
 */
package org.example.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "admins")  // 指定该类映射到数据库表 "admins"
public class Admin {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;  // 主键 ID

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;  // 用户名

    @Column(name = "password", nullable = false, length = 255)
    private String password;  // 密码

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;  // 邮箱

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 账户创建时间

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;  // 信息更新时间

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;

    public Admin() {

    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Admin(String email, String password) {
        this.email = email;
        this.password = password;
    }

}
