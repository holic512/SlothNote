import axios from "../../../../../axios";
import {tokenStore} from "@/pinia/token";
import {logIDStore} from "@/pinia/logIDStore";

async function sendMail(email: string): Promise<number> {
    try {
        const response = await axios.post(
            "user/auth/sendLoginCode",
            {
                email: email,
            });
        const status = response.data.status;

        // 请求成功 获取logID 作为登录唯一标识符
        if (status == 200) {
            logIDStore().setLogID(response.data.data)
        }
        return status;

    } catch (error) {
        console.error("Error sending mail:", error);
        return 500;
    }
}

async function verifyLoginCode(code: string): Promise<number> {
    try {
        const response = await axios.post(
            "user/auth/verifyLoginCode",
            {
                logID: logIDStore().getLogID(),
                code: code,
            }
        )
        const status = response.data.status;
        if (status === 200) {
            const {resetUserSessionState} = await import("@/session/userSession");
            resetUserSessionState();
            // 插入token
            tokenStore().setUserToken(response.data.data);

            // 清空 logID
            logIDStore().clearLogID()

            return status;
        } else {
            return status;
        }
    } catch (error) {
        return 500;
    }
}

export {sendMail, verifyLoginCode} ;
