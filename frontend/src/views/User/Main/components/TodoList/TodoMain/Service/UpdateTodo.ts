import axios from "@/axios";

export const updateTodo = async (todoId: number, todoData: {
    title: string;
    description: string;
    categoryId: number | null;
    dueDate: string | null;
    status?: number;
}) => {
    try {
        const response = await axios.put(
            "user/todo/update",
            {
                title: todoData.title,
                description: todoData.description || '',
                categoryId: todoData.categoryId,
                dueDate: todoData.dueDate || null,
                status: todoData.status
            },
            {
                params: { todoId }
            }
        );
        return response.data.status;
    } catch (error) {
        console.error(error);
        return 500;
    }
};