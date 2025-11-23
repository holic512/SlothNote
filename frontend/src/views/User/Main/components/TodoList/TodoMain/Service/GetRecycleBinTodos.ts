import axios from "@/axios";

// 获取回收站（逻辑删除）待办事项
export const getRecycleBinTodos = async () => {
    try {
        const response = await axios.get(
            "user/todo/recycleBin"
        );
        return response.data || [];
    } catch (error) {
        console.error("获取回收站待办失败:", error);
        return [];
    }
}