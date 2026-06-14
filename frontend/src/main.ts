/**
 * @file AppMainEntry
 * @project SlothNote
 * @module 前端应用 / 启动入口
 * @description 初始化 Vue 应用、路由、状态管理、Element Plus 与全局主题。
 * @logic 1. 注册 Element Plus 与图标；2. 注册 旧组件迁移期 Element Plus 兼容组件；3. 挂载 Pinia、Router 和右键菜单插件。
 * @dependencies Vue: createApp, ElementPlus, Pinia, VueRouter, @imengyu/vue3-context-menu
 * @index_tags main.ts, Vue启动, ElementPlus主题, ElementPlus迁移, 全局组件
 * @author holic512
 */
import {createApp} from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/index.css'
import App from './App.vue'
import router from './router/index.js'
import {createPinia} from "pinia";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import CompatButton from './components/element-compat/CompatButton.vue'
import CompatColumn from './components/element-compat/CompatColumn.vue'
import CompatDataTable from './components/element-compat/CompatDataTable.vue'
import CompatDialog from './components/element-compat/CompatDialog.vue'
import CompatIconField from './components/element-compat/CompatIconField.vue'
import CompatInputIcon from './components/element-compat/CompatInputIcon.vue'
import CompatInputText from './components/element-compat/CompatInputText.vue'
import CompatTag from './components/element-compat/CompatTag.vue'

// context-menu-scss
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import './css/ContextMenu.scss'

const app = createApp(App)

// 配置 图标
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

// 配置pinia
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 旧组件迁移期兼容组件，内部全部使用 Element Plus 渲染。
app.component('Button', CompatButton)
app.component('Column', CompatColumn)
app.component('DataTable', CompatDataTable)
app.component('Dialog', CompatDialog)
app.component('IconField', CompatIconField)
app.component('InputIcon', CompatInputIcon)
app.component('InputText', CompatInputText)
app.component('Tag', CompatTag)

// 配置 vue3-context-menu 右键菜单
import '@imengyu/vue3-context-menu/lib/vue3-context-menu.css'
import ContextMenu from '@imengyu/vue3-context-menu'

app.use(ContextMenu)

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
