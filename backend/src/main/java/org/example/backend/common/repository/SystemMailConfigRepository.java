/**
 * @file SystemMailConfigRepository
 * @project SlothNote
 * @module 公共配置 / 邮箱配置
 * @description 提供系统级 SMTP 邮箱配置的数据库访问能力。
 * @logic 1. 通过固定 id 读取单例邮箱配置；2. 复用 Spring Data JPA 基础 CRUD。
 * @dependencies Entity: SystemMailConfig, Spring Data JPA
 * @index_tags 邮箱配置, Repository, SMTP, 系统配置
 * @author holic512
 */
package org.example.backend.common.repository;

import org.example.backend.common.entity.SystemMailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemMailConfigRepository extends JpaRepository<SystemMailConfig, Long> {
}
