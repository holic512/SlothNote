import axios from "../../../../../axios";
import {tokenStore} from "@/pinia/token";

async function pwLogin(username: string, password: string): Promise<number> {
    try {
        const response = await axios.post(
            "user/auth/PLogin",
            {
                username: username,
                password: password,
            }
        );
        const status = response.data.status;
        if (status === 200) {
            const {resetUserSessionState} = await import("@/session/userSession");
            resetUserSessionState();
            tokenStore().setUserToken(response.data.data);
            return status;
        }

        return status; // 登录成功

    } catch (error) {
        return 500;
    }
}

export default pwLogin;
