import axios from "../../../../../../axios";

const putPassword = async (oldPassword: string, newPassword: string): Promise<number> => {
    try {
        const response = await axios.put(
            "user/settings/account/password",
            { oldPassword, newPassword }
        );
        return response.data.status;
    } catch (err) {
        return 500;
    }
}

export { putPassword }