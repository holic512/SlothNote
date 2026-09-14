/**
 * @file SaveNote
 * @project SlothNote
 * @module 用户端 / 笔记保存
 * @description 封装当前笔记正文保存请求，并同步编辑器保存状态。
 * @logic 1. 解析传入的 Editor 或 Ref<Editor>；2. 校验笔记与编辑器状态；3. 序列化 Tiptap JSON 后调用保存接口。
 * @dependencies API: user/note/SaveNote, Store: currentNoteInfo/SaveNoteState, Helper: resolveEditor
 * @index_tags 笔记保存, Tiptap JSON, editor实例, 保存状态
 * @author holic512
 */
import {useCurrentNoteInfoStore} from "../Pinia/currentNoteInfo";
import axios from "../../../../../../axios";
import {ElMessage} from "element-plus";
import {useSaveNoteState} from "../Pinia/SaveNoteState";
import {resolveEditor, type MaybeEditorRef} from "@/views/User/Main/components/Edit/editor/editorContext";

export interface SaveNoteOptions {
    silent?: boolean;
}

export const SaveNote = async (editorInput: MaybeEditorRef, options: SaveNoteOptions = {}): Promise<boolean> => {
    const editor = resolveEditor(editorInput);
    const notify = (type: "success" | "warning" | "error", message: string) => {
        if (!options.silent) {
            ElMessage[type](message);
        }
    };

    // 笔记信息 pinia 实例
    const currentNoteInfo = useCurrentNoteInfoStore()

    if (!editor) {
        notify("warning", "编辑器尚未初始化，无法保存");
        return false;
    }

    if (currentNoteInfo.noteId == null) {
        notify("warning", "请先打开一篇笔记");
        return false;
    }

    try {
        // 获取 编辑器中的 笔记json
        const NoteDataJson = editor.getJSON()
        const contentString = JSON.stringify(NoteDataJson);

        // 获取 当前笔记的 ID
        const NoteId = currentNoteInfo.noteId;

        // 调用保存功能
        const response = await axios.post(
            "user/note/SaveNote",
            {
                "noteId": NoteId,
                "content": contentString
            });

        // 状态反馈
        if (response.data.status == 200) {
            // 恢复 保存状态
            const SaveNoteState = useSaveNoteState();
            SaveNoteState.saveContent();

            notify("success", "笔记保存成功");
            return true;
        } else {
            notify("error", "笔记保存失败");
            return false;
        }

    } catch (e) {
        console.error(e);
        notify("error", "笔记保存失败");
        return false;
    }
}
