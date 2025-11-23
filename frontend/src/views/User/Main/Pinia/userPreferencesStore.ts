// 控制 用户的个性化设置
import {defineStore} from "pinia";

export const useUserPreferencesStore = defineStore('userPreferencesStore', {
    state: () => ({
        // 左侧面板的显示状态
        LeftPanelVis: true,

        // 编辑器工具栏的显示状态
        editorToolbarVisible: true,

    }),
    actions: {
        // 切换左侧面板的显示状态
        toggleLeftPanel() {
            this.LeftPanelVis = !this.LeftPanelVis;
        },
        // 切换编辑器工具栏显示状态
        toggleEditorToolbar() {
            this.editorToolbarVisible = !this.editorToolbarVisible;
        }
    },

    persist: true,
});