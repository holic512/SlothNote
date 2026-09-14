/**
 * @file AdminAuthApi
 * @project SlothNote
 * @module 管理后台 / 认证请求
 * @description 集中调用管理员初始化状态、首次初始化、登录和验证码验证接口。
 * @logic 1. 读取服务端初始化状态；2. 登录或初始化成功时保存管理员令牌；3. 统一将网络异常转换为页面可展示的响应。
 * @dependencies Axios, TokenStore, LogIdStore
 * @index_tags 管理员认证, 初始化状态, 登录请求, 初始化请求
 * @author holic512
 */
import axios from "../../../../axios";
import { logIDStore } from "@/pinia/logIDStore";
import { tokenStore } from "@/pinia/token";

type AuthPayload = {
    token?: string | null;
    requiresVerification?: boolean;
    logId?: string | null;
    hasEmail?: boolean;
    needInit?: boolean;
};

type AuthResponse = {
    status: number;
    message: string;
    data?: AuthPayload;
};

async function getInitializationStatus(): Promise<AuthResponse> {
    try {
        const response = await axios.get("admin/auth/status");
        return {
            status: response.data.status,
            message: response.data.message,
            data: response.data?.data as AuthPayload | undefined,
        };
    } catch (error) {
        return {status: 500, message: "无法检测管理员初始化状态"};
    }
}

async function login(username: string, password: string): Promise<AuthResponse> {
    try {
        const response = await axios.post("admin/auth/login", { username, password });
        const payload = response.data?.data as AuthPayload | undefined;
        if (payload?.logId) {
            logIDStore().setLogID(payload.logId);
        } else {
            logIDStore().clearLogID();
        }
        if (payload?.token) {
            tokenStore().setAdminToken(payload.token);
        }
        return {
            status: response.data.status,
            message: response.data.message,
            data: payload,
        };
    } catch (error) {
        return { status: 500, message: "服务器连接失败" };
    }
}

async function initAdmin(username: string, password: string, email?: string): Promise<AuthResponse> {
    try {
        const response = await axios.post("admin/auth/init", {
            username,
            password,
            email,
        });
        const payload = response.data?.data as AuthPayload | undefined;
        logIDStore().clearLogID();
        if (payload?.token) {
            tokenStore().setAdminToken(payload.token);
        }
        return {
            status: response.data.status,
            message: response.data.message,
            data: payload,
        };
    } catch (error) {
        return { status: 500, message: "服务器连接失败" };
    }
}

async function verCode(code: string): Promise<AuthResponse> {
    try {
        const logID = logIDStore().getLogID();
        const response = await axios.post("admin/auth/verLogin", {
            code,
            logID,
        });
        const payload = response.data?.data as AuthPayload | undefined;
        if (response.data.status === 200 && payload?.token) {
            tokenStore().setAdminToken(payload.token);
            logIDStore().clearLogID();
        }
        return {
            status: response.data.status,
            message: response.data.message,
            data: payload,
        };
    } catch (error) {
        return { status: 500, message: "服务器连接失败" };
    }
}

export { getInitializationStatus, login, initAdmin, verCode };
