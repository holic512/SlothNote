/**
 * @file RoutePaths
 * @project SlothNote
 * @module 前端路由 / 公共路径契约
 * @description 集中维护认证、主页面与权限页路径，避免守卫、请求层和页面跳转使用不同字符串。
 * @logic 1. 对外暴露稳定路由常量；2. 认证跳转统一复用同一契约。
 * @dependencies None
 * @index_tags 路由常量, 登录路径, 导航契约
 * @author holic512
 */

export const ROUTE_PATHS = {
    home: '/',
    permissionDenied: '/pd',
    userLogin: '/user/login',
    userRegister: '/user/register',
    userMain: '/user/main',
    userEdit: '/user/main/edit',
    adminLogin: '/admin/auth/login',
    adminMain: '/admin/main',
} as const;
