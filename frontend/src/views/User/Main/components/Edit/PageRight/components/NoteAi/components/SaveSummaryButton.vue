<script setup lang="ts">
import {ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import axios from "@/axios";

// 接收生成的简介内容
const props = defineProps({
  summary: {
    type: String,
    required: true
  }
});

// 对话框可见性
const dialogVisible = ref(false);

// 获取当前笔记信息
const currentNoteInfo = useCurrentNoteInfoStore();

// 保存为笔记简介
const saveAsNoteSummary = async () => {
  try {
    // 获取当前笔记ID
    const noteId = currentNoteInfo.noteId || 1;
    const noteTitle = currentNoteInfo.noteTitle || '当前笔记';

    ElMessageBox.confirm(
        `是否将此内容保存为笔记"${noteTitle}"的简介？`,
        '保存简介',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
    ).then(async () => {
      try {
        const response = await axios.put(
            "user/noteTree/NoteSummary",
            {
              noteId: noteId,
              noteDescription: props.summary
            }
        );

        if (response.data.status === 200) {
          ElMessage.success('简介已成功保存到笔记');
          dialogVisible.value = false;
        } else {
          ElMessage.error('保存失败，请重试');
        }
      } catch (error) {
        console.error('保存笔记简介失败:', error);
        ElMessage.error('保存失败，请重试');
      }
    }).catch(() => {
      // 用户取消，不做处理
    });
  } catch (error) {
    console.error('处理简介保存失败:', error);
    ElMessage.error('操作失败，请重试');
  }
};

// 打开对话框
const showDialog = () => {
  dialogVisible.value = true;
};

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false;
};
</script>

<template>
  <div class="save-summary-wrapper">
    <el-button type="primary" size="small" @click="showDialog">
      保存为简介
    </el-button>

    <el-dialog
        v-model="dialogVisible"
        title="保存简介内容"
        width="400px"
    >
      <div class="summary-preview">
        <strong>内容预览：</strong>
        <p class="preview-text">{{ props.summary }}</p>
      </div>

      <div class="action-buttons">
        <el-button type="primary" @click="saveAsNoteSummary">
          保存到当前笔记
        </el-button>
      </div>

      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.save-summary-wrapper {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.summary-preview {
  margin-bottom: 20px;
}

.preview-text {
  padding: 10px;
  background-color: #f7f7f7;
  border-radius: 4px;
  max-height: 150px;
  overflow-y: auto;
  white-space: pre-wrap;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style> 