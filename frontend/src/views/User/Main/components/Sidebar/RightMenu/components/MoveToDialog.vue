<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, defineProps, defineEmits, watch, computed } from 'vue';
import { ElDialog, ElTree, ElButton, ElMessage, ElScrollbar } from 'element-plus';
import { getUserAllTreeData } from '@/views/User/Main/components/Sidebar/NoteTree/service/GetUserAllTreeData';
import { Tree } from '@/views/User/Main/components/Sidebar/NoteTree/interface/treeInterface';
import { Folder, House } from '@element-plus/icons-vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  // 要移动的项目类型 'NOTE' 或 'FOLDER'
  type: {
    type: String,
    default: 'NOTE'
  },
  // 要移动的项目ID
  itemId: {
    type: Number,
    default: 0
  },
  // 要移动的项目当前在的文件夹ID（用于高亮显示和避免选择）
  currentFolderId: {
    type: Number,
    default: 0
  }
});

const emit = defineEmits(['close', 'select', 'cancel']);

// 笔记树数据
const treeData = ref<Tree[]>([]);
// 选中的目标文件夹ID，0表示根目录
const selectedFolderId = ref<number>(0);
// 加载状态
const loading = ref<boolean>(true);

// 树的配置
const defaultProps = {
  children: 'children',
  label: 'label'
};

// 加载笔记树数据
const loadTreeData = async () => {
  loading.value = true;
  try {
    const data = await getUserAllTreeData();
    // 添加根目录作为第一个节点
    const rootNode: Tree = {
      id: 0,
      label: '根目录',
      type: 'FOLDER',
      children: data,
      uniqueId: 'root'
    };
    treeData.value = [rootNode];
    console.log('树数据加载完成:', treeData.value);
  } catch (error) {
    console.error('加载笔记树失败', error);
  } finally {
    loading.value = false;
  }
};

// 点击树节点
const handleNodeClick = (data: Tree) => {
  // 只可以选择文件夹作为目标
  if (data.type === 'FOLDER') {
    // 避免选择自己或当前已在的文件夹
    if (props.type === 'FOLDER' && data.id === props.itemId) {
      return;
    }
    
    // 设置为选中
    selectedFolderId.value = data.id;
  }
};

// 确认移动
const confirmMove = () => {
  if (selectedFolderId.value >= 0) {
    emit('select', selectedFolderId.value);
  }
};

// 关闭对话框
const closeDialog = () => {
  // 重置数据
  resetDialogState();
  emit('close');
};

// 取消移动
const cancelMove = () => {
  // 重置数据
  resetDialogState();
  emit('cancel');
};

// 重置对话框状态
const resetDialogState = () => {
  selectedFolderId.value = 0;
  loading.value = true;
  treeData.value = [];
};

// 判断节点是否禁用
const isNodeDisabled = (data: Tree) => {
  return (props.type === 'FOLDER' && data.id === props.itemId) || data.type !== 'FOLDER';
};

// 判断节点是否是当前位置
const isCurrentNode = (data: Tree) => {
  return data.id === props.currentFolderId;
};

// 判断节点是否是根目录
const isRootNode = (data: Tree) => {
  return data.id === 0;
};

// 获取节点显示名称
const getNodeLabel = (data: Tree) => {
  if (data.label == null || data.label === '') {
    return data.type === 'FOLDER' ? '新建文件夹' : '新建笔记';
  }
  return data.label;
};

// 监听对话框可见性变化
watch(() => props.visible, (newValue) => {
  if (newValue) {
    // 对话框打开时，加载数据
    loadTreeData();
  } else {
    // 对话框关闭时，重置状态
    resetDialogState();
  }
});

// 组件挂载时加载数据
onMounted(() => {
  if (props.visible) {
    loadTreeData();
  }
});

// 组件卸载前清理资源
onBeforeUnmount(() => {
  resetDialogState();
});
</script>

<template>
  <el-dialog
    title="选择目标文件夹"
    :modelValue="visible"
    @update:modelValue="closeDialog"
    width="600px"
    :close-on-click-modal="false"
    :before-close="closeDialog"
    destroy-on-close
    class="move-dialog"
  >
    <div class="move-dialog-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="loading-icon is-loading"><i class="el-icon-loading" /></el-icon>
        <span>加载文件夹...</span>
      </div>
      <div v-else class="tree-container">
        <el-scrollbar height="320px">
          <el-tree
            ref="treeRef"
            :data="treeData"
            :props="defaultProps"
            @node-click="handleNodeClick"
            highlight-current
            default-expand-all
          >
            <template #default="{ node, data }">
              <div 
                :class="[
                  'custom-tree-node', 
                  { 
                    'disabled': isNodeDisabled(data),
                    'current': isCurrentNode(data),
                    'root-node': isRootNode(data)
                  }
                ]"
              >
                <span class="node-icon">
                  <el-icon v-if="isRootNode(data)"><House /></el-icon>
                  <el-icon v-else-if="data.type === 'FOLDER'"><Folder /></el-icon>
                </span>
                <span :class="['node-label', selectedFolderId === data.id ? 'selected' : '']">
                  {{ getNodeLabel(data) }}
                </span>
              </div>
            </template>
          </el-tree>
        </el-scrollbar>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancelMove" size="small">取消</el-button>
        <el-button 
          type="primary" 
          @click="confirmMove" 
          size="small"
          :disabled="selectedFolderId < 0"
        >
          确认
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
:root {
  --base-color: rgb(247, 247, 245);
  --base-color-light: rgb(252, 252, 250);
  --base-color-dark: rgb(242, 242, 240);
  --accent-color: rgb(70, 130, 180);
  --accent-color-light: rgb(100, 150, 200);
  --accent-color-dark: rgb(50, 110, 160);
  --text-color: rgb(60, 60, 60);
  --text-color-light: rgb(100, 100, 100);
  --border-color: rgb(230, 230, 230);
}

:deep(.move-dialog) {
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  background-color: var(--base-color);
}

:deep(.el-dialog__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-color);
  margin-right: 0;
  background-color: var(--base-color-light);
  border-radius: 8px 8px 0 0;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: var(--text-color);
  font-size: 18px;
}

:deep(.el-dialog__body) {
  padding: 20px;
  background-color: var(--base-color);
}

:deep(.el-dialog__footer) {
  padding: 12px 20px;
  border-top: 1px solid var(--border-color);
  background-color: var(--base-color-light);
  border-radius: 0 0 8px 8px;
}

.move-dialog-content {
  min-height: 300px;
  max-height: 450px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 250px;
  color: var(--text-color-light);
}

.loading-icon {
  font-size: 28px;
  margin-bottom: 12px;
  color: var(--accent-color);
}

.tree-container {
  height: 100%;
}

.dialog-message {
  margin-bottom: 16px;
  color: var(--text-color);
  font-size: 15px;
}

:deep(.el-tree) {
  margin-top: 12px;
  border-radius: 6px;
  padding: 8px;
  background-color: var(--base-color-light);
  border: 1px solid var(--border-color);
}

:deep(.el-tree-node__content) {
  height: 36px;
  border-radius: 4px;
  margin: 2px 0;
}

:deep(.el-tree-node__content:hover) {
  background-color: var(--base-color-dark);
}

.custom-tree-node {
  display: flex;
  align-items: center;
  font-size: 14px;
  padding: 6px 0;
  width: 100%;
  color: var(--text-color);
}

.custom-tree-node.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.custom-tree-node.current {
  font-weight: bold;
  color: var(--accent-color);
}

.custom-tree-node.root-node {
  font-weight: 600;
  color: var(--accent-color-dark);
}

.node-icon {
  margin-right: 8px;
  color: var(--accent-color);
}

.root-node .node-icon {
  color: var(--accent-color-dark);
}

.node-label {
  flex: 1;
  padding: 2px 0;
}

.node-label.selected {
  font-weight: bold;
  color: var(--accent-color);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:deep(.el-button--primary) {
  padding: 8px 20px;
  background-color: var(--accent-color);
  border-color: var(--accent-color);
}

:deep(.el-button--primary:hover) {
  background-color: var(--accent-color-light);
  border-color: var(--accent-color-light);
}

:deep(.el-button) {
  border-radius: 4px;
  font-weight: 500;
  color: var(--text-color);
  border-color: var(--border-color);
}

:deep(.el-button:hover) {
  color: var(--accent-color);
  border-color: var(--accent-color-light);
  background-color: var(--base-color-light);
}

:deep(.el-scrollbar__bar) {
  background-color: var(--base-color-dark);
}

:deep(.el-scrollbar__thumb) {
  background-color: var(--accent-color-light);
}

:deep(.el-scrollbar__thumb:hover) {
  background-color: var(--accent-color);
}
</style> 