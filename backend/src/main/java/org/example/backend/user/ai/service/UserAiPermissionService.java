/**
 * @file UserAiPermissionService
 * @project SlothNote
 * @module 用户端 / AI 权限
 * @description 管理用户全局 AI 笔记权限，并将权限映射为可执行 AI 工具。
 * @logic 1. 缺失记录时返回最小写入权限默认值；2. 仅更新当前用户的权限；3. 每次工具调用前按名称验证权限。
 * @dependencies Repository: UserAiPermissionRepository, Tool: UserAiToolService
 * @index_tags 用户AI, 权限校验, 工具白名单, 全部笔记
 * @author holic512
 */
package org.example.backend.user.ai.service;

import org.example.backend.common.entity.UserAiPermission;
import org.example.backend.user.ai.dto.UserAiPermissionDto;
import org.example.backend.user.ai.repository.UserAiPermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserAiPermissionService {
    private final UserAiPermissionRepository repository;

    public UserAiPermissionService(UserAiPermissionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UserAiPermissionDto getPermissions(Long userId) {
        return repository.findByUserId(userId)
                .map(this::toDto)
                .orElseGet(UserAiPermissionService::defaultPermissions);
    }

    @Transactional
    public UserAiPermissionDto updatePermissions(Long userId, UserAiPermissionDto request) {
        validateCompleteRequest(request);
        UserAiPermission permission = repository.findByUserId(userId).orElseGet(() -> {
            UserAiPermission created = new UserAiPermission();
            created.setUserId(userId);
            return created;
        });
        permission.setCanReadAllNotes(request.getCanReadAllNotes());
        permission.setCanWriteNoteContent(request.getCanWriteNoteContent());
        permission.setCanWriteNoteTitle(request.getCanWriteNoteTitle());
        permission.setCanWriteNoteSummary(request.getCanWriteNoteSummary());
        permission.setCanWriteNoteCover(request.getCanWriteNoteCover());
        return toDto(repository.save(permission));
    }

    public boolean canUseTool(UserAiPermissionDto permissions, String tool) {
        return switch (tool) {
            case UserAiToolService.SEARCH_USER_NOTES,
                    UserAiToolService.READ_NOTE,
                    UserAiToolService.GET_CURRENT_NOTE -> Boolean.TRUE.equals(permissions.getCanReadAllNotes());
            case UserAiToolService.REPLACE_SELECTED_TEXT,
                    UserAiToolService.APPEND_TO_CURRENT_NOTE,
                    UserAiToolService.INSERT_AFTER_SELECTED_TEXT -> Boolean.TRUE.equals(permissions.getCanWriteNoteContent());
            case UserAiToolService.UPDATE_CURRENT_NOTE_TITLE -> Boolean.TRUE.equals(permissions.getCanWriteNoteTitle());
            case UserAiToolService.SAVE_CURRENT_NOTE_SUMMARY -> Boolean.TRUE.equals(permissions.getCanWriteNoteSummary());
            case UserAiToolService.UPDATE_CURRENT_NOTE_COVER -> Boolean.TRUE.equals(permissions.getCanWriteNoteCover());
            default -> false;
        };
    }

    public void requireToolAccess(UserAiPermissionDto permissions, String tool) {
        if (!canUseTool(permissions, tool)) {
            throw new IllegalArgumentException("当前 AI 权限未允许执行工具：" + tool);
        }
    }

    public List<String> allowedToolNames(UserAiPermissionDto permissions) {
        return UserAiToolService.ALL_TOOL_NAMES.stream()
                .filter(tool -> canUseTool(permissions, tool))
                .toList();
    }

    private static UserAiPermissionDto defaultPermissions() {
        return new UserAiPermissionDto(true, false, false, false, false);
    }

    private UserAiPermissionDto toDto(UserAiPermission permission) {
        return new UserAiPermissionDto(
                Boolean.TRUE.equals(permission.getCanReadAllNotes()),
                Boolean.TRUE.equals(permission.getCanWriteNoteContent()),
                Boolean.TRUE.equals(permission.getCanWriteNoteTitle()),
                Boolean.TRUE.equals(permission.getCanWriteNoteSummary()),
                Boolean.TRUE.equals(permission.getCanWriteNoteCover())
        );
    }

    private void validateCompleteRequest(UserAiPermissionDto request) {
        if (request == null
                || request.getCanReadAllNotes() == null
                || request.getCanWriteNoteContent() == null
                || request.getCanWriteNoteTitle() == null
                || request.getCanWriteNoteSummary() == null
                || request.getCanWriteNoteCover() == null) {
            throw new IllegalArgumentException("AI 权限更新必须包含全部开关");
        }
    }
}
