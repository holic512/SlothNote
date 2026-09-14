/**
 * @file AuthGuard
 * @project SlothNote
 * @module 前端路由 / 鉴权守卫
 * @description 根据目标路由和本地会话令牌决定是否需要跳转登录页。
 * @logic 1. 从路径识别用户端或管理端；2. 仅检查对应令牌是否存在；3. 返回统一登录路径。
 * @dependencies RoutePaths
 * @index_tags 路由守卫, 用户鉴权, 管理员鉴权, 可测试规则
 * @author holic512
 */

import {ROUTE_PATHS} from "@/router/paths";

export type AuthRole = 'user' | 'admin';

export interface AuthTokens {
    userToken?: string;
    adminToken?: string;
}

export interface AuthTarget {
    path: string;
    requiresAuth: boolean;
}

export const getAuthRoleByPath = (path: string): AuthRole | null => {
    if (path.startsWith('/admin')) return 'admin';
    if (path.startsWith('/user')) return 'user';
    return null;
};
export const resolveAuthRedirect = (target: AuthTarget, tokens: AuthTokens): string | null => {
    if (!target.requiresAuth) return null;

    const role = getAuthRoleByPath(target.path);
    if (role === 'admin' && !tokens.adminToken) return ROUTE_PATHS.adminLogin;
    if (role === 'user' && !tokens.userToken) return ROUTE_PATHS.userLogin;
    return null;
};
