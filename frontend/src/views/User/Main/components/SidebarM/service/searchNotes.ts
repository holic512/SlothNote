/**
 * @file SearchNotesService
 * @project SlothNote
 * @module 用户端 / 笔记搜索服务
 * @description 查询当前用户笔记，并允许调用方取消已过时的搜索请求。
 * @logic 1. 传递关键词和 AbortSignal；2. 仅返回成功响应数据；3. 失败或取消时返回空结果。
 * @dependencies Axios: frontend/src/axios, API: user/note/search
 * @index_tags 笔记搜索, AbortSignal, 请求取消
 * @author holic512
 */
import axios from '@/axios';

export const searchNotes = async (q: string, signal?: AbortSignal) => {
  try {
    const resp = await axios.get('user/note/search', {params: {q}, signal});
    if (resp.data?.status === 200) return resp.data.data;
    return [];
  } catch {
    return [];
  }
}
