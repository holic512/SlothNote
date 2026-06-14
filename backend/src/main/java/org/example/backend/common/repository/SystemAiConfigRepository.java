/**
 * @file SystemAiConfigRepository
 * @project SlothNote
 * @module 公共配置 / AI 配置
 * @description 提供系统级 AI 配置表的 JPA 访问入口。
 * @logic 1. 通过单例主键读取配置；2. 保存管理员维护后的配置。
 * @dependencies Entity: SystemAiConfig, Spring Data JPA
 * @index_tags AI配置, Repository, 系统配置, JPA
 * @author holic512
 */
package org.example.backend.common.repository;

import org.example.backend.common.entity.SystemAiConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemAiConfigRepository extends JpaRepository<SystemAiConfig, Long> {
}
