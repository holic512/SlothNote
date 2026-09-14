/**
 * @file AdminRouter
 * @project SlothNote
 * @module 管理后台 / 路由配置
 * @description 定义管理端认证页、首次初始化入口、主框架与各管理模块的路由映射。
 * @logic 1. 提供独立的管理员初始化路径；2. /admin/main 作为无名布局路由并重定向到仪表盘；3. 管理页按模块懒加载并保留历史路径重定向。
 * @dependencies VueRouter: route records, Component: Admin/Main/index.vue
 * @index_tags 后台路由, 管理端菜单, 懒加载, 仪表盘路由
 * @author holic512
 */

const adminDashboardMeta = {
    title: '仪表盘',
    subtitle: '查看系统核心数据、近期内容和管理概览'
};


export default [
    {
        path: '/admin',
        component: () => import("./index.vue"),
        meta: {requiresAuth: true},
        children: [
            // 默认页面
            {
                path: '',
                name: 'admin-home',
                meta: {requiresAuth: false},
                component: () => import('./Auth/components/login.vue'),
            },
            // 授权页面
            {
                path: 'auth',
                name: 'admin-auth',
                meta: {requiresAuth: false},
                component: () => import('./Auth/index.vue'),
                children: [
                    {
                        path: '',
                        name: 'admin-auth-home',
                        component: () => import('./Auth/components/login.vue'),
                    },
                    {
                        path: 'login',
                        name: 'admin-auth-login',
                        component: () => import('./Auth/components/login.vue'),
                    },
                    {
                        path: 'init',
                        name: 'admin-auth-init',
                        component: () => import('./Auth/components/login.vue'),
                    }
                ]
            },
            // 主内容页面
            {
                path: 'main',
                component: () => import("./Main/index.vue"),
                children: [
                    {
                        path: '',
                        name: 'admin-main-redirect',
                        redirect: {name: 'admin-main-home'},
                    },
                {
                    path: 'dashboardMm',
                    redirect: {name: 'admin-main-home'},
                },
                {
                    path: 'home',
                    name: 'admin-main-home',
                    meta: adminDashboardMeta,
                    component: () => import('./Main/view/DashboardMm/DashboardMm.vue'),
                },
                    {
                        path: 'userMm',
                        name: 'admin-main-userMm',
                        meta: {
                            title: '用户管理',
                            subtitle: '管理平台全部用户、状态和在线会话'
                        },
                        component: () => import('./Main/view/userMm/UserMm.vue'),
                    },
                {
                    path: 'commentMm',
                    name: 'admin-main-commentMm',
                    meta: {
                        title: '评论管理',
                        subtitle: '管理笔记评论，支持搜索、筛选和批量操作'
                    },
                    component: () => import('./Main/view/CommentMm/CommentMm.vue'),
                },
                {
                    path: 'noteMm',
                    name: 'admin-main-noteMm',
                    meta: {
                        title: '笔记管理',
                        subtitle: '统一管理笔记基础信息与内容预览'
                    },
                    component: () => import('./Main/view/NoteMm/NoteMm.vue'),
                },
                {
                    path: 'folderMm',
                    name: 'admin-main-folderMm',
                    meta: {
                        title: '文件夹管理',
                        subtitle: '统一管理系统文件夹和层级数据'
                    },
                    component: () => import('./Main/view/FolderMm/FolderMm.vue'),
                },
                {
                    path: 'todoMm',
                    name: 'admin-main-todoMm',
                    meta: {
                        title: '待办管理',
                        subtitle: '统一管理待办分类与待办记录'
                    },
                    component: () => import('./Main/view/TodoMm/TodoMm.vue'),
                },
                {
                    path: 'setting',
                    name: 'admin-main-setting',
                    meta: {
                        title: '系统设置',
                        subtitle: '维护管理员资料并执行系统级初始化操作'
                    },
                    component: () => import('./Main/view/Setting/Setting.vue'),
                },
                {
                    path: 'favoriteMm',
                    name: 'admin-main-favoriteMm',
                    meta: {
                        title: '收藏管理',
                        subtitle: '统一管理收藏文件夹与收藏记录'
                    },
                    component: () => import('./Main/view/FavoriteMm/FavoriteMm.vue'),
                },
                {
                    path: 'aiMm',
                    name: 'admin-main-aiMm',
                    meta: {
                        title: 'AI 记录',
                        subtitle: '统一管理用户 AI 会话、上下文笔记和消息记录'
                    },
                    component: () => import('./Main/view/AiMm/AiMm.vue'),
                },
                ],
            },
        ]
    }
];
