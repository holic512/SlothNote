/**
 * @file SaveNoteState
 * @project SlothNote
 * @module 用户端 / 笔记保存状态
 * @description 维护当前编辑器内容是否已保存的全局状态。
 * @logic 1. 编辑器内容更新时标记未保存；2. 保存成功或内容重载后标记已保存。
 * @dependencies Pinia: defineStore
 * @index_tags 笔记保存状态, Pinia, isSaved, 编辑器状态
 * @author holic512
 */
import {defineStore} from 'pinia'

export const useSaveNoteState = defineStore('SaveNoteState', {
    // Define state variables
    state() {
        return {
            isSaved: true,
            // Whether content is saved
        }
    },

    // Define actions (methods)
    actions: {

        // 未保存
        updateContent(): void {
            this.isSaved = false
        },

        // 已保存
        saveContent(): void {
            if (!this.isSaved) {
                this.isSaved = true
            }
        }
    },

})
