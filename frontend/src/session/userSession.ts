/**
 * @file UserSession
 * @project SlothNote
 * @module 用户端 / 会话边界
 * @description 在登录切换、退出登录和 401 失效时统一清理用户级缓存与状态。
 * @logic 1. 中止进行中的 AI 请求；2. 清理笔记、待办、弹窗与选择状态；3. 清空笔记树缓存和用户令牌。
 * @dependencies Pinia stores, NoteTree cache, AiChat store
 * @index_tags 用户会话, 账号隔离, 状态清理, 缓存失效
 * @author holic512
 */

import {tokenStore} from "@/pinia/token";
import {logIDStore} from "@/pinia/logIDStore";
import {useUserInfoInitialized} from "@/views/User/Main/Pinia/UserInfoInitialized";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {useSaveNoteState} from "@/views/User/Main/components/Edit/Pinia/SaveNoteState";
import {useIndexItemsStore} from "@/views/User/Main/components/Edit/Pinia/IndexItems";
import {useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import {useFavoriteDialogStore} from "@/views/User/Main/components/Edit/Pinia/FavoriteDialogStore";
import {useNoteCoverState} from "@/views/User/Main/components/Edit/Main/SetCover/paina/NoteCoverState";
import {useAiChatStore} from "@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat";
import {useAiPermissionStore} from "@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiPermissions";
import {UseUpdateCommentState} from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/pinia/UpdateCommentState";
import {useTodoState} from "@/views/User/Main/components/TodoList/Pinia/TodoState";
import {useTodoCategoryState} from "@/views/User/Main/components/TodoList/Pinia/TodoCategoryState";
import {useSearchDialogStore} from "@/views/User/Main/components/SidebarM/Pinia/SearchDialogStore";
import {useRenameData} from "@/views/User/Main/components/Sidebar/Pinia/RenameData";
import {useDescriptionState} from "@/views/User/Main/components/Sidebar/Pinia/DescriptionState";
import {useDetailsState} from "@/views/User/Main/components/Sidebar/Pinia/DetailsState";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";
import {useNoteTreeUpdate} from "@/views/User/Main/components/Sidebar/Pinia/isNoteTreeUpdated";
import {clearUserAllTreeDataCache} from "@/views/User/Main/components/Sidebar/NoteTree/service/GetUserAllTreeData";

export interface ResetUserSessionOptions {
    clearToken?: boolean;
}

export const resetUserSessionState = ({clearToken = true}: ResetUserSessionOptions = {}): void => {
    useAiChatStore().resetClientState();
    useAiPermissionStore().resetPermissions();
    clearUserAllTreeDataCache();

    useCurrentNoteInfoStore().$reset();
    useSaveNoteState().$reset();
    useIndexItemsStore().$reset();
    useRightPageState().$reset();
    useFavoriteDialogStore().$reset();
    useNoteCoverState().$reset();
    UseUpdateCommentState().$reset();
    useTodoState().$reset();
    useTodoCategoryState().$reset();
    useSearchDialogStore().$reset();
    useRenameData().$reset();
    useDescriptionState().$reset();
    useDetailsState().$reset();
    useRightSelectNodeId().$reset();
    useNoteTreeUpdate().$reset();
    useUserInfoInitialized().$reset();
    logIDStore().clearLogID();

    if (typeof localStorage !== 'undefined') {
        localStorage.removeItem('hasCompletedInit');
        localStorage.removeItem('user-login-remember-password');
    }

    if (clearToken) tokenStore().clearUserToken();
};
