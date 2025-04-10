<script setup lang="ts">
import { useAiChatStore } from '@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat'
import { computed, ref, watch, nextTick, onMounted, inject } from 'vue'
import { CircleCheck, Loading, Plus, Delete, Position, ChatLineSquare, MagicStick } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const aiChat = useAiChatStore()
const scrollbarRef = ref()
const inputRef = ref()
const inputText = ref('');

const isEmpty = computed(() => aiChat.messages.length === 0)
const isLoading = computed(() => aiChat.loading)

// 判断是否为开发环境
const isDev = computed(() => {
  // 可以通过其他方式判断，或者设为 false
  // 如果你的项目中有定义环境变量，可以使用 import.meta.env.DEV
  return false; // 生产环境下不显示调试信息
})

const editor = defineModel()

// --- Header Logic ---
const handleClear = () => {
  ElMessageBox.confirm(
      '确定要清空所有对话记录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    aiChat.clearMessages()
    ElMessage.success('对话记录已清空')
  })
}

// --- Input Logic ---
const handleSend = () => {
  if (inputText.value.trim() && !isLoading.value) {
    aiChat.sendMessage(inputText.value);
    inputText.value = '';
  }
};

const handleStop = () => {
  aiChat.stopChat()
};

const handleKeydown = (e: KeyboardEvent) => {
  // 按下回车键但没有按下 Ctrl 或 Meta 键时发送消息
  if (e.key === 'Enter' && !e.ctrlKey && !e.metaKey && !e.shiftKey) {
    e.preventDefault(); // 阻止默认的换行行为
    handleSend();
  }
  // Ctrl+Enter 或 Shift+Enter 允许换行
  // 不阻止默认行为，自动插入换行符
};

const adjustHeight = (el: HTMLTextAreaElement) => {
  if (el) {
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 120) + 'px'; // 限制最大高度
  }
};

// --- Page Logic ---
const scrollToBottom = () => {
  if (scrollbarRef.value) {
    const wrap = scrollbarRef.value.wrapRef
    nextTick(() => {
      wrap.scrollTo({
        top: wrap.scrollHeight,
        behavior: 'smooth'
      })
    })
  }
}

const insertText = (content: string) => {
  if (editor) {
    editor.value.commands.insertContent(content)
    ElMessage.success('文本已插入到笔记中')
  }
}

watch(() => aiChat.messages.length, () => {
  scrollToBottom()
}, { deep: true })

watch(() => aiChat.messages[aiChat.messages.length - 1]?.content, () => {
  scrollToBottom()
})

onMounted(() => {
  scrollToBottom()
  // Adjust height on mount if needed
  if (inputRef.value) {
    adjustHeight(inputRef.value.$el.querySelector('textarea'))
  }
})
</script>

<template>
  <div class="ai-page-container">
    <!-- Header -->
    <div class="ai-header">
      <div class="header-content">
        <div class="title-section">
          <span class="title">AI 助手</span>
        </div>
        <el-button
            text
            class="clear-button"
            @click="handleClear"
            :disabled="aiChat.loading"
        >
          <el-icon>
            <Delete />
          </el-icon>
        </el-button>
      </div>
    </div>

    <!-- Chat Content -->
    <el-scrollbar v-if="!isEmpty" class="chat-container" ref="scrollbarRef">
      <div class="messages-container">
        <div
            v-for="message in aiChat.messages"
            :key="message.id"
            :class="['message', message.role]"
        >
          <div class="message-time">
            {{ new Date(message.timestamp).toLocaleTimeString() }}
            <small v-if="isDev">(ID: {{ message.id }}, {{ message.role }})</small>
          </div>
          <div class="message-content">
            <template v-if="message.role === 'assistant'">
              <div class="assistant-header">
                <el-icon><CircleCheck /></el-icon>
                <span>AI助手</span>
              </div>
              <div class="insert-button" @click="insertText(message.content)">
                <el-icon><Plus /></el-icon>
              </div>
            </template>
            <div class="text">{{ message.content }}</div>
          </div>
        </div>
      </div>
    </el-scrollbar>

    <div v-else class="empty-state">
      <el-icon size="64">
        <MagicStick />
      </el-icon>
      <el-text>AI 助手准备就绪，开始对话吧</el-text>
    </div>

    <!-- Input Area -->
    <div class="ai-input-wrapper">
      <div class="ai-input-container">
        <div class="input-area">
          <el-input
              ref="inputRef"
              v-model="inputText"
              type="textarea"
              :rows="1"
              :autosize="{ minRows: 1, maxRows: 5 }"
              placeholder="回车发送消息，Ctrl+Enter 换行"
              resize="none"
              :disabled="isLoading"
              @keydown="handleKeydown"
              @input="() => adjustHeight(inputRef?.value?.$el?.querySelector('textarea'))"
          >
            <template #prefix>
              <el-icon><ChatLineSquare /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="button-group">
          <el-button
              v-if="!isLoading"
              type="primary"
              :disabled="!inputText.trim()"
              @click="handleSend"
              class="send-button"
          >
            <el-icon><Position /></el-icon>
          </el-button>
          <el-button
              v-else
              type="danger"
              @click="handleStop"
              class="send-button"
          >
            <el-icon><Loading class="loading-icon" /></el-icon>
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Container */
.ai-page-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #f9fafb;
}

/* Header Styles */
.ai-header {
  position: relative;
  z-index: 2;
  height: 48px; /* 调整高度 */
  border-bottom: 1px solid var(--el-border-color-lighter);
  background-color: #ffffff;
  flex-shrink: 0; /* 防止 Header 被压缩 */
  padding: 0 16px; /* 直接在 Header 上加 padding */
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.title-section {
  display: flex;
  align-items: center;
}

.title {
  font-size: 15px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  letter-spacing: 0.3px;
}

.clear-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 4px;
  transition: all 0.2s ease;
  color: var(--el-text-color-secondary);
}

.clear-button:hover {
  background-color: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
}

.clear-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.clear-button .el-icon {
  font-size: 16px;
}

/* Chat Container Styles */
.chat-container {
  flex: 1; /* 占据剩余空间 */
  padding: 16px;
  background-color: #ffffff;
  overflow-y: auto;
}

.messages-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-bottom: 16px;
}

.message {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-time {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  padding: 0 12px;
  opacity: 0.8;
}

.message-content {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 8px;
  position: relative;
  word-wrap: break-word; /* 自动换行 */
  white-space: pre-wrap; /* 保留空白符和换行 */
}

/* User message 用户消息样式 */
.message.user .message-content {
  align-self: flex-end;
  background-color: var(--el-color-primary);
  color: white;
  margin-left: auto; /* 靠右对齐 */
  margin-right: 0;
}

/* Assistant message AI助手消息样式 */
.message.assistant .message-content {
  align-self: flex-start;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
  margin-left: 0; /* 靠左对齐 */
  margin-right: auto;
}

.assistant-header {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  opacity: 0.8;
}

.insert-button {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s ease;
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.message.assistant .message-content:hover .insert-button {
  opacity: 1;
}

.insert-button:hover {
  background-color: var(--el-color-primary-light-8);
  transform: scale(1.05);
}

.insert-button .el-icon {
  font-size: 14px;
}

.text {
  line-height: 1.5;
  font-size: 14px;
}

/* Empty State Styles */
.empty-state {
  flex: 1; /* 占据剩余空间 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
  color: var(--el-text-color-secondary);
  padding: 24px;
  text-align: center;
  background-color: var(--el-fill-color-light);
  margin: 16px;
  border-radius: 8px;
}

/* Input Area Styles */
.ai-input-wrapper {
  background-color: #ffffff;
  padding: 12px 16px;
  border-top: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0; /* 防止 Input 被压缩 */
}

.ai-input-container {
  display: flex;
  gap: 8px;
  align-items: flex-end;
}

.input-area {
  flex: 1;
  background-color: var(--el-fill-color-light);
  border-radius: 6px;
  transition: all 0.2s ease;
}

.input-area:focus-within {
  background-color: #ffffff;
  box-shadow: 0 0 0 1px var(--el-color-primary-light-5);
}

:deep(.el-textarea__inner) {
  padding: 8px 12px;
  line-height: 1.5;
  font-size: 14px;
  border: 1px solid transparent; /* 默认透明边框 */
  border-radius: 6px;
  background-color: transparent;
  resize: none;
  transition: all 0.2s ease;
  max-height: 120px; /* 限制最大高度 */
  box-shadow: none; /* 移除默认阴影 */
}

:deep(.el-textarea__inner:focus) {
  border-color: var(--el-color-primary); /* 聚焦时显示边框 */
  box-shadow: none;
}

.button-group {
  display: flex;
  align-items: flex-end;
}

.send-button {
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.send-button:not(:disabled):hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.send-button .el-icon {
  font-size: 18px;
}

.send-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-icon {
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Scrollbar Styles */
.chat-container::-webkit-scrollbar {
  width: 6px;
}

.chat-container::-webkit-scrollbar-thumb {
  background-color: #dcdfe6;
  border-radius: 3px;
}

.chat-container::-webkit-scrollbar-track {
  background-color: transparent;
}
</style>