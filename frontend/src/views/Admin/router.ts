// AdminRouter


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
                    }
                ]
            },
            // 主内容页面
            {
                path: 'main',
                name: 'admin-main',
                component: () => import("./Main/index.vue"),
                children: [
                    {
                        path: '',
                        name: 'admin-main-home',
                        component: () => import('./Main/view/DashboardMm/DashboardMm.vue'),
                    },
                {
                    path: 'dashboardMm',
                    name: 'admin-main-dashboardMm',
                    component: () => import('./Main/view/DashboardMm/DashboardMm.vue'),
                },
                {
                    path: 'home',
                    name: 'admin-main-home1',
                    component: () => import('./Main/view/DashboardMm/DashboardMm.vue'),
                },
                    {
                        path: 'userMm',
                        name: 'admin-main-userMm',
                        component: () => import('./Main/view/userMm/UserMm.vue'),
                    },
                {
                    path: 'commentMm',
                    name: 'admin-main-commentMm',
                    component: () => import('./Main/view/CommentMm/CommentMm.vue'),
                },
                {
                    path: 'noteMm',
                    name: 'admin-main-noteMm',
                    component: () => import('./Main/view/NoteMm/NoteMm.vue'),
                },
                {
                    path: 'folderMm',
                    name: 'admin-main-folderMm',
                    component: () => import('./Main/view/FolderMm/FolderMm.vue'),
                },
                {
                    path: 'todoMm',
                    name: 'admin-main-todoMm',
                    component: () => import('./Main/view/TodoMm/TodoMm.vue'),
                },
                {
                    path: 'setting',
                    name: 'admin-main-setting',
                    component: () => import('./Main/view/Setting/Setting.vue'),
                },
                {
                    path: 'favoriteMm',
                    name: 'admin-main-favoriteMm',
                    component: () => import('./Main/view/FavoriteMm/FavoriteMm.vue'),
                },
                ],
            },
        ]
    }
];