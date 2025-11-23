<script setup lang="ts">
import {useAiChatStore} from '@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat'
import {computed, nextTick, onMounted, ref, watch} from 'vue'
import {
  ArrowRight,
  ChatLineSquare,
  CircleCheck,
  Delete,
  Document,
  Edit,
  InfoFilled,
  MagicStick,
  Plus,
  Position,
  VideoPause
} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import MarkdownIt from 'markdown-it'
import SaveSummaryButton from './SaveSummaryButton.vue'
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";

// 初始化markdown-it解析器
const md = new MarkdownIt()

const aiChat = useAiChatStore()
const scrollbarRef = ref()
const inputRef = ref()
const inputText = ref('')
const selectedAction = ref(null)
const isLoading = ref(false)
const hasSelectedText = computed(() => !!aiChat.getSelectedText())

// 控制选中文本弹窗的显示
const selectedTextPopoverVisible = ref(false)

const isEmpty = computed(() => aiChat.messages.length === 0)

// 判断是否为开发环境
const isDev = computed(() => {
  return false;
})

const editor = defineModel()

// 获取当前笔记信息
const currentNoteInfo = useCurrentNoteInfoStore();

// 判断消息是否为简介消息
const isSummaryMessage = (message) => {
  if (message.role !== 'assistant') return false;
  // 从消息历史判断是否为简介请求
  const index = aiChat.messages.findIndex(m => m.id === message.id);
  if (index > 0 && aiChat.messages[index - 1].role === 'user') {
    return aiChat.messages[index - 1].content.includes('请为以下文本生成简介');
  }
  return false;
};

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
  if (isLoading.value) {
    return;
  }

  // 获取选中文本
  const selectedText = aiChat.getSelectedText();
  console.log("选中文本检查:", selectedText);

  if (selectedAction.value === 'explain') {
    aiChat.explainText();
  } else if (selectedAction.value === 'polish') {
    aiChat.polishText();
  } else if (selectedAction.value === 'summary') {
    aiChat.generateSummary();
  } else if (inputText.value.trim()) {
    // 如果有输入内容
    if (selectedText) {
      // 有选中文本，将用户问题和选中文本一起发送
      console.log("发送选中文本:", selectedText);

      // 构建消息：用户问题 + 选中文本
      const message = `${inputText.value.trim()}\n\n选中文本：\n${selectedText}`;
      console.log("发送的完整消息:", message);

      // 直接发送消息到AI，不要手动添加用户消息（这会导致冲突）
      aiChat.sendMessage(message);
    } else {
      // 没有选中文本，只发送输入框内容
      aiChat.sendMessage(inputText.value);
    }
  }

  // 发送后清空输入框
  inputText.value = '';
  selectedAction.value = null;
};

const handleStop = () => {
  aiChat.stopChat()
};

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.ctrlKey && !e.metaKey && !e.shiftKey) {
    e.preventDefault();
    handleSend();
  }
};

const adjustHeight = (el: HTMLTextAreaElement) => {
  if (el) {
    el.style.height = 'auto';
    el.style.height = Math.min(el.scrollHeight, 120) + 'px';
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

// 修改插入文本的方法，支持Markdown
const insertText = (content: string) => {
  if (editor) {
    try {
      console.log("原始内容:", content);

      // 尝试直接使用编辑器命令处理Markdown
      let processedContent = content;

      // 逐行处理内容，确保标题能被正确识别
      const lines = processedContent.split('\n');
      const processedLines = lines.map(line => {
        // 处理标题 (# ## ### #### ##### #####)
        const headingMatch = line.match(/^(#{1,6})\s+(.+)$/);
        if (headingMatch) {
          const level = headingMatch[1].length;
          const text = headingMatch[2];
          console.log(`检测到标题: 级别=${level}, 文本=${text}`);
          return `<h${level}>${text}</h${level}>`;
        }
        return line;
      });

      processedContent = processedLines.join('\n');

      // 处理代码块
      processedContent = processedContent.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>');

      // 处理列表
      processedContent = processedContent.replace(/^\s*-\s+(.+)$/gm, '<ul><li>$1</li></ul>');
      processedContent = processedContent.replace(/^\s*\d+\.\s+(.+)$/gm, '<ol><li>$1</li></ol>');

      // 处理粗体
      processedContent = processedContent.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

      // 处理斜体
      processedContent = processedContent.replace(/\*(.*?)\*/g, '<em>$1</em>');

      console.log("处理后内容:", processedContent);

      // 使用editor的insertContent插入HTML内容
      editor.value.commands.insertContent(processedContent);
      ElMessage.success('Markdown文本已插入到笔记中');
    } catch (error) {
      console.error('Markdown转换失败，使用纯文本插入', error);
      editor.value.commands.insertContent(content);
      ElMessage.success('文本已插入到笔记中');
    }
  }
}

watch(() => aiChat.messages.length, () => {
  scrollToBottom()
}, {deep: true})

watch(() => aiChat.messages[aiChat.messages.length - 1]?.content, () => {
  scrollToBottom()
})

// 监听选中文本的变化
watch(() => aiChat.getSelectedText(), (newText) => {
  if (newText) {
    // 不再自动填充到输入框
    // inputText.value = newText;
  }
}, {immediate: true});

// 监听aiChat的loading状态
watch(() => aiChat.loading, (newLoadingStatus) => {
  isLoading.value = newLoadingStatus;
  console.log('AI加载状态变化:', isLoading.value);
});

onMounted(() => {
  // 初始化isLoading状态
  isLoading.value = aiChat.loading;

  scrollToBottom()
  if (inputRef.value) {
    adjustHeight(inputRef.value.$el.querySelector('textarea'))
  }
})

// --- 功能按钮逻辑 ---
const handleExplain = () => {
  if (!hasSelectedText.value) {
    ElMessage.warning('请先选中需要解释的文本')
    return
  }
  selectedAction.value = selectedAction.value === 'explain' ? null : 'explain';
}

const handlePolish = () => {
  if (!hasSelectedText.value) {
    ElMessage.warning('请先选中需要润色的文本')
    return
  }
  selectedAction.value = selectedAction.value === 'polish' ? null : 'polish';
}

// 新增的生成简介处理函数
const handleSummary = () => {
  if (!hasSelectedText.value) {
    ElMessage.warning('请先选中需要生成简介的文本')
    return
  }
  selectedAction.value = selectedAction.value === 'summary' ? null : 'summary';
}
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
            :disabled="isLoading"
        >
          <el-icon>
            <Delete/>
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
                <el-icon>
                  <CircleCheck/>
                </el-icon>
                <span>AI助手</span>
              </div>
              <div class="insert-button" @click="insertText(message.content)">
                <el-icon>
                  <Plus/>
                </el-icon>
              </div>
            </template>
            <div class="text" v-if="message.role === 'user'">{{ message.content }}</div>
            <div class="text markdown-content" v-else v-html="md.render(message.content)"></div>

            <!-- 如果是生成简介的回复，显示保存简介按钮 -->
            <div v-if="isSummaryMessage(message)" class="summary-actions">
              <SaveSummaryButton :summary="message.content"/>
            </div>
          </div>
        </div>
      </div>
    </el-scrollbar>

    <div v-else class="empty-state">
      <el-icon size="64">
        <MagicStick/>
      </el-icon>
      <el-text>AI 助手准备就绪，开始对话吧</el-text>
    </div>

    <!-- Input Area -->
    <div class="ai-input-wrapper">
      <div class="ai-input-container">
        <div class="input-area">
          <!-- 选中文本标签 - 点击显示弹出框 -->
          <el-popover
              v-if="hasSelectedText"
              trigger="click"
              placement="top"
              :width="220"
              popper-class="text-popover"
          >
            <template #reference>
              <div class="selected-text-container">
                <div class="selected-text-label">已选文本</div>
                <el-icon>
                  <ArrowRight/>
                </el-icon>
              </div>
            </template>
            <div class="selected-text-popover-wrapper">
              <div class="selected-text-popover-content">
                {{ aiChat.getSelectedText() }}
              </div>
              <div class="selected-text-popover-footer">
                <el-button
                    size="small"
                    type="danger"
                    @click="() => { aiChat.setSelectedText(''); }"
                    class="cancel-selection-button"
                >
                  取消选择
                </el-button>
              </div>
            </div>
          </el-popover>

          <!-- 重新设计输入区域布局 -->
          <div class="input-container">
            <!-- 纯文本输入区域 -->
            <div class="text-input-wrapper">
              <el-input
                  ref="inputRef"
                  v-model="inputText"
                  type="textarea"
                  :rows="1"
                  :autosize="{ minRows: 1, maxRows: 5 }"
                  :placeholder="selectedAction ? '点击发送按钮开始处理' : '回车发送消息，Ctrl+Enter 换行'"
                  resize="none"
                  :disabled="isLoading"
                  @keydown="handleKeydown"
                  @input="() => adjustHeight(inputRef?.value?.$el?.querySelector('textarea'))"
              >
                <template #prefix>
                  <el-icon>
                    <ChatLineSquare/>
                  </el-icon>
                </template>
              </el-input>
            </div>

            <!-- 功能区容器 -->
            <div class="action-buttons">
              <!-- AI功能按钮 -->
              <div class="left-buttons">
                <el-dropdown v-if="hasSelectedText" trigger="click" placement="top">
                  <div class="ai-function-button">
                    <el-icon>
                      <MagicStick/>
                    </el-icon>
                    <span>AI功能</span>
                  </div>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item
                          :class="{ 'is-selected': selectedAction === 'explain' }"
                          @click="handleExplain"
                          :disabled="isLoading"
                      >
                        <el-icon>
                          <Document/>
                        </el-icon>
                        <span>解释</span>
                      </el-dropdown-item>
                      <el-dropdown-item
                          :class="{ 'is-selected': selectedAction === 'polish' }"
                          @click="handlePolish"
                          :disabled="isLoading"
                      >
                        <el-icon>
                          <Edit/>
                        </el-icon>
                        <span>润色</span>
                      </el-dropdown-item>
                      <el-dropdown-item
                          :class="{ 'is-selected': selectedAction === 'summary' }"
                          @click="handleSummary"
                          :disabled="isLoading"
                      >
                        <el-icon>
                          <InfoFilled/>
                        </el-icon>
                        <span>生成简介</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <!-- 发送按钮 -->
              <div class="right-buttons">
                <el-button
                    v-if="!isLoading"
                    type="primary"
                    :disabled="!inputText.trim() && !selectedAction"
                    @click="handleSend"
                    class="send-button"
                >
                  <el-icon>
                    <Position/>
                  </el-icon>
                </el-button>
                <el-tooltip
                    v-else
                    content="点击停止AI回复"
                    placement="top"
                    :show-after="300"
                >
                  <el-button
                      type="danger"
                      @click="handleStop"
                      class="send-button stop-button"
                  >
                    <el-icon>
                      <VideoPause/>
                    </el-icon>
                  </el-button>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
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
  height: 48px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background-color: #ffffff;
  flex-shrink: 0;
  padding: 0 16px;
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
  flex: 1;
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
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message.user .message-content {
  align-self: flex-end;
  background-color: var(--el-color-primary);
  color: white;
  margin-left: auto;
  margin-right: 0;
}

.message.assistant .message-content {
  align-self: flex-start;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-primary);
  margin-left: 0;
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
  flex: 1;
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
  flex-shrink: 0;
}

.ai-input-container {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  margin-top: 12px;
}

.input-area {
  flex: 1;
  background-color: var(--el-fill-color-light);
  border-radius: 6px;
  transition: all 0.2s ease;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 移除输入区域focus-within的样式 */
.input-area:focus-within {
  background-color: #ffffff;
  /* 移除这里的box-shadow */
  /* box-shadow: 0 0 0 1px var(--el-color-primary-light-5); */
}

/* 选中文本显示区域样式 */
.selected-text-container {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background-color: var(--el-fill-color);
  border-radius: 4px;
  border: 1px solid var(--el-border-color-lighter);
  margin: 0 12px;
  margin-top: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  width: fit-content; /* 使按钮宽度适应内容 */

  &:hover {
    background-color: var(--el-fill-color-light);
  }
}

.selected-text-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
}

/* 选中文本弹出框内容样式 */
.selected-text-popover-wrapper {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.selected-text-popover-content {
  max-height: 250px;
  overflow-y: auto;
  padding: 10px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 4px;
  white-space: pre-wrap;
  line-height: 1.5;
  font-size: 12px;
  color: var(--el-text-color-primary);
  word-break: break-all;
  text-align: left;
  overflow-x: hidden;
}

.selected-text-popover-footer {
  display: flex;
  justify-content: flex-end;
}

.cancel-selection-button {
  font-size: 12px;
  padding: 4px 10px;
}

/* 重新设计的输入区域样式 */
.input-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.text-input-wrapper {
  width: 100%;
}

/* 重新设计按钮布局样式 */
.action-buttons {
  display: flex;
  justify-content: space-between; /* 左右分开排列 */
  align-items: center;
  width: 100%;
  padding: 0 5px 5px 0;
}

.left-buttons {
  display: flex;
  align-items: center;
}

.right-buttons {
  display: flex;
  align-items: center;
}

.ai-function-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;

  &:hover {
    background-color: var(--el-color-primary-light-8);
  }

  .el-icon {
    font-size: 14px;
  }
}

/* 发送按钮样式 */
.send-button {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

/* 停止按钮样式 */
.stop-button {
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(var(--el-color-danger-rgb), 0.4);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(var(--el-color-danger-rgb), 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(var(--el-color-danger-rgb), 0);
  }
}

/* 删除不再需要的样式 */
.input-buttons,
.ai-function-wrapper,
.send-button-wrapper {
  display: none;
}

/* 输入框样式更新 */
:deep(.el-textarea__inner) {
  padding-right: 14px; /* 恢复正常内边距，因为按钮已经移到外部 */
  line-height: 1.5;
  font-size: 14px;
  border: 1px solid transparent;
  border-radius: 6px;
  background-color: transparent;
  resize: none;
  transition: all 0.2s ease;
  max-height: 120px;
  box-shadow: none;
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

/* 自定义el-dropdown-item样式 */
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;

  &.is-selected {
    color: var(--el-color-primary);
    background-color: var(--el-color-primary-light-9);
  }
}

/* Markdown内容样式 */
.markdown-content {
  :deep(h1) {
    font-size: 1.5em;
    margin-top: 0.5em;
    margin-bottom: 0.5em;
    font-weight: bold;
  }

  :deep(h2) {
    font-size: 1.3em;
    margin-top: 0.5em;
    margin-bottom: 0.5em;
    font-weight: bold;
  }

  :deep(h3) {
    font-size: 1.2em;
    margin-top: 0.5em;
    margin-bottom: 0.5em;
    font-weight: bold;
  }

  :deep(h4, h5, h6) {
    font-size: 1.1em;
    margin-top: 0.5em;
    margin-bottom: 0.5em;
    font-weight: bold;
  }

  :deep(p) {
    margin-bottom: 0.5em;
  }

  :deep(ul, ol) {
    padding-left: 1.5em;
    margin-bottom: 0.5em;
  }

  :deep(li) {
    margin-bottom: 0.2em;
  }

  :deep(code) {
    background-color: #f3f3f3;
    padding: 0.2em 0.4em;
    border-radius: 3px;
    font-family: monospace;
    font-size: 0.9em;
  }

  :deep(pre) {
    background-color: #f3f3f3;
    padding: 0.5em;
    border-radius: 5px;
    overflow-x: auto;
    margin-bottom: 0.5em;

    code {
      background-color: transparent;
      padding: 0;
    }
  }

  :deep(strong) {
    font-weight: bold;
  }

  :deep(em) {
    font-style: italic;
  }

  :deep(blockquote) {
    border-left: 3px solid #ddd;
    padding-left: 1em;
    margin-left: 0;
    margin-right: 0;
    color: #666;
  }
}

.summary-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>