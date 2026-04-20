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

export { login, initAdmin, verCode };
