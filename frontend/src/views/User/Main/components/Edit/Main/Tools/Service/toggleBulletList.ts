/**
 * @file toggleBulletList
 * @project SlothNote
 * @module 用户端 / 编辑器工具命令
 * @description 提供 Tiptap 无序列表切换命令。
 * @logic 兼容 Editor 与 Ref<Editor> 后执行 toggleBulletList 并恢复焦点。
 * @dependencies Helper: resolveEditor, Tiptap: Editor
 * @index_tags Tiptap, 无序列表, 工具栏, editor命令
 * @author holic512
 */
import {resolveEditor, type MaybeEditorRef} from "@/views/User/Main/components/Edit/editor/editorContext";

export const toggleBulletList = (editor: MaybeEditorRef) => {
    const instance = resolveEditor(editor);
    instance?.commands.toggleBulletList();
    // 回到焦点
    instance?.commands.focus();
}
