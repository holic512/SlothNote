/**
 * @file GetComments
 * @project SlothNote
 * @module 用户端 / 笔记评论服务
 * @description 获取指定笔记的评论和回复列表，并支持取消过期请求。
 * @logic 传递 noteId 与 AbortSignal，只向调用方返回规范化数组。
 * @dependencies Axios, API: user/comments/comments
 * @index_tags 评论接口, AbortSignal, 请求取消
 * @author holic512
 */
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

export const GetComments = async (noteId: number, signal?: AbortSignal): Promise<IComment[]> => {
    const response = await axios.get(
        "user/comments/comments",
        {
            params: {noteId},
            signal,
        }
    )
    return Array.isArray(response.data?.data) ? response.data.data : []
}
