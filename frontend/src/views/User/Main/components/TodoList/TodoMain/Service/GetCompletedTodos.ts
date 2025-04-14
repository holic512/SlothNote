import axios from "@/axios";

export const getCompletedTodos = async () => {
    try {
        const response = await axios.get(
            "user/todo/completed"
        );
        console.log("获取已完成待办响应:", response);
        return response.data || [];
    } catch (error) {
        console.error("获取已完成待办失败:", error);
        return [];
    }
};