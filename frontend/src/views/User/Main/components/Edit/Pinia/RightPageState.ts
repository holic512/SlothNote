// 用与区分 右边侧边栏 主要为 打开 和控制显示哪一部分
import {defineStore} from "pinia";

// 用于控制右边栏,显示什么
export enum RightPageModeEnum {
    null,
    comment,
    Ai,

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
        }

    },
})