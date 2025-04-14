import axios from "@/axios";

export const getTodosByDate = async (date: string) => {
    try {
        const response = await axios.get(
            "user/todo/byDate",
            {
                params: { date }
            }
        );
        return response.data;
    } catch (error) {
        console.error(error);
        return [];
    }
};