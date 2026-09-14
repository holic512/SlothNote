<!--
@file SendNoteComment
@project SlothNote
@module 用户端 / 笔记评论
@description 提交当前笔记评论并发布列表刷新事件。
@logic 1. 校验评论与当前笔记；2. 提交期间阻止重复请求；3. 仅成功时清空原输入并递增评论 revision。
@dependencies Service: SendComment, Store: currentNoteInfo/UpdateCommentState
@index_tags 评论提交, 重复提交保护, 评论刷新
@author holic512
-->
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
const submitting = ref(false);

// 发送功能
const handleSend = async () => {
  if (submitting.value) return;

  // 数据校验逻辑
  const content = textarea.value.trim();
  if (!content) {
    ElMessage.warning('评论内容不能为空');
    return;
  }

  const noteId = currentNoteInfo.noteId;
  if (noteId == null) return;

  submitting.value = true;
  try {
    const status = await SendComment(noteId, content)
    if (status !== 200) {
      ElMessage.error("添加评论失败");
      return;
    }

    ElMessage.success("添加评论成功")
    if (noteId === currentNoteInfo.noteId && textarea.value.trim() === content) {
      textarea.value = '';
    }
    UpdateCommentState.needUpdate();
  } finally {
    submitting.value = false;
  }

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
        <el-button type="primary" size="small" :loading="submitting" @click="handleSend">发送评论</el-button>
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
