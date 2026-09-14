/**
 * @file getKnowledgeGraph
 * @project SlothNote
 * @module 用户端 / 笔记知识星图
 * @description 获取当前用户笔记知识星图节点与引用边，并允许调用方取消过期请求。
 * @logic 调用 /user/note/graph，传递当前 noteId 与 AbortSignal，用于高亮焦点节点和隔离快速切换竞态。
 * @dependencies API: /user/note/graph, Axios: @/axios
 * @index_tags 知识星图, 笔记图谱, 引用关系, AbortSignal, graph接口
 * @author holic512
 */
import axios from "@/axios";

export interface KnowledgeGraphNode {
    noteId: number;
    title: string;
    summary?: string;
    avatar?: string;
    folderId?: number | null;
    degree: number;
    current: boolean;
}

export interface KnowledgeGraphEdge {
    sourceNoteId: number;
    targetNoteId: number;
    labelSnapshot?: string;
}

export interface KnowledgeGraphData {
    currentNoteId?: number | null;
    nodes: KnowledgeGraphNode[];
    edges: KnowledgeGraphEdge[];
}

export const getKnowledgeGraph = async (
    noteId?: number | null,
    signal?: AbortSignal,
): Promise<KnowledgeGraphData> => {
    const response = await axios.get("user/note/graph", {
        params: noteId ? {noteId} : {},
        signal,
    });

    if (response.data?.status === 200 && response.data.data) {
        return response.data.data;
    }

    return {
        currentNoteId: noteId,
        nodes: [],
        edges: [],
    };
};
