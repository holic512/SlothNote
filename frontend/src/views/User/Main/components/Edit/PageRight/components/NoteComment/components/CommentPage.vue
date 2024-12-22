<script setup lang="ts">
// =============== 导入区 ===============
import {onMounted, ref, watch} from 'vue';
import {formatDistanceToNow} from 'date-fns';
import {zhCN} from 'date-fns/locale';

// 图标
import {
  Position
} from '@element-plus/icons-vue'
import IconComment from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/icon/IconComment.vue";
import {
  GetComments,
  IComment
} from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/service/GetComments";
import {RightPageModeEnum, useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {ReplyComment} from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/service/ReplyComment";
import {ElMessage} from "element-plus";
import {
  UseUpdateCommentState
} from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/pinia/UpdateCommentState";

//================= pinia =================

// 获取当前笔记 基础信息 的 实体类
const currentNoteInfo = useCurrentNoteInfoStore()
// 获取 是否需要更新评论 的 实体类
const UpdateCommentState = UseUpdateCommentState();


// =============== 数据定义 ===============

// 判断评论内容是否为空
const isCommentEmpty = ref(false);

// 使用接口约束 ref 显示笔记内容
const comments = ref<IComment[]>([]);

// =============== 基础方法 ===============
const getComments = async () => {
  if (currentNoteInfo.noteId != null) {
    comments.value = await GetComments(currentNoteInfo.noteId)
    if (comments.value.length != null) isCommentEmpty.value = true;
  }
}


// =============== 初始化数据 ===============

// 钩子函数
onMounted(() => {
  getComments();
})

// =============== 数据监听 ===============

// 监听 当前笔记 是否切换 - 当笔记切换时 加载评论
watch(() => currentNoteInfo.noteId, (newValue) => {
  if (newValue != null) {
    getComments();
  }
})

// 监听 评论是否需要更新
watch(() => UpdateCommentState.isNeedUpdate, (newValue) => {
  if (newValue) {
    getComments();
    UpdateCommentState.completeUpdate();
  }
})


// =============== 工具方法 ===============
const formatTime = (date: string) => {
  return formatDistanceToNow(new Date(date), {locale: zhCN, addSuffix: true});
};

// =============== 回复功能 ===============

// 定义回复信息
const newReplyModels = ref({});

const handleReply = async (commentId: number, content: any) => {
  // 调用服务函数
  const status = await ReplyComment(commentId, content)

  // 成功后逻辑
  if (status == 200) ElMessage.success("回复成功")
  newReplyModels.value[commentId] = "";
  UpdateCommentState.needUpdate();
};


</script>

<template>
  <el-scrollbar style="height: 100%;width: 100%;" v-if="isCommentEmpty">

    <div v-for="comment in comments" :key="comment.id" class="comment-item">

      <!-- 主评论区域 -->
      <div class="comment-main">
        <div style="display: flex;flex-direction: column;">
          <el-avatar style="height: 24px;width: 24px;" :src="comment.avatar" class="comment-avatar"/>
          <div style="width: 100%;flex: 1;display: flex;justify-content: center;padding-top: 8px;">
            <el-divider direction="vertical" style="height: 100%"/>
          </div>
        </div>

        <div class="comment-content-wrapper">
          <!-- 评论头部：用户名和时间 -->
          <div class="comment-header">
            <span class="username">{{ comment.username }}</span>
            <span class="comment-date">{{ formatTime(comment.date) }}</span>
          </div>

          <!-- 评论正文 -->
          <div class="comment-content">{{ comment.content }}</div>

        </div>
      </div>

      <!-- 回复列表区域 -->
      <div v-if="comment.replies?.length" class="reply-list">
        <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">

          <div style="display: flex;flex-direction: column;">
            <el-avatar style="height: 24px;width: 24px;" :src="comment.avatar" class="comment-avatar"/>
            <div style="width: 100%;flex: 1;display: flex;justify-content: center;padding-top: 8px;">
              <el-divider direction="vertical" style="height: 100%"/>
            </div>
          </div>

          <div class="reply-content-wrapper">
            <!-- 回复头部信息 -->
            <div class="reply-header">
              <span class="username">{{ reply.username }}</span>

              <span class="comment-date">{{ formatTime(reply.date) }}</span>
            </div>
            <!-- 回复正文 -->
            <div class="reply-content">{{ reply.content }}</div>
          </div>
        </div>
      </div>

      <!-- 回复input-->
      <div style="display: flex;align-items: center;height: 36px;gap: 8px;width: 100%;">
        <el-avatar style="height: 24px;width: 24px;" :src="comment.avatar" class="comment-avatar"/>
        <div class="reply-input-box">
          <input
              class="reply-input"
              placeholder="回复..."
              v-model="newReplyModels[comment.id]"
          >
          <el-button size="small" circle :icon="Position"
                     @click="handleReply(comment.id,newReplyModels[comment.id])"></el-button>
        </div>
      </div>

    </div>
  </el-scrollbar>

  <div style="height: 100%;display: flex;justify-content: center;align-items: center;flex-direction: column" v-else>
    <!--图标-->
    <el-icon size="64" color="#A5A4A1">
      <IconComment/>
    </el-icon>

    <el-text>没有公开的评论或建议</el-text>
  </div>


</template>

<style scoped>
/* =============== 主评论样式 =============== */
.comment-item {
  padding: 16px;
  background: #ffffff;
  transition: background 0.1s ease;
}

.comment-item:hover {
  background: #F9F9F8;
}


.comment-main {
  display: flex;
  gap: 8px;
}

.comment-content-wrapper {
  flex: 1;
  margin-top: 4px;
}

/* =============== 评论头部样式 =============== */
.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 2px;
}

.username {
  color: #606266;
  font-size: 14px;
}

.comment-date {
  font-size: 12px;
  color: #8c8c8c;
}

/* =============== 评论内容样式 =============== */
.comment-content {
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
  margin: 8px 0;
}


/* =============== 回复列表样式 =============== */
.reply-list {
  padding-top: 4px;
}

.reply-item {
  display: flex;
  gap: 6px;
  margin-bottom: 4px;
}

/* =============== 回复内容样式 =============== */
.reply-content-wrapper {
  flex: 1;
  margin-top: 2px;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.reply-to {
  font-size: 12px;
  color: #8c8c8c;
}

.reply-content {
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
  margin: 4px 0;
}

.reply-input {
  flex: 1;
  border: none;
  outline: none;
  background: none;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.reply-input::placeholder {
  color: var(--el-text-color-placeholder);
}

.reply-input:focus {
  outline: none;
  box-shadow: none;
}

.reply-input-box {
  flex: 1;
  display: flex;
  align-items: center;
  border-radius: 8px;
  padding: 4px;
  border: 2px solid transparent; /* 添加透明边框防止抖动 */
  transition: all 0.2s ease; /* 添加过渡效果 */
  gap: 4px;
}

/* 当输入框获得焦点时，改变外层容器的边框 */
.reply-input-box:has(.reply-input:focus) {
  border-color: #9BC5E9;
}

.reply-input-box:focus {
  border-color: #9BC5E9;
}

.reply-input-box:hover {
  border-color: #9BC5E9;
}

</style>
