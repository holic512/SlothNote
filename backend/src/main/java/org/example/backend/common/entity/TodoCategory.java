/**
 * File Name: TodoCategory.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-29
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.common.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "todo_category")
public class TodoCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "BIGINT UNSIGNED COMMENT '自增主键，唯一标识每一条记录'")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long user_id;

    @Column(name = "type", nullable = false)
    private int type;

    @Column(name = "name", nullable = false, length = 255, columnDefinition = "VARCHAR(255) NOT NULL COMMENT '分类名称'")
    private String name;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT UNSIGNED DEFAULT 0 COMMENT '是否已删除，0:未删除，1:已删除'")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间，默认为当前时间戳'")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间，默认值为当前时间戳，并在每次更新时自动修改'")
    private LocalDateTime updatedAt;

}
