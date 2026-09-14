package org.example.backend.user.ai.service;

import org.example.backend.common.entity.UserAiPermission;
import org.example.backend.user.ai.dto.UserAiPermissionDto;
import org.example.backend.user.ai.repository.UserAiPermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserAiPermissionServiceTest {

    private final Map<Long, UserAiPermission> permissionsByUser = new HashMap<>();
    private UserAiPermissionService service;

    @BeforeEach
    void setUp() {
        UserAiPermissionRepository repository = mock(UserAiPermissionRepository.class);
        when(repository.findByUserId(anyLong())).thenAnswer(invocation ->
                Optional.ofNullable(permissionsByUser.get(invocation.getArgument(0))));
        when(repository.save(any(UserAiPermission.class))).thenAnswer(invocation -> {
            UserAiPermission permission = invocation.getArgument(0);
            permissionsByUser.put(permission.getUserId(), permission);
            return permission;
        });
        service = new UserAiPermissionService(repository);
    }

    @Test
    void returnsReadOnlyDefaultsWhenTheCurrentUserHasNoRecord() {
        UserAiPermissionDto permissions = service.getPermissions(100L);

        assertThat(permissions.getCanReadAllNotes()).isTrue();
        assertThat(permissions.getCanWriteNoteContent()).isFalse();
        assertThat(permissions.getCanWriteNoteTitle()).isFalse();
        assertThat(permissions.getCanWriteNoteSummary()).isFalse();
        assertThat(permissions.getCanWriteNoteCover()).isFalse();
    }

    @Test
    void persistsPermissionsPerUserRatherThanPerNote() {
        UserAiPermissionDto userOne = new UserAiPermissionDto(true, true, false, false, false);
        UserAiPermissionDto userTwo = new UserAiPermissionDto(false, false, true, true, true);

        service.updatePermissions(1L, userOne);
        service.updatePermissions(2L, userTwo);

        assertThat(service.getPermissions(1L)).usingRecursiveComparison().isEqualTo(userOne);
        assertThat(service.getPermissions(2L)).usingRecursiveComparison().isEqualTo(userTwo);
        assertThat(permissionsByUser).hasSize(2).doesNotContainKey(3L);
    }

    @Test
    void mapsEveryToolToItsRequiredGlobalPermission() {
        UserAiPermissionDto readOnly = new UserAiPermissionDto(true, false, false, false, false);
        assertThat(service.allowedToolNames(readOnly)).containsExactlyInAnyOrder(
                UserAiToolService.SEARCH_USER_NOTES,
                UserAiToolService.READ_NOTE,
                UserAiToolService.GET_CURRENT_NOTE
        );

        assertThat(service.canUseTool(new UserAiPermissionDto(false, true, false, false, false),
                UserAiToolService.APPEND_TO_CURRENT_NOTE)).isTrue();
        assertThat(service.canUseTool(new UserAiPermissionDto(false, true, false, false, false),
                UserAiToolService.REPLACE_SELECTED_TEXT)).isTrue();
        assertThat(service.canUseTool(new UserAiPermissionDto(false, true, false, false, false),
                UserAiToolService.INSERT_AFTER_SELECTED_TEXT)).isTrue();
        assertThat(service.canUseTool(new UserAiPermissionDto(false, false, true, false, false),
                UserAiToolService.UPDATE_CURRENT_NOTE_TITLE)).isTrue();
        assertThat(service.canUseTool(new UserAiPermissionDto(false, false, false, true, false),
                UserAiToolService.SAVE_CURRENT_NOTE_SUMMARY)).isTrue();
        assertThat(service.canUseTool(new UserAiPermissionDto(false, false, false, false, true),
                UserAiToolService.UPDATE_CURRENT_NOTE_COVER)).isTrue();
        assertThat(service.canUseTool(readOnly, UserAiToolService.APPEND_TO_CURRENT_NOTE)).isFalse();
        assertThat(service.canUseTool(readOnly, UserAiToolService.UPDATE_CURRENT_NOTE_TITLE)).isFalse();
    }

    @Test
    void rejectsToolExecutionWhenTheCorrespondingSwitchIsOff() {
        UserAiPermissionDto readOnly = new UserAiPermissionDto(true, false, false, false, false);

        assertThatThrownBy(() -> service.requireToolAccess(readOnly, UserAiToolService.SAVE_CURRENT_NOTE_SUMMARY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UserAiToolService.SAVE_CURRENT_NOTE_SUMMARY);
    }

    @Test
    void requiresACompletePermissionObjectForUpdates() {
        assertThatThrownBy(() -> service.updatePermissions(1L, new UserAiPermissionDto(true, null, false, false, false)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("必须包含全部开关");
    }
}
