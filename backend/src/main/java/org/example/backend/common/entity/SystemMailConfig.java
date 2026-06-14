/**
 * @file SystemMailConfig
 * @project SlothNote
 * @module 公共配置 / 邮箱配置
 * @description 映射系统级 SMTP 邮箱配置，供验证码与测试邮件统一读取。
 * @logic 1. 保存 SMTP 连接参数与发件人信息；2. 维护启用状态；3. 自动维护创建与更新时间。
 * @dependencies JPA: jakarta.persistence, Lombok: Data
 * @index_tags 邮箱配置, SMTP, 系统配置, 数据库存储
 * @author holic512
 */
package org.example.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "system_mail_config")
public class SystemMailConfig {

    @Id
    private Long id;

    @Column(length = 255)
    private String host;

    @Column
    private Integer port = 465;

    @Column(length = 255)
    private String username;

    @Column(length = 1024)
    private String password;

    @Column(length = 32, nullable = false)
    private String protocol = "smtps";

    @Column(name = "default_encoding", length = 64, nullable = false)
    private String defaultEncoding = "UTF-8";

    @Column(name = "from_address", length = 255)
    private String fromAddress;

    @Column(name = "from_name", length = 128)
    private String fromName;

    @Column(name = "smtp_auth", nullable = false)
    private Integer smtpAuth = 1;

    @Column(name = "ssl_enable", nullable = false)
    private Integer sslEnable = 1;

    @Column(name = "starttls_enable", nullable = false)
    private Integer starttlsEnable = 1;

    @Column(name = "starttls_required", nullable = false)
    private Integer starttlsRequired = 1;

    @Column(nullable = false)
    private Integer enabled = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        applyDefaults();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        applyDefaults();
    }

    private void applyDefaults() {
        if (this.port == null) {
            this.port = 465;
        }
        if (this.protocol == null || this.protocol.isBlank()) {
            this.protocol = "smtps";
        }
        if (this.defaultEncoding == null || this.defaultEncoding.isBlank()) {
            this.defaultEncoding = "UTF-8";
        }
        if (this.smtpAuth == null) {
            this.smtpAuth = 1;
        }
        if (this.sslEnable == null) {
            this.sslEnable = 1;
        }
        if (this.starttlsEnable == null) {
            this.starttlsEnable = 1;
        }
        if (this.starttlsRequired == null) {
            this.starttlsRequired = 1;
        }
        if (this.enabled == null) {
            this.enabled = 0;
        }
    }
}
