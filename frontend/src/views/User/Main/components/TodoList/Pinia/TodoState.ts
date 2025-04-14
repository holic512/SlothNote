// stores/TodoState.ts
import {defineStore} from 'pinia';

interface State {
    // 描述更新状态
    isDescriptionVisible: boolean

    /**
     页面状态 标识符
     全部待办:0 今日待办:1 分类待办:2 未分类:3 已完成:4 已过期:5 回收站:6 自定义视图:7
     */
    state: number;

    /**
     * 用来存储 点击分类的 信息
     */
    AClass: any;

    /**
     * 自定义视图的额外数据
     */
    customViewData: any;
}

export const useTodoState = defineStore('TodoState', {
    state(): State {
        return {
            isDescriptionVisible: true,
            state: 0,
            AClass: null,
            customViewData: null
        }
    },
    actions: {
        // 切换全部待办
        ToAll() {
            // 触发更新操作
            this.description()
            this.state = 0;
        },

        // 切换今日待办
        ToToday() {
            // 触发更新操作
            this.description()
            this.state = 1;
        },

        ToClass(class1: any) {
            // 触发更新操作
            this.description()
            this.clearClassId();
            this.state = 2;
            this.AClass = class1;
        },

        // 未分类待办
        ToUncategorized() {
            this.description()
            this.state = 3;
        },

        // 已完成视图
        ToCompletedView() {
            this.description()
            this.state = 4;
        },

        // 已过期视图
        ToExpiredView() {
            this.description()
            this.state = 5;
        },

        // 回收站视图
        ToRecycleBin() {
            this.description()
            this.state = 6;
        },

        // 自定义视图
        ToCustomView(data: any = null) {
            this.description()
            this.state = 7;
            this.customViewData = data;
        },

        description() {
            this.isDescriptionVisible = !this.isDescriptionVisible;
        },

        clearClassId() {
            this.AClass = null;
        }
    },
});