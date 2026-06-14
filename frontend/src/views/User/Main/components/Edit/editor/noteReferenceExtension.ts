/**
 * @file noteReferenceExtension
 * @project SlothNote
 * @module 用户端 / 笔记编辑器
 * @description 定义 Tiptap 笔记引用节点，用于在正文中引用并跳转到其他笔记。
 * @logic 1. 作为 inline atom 节点保存 noteId/title；2. 渲染 data-note-reference-id 供点击代理识别；3. HTML 解析时还原属性。
 * @dependencies Tiptap: Node/mergeAttributes
 * @index_tags Tiptap, 笔记引用, noteReference, 正文跳转
 * @author holic512
 */
import {mergeAttributes, Node} from "@tiptap/core";

export const NoteReference = Node.create({
    name: "noteReference",
    group: "inline",
    inline: true,
    atom: true,

    addAttributes() {
        return {
            noteId: {
                default: null,
                parseHTML: element => Number(element.getAttribute("data-note-reference-id")),
                renderHTML: attributes => ({
                    "data-note-reference-id": attributes.noteId,
                }),
            },
            title: {
                default: null,
                parseHTML: element => element.getAttribute("data-note-reference-title"),
                renderHTML: attributes => ({
                    "data-note-reference-title": attributes.title,
                }),
            },
        };
    },

    parseHTML() {
        return [{tag: "span[data-note-reference-id]"}];
    },

    renderHTML({node, HTMLAttributes}) {
        const title = node.attrs.title || "未命名笔记";

        return [
            "span",
            mergeAttributes(HTMLAttributes, {
                "data-note-reference": "true",
                class: "note-reference-chip",
                title: `跳转到 ${title}`,
            }),
            `↗ ${title}`,
        ];
    },
});
