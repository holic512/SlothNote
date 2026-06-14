/**
 * @file RightPageState
 * @project SlothNote
 * @module 用户端 / 笔记右侧栏状态
 * @description 控制编辑页右侧栏当前显示评论、AI 或知识星图。
 * @logic 1. model 保存当前右侧栏模式；2. 同一模式再次点击则关闭；3. 提供评论、AI、知识星图切换动作。
 * @dependencies Pinia: defineStore
 * @index_tags 右侧栏, 评论, AI, 知识星图, Pinia
 * @author holic512
 */
import {defineStore} from "pinia";

// 用于控制右边栏,显示什么
export enum RightPageModeEnum {
    null,
    comment,
    Ai,
    KnowledgeGraph,

}

export const useRightPageState = defineStore('RightPageState', {
    state: () => ({
        model: RightPageModeEnum.null as RightPageModeEnum
    }),
    actions: {

        // 关闭右侧边栏
        close() {
            this.model = RightPageModeEnum.null
        },

        // 切换评论
        toComment() {
            if (this.model === RightPageModeEnum.comment) {
                this.model = RightPageModeEnum.null
            } else {
                this.model = RightPageModeEnum.comment
            }
        },
        OpenAi() {
            this.model = RightPageModeEnum.Ai;
        },
        SwitchAi() {
            if (this.model === RightPageModeEnum.Ai) {
                this.model = RightPageModeEnum.null
            } else {
                this.model = RightPageModeEnum.Ai
            }
        },
        SwitchKnowledgeGraph() {
            if (this.model === RightPageModeEnum.KnowledgeGraph) {
                this.model = RightPageModeEnum.null
            } else {
                this.model = RightPageModeEnum.KnowledgeGraph
            }
        }

    },
})
