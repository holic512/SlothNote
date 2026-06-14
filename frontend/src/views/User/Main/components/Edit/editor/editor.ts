/**
 * @file editor
 * @project SlothNote
 * @module 用户端 / 笔记编辑器
 * @description 创建并配置用户笔记页的 Tiptap Editor 实例。
 * @logic 1. 注册基础文本、图片、表格、目录、拖拽与任务列表扩展；2. 将内容更新同步到保存状态；3. 维护目录与选中文本状态。
 * @dependencies Tiptap: useEditor/extensions, Store: SaveNoteState/IndexItems/AiChat, Service: SaveNote
 * @index_tags Tiptap, 编辑器配置, 粘贴处理, 自动保存状态, 目录
 * @author holic512
 */
import {useEditor} from "@tiptap/vue-3";
import StarterKit from "@tiptap/starter-kit";
import Underline from "@tiptap/extension-underline";
import TextStyle from "@tiptap/extension-text-style";
import Placeholder from "@tiptap/extension-placeholder";
import {Color} from "@tiptap/extension-color";
import Highlight from "@tiptap/extension-highlight";
import Image from "@tiptap/extension-image";
import FileHandler from "@tiptap-pro/extension-file-handler";
import fileHandlerConfig from "./fileHandlerConfig";
import NodeRange from "@tiptap-pro/extension-node-range";
import DragHandle from "@tiptap-pro/extension-drag-handle";
import {useSaveNoteState} from "../Pinia/SaveNoteState";
import {TaskItem} from "@tiptap/extension-task-item";
import {TaskList} from "@tiptap/extension-task-list";
import {Table} from "@tiptap/extension-table";
import {TableRow} from "@tiptap/extension-table-row";
import {TableHeader} from "@tiptap/extension-table-header";
import {TableCell} from "@tiptap/extension-table-cell";
import {getHierarchicalIndexes, TableOfContents} from "@tiptap-pro/extension-table-of-contents";
import {useIndexItemsStore} from "@/views/User/Main/components/Edit/Pinia/IndexItems";
import {onBeforeUnmount, onMounted} from "vue";
import {SaveNote} from "@/views/User/Main/components/Edit/service/SaveNote";
import {useAiChatStore} from "@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat";
import {NoteReference} from "@/views/User/Main/components/Edit/editor/noteReferenceExtension";


export function createEditorInstance() {

    const editorSaveState = useSaveNoteState();

    // 存储目录
    const IndexItemsStore = useIndexItemsStore();

    // AI聊天存储
    const aiChat = useAiChatStore();


    const editor = useEditor({
        extensions: [

            StarterKit,

            // 启用下划线扩展
            Underline,

            // 文本样式
            TextStyle,

            Placeholder.configure({
                placeholder: '创造你的无限可能',
            }),

            // 颜色
            Color,

            // 高亮
            Highlight.configure({
                multicolor: true,
            }),

            // 目录
            TableOfContents.configure({
                getIndex: getHierarchicalIndexes,
                onUpdate(content) {

                    // 在这里更新目录项
                    IndexItemsStore.IndexItems = content;
                },
            }),

            // 图片
            Image,

            // 推拽 粘贴
            FileHandler.configure(fileHandlerConfig),

            // node
            NodeRange.configure({
                // allow to select only on depth 0
                // depth: 0,
                key: null,
            }),

            // 拖拽区块
            DragHandle.configure({
                render() {
                    const element = document.createElement('div')

                    element.classList.add('custom-drag-handle')

                    return element
                },
            }),

            // 任务清单
            TaskList,
            TaskItem.configure({
                nested: true,
            }),

            // 表格
            Table.configure({
                resizable: true,
            }),
            TableRow,
            TableHeader,
            TableCell,
            NoteReference,


        ],

        content: "",


        editorProps: {
            transformPastedText(text) {
                return text.replace(/<\/?[^>]+(>|$)/g, "");
            },
        },


// 监听 editorContent 的变化
        onUpdate: ({}) => {
            // 更新内容状态，触发保存或其他处理
            editorSaveState.updateContent()
        },
        
        // 监听选择变化事件
        onSelectionUpdate: ({editor}) => {
            const {from, to, empty} = editor.state.selection;
            
            // 如果有选中的文本（非空选区）
            if (!empty) {
                // 获取选中的文本内容
                const selectedText = editor.state.doc.textBetween(from, to, ' ');
                // 更新选中的文本到AI聊天存储
                aiChat.setSelectedText(selectedText);
                return;
            }

            aiChat.setSelectedText('');
        }
    })

    // 监听键盘事件
    const handleKeyDown = (event: any) => {
        if ((event.ctrlKey || event.metaKey) && event.key === 's') {
            event.preventDefault();  // 阻止默认保存行为

            // 检测笔记状态
            const SaveNoteState = useSaveNoteState();
            if (SaveNoteState.isSaved == true) return
            // 调用保存
            void SaveNote(editor)
        }
    };

    // 添加事件监听器
    onMounted(() => {
        document.addEventListener('keydown', handleKeyDown);
    });

    // 移除事件监听器
    onBeforeUnmount(() => {
        document.removeEventListener('keydown', handleKeyDown);
    });


    return editor
}
