/**
 * @file strike
 * @project SlothNote
 * @module 用户端 / 编辑器工具命令
 * @description 提供 Tiptap 删除线命令与删除线状态读取。
 * @logic 1. 兼容 Editor 与 Ref<Editor>；2. 执行 toggleStrike；3. 返回当前 strike 激活状态。
 * @dependencies Helper: resolveEditor, Tiptap: Editor
 * @index_tags Tiptap, 删除线, 工具栏, 浮动菜单, editor命令
 * @author holic512
 */
import {resolveEditor, type MaybeEditorRef} from "@/views/User/Main/components/Edit/editor/editorContext";

export const strike = (editor: MaybeEditorRef) => {
    resolveEditor(editor)?.chain().focus().toggleStrike().run();
}

//  检查删除线状态
export const isStrikeActive = (editor: MaybeEditorRef): boolean => {
    const instance = resolveEditor(editor);
    if (!instance) {
        return false; // 如果 editor 未定义，则返回 false
    }
    return instance.isActive('strike');
};
