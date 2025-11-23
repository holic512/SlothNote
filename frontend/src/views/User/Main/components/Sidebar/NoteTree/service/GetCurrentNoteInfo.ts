// 获取 当前选中的笔记信息
// 用于 根据当前选中的 笔记信息 显示编辑器等其他内容
import {useCurrentNoteInfoStore} from "../../../Edit/Pinia/currentNoteInfo";
import {updateNoteTitle} from "@/views/User/Main/components/Edit/Main/Service/updateNoteTitle";
import {useNoteTreeUpdate} from "../../Pinia/isNoteTreeUpdated";
import type Node from "element-plus/es/components/tree/src/model/node";

export const getCurrentNoteInfo = async (node: Node) => {
    // 获取 文章 目录
    const parents: string[] = [];
    let currentNode = node;
    while (currentNode.parent.data.label) {
        parents.unshift(currentNode.parent.data.label)
        currentNode = currentNode.parent;
    }

    // 存储到 pinia中
    const currentNoteInfo = useCurrentNoteInfoStore()

    // console.log(node.data)

    // 先清理 在储存
    currentNoteInfo.clearNoteInfo()
    if (node.data.type === 'NOTE' && (!node.data.label || node.data.label === '')) {
        await updateNoteTitle(node.data.id, '新建文档')
        node.data.label = '新建文档'
        const isNoteTreeUpdated = useNoteTreeUpdate()
        isNoteTreeUpdated.UpdatedNoteTree()
    }
    currentNoteInfo.setNoteInfo(node.data.id, node.data.label, parents, node.data.avatar, node.data.cover)
}