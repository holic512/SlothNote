import axios from "@/axios";

export const SendComment = async (node_id: number, content: string) => {
    try {
        const response = await axios.post(
            "user/comments/create",
            {
                "noteId": node_id,
                "content": content
            }
        )
        return response.data.status;
    } catch (error) {
        console.log(error);
        return 500;
    }
}