import axios from "@/axios";

// 获取未分类待办事项
export const getUncategorizedTodos = async () => {
    try {
        const response = await axios.get(
            "user/todo/uncategorized"
        );
        return response.data || [];
    } catch (error) {
        console.error("获取未分类待办失败:", error);
        return [];
    }
}