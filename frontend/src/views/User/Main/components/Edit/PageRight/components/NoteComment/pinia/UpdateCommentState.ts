// 用于保存 评论更新状态的 pinia
import {defineStore} from 'pinia'

export const UseUpdateCommentState = defineStore('UpdateCommentState', {
    state() {
        return {
            // 是否 需要更新 评论
            isNeedUpdate: false as boolean,
        }
    },

    actions: {
        // 需要更新评论
        needUpdate(): void {
            this.isNeedUpdate = true
        },

        // 已经更新评论
        completeUpdate(): void {
            this.isNeedUpdate = false
        }
    },

})
