/**
 * @file HttpClient
 * @project SlothNote
 * @module 前端基础设施 / HTTP
 * @description 提供统一 Axios 实例、按接口命名空间注入令牌，并向应用层报告 401/403。
 * @logic 1. 从环境变量解析 API 地址；2. 仅为相对地址或同 API 基址请求按 user/admin 前缀注入令牌；3. 通过可配置回调统一处理 Axios 与流式 fetch 的权限错误。
 * @dependencies Axios, TokenStore
 * @index_tags Axios, 请求拦截器, 响应拦截器, 令牌防泄漏, 环境变量, 循环依赖治理
 * @author holic512
 */

import axios, {type AxiosError} from 'axios';
import {tokenStore} from "@/pinia/token";

export type HttpAuthRole = 'user' | 'admin';

interface HttpErrorHandlers {
    onUnauthorized?: (role: HttpAuthRole) => void | Promise<void>;
    onForbidden?: () => void | Promise<void>;
}

const normalizeBaseURL = (value: string): string => value.endsWith('/') ? value : `${value}/`;
const configuredBaseURL = import.meta.env.VITE_API_BASE_URL?.trim();

export const API_BASE_URL = normalizeBaseURL(
    configuredBaseURL || (import.meta.env.DEV ? 'http://localhost:8080/' : '/'),
);

let errorHandlers: HttpErrorHandlers = {};
const unauthorizedTasks: Record<HttpAuthRole, Promise<void> | null> = {
    user: null,
    admin: null,
};

export const configureHttpErrorHandlers = (handlers: HttpErrorHandlers): void => {
    errorHandlers = handlers;
};

const getRequestPath = (url?: string): string | null => {
    const requestUrl = url?.trim();
    if (!requestUrl) return null;

    if (!/^https?:\/\//i.test(requestUrl)) {
        return requestUrl.replace(/^\/+/, '');
    }

    if (!/^https?:\/\//i.test(API_BASE_URL)) return null;

    try {
        const request = new URL(requestUrl);
        const apiBase = new URL(API_BASE_URL);
        const apiBasePath = apiBase.pathname.endsWith('/') ? apiBase.pathname : `${apiBase.pathname}/`;

        if (request.origin !== apiBase.origin || !request.pathname.startsWith(apiBasePath)) return null;
        return request.pathname.slice(apiBasePath.length).replace(/^\/+/, '');
    } catch {
        return null;
    }
};

export const getRequestAuthRole = (url?: string): HttpAuthRole | null => {
    const normalizedPath = getRequestPath(url);
    if (!normalizedPath) return null;

    if (normalizedPath.startsWith('admin/')) return 'admin';
    if (normalizedPath.startsWith('user/')) return 'user';
    return null;
};

const isPublicAuthRequest = (url?: string): boolean => {
    const normalizedPath = getRequestPath(url);
    return Boolean(normalizedPath?.includes('/auth/') && !normalizedPath.endsWith('/logout'));
};

const runUnauthorizedHandler = (role: HttpAuthRole): void => {
    if (unauthorizedTasks[role]) return;

    const fallback = () => {
        const tokens = tokenStore();
        if (role === 'admin') tokens.clearAdminToken();
        else tokens.clearUserToken();
    };

    unauthorizedTasks[role] = Promise.resolve(errorHandlers.onUnauthorized?.(role) ?? fallback())
        .finally(() => {
            unauthorizedTasks[role] = null;
        });
};

export const handleHttpAuthStatus = (status?: number, requestUrl?: string): void => {
    if (status === 401 && !isPublicAuthRequest(requestUrl)) {
        const role = getRequestAuthRole(requestUrl);
        if (role) runUnauthorizedHandler(role);
        return;
    }

    if (status === 403) {
        void errorHandlers.onForbidden?.();
    }
};

// 创建一个 Axios 实例
const instance = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {"Content-Type": "application/json"}
});

// 创建请求拦截器
instance.interceptors.request.use(config => {
    if (config.headers?.satoken || isPublicAuthRequest(config.url)) {
        return config;
    }

    const role = getRequestAuthRole(config.url);
    const tokens = tokenStore();
    const token = role === 'admin' ? tokens.getAdminToken() : role === 'user' ? tokens.getUserToken() : undefined;
    if (token) config.headers.satoken = token;

    return config;
}, error => {
    return Promise.reject(error);
});

// 响应拦截器
instance.interceptors.response.use(response => response, (error: AxiosError) => {
    const status = error.response?.status;
    const requestUrl = error.config?.url;

    handleHttpAuthStatus(status, requestUrl);

    return Promise.reject(error);
});

export default instance;
