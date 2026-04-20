<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatLineSquare,
  Close,
  CollectionTag,
  Delete,
  Document,
  Edit,
  InfoFilled,
  MagicStick,
  Plus,
  Position,
  Search,
  VideoPause
} from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import { useAiChatStore, type ChatMessage, type ContextNote } from '@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat'
import { searchNotes } from '@/views/User/Main/components/SidebarM/service/searchNotes'
import { useCurrentNoteInfoStore } from '@/views/User/Main/components/Edit/Pinia/currentNoteInfo'
import SaveSummaryButton from './SaveSummaryButton.vue'

const aiChat = useAiChatStore()
const currentNoteInfo = useCurrentNoteInfoStore()
const inputText = ref('')
const selectedAction = ref<'chat' | 'explain' | 'polish' | 'summary'>('chat')
const scrollbarRef = ref()
const noteSearchVisible = ref(false)
const noteKeyword = ref('')
const noteSearching = ref(false)
const noteSearchResults = ref<ContextNote[]>([])

const editor = defineModel<any>()

const md = new MarkdownIt({
  html: false,
  breaks: true,
  linkify: true
})

const hasSelectedText = computed(() => !!aiChat.getSelectedText().trim())
const isSummaryMessage = (message: ChatMessage) => message.role === 'assistant' && message.messageType === 'summary'

const renderMarkdown = (content: string) => DOMPurify.sanitize(md.render(content || ''))

const scrollToBottom = () => {
  if (!scrollbarRef.value?.wrapRef) {
    return
  }
  nextTick(() => {
    scrollbarRef.value.wrapRef.scrollTo({
      top: scrollbarRef.value.wrapRef.scrollHeight,
      behavior: 'smooth'
    })
  })
}

const insertText = (content: string) => {
  if (!editor?.value) {
    return
  }
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
  ElMessage.success('已加入当前笔记')
}

const selectContextNote = async (note: ContextNote) => {
  await aiChat.addContextNote(note)
  noteSearchVisible.value = false
}

const send = async () => {
  if (aiChat.loading) {
    return
  }
  if (!inputText.value.trim() && selectedAction.value === 'chat') {
    return
  }
  if (selectedAction.value !== 'chat' && !hasSelectedText.value && !inputText.value.trim()) {
    ElMessage.warning('请先选中文本或输入问题')
    return
  }

  await aiChat.sendMessage(inputText.value, selectedAction.value)
  inputText.value = ''
  if (selectedAction.value !== 'chat') {
    selectedAction.value = 'chat'
  }
}

const clearAll = async () => {
  await ElMessageBox.confirm('确定删除全部 AI 历史会话吗？', '提示', {
    type: 'warning'
  })
  await aiChat.clearAllSessions()
  ElMessage.success('已清空全部会话')
}

const removeSession = async (sessionId: number) => {
  await aiChat.deleteSession(sessionId)
  ElMessage.success('会话已删除')
}

watch(() => aiChat.messages.map(message => `${message.id}-${message.content.length}-${message.status}`).join('|'), () => {
  scrollToBottom()
})

watch(noteKeyword, async () => {
  await reloadSearchResults()
})

onMounted(async () => {
  await aiChat.loadSessions()
})
</script>

<template>
  <div class="ai-page">
    <div class="session-panel">
      <div class="session-header">
        <span>AI 会话</span>
        <div class="session-actions">
          <el-button text @click="aiChat.createEmptySession()">新建</el-button>
          <el-button text :disabled="!aiChat.sessions.length" @click="clearAll">清空</el-button>
        </div>
      </div>

      <el-scrollbar class="session-list">
        <div
          v-for="session in aiChat.sessions"
          :key="session.id"
          :class="['session-item', { active: session.id === aiChat.activeSessionId }]"
          @click="aiChat.loadSessionDetail(session.id)"
        >
          <div class="session-main">
            <div class="session-title">{{ session.title }}</div>
            <div class="session-time">{{ new Date(session.lastMessageAt).toLocaleString() }}</div>
          </div>
          <el-button text class="delete-session" @click.stop="removeSession(session.id)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </el-scrollbar>
    </div>

    <div class="chat-panel">
      <div class="chat-header">
        <div class="chat-title">
          <el-icon><MagicStick /></el-icon>
          <span>{{ aiChat.activeSession?.title || '新对话' }}</span>
        </div>
      </div>

      <div class="context-bar">
        <div class="context-actions">
          <el-button size="small" @click="openNoteSelector">
            <el-icon><CollectionTag /></el-icon>
            添加笔记上下文
          </el-button>
          <el-button size="small" @click="addCurrentNote">加入当前笔记</el-button>
        </div>
        <div v-if="hasSelectedText" class="selected-text-tip">
          已选文本将作为精确上下文发送
        </div>
        <div v-if="aiChat.contextNotes.length" class="context-tags">
          <div v-for="note in aiChat.contextNotes" :key="note.noteId" class="context-tag">
            <span class="tag-icon">{{ note.icon || '📝' }}</span>
            <span class="tag-title">{{ note.title }}</span>
            <button class="tag-remove" @click="aiChat.removeContextNote(note.noteId)">
              <el-icon><Close /></el-icon>
            </button>
          </div>
        </div>
      </div>

      <el-scrollbar ref="scrollbarRef" class="messages-wrapper">
        <div v-if="!aiChat.messages.length" class="empty-state">
          <el-icon size="48"><ChatLineSquare /></el-icon>
          <div>开始对话，或先加入几篇笔记作为上下文。</div>
        </div>

        <div v-else class="messages">
          <div v-for="message in aiChat.messages" :key="message.id" :class="['message', message.role]">
            <div class="message-meta">
              <span>{{ message.role === 'assistant' ? 'AI' : '你' }}</span>
              <span>{{ new Date(message.createdAt).toLocaleTimeString() }}</span>
              <span v-if="message.status !== 'completed'" class="message-status">{{ message.status }}</span>
            </div>
            <div class="message-body">
              <div v-if="message.role === 'user'" class="plain-text">{{ message.content }}</div>
              <div v-else class="markdown-text" v-html="renderMarkdown(message.content)"></div>
              <div v-if="message.role === 'assistant'" class="message-tools">
                <el-button text @click="insertText(message.content)">
                  <el-icon><Plus /></el-icon>
                  插入到笔记
                </el-button>
              </div>
              <div v-if="isSummaryMessage(message)" class="summary-actions">
                <SaveSummaryButton :summary="message.content" />
              </div>
            </div>
          </div>
        </div>
      </el-scrollbar>

      <div class="composer">
        <div class="quick-actions">
          <el-button :type="selectedAction === 'chat' ? 'primary' : 'default'" plain @click="selectedAction = 'chat'">对话</el-button>
          <el-button :type="selectedAction === 'explain' ? 'primary' : 'default'" plain @click="selectedAction = 'explain'">
            <el-icon><Document /></el-icon>
            解释
          </el-button>
          <el-button :type="selectedAction === 'polish' ? 'primary' : 'default'" plain @click="selectedAction = 'polish'">
            <el-icon><Edit /></el-icon>
            润色
          </el-button>
          <el-button :type="selectedAction === 'summary' ? 'primary' : 'default'" plain @click="selectedAction = 'summary'">
            <el-icon><InfoFilled /></el-icon>
            摘要
          </el-button>
        </div>

        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          resize="none"
          :disabled="aiChat.loading"
          :placeholder="selectedAction === 'chat' ? '输入问题，Enter 发送，Shift+Enter 换行' : '可补充额外要求，不填也会直接处理选中文本'"
          @keydown.enter.exact.prevent="send"
        />

        <div class="composer-actions">
          <el-button v-if="!aiChat.loading" type="primary" @click="send">
            <el-icon><Position /></el-icon>
            发送
          </el-button>
          <el-button v-else type="danger" @click="aiChat.stopChat()">
            <el-icon><VideoPause /></el-icon>
            停止
          </el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="noteSearchVisible" title="添加笔记上下文" width="560px">
      <el-input v-model="noteKeyword" placeholder="搜索笔记标题、摘要或正文片段">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <div class="search-result-panel">
        <div v-if="noteSearching" class="search-empty">搜索中...</div>
        <div v-else-if="!noteSearchResults.length" class="search-empty">输入关键词后可添加笔记</div>
        <div v-else class="search-result-list">
          <div v-for="note in noteSearchResults" :key="note.noteId" class="search-item" @click="selectContextNote(note)">
            <div class="search-item-title">
              <span>{{ note.icon || '📝' }}</span>
              <span>{{ note.title }}</span>
            </div>
            <div class="search-item-summary">{{ note.summary || '无摘要' }}</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.ai-page {
  height: 100%;
  display: grid;
  grid-template-columns: 132px 1fr;
  background: #f6f7fb;
}

.session-panel {
  border-right: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.session-header,
.chat-header {
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  border-bottom: 1px solid #eceff4;
}

.session-actions {
  display: flex;
  gap: 4px;
}

.session-list {
  flex: 1;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 8px;
  border-bottom: 1px solid #f4f5f7;
  cursor: pointer;
}

.session-item.active {
  background: #eff6ff;
}

.session-main {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  margin-top: 4px;
  font-size: 11px;
  color: #6b7280;
}

.delete-session {
  color: #9ca3af;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #1f2937;
}

.context-bar {
  background: #fff;
  border-bottom: 1px solid #eceff4;
  padding: 10px 12px;
}

.context-actions,
.quick-actions,
.composer-actions,
.message-tools {
  display: flex;
  gap: 8px;
  align-items: center;
}

.selected-text-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #2563eb;
}

.context-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.context-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #eef2ff;
  color: #3730a3;
  font-size: 12px;
}

.tag-remove {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: inherit;
  cursor: pointer;
  padding: 0;
}

.messages-wrapper {
  flex: 1;
  padding: 16px;
}

.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #6b7280;
  text-align: center;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.message {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message.user {
  align-items: flex-end;
}

.message.assistant {
  align-items: flex-start;
}

.message-meta {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: #6b7280;
}

.message-status {
  text-transform: capitalize;
}

.message-body {
  max-width: 88%;
  background: #fff;
  border-radius: 14px;
  padding: 12px 14px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
}

.message.user .message-body {
  background: #2563eb;
  color: #fff;
}

.plain-text {
  white-space: pre-wrap;
  line-height: 1.6;
}

.markdown-text {
  line-height: 1.7;
  color: #111827;
  word-break: break-word;
}

.message.user .markdown-text {
  color: inherit;
}

.markdown-text :deep(pre) {
  overflow: auto;
  background: #0f172a;
  color: #e5e7eb;
  padding: 12px;
  border-radius: 10px;
}

.markdown-text :deep(code) {
  background: rgba(148, 163, 184, 0.18);
  padding: 2px 5px;
  border-radius: 6px;
}

.markdown-text :deep(blockquote) {
  margin: 12px 0;
  padding-left: 12px;
  border-left: 3px solid #cbd5e1;
  color: #475569;
}

.composer {
  background: #fff;
  border-top: 1px solid #eceff4;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-result-panel {
  margin-top: 12px;
  min-height: 180px;
}

.search-empty {
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
}

.search-result-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.search-item {
  padding: 10px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  background: #fff;
}

.search-item:hover {
  border-color: #93c5fd;
  background: #f8fbff;
}

.search-item-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #1f2937;
}

.search-item-summary {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
}

.summary-actions {
  margin-top: 8px;
}
</style>
