/**
 * @file bold
 * @project SlothNote
 * @module 用户端 / 编辑器工具命令
 * @description 提供 Tiptap 加粗命令与加粗状态读取。
 * @logic 1. 兼容 Editor 与 Ref<Editor>；2. 执行 toggleBold；3. 返回当前 bold 激活状态。
 * @dependencies Helper: resolveEditor, Tiptap: Editor
 * @index_tags Tiptap, 加粗, 工具栏, 浮动菜单, editor命令
 * @author holic512
 */
import {resolveEditor, type MaybeEditorRef} from "@/views/User/Main/components/Edit/editor/editorContext";

export const bold = (editor: MaybeEditorRef) => {
    resolveEditor(editor)?.chain().focus().toggleBold().run()
}

// 检查当前文本是否已加粗
export const isBoldActive = (editor: MaybeEditorRef): boolean => {
    const instance = resolveEditor(editor);
    if (!instance) {
        return false; // 如果 editor 未定义，则返回 false
    }
    return instance.isActive('bold');
}
