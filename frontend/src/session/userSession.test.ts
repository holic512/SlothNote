import {beforeEach, describe, expect, it, vi} from 'vitest';

const sessionMocks = vi.hoisted(() => ({
    aiReset: vi.fn(),
    clearTreeCache: vi.fn(),
    clearLogId: vi.fn(),
    clearUserToken: vi.fn(),
    resets: {
        currentNote: vi.fn(),
        saveNote: vi.fn(),
        indexItems: vi.fn(),
        rightPage: vi.fn(),
        favoriteDialog: vi.fn(),
        noteCover: vi.fn(),
        comments: vi.fn(),
        todo: vi.fn(),
        todoCategory: vi.fn(),
        searchDialog: vi.fn(),
        rename: vi.fn(),
        description: vi.fn(),
        details: vi.fn(),
        rightSelectNode: vi.fn(),
        noteTreeUpdate: vi.fn(),
        userInfoInitialized: vi.fn(),
    },
}));

vi.mock('@/pinia/token', () => ({
    tokenStore: () => ({clearUserToken: sessionMocks.clearUserToken}),
}));
vi.mock('@/pinia/logIDStore', () => ({
    logIDStore: () => ({clearLogID: sessionMocks.clearLogId}),
}));
vi.mock('@/views/User/Main/Pinia/UserInfoInitialized', () => ({
    useUserInfoInitialized: () => ({$reset: sessionMocks.resets.userInfoInitialized}),
}));
vi.mock('@/views/User/Main/components/Edit/Pinia/currentNoteInfo', () => ({
    useCurrentNoteInfoStore: () => ({$reset: sessionMocks.resets.currentNote}),
}));
vi.mock('@/views/User/Main/components/Edit/Pinia/SaveNoteState', () => ({
    useSaveNoteState: () => ({$reset: sessionMocks.resets.saveNote}),
}));
vi.mock('@/views/User/Main/components/Edit/Pinia/IndexItems', () => ({
    useIndexItemsStore: () => ({$reset: sessionMocks.resets.indexItems}),
}));
vi.mock('@/views/User/Main/components/Edit/Pinia/RightPageState', () => ({
    useRightPageState: () => ({$reset: sessionMocks.resets.rightPage}),
}));
vi.mock('@/views/User/Main/components/Edit/Pinia/FavoriteDialogStore', () => ({
    useFavoriteDialogStore: () => ({$reset: sessionMocks.resets.favoriteDialog}),
}));
vi.mock('@/views/User/Main/components/Edit/Main/SetCover/paina/NoteCoverState', () => ({
    useNoteCoverState: () => ({$reset: sessionMocks.resets.noteCover}),
}));
vi.mock('@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat', () => ({
    useAiChatStore: () => ({resetClientState: sessionMocks.aiReset}),
}));
vi.mock('@/views/User/Main/components/Edit/PageRight/components/NoteComment/pinia/UpdateCommentState', () => ({
    UseUpdateCommentState: () => ({$reset: sessionMocks.resets.comments}),
}));
vi.mock('@/views/User/Main/components/TodoList/Pinia/TodoState', () => ({
    useTodoState: () => ({$reset: sessionMocks.resets.todo}),
}));
vi.mock('@/views/User/Main/components/TodoList/Pinia/TodoCategoryState', () => ({
    useTodoCategoryState: () => ({$reset: sessionMocks.resets.todoCategory}),
}));
vi.mock('@/views/User/Main/components/SidebarM/Pinia/SearchDialogStore', () => ({
    useSearchDialogStore: () => ({$reset: sessionMocks.resets.searchDialog}),
}));
vi.mock('@/views/User/Main/components/Sidebar/Pinia/RenameData', () => ({
    useRenameData: () => ({$reset: sessionMocks.resets.rename}),
}));
vi.mock('@/views/User/Main/components/Sidebar/Pinia/DescriptionState', () => ({
    useDescriptionState: () => ({$reset: sessionMocks.resets.description}),
}));
vi.mock('@/views/User/Main/components/Sidebar/Pinia/DetailsState', () => ({
    useDetailsState: () => ({$reset: sessionMocks.resets.details}),
}));
vi.mock('@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId', () => ({
    useRightSelectNodeId: () => ({$reset: sessionMocks.resets.rightSelectNode}),
}));
vi.mock('@/views/User/Main/components/Sidebar/Pinia/isNoteTreeUpdated', () => ({
    useNoteTreeUpdate: () => ({$reset: sessionMocks.resets.noteTreeUpdate}),
}));
vi.mock('@/views/User/Main/components/Sidebar/NoteTree/service/GetUserAllTreeData', () => ({
    clearUserAllTreeDataCache: sessionMocks.clearTreeCache,
}));

import {resetUserSessionState} from './userSession';

const resetSpies = Object.values(sessionMocks.resets);

describe('resetUserSessionState', () => {
    beforeEach(() => {
        sessionMocks.aiReset.mockClear();
        sessionMocks.clearTreeCache.mockClear();
        sessionMocks.clearLogId.mockClear();
        sessionMocks.clearUserToken.mockClear();
        resetSpies.forEach(reset => reset.mockClear());
    });

    it('clears all account-scoped stores, caches and the user token by default', () => {
        const removeItem = vi.fn();
        vi.stubGlobal('localStorage', {removeItem});

        resetUserSessionState();

        expect(sessionMocks.aiReset).toHaveBeenCalledOnce();
        expect(sessionMocks.clearTreeCache).toHaveBeenCalledOnce();
        resetSpies.forEach(reset => expect(reset).toHaveBeenCalledOnce());
        expect(sessionMocks.clearLogId).toHaveBeenCalledOnce();
        expect(removeItem).toHaveBeenCalledWith('hasCompletedInit');
        expect(sessionMocks.clearUserToken).toHaveBeenCalledOnce();
    });

    it('can preserve the replacement token while still clearing account-scoped state', () => {
        const removeItem = vi.fn();
        vi.stubGlobal('localStorage', {removeItem});

        resetUserSessionState({clearToken: false});

        expect(sessionMocks.aiReset).toHaveBeenCalledOnce();
        expect(sessionMocks.clearTreeCache).toHaveBeenCalledOnce();
        resetSpies.forEach(reset => expect(reset).toHaveBeenCalledOnce());
        expect(sessionMocks.clearLogId).toHaveBeenCalledOnce();
        expect(removeItem).toHaveBeenCalledWith('hasCompletedInit');
        expect(sessionMocks.clearUserToken).not.toHaveBeenCalled();
    });
});
