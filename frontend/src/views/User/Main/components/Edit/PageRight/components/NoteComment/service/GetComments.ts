// =============== 数据定义 ===============
// 定义回复的接口
import axios from "@/axios";

export interface IReply {
    id: number;
    content: string;
    date: string;
    userId: number;
    username: string;
    avatar: string;
}

// 定义评论的接口
export interface IComment {
    id: number;
    content: string;
    date: string;
    userId: number;
    username: string;
    avatar: string;
    replies?: IReply[];  // 可选的回复数组
}

export const GetComments = async (noteId: number) => {
    try {
        const response = await axios.get(
            "user/comments/comments",
            {
                params: {
                    noteId: noteId
                }
            }
        )
        return response.data.data
    } catch (e) {
        console.log(e)
    }
}