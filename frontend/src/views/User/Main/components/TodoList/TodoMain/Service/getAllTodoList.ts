import axios from "@/axios";

export const getAllTodoList = async () => {
    try {
        const response = await axios.get(
            "user/todo/all"
        );
        console.log("获取全部待办响应:", response);
        return response.data || [];
    } catch (error) {
        console.error("获取全部待办失败:", error);
        return [];
    }
};