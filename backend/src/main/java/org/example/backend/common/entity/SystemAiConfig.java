/**
 * @file SystemAiConfig
 * @project SlothNote
 * @module 公共配置 / AI 配置
 * @description 映射系统级 AI 供应商配置，供用户 AI 服务统一读取。
 * @logic 1. 保存 OpenAI 兼容接口参数；2. 维护启用状态与生成参数；3. 自动维护创建与更新时间。
 * @dependencies JPA: jakarta.persistence, Lombok: Data
 * @index_tags AI配置, 系统配置, 数据库存储, OpenAI兼容
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
@Table(name = "system_ai_config")
public class SystemAiConfig {

    @Id
    private Long id;

    @Column(name = "provider_name", nullable = false, length = 64)
    private String providerName;

    @Column(name = "base_url", length = 500)
    private String baseUrl;

    @Column(name = "api_key", length = 1024)
    private String apiKey;

    @Column(name = "model", length = 128)
    private String model;

    @Column(nullable = false)
    private Double temperature = 0.7;

    @Column(name = "max_tokens", nullable = false)
    private Integer maxTokens = 4096;

    @Column(name = "planner_temperature", nullable = false)
    private Double plannerTemperature = 0.1;

    @Column(name = "planner_max_tokens", nullable = false)
    private Integer plannerMaxTokens = 256;

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
        if (this.providerName == null || this.providerName.isBlank()) {
            this.providerName = "openai-compatible";
        }
        if (this.temperature == null) {
            this.temperature = 0.7;
        }
        if (this.maxTokens == null) {
            this.maxTokens = 4096;
        }
        if (this.plannerTemperature == null) {
            this.plannerTemperature = 0.1;
        }
        if (this.plannerMaxTokens == null) {
            this.plannerMaxTokens = 256;
        }
        if (this.enabled == null) {
            this.enabled = 0;
        }
    }
}
