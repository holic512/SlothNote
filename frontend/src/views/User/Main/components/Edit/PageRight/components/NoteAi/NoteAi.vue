<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatLineSquare,
  Close,
  Delete,
  Document,
  Edit,
  MagicStick,
  Plus,
  Position,
  Search,
  VideoPause,
  Clock,
  Link,
  DocumentAdd,
  MoreFilled
} from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import { useAiChatStore, type ChatMessage, type ContextNote } from '@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat'
import { searchNotes } from '@/views/User/Main/components/SidebarM/service/searchNotes'
import { useCurrentNoteInfoStore } from '@/views/User/Main/components/Edit/Pinia/currentNoteInfo'
import SaveSummaryButton from './components/SaveSummaryButton.vue'

const aiChat = useAiChatStore()
const currentNoteInfo = useCurrentNoteInfoStore()
const inputText = ref('')
const selectedAction = ref<'chat' | 'explain' | 'polish' | 'summary'>('chat')
const scrollbarRef = ref()
const noteSearchVisible = ref(false)
const noteKeyword = ref('')
const noteSearching = ref(false)
const noteSearchResults = ref<ContextNote[]>([])
const quickMenuVisible = ref(false)
const historyVisible = ref(false)

const editor = defineModel<any>()

const md = new MarkdownIt({ html: false, breaks: true, linkify: true })

const hasSelectedText = computed(() => !!aiChat.getSelectedText().trim())
const isSummaryMessage = (message: ChatMessage) => message.role === 'assistant' && message.messageType === 'summary'

const renderMarkdown = (content: string) => DOMPurify.sanitize(md.render(content || ''))

const scrollToBottom = () => {
  if (!scrollbarRef.value?.wrapRef) return
  nextTick(() => {
    scrollbarRef.value.wrapRef.scrollTo({
      top: scrollbarRef.value.wrapRef.scrollHeight,
      behavior: 'smooth'
    })
  })
}

const insertText = (content: string) => {
  if (!editor?.value) return
  try {
    editor.value.commands.insertContent(renderMarkdown(content))
    ElMessage.success('已插入到笔记')
  } catch {
    editor.value.commands.insertContent(content)
    ElMessage.success('已按原始 Markdown 插入到笔记')
  }
}

const reloadSearchResults = async () => {
  if (!noteKeyword.value.trim()) {
    noteSearchResults.value = []
    return
  }
  noteSearching.value = true
  try {
    const result = await searchNotes(noteKeyword.value.trim())
    noteSearchResults.value = (result || []).map((item: any) => ({
      noteId: item.noteId,
      title: item.title || '未命名笔记',
      summary: item.summary || item.snippet || '',
      icon: item.icon || null
    }))
  } finally {
    noteSearching.value = false
  }
}

const openNoteSelector = () => {
  quickMenuVisible.value = false
  noteSearchVisible.value = true
  noteKeyword.value = ''
  noteSearchResults.value = []
}

const addCurrentNote = async () => {
  if (!currentNoteInfo.noteId) {
    ElMessage.warning('当前没有可加入的笔记')
    return
  }
  await aiChat.addContextNote({
    noteId: currentNoteInfo.noteId,
    title: currentNoteInfo.noteName || '当前笔记',
    icon: currentNoteInfo.avatar || null,
    summary: ''
  })
  ElMessage.success('已关联当前笔记')
}

const selectContextNote = async (note: ContextNote) => {
  await aiChat.addContextNote(note)
  noteSearchVisible.value = false
}

const selectQuickAction = (action: 'chat' | 'explain' | 'polish' | 'summary') => {
  quickMenuVisible.value = false
  selectedAction.value = action
}

const send = async () => {
  if (aiChat.loading) return
  if (!inputText.value.trim() && selectedAction.value === 'chat') return
  if (selectedAction.value !== 'chat' && !hasSelectedText.value && !inputText.value.trim()) {
    ElMessage.warning('请先选中文本或输入内容')
    return
  }

  await aiChat.sendMessage(inputText.value, selectedAction.value)
  inputText.value = ''
  if (selectedAction.value !== 'chat') {
    selectedAction.value = 'chat'
  }
}

const clearAll = async () => {
  await ElMessageBox.confirm('确定清空所有对话历史吗？', '提示', { type: 'warning' })
  await aiChat.clearAllSessions()
  historyVisible.value = false
  ElMessage.success('已清空')
}

const removeSession = async (sessionId: number) => {
  await aiChat.deleteSession(sessionId)
}

watch(() => aiChat.messages.map(m => m.id).join(','), scrollToBottom)
watch(noteKeyword, reloadSearchResults)

onMounted(async () => {
  await aiChat.loadSessions()
})
</script>

<template>
  <div class="modern-ai-container">
    <!-- Header -->
    <header class="chat-header">
      <div class="header-title">
        <el-icon class="icon-spark"><MagicStick /></el-icon>
        <span>AI Assistant</span>
      </div>
      <div class="header-actions">
        <!-- 历史记录收纳至 Popover -->
        <el-popover v-model:visible="historyVisible" placement="bottom-end" :width="280" trigger="click" popper-class="minimal-popover">
          <template #reference>
            <el-button text circle size="small" title="历史记录">
              <el-icon><Clock /></el-icon>
            </el-button>
          </template>
          <div class="history-panel">
            <div class="history-header">
              <span>最近对话</span>
              <el-button text size="small" type="danger" :disabled="!aiChat.sessions.length" @click="clearAll">清空</el-button>
            </div>
            <div v-if="!aiChat.sessions.length" class="history-empty">暂无历史记录</div>
            <div v-else class="history-list">
              <div
                  v-for="session in aiChat.sessions" :key="session.id"
                  :class="['history-item', { active: session.id === aiChat.activeSessionId }]"
                  @click="aiChat.loadSessionDetail(session.id); historyVisible = false"
              >
                <span class="history-title">{{ session.title }}</span>
                <el-icon class="history-del" @click.stop="removeSession(session.id)"><Delete /></el-icon>
              </div>
            </div>
          </div>
        </el-popover>
        <el-button text circle size="small" title="新对话" @click="aiChat.createEmptySession()">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
    </header>

    <!-- Chat Area -->
    <el-scrollbar ref="scrollbarRef" class="chat-viewport">
      <div v-if="!aiChat.messages.length" class="empty-state">
        <div class="empty-icon"><el-icon><MagicStick /></el-icon></div>
        <h3>有什么可以帮你的？</h3>
        <p>你可以直接提问，或选择笔记内容让我为你解释和润色。</p>
      </div>

      <div v-else class="message-list">
        <div v-for="message in aiChat.messages" :key="message.id" :class="['message', message.role]">
          <div v-if="message.role === 'assistant'" class="msg-avatar ai">
            <el-icon><MagicStick /></el-icon>
          </div>

          <div class="msg-content">
            <div v-if="message.role === 'user'" class="bubble user">
              {{ message.content }}
            </div>
            <div v-else class="bubble ai markdown-body" v-html="renderMarkdown(message.content)"></div>

            <!-- AI Message Actions -->
            <div v-if="message.role === 'assistant' && message.status === 'completed'" class="msg-actions">
              <el-button size="small" text @click="insertText(message.content)">
                <el-icon><Plus /></el-icon>插入笔记
              </el-button>
              <SaveSummaryButton v-if="isSummaryMessage(message)" :summary="message.content" />
            </div>
          </div>
        </div>
      </div>
    </el-scrollbar>

    <!-- Unified Composer -->
    <div class="composer-container">
      <div class="composer-box" :class="{ 'is-focused': quickMenuVisible }">
        <!-- Context Tags (Inside Composer) -->
        <div v-if="aiChat.contextNotes.length" class="composer-contexts">
          <span v-for="note in aiChat.contextNotes" :key="note.noteId" class="context-chip">
            <el-icon><Link /></el-icon> {{ note.title }}
            <el-icon class="chip-close" @click="aiChat.removeContextNote(note.noteId)"><Close /></el-icon>
          </span>
        </div>

        <el-input
            v-model="inputText"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 6 }"
            resize="none"
            :disabled="aiChat.loading"
            :placeholder="selectedAction === 'chat' ? '输入指令，Enter 发送...' : `当前模式: ${selectedAction}，可输入额外要求`"
            @keydown.enter.exact.prevent="send"
        />

        <div class="composer-toolbar">
          <div class="toolbar-left">
            <!-- 附件工具 -->
            <el-dropdown trigger="click" placement="top-start">
              <el-button text circle size="small" title="附加笔记上下文">
                <el-icon><DocumentAdd /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="addCurrentNote">关联当前笔记</el-dropdown-item>
                  <el-dropdown-item @click="openNoteSelector">搜索其他笔记...</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 快捷指令工具 -->
            <el-dropdown trigger="click" placement="top-start" @command="selectQuickAction">
              <el-button text round size="small" class="mode-btn">
                <el-icon><MoreFilled /></el-icon>
                {{ selectedAction === 'chat' ? '智能指令' : selectedAction }}
              </el-button>
              <template #dropdown>
                <el-dropdown-menu class="minimal-menu">
                  <el-dropdown-item command="chat"><el-icon><ChatLineSquare /></el-icon> 对话 (默认)</el-dropdown-item>
                  <el-dropdown-item command="explain" divided><el-icon><Document /></el-icon> 解释选中内容</el-dropdown-item>
                  <el-dropdown-item command="polish"><el-icon><Edit /></el-icon> 润色选中内容</el-dropdown-item>
                  <el-dropdown-item command="summary"><el-icon><Document /></el-icon> 生成摘要</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <div class="toolbar-right">
            <el-button v-if="aiChat.loading" type="info" circle size="small" @click="aiChat.stopChat()">
              <el-icon><VideoPause /></el-icon>
            </el-button>
            <el-button v-else type="primary" circle size="small" :disabled="!inputText.trim() && !hasSelectedText" @click="send">
              <el-icon><Position /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
      <div class="composer-footer">AI 生成内容可能存在错误，请核实。</div>
    </div>

    <!-- Search Dialog -->
    <el-dialog v-model="noteSearchVisible" title="选择笔记" width="480px" class="minimal-dialog" :show-close="false">
      <el-input v-model="noteKeyword" placeholder="搜索笔记..." :prefix-icon="Search" clearable />
      <div class="search-results">
        <div v-if="!noteSearchResults.length" class="empty-tip">未找到相关笔记</div>
        <div v-for="note in noteSearchResults" :key="note.noteId" class="search-item" @click="selectContextNote(note)">
          <span class="item-title">{{ note.title }}</span>
          <span class="item-summary">{{ note.summary }}</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
/* 基础容器 */
.modern-ai-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #ffffff;
  color: #111827;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 顶部 Header：极致极简 */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f3f4f6;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  z-index: 10;

  .header-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 15px;
    font-weight: 600;

    .icon-spark {
      color: #3b82f6;
      font-size: 18px;
    }
  }

  .header-actions {
    display: flex;
    gap: 4px;
  }
}

/* 聊天区域 */
.chat-viewport {
  flex: 1;
  padding: 0 16px;
}

/* 空状态：轻量化 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b7280;
  text-align: center;
  padding-bottom: 20%;

  .empty-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: #f3f4f6;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    color: #9ca3af;
    margin-bottom: 16px;
  }

  h3 {
    margin: 0 0 8px 0;
    font-size: 16px;
    font-weight: 500;
    color: #374151;
  }

  p {
    font-size: 13px;
    max-width: 260px;
    line-height: 1.5;
  }
}

/* 消息列表：现代排版 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px 0;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 100%;

  &.user {
    flex-direction: row-reverse;
  }

  &.assistant {
    align-items: flex-start;
  }
}

.msg-avatar.ai {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  background: #f3f4f6;
  border: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #374151;
  font-size: 14px;
}

.msg-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-width: 85%;
}

/* 对话气泡：去阴影、去渐变 */
.bubble {
  font-size: 14px;
  line-height: 1.6;

  &.user {
    background: #f4f4f5;
    color: #18181b;
    padding: 10px 14px;
    border-radius: 14px 14px 4px 14px;
    white-space: pre-wrap;
  }

  &.ai {
    color: #111827;
    padding: 2px 0; // AI无背景，直接输出文本
  }
}

/* Markdown 优化 */
.markdown-body {
  :deep(p) { margin: 0 0 12px 0; }
  :deep(p:last-child) { margin-bottom: 0; }
  :deep(pre) {
    background: #f9fafb;
    border: 1px solid #e5e7eb;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    font-size: 13px;
    margin: 12px 0;
  }
  :deep(code) {
    background: #f3f4f6;
    padding: 2px 4px;
    border-radius: 4px;
    font-size: 0.9em;
    color: #ef4444;
  }
  :deep(pre code) {
    color: #374151;
    background: transparent;
    padding: 0;
  }
}

.msg-actions {
  display: flex;
  gap: 8px;
  margin-top: 4px;

  .el-button {
    font-size: 12px;
    color: #6b7280;
    &:hover { color: #3b82f6; background: #eff6ff; }
  }
}

/* 输入区 (Composer)：集成的控制台 */
.composer-container {
  padding: 16px;
  background: linear-gradient(to top, #ffffff 80%, rgba(255,255,255,0));
}

.composer-box {
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  background: #ffffff;
  padding: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: border-color 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
  gap: 8px;

  &:focus-within, &.is-focused {
    border-color: #d1d5db;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  /* 隐藏 el-input 默认边框 */
  :deep(.el-textarea__inner) {
    border: none !important;
    box-shadow: none !important;
    padding: 0 4px;
    font-size: 14px;
    line-height: 1.6;
    background: transparent;

    &::placeholder { color: #9ca3af; }
  }
}

/* 沉浸式上下文标签 */
.composer-contexts {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 0 4px;

  .context-chip {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #4b5563;
    background: #f3f4f6;
    padding: 4px 8px;
    border-radius: 6px;

    .chip-close {
      cursor: pointer;
      color: #9ca3af;
      &:hover { color: #ef4444; }
    }
  }
}

.composer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;

  .toolbar-left, .toolbar-right {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .mode-btn {
    color: #4b5563;
    font-size: 12px;
    background: #f9fafb;
    border-color: transparent;
    &:hover { background: #f3f4f6; }
  }
}

.composer-footer {
  text-align: center;
  font-size: 11px;
  color: #9ca3af;
  margin-top: 10px;
}

/* 历史记录 Popover 面板 */
.history-panel {
  display: flex;
  flex-direction: column;

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 8px;
    border-bottom: 1px solid #f3f4f6;
    font-size: 13px;
    font-weight: 600;
    color: #374151;
  }

  .history-empty {
    padding: 20px 0;
    text-align: center;
    font-size: 13px;
    color: #9ca3af;
  }

  .history-list {
    max-height: 240px;
    overflow-y: auto;
    margin-top: 8px;
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .history-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 13px;
    color: #4b5563;
    transition: background 0.2s;

    &:hover { background: #f3f4f6; }
    &.active { background: #eff6ff; color: #1d4ed8; font-weight: 500; }

    .history-title {
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      flex: 1;
      padding-right: 8px;
    }

    .history-del {
      opacity: 0;
      color: #9ca3af;
      &:hover { color: #ef4444; }
    }
    &:hover .history-del { opacity: 1; }
  }
}

/* 搜索笔记 Dialog 优化 */
.search-results {
  margin-top: 12px;
  max-height: 300px;
  overflow-y: auto;

  .empty-tip {
    text-align: center;
    color: #9ca3af;
    padding: 30px 0;
    font-size: 13px;
  }

  .search-item {
    padding: 10px;
    border-radius: 8px;
    cursor: pointer;
    border: 1px solid transparent;
    display: flex;
    flex-direction: column;
    gap: 4px;

    &:hover { background: #f9fafb; border-color: #e5e7eb; }

    .item-title { font-size: 14px; font-weight: 500; color: #1f2937; }
    .item-summary { font-size: 12px; color: #6b7280; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  }
}

/* 覆盖 Element UI 弹出层默认样式 */
:global(.minimal-popover) {
  padding: 12px !important;
  border-radius: 12px !important;
  border-color: #e5e7eb !important;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1) !important;
}

:global(.minimal-menu .el-dropdown-menu__item) {
  font-size: 13px;
  padding: 6px 16px;
  gap: 8px;
}
</style>