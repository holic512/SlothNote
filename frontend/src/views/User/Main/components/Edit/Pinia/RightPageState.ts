// 用与区分 右边侧边栏 主要为 打开 和控制显示哪一部分
import {defineStore} from "pinia";

// 用于控制右边栏,显示什么
export enum RightPageModeEnum {
    null,
    comment,

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
            if (this.model == RightPageModeEnum.null) {
                this.model = RightPageModeEnum.comment
            } else {
                this.model = RightPageModeEnum.null
            }
        }
    },
})