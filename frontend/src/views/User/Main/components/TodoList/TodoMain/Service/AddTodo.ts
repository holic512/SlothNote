import axios from "@/axios";

export const addTodo = async (todoData: {
    title: string;
    description: string;
    categoryId: number | null;
    dueDate: string | null;
}) => {
    try {
        const response = await axios.post(
            "user/todo/add",
            {
                title: todoData.title,
                description: todoData.description || '',
                categoryId: todoData.categoryId || 0,
                dueDate: todoData.dueDate || null
            }
        );
        return response.data.status;
    } catch (error) {
        console.error(error);
        return 500;
    }
};