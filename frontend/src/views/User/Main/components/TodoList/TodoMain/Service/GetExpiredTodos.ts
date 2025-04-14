import axios from "@/axios";

export const getExpiredTodos = async () => {
    try {
        const response = await axios.get(
            "user/todo/expired"
        );
        console.log("获取已过期待办响应:", response);
        return response.data || [];
    } catch (error) {
        console.error("获取已过期待办失败:", error);
        return [];
    }
};