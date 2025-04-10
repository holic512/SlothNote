// 用于处理右键菜单中的移动操作

import { useRightSelectNodeId } from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";
import { MoveNote } from "./MoveNote";
import { MoveFolder } from "./MoveFolder";
import { ref } from "vue";
import { ElMessageBox } from "element-plus";

// 控制对话框显示状态
export const moveDialogVisible = ref(false);
// 当前移动的项目类型
export const moveItemType = ref<'NOTE' | 'FOLDER'>('NOTE');
// 当前移动的项目ID
export const moveItemId = ref<number>(0);
// 当前移动项目所在的文件夹ID
export const currentFolderId = ref<number>(0);

/**
 * 处理移动笔记操作
 */
export const onRightNFMoveNote = () => {
  // 获取右键选中的笔记数据
  const RightId = useRightSelectNodeId();
  const selectedData = RightId.data as any;
  
  // 设置移动对话框的数据
  moveItemType.value = 'NOTE';
  moveItemId.value = selectedData.id;
  currentFolderId.value = RightId.GetSelectNodeId(); // 获取当前文件夹ID
  
  // 显示移动对话框
  moveDialogVisible.value = true;
};

/**
 * 处理移动文件夹操作
 */
export const onRightNFMoveFolder = () => {
  // 获取右键选中的文件夹数据
  const RightId = useRightSelectNodeId();
  const selectedData = RightId.data as any;
  
  // 设置移动对话框的数据
  moveItemType.value = 'FOLDER';
  moveItemId.value = selectedData.id;
  currentFolderId.value = selectedData.parentId || 0; // 获取父文件夹ID
  
  // 显示移动对话框
  moveDialogVisible.value = true;
};

/**
 * 处理对话框选择后的移动操作
 * @param targetFolderId 目标文件夹ID
 */
export const handleMoveToFolder = async (targetFolderId: number) => {
  try {
    let result;
    
    // 根据类型调用不同的移动API
    if (moveItemType.value === 'NOTE') {
      result = await MoveNote(moveItemId.value, targetFolderId);
    } else {
      result = await MoveFolder(moveItemId.value, targetFolderId);
    }
    
    // 关闭对话框
    moveDialogVisible.value = false;
    
    return result;
  } catch (error) {
    console.error('移动操作失败:', error);
    moveDialogVisible.value = false;
    return 500;
  }
};

/**
 * 关闭移动对话框
 */
export const closeMoveDialog = () => {
  moveDialogVisible.value = false;
}; 