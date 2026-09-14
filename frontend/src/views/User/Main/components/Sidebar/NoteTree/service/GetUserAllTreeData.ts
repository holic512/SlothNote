/**
 * @file GetUserAllTreeData
 * @project SlothNote
 * @module 用户端 / 笔记树服务
 * @description 获取、规范化并按账号缓存当前用户的完整笔记树数据。
 * @logic 1. 复用同账号缓存并合并并发首次请求；2. forceRefresh 分配新序号且仅最新响应可提交缓存；3. 为节点补充 uniqueId 并支持只复制变化路径的元数据补丁。
 * @dependencies Axios: frontend/src/axios, API: user/noteTree/UserAll
 * @index_tags 笔记树, 侧栏性能, 请求去重, 竞态保护, 树缓存, UserAll
 * @author holic512
 */
import axios from "../../../../../../../axios";
import type {Tree} from "@/views/User/Main/components/Sidebar/NoteTree/interface/treeInterface";
import type {
    NoteTreeNodeChanges,
    NoteTreeNodeType,
} from "@/views/User/Main/components/Sidebar/Pinia/isNoteTreeUpdated";
import {tokenStore} from "@/pinia/token";

let cachedTreeData: Tree[] | null = null;
let cachedToken: string | undefined;
let treeRequestSequence = 0;
let pendingTreeRequest: {token: string | undefined; promise: Promise<Tree[]>} | null = null;

export const clearUserAllTreeDataCache = (): void => {
    treeRequestSequence += 1;
    cachedTreeData = null;
    cachedToken = undefined;
    pendingTreeRequest = null;
};

export const patchNoteTreeNodes = (
    nodes: Tree[],
    nodeType: NoteTreeNodeType,
    nodeId: number,
    changes: NoteTreeNodeChanges,
): Tree[] => {
    let changed = false;

    const nextNodes = nodes.map((node) => {
        const isTarget = node.type === nodeType && node.id === nodeId;
        const children = node.children?.length
            ? patchNoteTreeNodes(node.children, nodeType, nodeId, changes)
            : node.children;

        if (!isTarget && children === node.children) return node;

        changed = true;
        return {
            ...node,
            ...(isTarget ? changes : {}),
            children,
        };
    });

    return changed ? nextNodes : nodes;
};

export const patchUserAllTreeDataCache = (
    nodeType: NoteTreeNodeType,
    nodeId: number,
    changes: NoteTreeNodeChanges,
): Tree[] | null => {
    const currentToken = tokenStore().getUserToken();
    if (!cachedTreeData || cachedToken !== currentToken) return null;

    cachedTreeData = patchNoteTreeNodes(cachedTreeData, nodeType, nodeId, changes);
    return cachedTreeData;
};

export const getUserAllTreeData = async (forceRefresh = false): Promise<Tree[]> => {
    const currentToken = tokenStore().getUserToken();
    if (!forceRefresh && cachedTreeData && cachedToken === currentToken) return cachedTreeData;
    const pendingRequest = pendingTreeRequest;
    if (!forceRefresh && pendingRequest && pendingRequest.token === currentToken) return pendingRequest.promise;

    const requestId = ++treeRequestSequence;
    const requestPromise = (async (): Promise<Tree[]> => {
        try {
            const response = await axios.get(
                "user/noteTree/UserAll",
            );

            // 将数据添加唯一字段 type+id
            const data = response.data.data;

            // 遍历数据并为每个节点添加 uniqueId
            const addUniqueId = (nodes: Tree[]): Tree[] => {
                return nodes.map((node: Tree) => {
                    // 添加唯一的 uniqueId，格式为 type+id
                    const normalizedNode: Tree = {
                        ...node,
                        uniqueId: `${node.type}_${node.id}`,
                    };

                    // 如果有子节点，则递归处理
                    if (normalizedNode.children) {
                        normalizedNode.children = addUniqueId(normalizedNode.children);
                    }

                    return normalizedNode;
                });
            };

            const normalizedTree = addUniqueId(data || []);
            if (requestId === treeRequestSequence && tokenStore().getUserToken() === currentToken) {
                cachedTreeData = normalizedTree;
                cachedToken = currentToken;
            }
            return normalizedTree;

        } catch (e) {
            return cachedToken === currentToken ? cachedTreeData || [] : [];
        }
    })();

    pendingTreeRequest = {token: currentToken, promise: requestPromise};
    try {
        return await requestPromise;
    } finally {
        if (pendingTreeRequest?.promise === requestPromise) pendingTreeRequest = null;
    }
}
