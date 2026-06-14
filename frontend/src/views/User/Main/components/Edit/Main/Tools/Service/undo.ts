/**
 * @file undo
 * @project SlothNote
 * @module 用户端 / 编辑器工具命令
 * @description 提供 Tiptap 撤销命令。
 * @logic 兼容 Editor 与 Ref<Editor> 后执行 undo。
 * @dependencies Helper: resolveEditor, Tiptap: Editor
 * @index_tags Tiptap, 撤销, 工具栏, editor命令
 * @author holic512
 */
import {resolveEditor, type MaybeEditorRef} from "@/views/User/Main/components/Edit/editor/editorContext";

export const undo = (editor: MaybeEditorRef) => {
    resolveEditor(editor)?.commands.undo()
}
