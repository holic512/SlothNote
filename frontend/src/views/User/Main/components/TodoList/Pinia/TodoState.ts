/**
 * @file TodoState
 * @project SlothNote
 * @module 用户端 / 待办状态
 * @description 维护当前待办查询视图及刷新版本，替代布尔翻转事件。
 * @logic 1. state 表示当前查询；2. AClass 保存分类条件；3. revision 驱动列表按当前条件重新加载。
 * @dependencies Pinia
 * @index_tags 待办查询, 视图状态, 刷新版本, Pinia
 * @author holic512
 */
import {defineStore} from 'pinia';

export interface TodoClassSelection {
    id: number;
    name: string;
    type?: number;
}

interface State {
    /**
     页面状态 标识符
     全部待办:0 今日待办:1 分类待办:2 未分类:3 已完成:4 已过期:5 回收站:6 自定义视图:7
     */
    state: number;

    /**
     * 用来存储 点击分类的 信息
     */
    AClass: TodoClassSelection | null;

    /**
     * 自定义视图的额外数据
     */
    customViewData: unknown;
    revision: number;
}

export const useTodoState = defineStore('TodoState', {
    state(): State {
        return {
            state: 0,
            AClass: null,
            customViewData: null,
            revision: 0,
        }
    },
    actions: {
        setView(state: number, selectedClass: TodoClassSelection | null = null, customViewData: unknown = null) {
            this.state = state;
            this.AClass = selectedClass;
            this.customViewData = customViewData;
            this.revision += 1;
        },

        refresh() {
            this.revision += 1;
        },

        // 切换全部待办
        ToAll() {
            this.setView(0);
        },

        // 切换今日待办
        ToToday() {
            this.setView(1);
        },

        ToClass(class1: TodoClassSelection) {
            this.setView(2, class1);
        },

        // 未分类待办
        ToUncategorized() {
            this.setView(3);
        },

        // 已完成视图
        ToCompletedView() {
            this.setView(4);
        },

        // 已过期视图
        ToExpiredView() {
            this.setView(5);
        },

        // 回收站视图
        ToRecycleBin() {
            this.setView(6);
        },

        // 自定义视图
        ToCustomView(data: unknown = null) {
            this.setView(7, null, data);
        },

        clearClassId() {
            this.AClass = null;
        }
    },
});
