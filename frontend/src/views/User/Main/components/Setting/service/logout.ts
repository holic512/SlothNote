import axios from "@/axios";

export const logout = async (): Promise<number> => {
    try {
        const response = await axios.post("user/auth/logout");
        return response.data.status;
    } catch (error) {
        return 500;
    }
}
