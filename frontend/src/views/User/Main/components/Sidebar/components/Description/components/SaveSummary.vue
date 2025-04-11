<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { putNoteDescription } from "@/views/User/Main/components/Sidebar/components/Description/Service/PutNoteDescription";
import { putFolderDescription } from "@/views/User/Main/components/Sidebar/components/Description/Service/PutFolderDescription";
import { useCurrentNoteInfoStore } from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import { getFolderIdByNoteId } from '@/views/User/Main/components/Edit/service/getFolderIdByNoteId';

// 接收生成的简介和当前选中的内容类型（笔记或文件夹）
const props = defineProps({
  summary: {
    type: String,
    required: true
  }
});

// 定义事件
const emits = defineEmits(['close']);

// 是否显示存储选项
const showSaveOptions = ref(false);

// 当前选中的项目类型和ID
const selectedItem = ref({
  type: 'NOTE',  // 'NOTE' 或 'FOLDER'
  id: null,
  label: ''
});

// 获取当前笔记信息
const currentNoteInfo = useCurrentNoteInfoStore();

// 获取当前笔记ID和标题
const currentNoteId = ref(null);
const currentNoteTitle = ref('');

// 获取当前文件夹ID和标题
const currentFolderId = ref(null);
const currentFolderTitle = ref('所属文件夹');

// 初始化数据
onMounted(async () => {
  if (currentNoteInfo.noteId) {
    currentNoteId.value = currentNoteInfo.noteId;
    currentNoteTitle.value = currentNoteInfo.noteTitle || '当前笔记';
    
    // 获取文件夹ID
    try {
      currentFolderId.value = await getFolderIdByNoteId(currentNoteInfo.noteId);
    } catch (error) {
      console.error('获取文件夹ID失败:', error);
    }
  }
});

// 打开存储选项
const openSaveOptions = () => {
  showSaveOptions.value = true;
};

// 选择保存为笔记简介
const saveAsNoteSummary = async () => {
  try {
    // 使用当前笔记ID或默认值
    const noteId = currentNoteId.value || 1;
    const noteTitle = currentNoteTitle.value || '当前笔记';
    
    selectedItem.value = {
      type: 'NOTE',
      id: noteId,
      label: noteTitle
    };
    
    ElMessageBox.confirm(
      `是否将生成的简介保存为笔记 "${noteTitle}" 的简介？`,
      '保存简介',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(async () => {
      const status = await putNoteDescription(noteId, props.summary);
      if (status == 200) {
        ElMessage.success('简介已成功保存到笔记');
        closeSaveOptions();
      } else {
        ElMessage.error('保存失败，请重试');
      }
    }).catch(() => {
      // 用户取消操作
    });
  } catch (error) {
    console.error('保存笔记简介时出错:', error);
    ElMessage.error('保存失败，请重试');
  }
};

// 选择保存为文件夹简介
const saveAsFolderSummary = async () => {
  try {
    // 使用当前文件夹ID或默认值
    const folderId = currentFolderId.value || 1;
    
    selectedItem.value = {
      type: 'FOLDER',
      id: folderId,
      label: currentFolderTitle.value
    };
    
    ElMessageBox.confirm(
      `是否将生成的简介保存为文件夹的简介？`,
      '保存简介',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    ).then(async () => {
      const status = await putFolderDescription(folderId, props.summary);
      if (status == 200) {
        ElMessage.success('简介已成功保存到文件夹');
        closeSaveOptions();
      } else {
        ElMessage.error('保存失败，请重试');
      }
    }).catch(() => {
      // 用户取消操作
    });
  } catch (error) {
    console.error('保存文件夹简介时出错:', error);
    ElMessage.error('保存失败，请重试');
  }
};

// 关闭保存选项
const closeSaveOptions = () => {
  showSaveOptions.value = false;
  emits('close');
};
</script>

<template>
  <div class="save-summary-container">
    <el-button type="primary" size="small" @click="openSaveOptions">
      保存为简介
    </el-button>
    
    <el-dialog
      v-model="showSaveOptions"
      title="选择保存位置"
      width="400px"
      destroy-on-close
    >
      <el-alert
        title="请选择要将此简介保存到哪里"
        type="info"
        :closable="false"
        style="margin-bottom: 20px;"
      />
      
      <div class="save-options">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="简介内容">
            <div class="summary-preview">{{ props.summary }}</div>
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="save-option-buttons">
          <el-button type="primary" @click="saveAsNoteSummary">
            保存到当前笔记
          </el-button>
          <el-button type="success" @click="saveAsFolderSummary">
            保存到所属文件夹
          </el-button>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="closeSaveOptions">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.save-summary-container {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.save-options {
  margin: 20px 0;
}

.save-option-buttons {
  display: flex;
  justify-content: space-around;
  margin-top: 20px;
}

.summary-preview {
  max-height: 100px;
  overflow-y: auto;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
  white-space: pre-wrap;
}
</style> 