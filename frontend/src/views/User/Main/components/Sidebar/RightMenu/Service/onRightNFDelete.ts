// 用于处理右键删除的通用方法

import { useRightSelectNodeId } from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";
import { DeleteNote } from "./DeleteNote";
import { DeleteFolder } from "./DeleteFolder";
import { ElMessageBox } from "element-plus";
import { Tree } from "@/views/User/Main/components/Sidebar/NoteTree/interface/treeInterface";

/**
 * 处理删除笔记的右键操作
 */
export const onRightNFDeleteNote = async () => {
    // 获取右键选中的节点ID
    const RightId = useRightSelectNodeId();
    // 获取当前选中的数据
    const selectedData = RightId.data as Tree;
    
    // 确认是否要删除
    try {
        await ElMessageBox.confirm(
            '确定要删除这个笔记吗？',
            '删除笔记',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        );
        
        // 用户确认后执行删除
        return await DeleteNote(selectedData.id);
    } catch (err) {
        // 用户取消删除
        return 0;
    }
};

/**
 * 处理删除文件夹的右键操作
 */
export const onRightNFDeleteFolder = async () => {
    // 获取右键选中的节点ID
    const RightId = useRightSelectNodeId();
    // 获取当前选中的数据
    const selectedData = RightId.data as Tree;
    
    // 确认是否要删除
    try {
        await ElMessageBox.confirm(
            '确定要删除这个文件夹吗？该操作可能会影响文件夹内的所有笔记和子文件夹。',
            '删除文件夹',
            {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }
        );
        
        // 用户确认后执行删除
        return await DeleteFolder(selectedData.id);
    } catch (err) {
        // 用户取消删除
        return 0;
    }
}; 