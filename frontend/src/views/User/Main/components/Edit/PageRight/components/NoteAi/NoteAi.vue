<!--
@file NoteAi
@project SlothNote
@module 用户端 / 笔记 AI 面板
@description 编排 AI 会话、用户全局权限、上下文笔记选择和自由消息输入。
@logic 1. 组合会话历史与消息列表组件；2. 调用搜索 composable 管理上下文选择；3. 按全局读取权限决定是否附带笔记上下文；4. 将工具授权交给服务端。
@dependencies Store: AiChat/AiPermissions/currentNoteInfo, Components: AiMessageList/AiSessionHistory, Composable: useContextNoteSearch
@index_tags AI 面板, 会话编排, 全局权限, 上下文选择, 自由对话
@author holic512
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {Close, MagicStick, Plus, Position, Search, VideoPause, Clock, Link, DocumentAdd} from '@element-plus/icons-vue'
import {useAiChatStore, type ContextNote} from '@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat'
import {useAiPermissionStore} from '@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiPermissions'
import { useCurrentNoteInfoStore } from '@/views/User/Main/components/Edit/Pinia/currentNoteInfo'
import {useSaveNoteState} from '@/views/User/Main/components/Edit/Pinia/SaveNoteState'
import {SaveNote} from '@/views/User/Main/components/Edit/service/SaveNote'
import {useNoteEditorContext} from '@/views/User/Main/components/Edit/editor/editorContext'
import AiMessageList from './components/AiMessageList.vue'
import AiSessionHistory from './components/AiSessionHistory.vue'
import { useContextNoteSearch } from './composables/useContextNoteSearch'

const aiChat = useAiChatStore()
const aiPermissions = useAiPermissionStore()
const currentNoteInfo = useCurrentNoteInfoStore()
const saveNoteState = useSaveNoteState()
const {editor} = useNoteEditorContext()
const inputText = ref('')
const messageListRef = ref<{ resetWindow: () => void } | null>(null)
const {
  noteSearchVisible,
  noteKeyword,
  noteSearching,
  noteSearchResults,
  openNoteSearch
} = useContextNoteSearch()
const historyVisible = ref(false)
const sendPreparing = ref(false)
const lastSendAt = ref(0)
const SEND_DEBOUNCE_MS = 500

const canReadAllNotes = computed(() => aiPermissions.permissions.canReadAllNotes)
const isAiBusy = computed(() => aiChat.loading || sendPreparing.value)

const resetMessageWindow = () => {
  messageListRef.value?.resetWindow()
}

const openNoteSelector = () => {
  if (isAiBusy.value || !canReadAllNotes.value) return
  openNoteSearch()
}

const addCurrentNote = async () => {
  if (isAiBusy.value || !canReadAllNotes.value) return
  if (!currentNoteInfo.noteId) {
    ElMessage.warning('当前没有可加入的笔记')
    return
  }
  try {
    await aiChat.addContextNote({
      noteId: currentNoteInfo.noteId,
      title: currentNoteInfo.noteName || '当前笔记',
      icon: currentNoteInfo.avatar || null,
      summary: ''
    })
    ElMessage.success('已关联当前笔记')
  } catch (error) {
    console.warn('[NoteAI] add current context note failed', error)
    ElMessage.error('关联笔记失败')
  }
}

const selectContextNote = async (note: ContextNote) => {
  if (isAiBusy.value || !canReadAllNotes.value) return
  try {
    await aiChat.addContextNote(note)
    noteSearchVisible.value = false
  } catch (error) {
    console.warn('[NoteAI] add context note failed', error)
    ElMessage.error('关联笔记失败')
  }
}

const removeContextNote = async (noteId: number) => {
  if (isAiBusy.value || !canReadAllNotes.value) return
  try {
    await aiChat.removeContextNote(noteId)
  } catch (error) {
    console.warn('[NoteAI] remove context note failed', error)
    ElMessage.error('移除关联笔记失败')
  }
}

const saveDirtyCurrentNoteBeforeAi = async (): Promise<boolean> => {
  if (!canReadAllNotes.value || !currentNoteInfo.noteId || saveNoteState.isSaved) {
    return true
  }

  const saved = await SaveNote(editor, {silent: true})
  if (!saved) {
    ElMessage.error('当前笔记保存失败，AI 请求未发送')
  }
  return saved
}

const send = async () => {
  if (aiChat.loading || sendPreparing.value) return
  if (Date.now() - lastSendAt.value < SEND_DEBOUNCE_MS) return
  if (!inputText.value.trim()) return
  lastSendAt.value = Date.now()
  resetMessageWindow()

  sendPreparing.value = true
  try {
    if (!await saveDirtyCurrentNoteBeforeAi()) {
      return
    }
    await aiChat.sendMessage(inputText.value, canReadAllNotes.value)
    inputText.value = ''
  } finally {
    sendPreparing.value = false
  }
}

const clearAll = async () => {
  if (isAiBusy.value) return
  await ElMessageBox.confirm('确定清空所有对话历史吗？', '提示', { type: 'warning' })
  await aiChat.clearAllSessions()
  historyVisible.value = false
  ElMessage.success('已清空')
}

const removeSession = async (sessionId: number) => {
  if (isAiBusy.value) return
  await aiChat.deleteSession(sessionId)
}

const openSession = async (sessionId: number) => {
  if (isAiBusy.value) return
  resetMessageWindow()
  await aiChat.loadSessionDetail(sessionId)
  historyVisible.value = false
}

const createNewSession = () => {
  if (isAiBusy.value) return
  resetMessageWindow()
  aiChat.createEmptySession()
}

onMounted(async () => {
  try {
    await aiPermissions.loadPermissions()
  } catch (error) {
    ElMessage.error('AI 权限加载失败，请稍后重试')
  }
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
            <el-button text circle size="small" title="历史记录" :disabled="isAiBusy">
              <el-icon><Clock /></el-icon>
            </el-button>
          </template>
          <AiSessionHistory
            :sessions="aiChat.sessions"
            :active-session-id="aiChat.activeSessionId"
            :visible="historyVisible"
            @clear="clearAll"
            @select="openSession"
            @remove="removeSession"
          />
        </el-popover>
        <el-button text circle size="small" title="新对话" :disabled="isAiBusy" @click="createNewSession">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>
    </header>

    <AiMessageList
      ref="messageListRef"
      :messages="aiChat.messages"
      :active-session-id="aiChat.activeSessionId"
      :get-timeline="aiChat.getTimeline"
    />

    <!-- Unified Composer -->
    <div class="composer-container">
      <div class="composer-box">
        <!-- Context Tags (Inside Composer) -->
        <div v-if="canReadAllNotes && aiChat.contextNotes.length" class="composer-contexts">
          <span v-for="note in aiChat.contextNotes" :key="note.noteId" class="context-chip">
            <el-icon><Link /></el-icon> {{ note.title }}
            <el-icon class="chip-close" @click="removeContextNote(note.noteId)"><Close /></el-icon>
          </span>
        </div>

        <el-input
            v-model="inputText"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 6 }"
            resize="none"
            :disabled="isAiBusy"
            placeholder="输入问题或需求，Enter 发送..."
            @keydown.enter.exact.prevent="send"
        />

        <div class="composer-toolbar">
          <div class="toolbar-left">
            <!-- 附件工具 -->
            <el-dropdown v-if="canReadAllNotes" trigger="click" placement="top-start">
              <el-button text circle size="small" title="附加笔记上下文" :disabled="isAiBusy">
                <el-icon><DocumentAdd /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="addCurrentNote">关联当前笔记</el-dropdown-item>
                  <el-dropdown-item @click="openNoteSelector">搜索其他笔记...</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

          </div>

          <div class="toolbar-right">
            <el-button v-if="aiChat.loading" type="info" circle size="small" @click="aiChat.stopChat()">
              <el-icon><VideoPause /></el-icon>
            </el-button>
            <el-button
              v-else
              type="primary"
              circle
              size="small"
              :loading="sendPreparing"
              :disabled="sendPreparing || !inputText.trim()"
              @click="send"
            >
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
      <div class="search-results" v-loading="noteSearching">
        <div v-if="!noteKeyword.trim()" class="empty-tip">输入关键词搜索笔记</div>
        <div v-else-if="!noteSearching && !noteSearchResults.length" class="empty-tip">未找到相关笔记</div>
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

  &:focus-within {
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

}

.composer-footer {
  text-align: center;
  font-size: 11px;
  color: #9ca3af;
  margin-top: 10px;
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
