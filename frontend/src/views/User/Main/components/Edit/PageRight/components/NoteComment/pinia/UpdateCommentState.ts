/**
 * @file CommentUpdateState
 * @project SlothNote
 * @module 用户端 / 评论状态
 * @description 发布评论列表刷新事件，确保连续新增或回复不会被布尔状态合并丢失。
 * @logic 每次评论变更递增 revision，评论列表监听版本号并按当前笔记重新加载。
 * @dependencies Pinia
 * @index_tags 评论刷新, revision, Pinia, 并发状态
 * @author holic512
 */
import {defineStore} from 'pinia'

export const UseUpdateCommentState = defineStore('UpdateCommentState', {
    state() {
        return {
            revision: 0,
        }
    },

    actions: {
        needUpdate(): void {
            this.revision += 1
        }
    },

})
