/**
 * File Name: Note.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-11
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.common.domain;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("note_content")
public class Note {

    @TableId(value = "note_id", type = IdType.INPUT)
    private Long noteId;

    @TableField("content")
    private String content;

    @TableField("last_saved_at")
    private LocalDateTime lastSavedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
