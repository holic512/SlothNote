import axios from "@/axios";

export const ReplyComment = async (parentId: number, content: string) => {
    try {
        const response = await axios.post(
            "user/comments/reply",
            {
                "parentId": parentId,
                "content": content
            }
        )
        return response.data.status;
    } catch (error) {
        console.log(error);
    }
}