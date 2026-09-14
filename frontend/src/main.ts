/**
 * @file AppMainEntry
 * @project SlothNote
 * @module 前端应用 / 启动入口
 * @description 初始化 Vue 应用、路由、状态管理与迁移期兼容组件。
 * @logic 1. 异步注册旧组件兼容层与迁移期提示指令；2. 挂载 Pinia、Router 和右键菜单插件；3. Element Plus 组件、图标和样式由 Vite 按需导入。
 * @dependencies Vue: createApp, Pinia, VueRouter
 * @index_tags main.ts, Vue启动, ElementPlus按需导入, 兼容组件
 * @author holic512
 */
import {createApp, defineAsyncComponent} from 'vue'
import './styles/index.css'
import App from './App.vue'
import router from './router/index.js'
import {createPinia} from "pinia";
import piniaPluginPersistedstate from "pinia-plugin-persistedstate";
import {configureHttpErrorHandlers} from './axios'
import {ROUTE_PATHS} from './router/paths'
import {tokenStore} from './pinia/token'

const app = createApp(App)

type TooltipBindingValue = string | { value?: string } | null | undefined

const resolveTooltipText = (value: TooltipBindingValue): string => {
    if (typeof value === 'string') return value
    if (value && typeof value.value === 'string') return value.value
    return ''
}

// 兼容旧页面的 v-tooltip 写法：旧 PrimeVue 指令已移除，保留原有提示文案并落到原生可访问提示。
const syncNativeTooltip = (element: HTMLElement, value: TooltipBindingValue) => {
    const text = resolveTooltipText(value)
    if (text) {
        element.setAttribute('title', text)
        if (!element.getAttribute('aria-label')) element.setAttribute('aria-label', text)
        return
    }

    element.removeAttribute('title')
}

app.directive('tooltip', {
    mounted(element: HTMLElement, binding: { value: TooltipBindingValue }) {
        syncNativeTooltip(element, binding.value)
    },
    updated(element: HTMLElement, binding: { value: TooltipBindingValue }) {
        syncNativeTooltip(element, binding.value)
    },
})

// 配置pinia
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 旧组件迁移期兼容组件按首次使用加载，避免进入任意页面都下载整套表格/弹窗实现。
app.component('Button', defineAsyncComponent(() => import('./components/element-compat/CompatButton.vue')))
app.component('Column', defineAsyncComponent(() => import('./components/element-compat/CompatColumn.vue')))
app.component('DataTable', defineAsyncComponent(() => import('./components/element-compat/CompatDataTable.vue')))
app.component('Dialog', defineAsyncComponent(() => import('./components/element-compat/CompatDialog.vue')))
app.component('IconField', defineAsyncComponent(() => import('./components/element-compat/CompatIconField.vue')))
app.component('InputIcon', defineAsyncComponent(() => import('./components/element-compat/CompatInputIcon.vue')))
app.component('InputText', defineAsyncComponent(() => import('./components/element-compat/CompatInputText.vue')))
app.component('Tag', defineAsyncComponent(() => import('./components/element-compat/CompatTag.vue')))

app.use(pinia)
app.use(router)

configureHttpErrorHandlers({
    onUnauthorized: async (role) => {
        const targetPath = role === 'admin' ? ROUTE_PATHS.adminLogin : ROUTE_PATHS.userLogin

        if (role === 'admin') {
            tokenStore().clearAdminToken()
        } else {
            const {resetUserSessionState} = await import('./session/userSession')
            resetUserSessionState()
        }

        if (router.currentRoute.value.path !== targetPath) {
            await router.replace({
                path: targetPath,
                query: {redirect: router.currentRoute.value.fullPath},
            })
        }
    },
    onForbidden: async () => {
        if (router.currentRoute.value.path !== ROUTE_PATHS.permissionDenied) {
            await router.replace(ROUTE_PATHS.permissionDenied)
        }
    },
})

app.mount('#app')
