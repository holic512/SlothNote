/**
 * @file redo
 * @project SlothNote
 * @module 用户端 / 编辑器工具命令
 * @description 提供 Tiptap 重做命令。
 * @logic 兼容 Editor 与 Ref<Editor> 后执行 redo。
 * @dependencies Helper: resolveEditor, Tiptap: Editor
 * @index_tags Tiptap, 重做, 工具栏, editor命令
 * @author holic512
 */
import {resolveEditor, type MaybeEditorRef} from "@/views/User/Main/components/Edit/editor/editorContext";

export const redo = (editor: MaybeEditorRef) => {
    resolveEditor(editor)?.commands.redo()
}
