import axios from "@/axios";

export const deleteTodo = async (todoId: number) => {
    try {
        const response = await axios.delete(
            "user/todo/delete",
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