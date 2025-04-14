import axios from "@/axios";

export const getTodosForWeek = async () => {
    try {
        const response = await axios.get(
            "user/todo/week"
        );
        console.log("获取七日待办响应:", response);
        return response.data || [];
    } catch (error) {
        console.error("获取七日待办失败:", error);
        return [];
    }
};