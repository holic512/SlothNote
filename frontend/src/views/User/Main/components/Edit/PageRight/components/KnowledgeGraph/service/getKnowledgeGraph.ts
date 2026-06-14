/**
 * @file getKnowledgeGraph
 * @project SlothNote
 * @module 用户端 / 笔记知识星图
 * @description 获取当前用户笔记知识星图节点与引用边。
 * @logic 调用 /user/note/graph，可传当前 noteId 用于高亮焦点节点。
 * @dependencies API: /user/note/graph, Axios: @/axios
 * @index_tags 知识星图, 笔记图谱, 引用关系, graph接口
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

export const getKnowledgeGraph = async (noteId?: number | null): Promise<KnowledgeGraphData> => {
    const response = await axios.get("user/note/graph", {
        params: noteId ? {noteId} : {},
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
