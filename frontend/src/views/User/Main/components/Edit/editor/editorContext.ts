/**
 * @file editorContext
 * @project SlothNote
 * @module 用户端 / 笔记编辑器
 * @description 提供 Tiptap Editor 实例的上下文注入与兼容式解析，减少跨层级 v-model 传递。
 * @logic 1. 在编辑页根组件 provide editor ref；2. 子组件优先 inject editor ref；3. 兼容旧组件传入的 Editor 或 Ref<Editor>。
 * @dependencies Tiptap: Editor, Vue: provide/inject/ref
 * @index_tags 笔记编辑器, Tiptap, provide-inject, editor上下文, v-model兼容
 * @author holic512
 */
import type {Editor} from "@tiptap/vue-3";
import {inject, provide, unref} from "vue";
import type {InjectionKey, Ref, ShallowRef} from "vue";

export type NoteEditorRef = Ref<Editor | undefined> | ShallowRef<Editor | undefined>;
export type MaybeEditorRef = Editor | NoteEditorRef | null | undefined;

interface NoteEditorContext {
    editor: NoteEditorRef;
}

const noteEditorContextKey: InjectionKey<NoteEditorContext> = Symbol("noteEditorContext");

export const provideNoteEditorContext = (editor: NoteEditorRef) => {
    provide(noteEditorContextKey, {editor});
};

export const useNoteEditorContext = (fallback?: NoteEditorRef): NoteEditorContext => {
    const context = inject(noteEditorContextKey, null);

    if (context) {
        return context;
    }

    if (fallback) {
        return {editor: fallback};
    }

    return {
        editor: {value: undefined} as NoteEditorRef,
    };
};

export const resolveEditor = (editor: MaybeEditorRef): Editor | undefined => {
    if (!editor) {
        return undefined;
    }

    return "value" in editor ? unref(editor) : editor;
};
