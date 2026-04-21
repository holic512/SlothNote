import axios from "../../../../../axios";

async function initiateReg(username: string, password: string, email: string) {
    try {
        const response = await axios.post(
            "user/auth/initiateReg",
            {
                username: username,
                password: password,
                email: email,
            }
        );
        return response.data;
    } catch (error) {
        return {
            status: 500,
            message: "请求失败"
        };
    }
}

export {initiateReg};
