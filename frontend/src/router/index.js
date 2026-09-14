/**
 * @file AppRouter
 * @project SlothNote
 * @module 前端路由 / 总路由
 * @description 聚合首页、用户端和管理端路由，并执行轻量本地会话守卫。
 * @logic 1. 聚合模块路由；2. 受保护页面仅校验对应本地令牌；3. 失效令牌由 HTTP 401 统一处理。
 * @dependencies VueRouter, TokenStore, AuthGuard
 * @index_tags 总路由, 本地鉴权, 登录重定向
 * @author holic512
 */

// @ts-check
import {createRouter, createWebHistory} from 'vue-router';
import homeRoutes from '../views/Home/router'
import adminRoutes from '../views/Admin/router'
import userRouters from '../views/User/router'
import {tokenStore} from "../pinia/token";
import {ElMessage} from "element-plus";
import {resolveAuthRedirect} from "./authGuard";


const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        ...homeRoutes,
        ...userRouters,
        ...adminRoutes,

    ]
});

router.beforeEach((to) => {
    const tokens = tokenStore();
    const redirectPath = resolveAuthRedirect(
        {
            path: to.path,
            requiresAuth: Boolean(to.meta.requiresAuth),
        },
        {
            userToken: tokens.getUserToken(),
            adminToken: tokens.getAdminToken(),
        },
    );

    if (!redirectPath) return true;

    ElMessage.warning('您还未登录或登录状态已过期，请重新登录');
    return {
        path: redirectPath,
        query: {redirect: to.fullPath},
    };
});

export default router;
