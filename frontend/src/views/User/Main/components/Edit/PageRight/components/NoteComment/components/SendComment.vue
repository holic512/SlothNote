<script setup lang="ts">
import {ref} from "vue";
import {ElMessage} from "element-plus";
import {SendComment} from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/service/SendComment";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {
  UseUpdateCommentState
} from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/pinia/UpdateCommentState";

//================= pinia =================

// 获取当前笔记 基础信息 的 实体类
const currentNoteInfo = useCurrentNoteInfoStore()
// 获取 是否需要更新评论 的 实体类
const UpdateCommentState = UseUpdateCommentState();


// 评论框内容
const textarea = ref('');

// 发送功能
const handleSend = async () => {
  // 数据校验逻辑
  if (!textarea.value.trim()) {
    ElMessage.warning('评论内容不能为空');
    return;
  }

  // 执行发送逻辑
  let status: any;
  if (currentNoteInfo.noteId != null) {
    status = await SendComment(currentNoteInfo.noteId, textarea.value)
  }
  if (status == 200) ElMessage.success("添加评论成功")

  // 成功后逻辑
  textarea.value = '';
  UpdateCommentState.needUpdate();

};
</script>

<template>
  <div class="comment-container">
    <div class="comment-box">
      <div class="input-container">
        <textarea
            v-model="textarea"
            class="input-field"
            placeholder="写下你的评论或建议..."
            @keydown.ctrl.enter="handleSend"
        ></textarea>
      </div>
      <div class="button-container">
        <el-text class="tip-text">Ctrl + Enter 快捷发送</el-text>
        <el-button type="primary" size="small" @click="handleSend">发送评论</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.comment-container {
  display: flex;
  height: 100px;
  padding: 0 8px 8px 8px;
  margin-top: 4px;
  border-bottom: 1px solid #ebebeb;
}

.comment-box {
  flex: 1;
  padding: 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.input-container {
  height: 80%;
}

.input-field {
  width: 100%;
  height: 50px;
  padding: 4px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  background-color: var(--el-bg-color);
  font-size: 14px;
  color: var(--el-text-color-primary);
  resize: none;
  font-family: system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  transition: all 0.1s ease;
}

.input-field:focus {
  border-color: var(--el-color-primary);
  outline: none;
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.input-field::placeholder {
  color: var(--el-text-color-placeholder);
}

.button-container {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 20%;
}

.tip-text {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
