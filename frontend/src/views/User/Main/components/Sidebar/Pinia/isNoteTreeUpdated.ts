/**
 * @file NoteTreeUpdateState
 * @project SlothNote
 * @module 用户端 / 笔记树状态
 * @description 发布笔记树全量刷新或单节点补丁，避免所有元数据修改都重新请求整棵树。
 * @logic 1. revision 标识新事件；2. refresh 用于结构变化；3. patch 用于标题、头像和封面等节点属性变化。
 * @dependencies Pinia, Tree interface
 * @index_tags 笔记树更新, 增量补丁, 全量刷新, Pinia
 * @author holic512
 */
import {defineStore} from 'pinia'
import type {Tree} from "@/views/User/Main/components/Sidebar/NoteTree/interface/treeInterface";

export type NoteTreeNodeType = 'NOTE' | 'FOLDER';
export type NoteTreeNodeChanges = Partial<Pick<Tree, 'label' | 'avatar' | 'cover'>>;

export type NoteTreeMutation =
    | {kind: 'refresh'}
    | {kind: 'patch'; nodeType: NoteTreeNodeType; nodeId: number; changes: NoteTreeNodeChanges};

export const useNoteTreeUpdate = defineStore('NoteTreeUpdate', {
    state() {
        return {
            revision: 0,
            mutation: null as NoteTreeMutation | null,
        }
    },

    actions: {
        requestRefresh(): void {
            this.mutation = {kind: 'refresh'};
            this.revision += 1;
        },

        patchNode(nodeType: NoteTreeNodeType, nodeId: number, changes: NoteTreeNodeChanges): void {
            this.mutation = {kind: 'patch', nodeType, nodeId, changes};
            this.revision += 1;
        },

        // 兼容现有结构变更调用点
        UpdatedNoteTree(): void {
            this.requestRefresh();
        },

        UpdatedNoteTreeCompleted(revision?: number) {
            if (revision == null || revision === this.revision) {
                this.mutation = null;
            }
        }
    },
})
