/**
 * @file noteNavigation
 * @project SlothNote
 * @module 用户端 / 笔记导航
 * @description 统一处理侧边栏、知识星图和正文引用的笔记跳转。
 * @logic 1. 检查未保存状态并提示；2. 优先使用已有笔记元数据，缺失时查询 share/info；3. 同步 currentNoteInfo 后路由跳转。
 * @dependencies Store: currentNoteInfo/SaveNoteState, Service: getNoteShareInfo, Router: vue-router
 * @index_tags 笔记跳转, 引用跳转, 知识图谱导航, 页面卡顿优化
 * @author holic512
 */
import type {Router} from "vue-router";
import {ElMessage, ElMessageBox} from "element-plus";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {useSaveNoteState} from "@/views/User/Main/components/Edit/Pinia/SaveNoteState";
import {getNoteShareInfo} from "@/views/User/Main/components/Edit/PageHeader/service/getNoteShareInfo";

export interface NavigateNoteMeta {
    noteId: number;
    noteName?: string;
    noteLocation?: string[];
    avatar?: string;
    cover?: string | null;
}

export const navigateToNote = async (router: Router, note: NavigateNoteMeta): Promise<boolean> => {
    const currentNoteInfo = useCurrentNoteInfoStore();
    const saveState = useSaveNoteState();

    if (currentNoteInfo.noteId === note.noteId && router.currentRoute.value.path === "/user/main/edit") {
        return true;
    }

    if (!saveState.isSaved) {
        try {
            await ElMessageBox.confirm(
                "当前笔记没有保存，确定要切换吗？",
                "笔记未保存",
                {
                    confirmButtonText: "确认",
                    cancelButtonText: "取消",
                    type: "warning",
                }
            );
        } catch {
            return false;
        }
    }

    const resolvedNote = await resolveNoteMeta(note);
    if (!resolvedNote) {
        ElMessage.warning("目标笔记不存在或无权访问");
        return false;
    }

    currentNoteInfo.setNoteInfo(
        resolvedNote.noteId,
        resolvedNote.noteName || "新建文档",
        resolvedNote.noteLocation || [],
        resolvedNote.avatar || "",
        resolvedNote.cover ?? null
    );
    saveState.saveContent();

    await router.push({path: "/user/main/edit", query: {noteId: String(resolvedNote.noteId)}});
    return true;
};

const resolveNoteMeta = async (note: NavigateNoteMeta): Promise<NavigateNoteMeta | null> => {
    if (note.noteName != null && note.noteLocation != null) {
        return note;
    }

    const shareInfo = await getNoteShareInfo(note.noteId);
    if (!shareInfo) {
        return null;
    }

    return {
        noteId: shareInfo.noteId,
        noteName: shareInfo.noteName,
        noteLocation: shareInfo.noteLocation,
        avatar: shareInfo.avatar,
        cover: shareInfo.cover,
    };
};
