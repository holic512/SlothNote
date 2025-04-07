package org.example.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.example.backend.common.enums.user.UserStatusEnum;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users") // 指定该类映射到数据库表
public class User {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "uid", unique = true, nullable = false, length = 50)
    private String uid;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "has_profile")
    private Integer hasProfile;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 可以在实体类中添加 @PrePersist 和 @PreUpdate 方法来自动处理创建时间和更新时间
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public User() {
    }

    // 内部静态类 Builder
    public static class Builder {
        private Long id;
        private String uid;
        private String username;
        private String password;
        private String email;
        private Integer status;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder uid(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public User build() {
            User user = new User();
            user.id = this.id;
            user.uid = this.uid;
            user.username = this.username;
            user.password = this.password;
            user.email = this.email;
            user.status = this.status;
            return user;
        }
    }

}
