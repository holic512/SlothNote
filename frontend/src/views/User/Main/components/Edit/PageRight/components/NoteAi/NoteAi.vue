<!--
@file NoteAi
@project SlothNote
@module 用户端 / 笔记 AI 面板
@description 编排 AI 会话、上下文笔记选择、消息输入和授权写入交互。
@logic 1. 组合会话历史与消息列表组件；2. 调用搜索 composable 管理上下文选择；3. 锁定预览准备阶段并校验输入状态；4. 对写入型工具保存笔记、会话、上下文快照并执行二次授权。
@dependencies Store: AiChat/currentNoteInfo, Components: AiMessageList/AiSessionHistory, Composable: useContextNoteSearch
@index_tags AI 面板, 会话编排, 上下文选择, 写入授权, 授权快照, 重复提交保护
@author holic512
-->
<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatLineSquare,
  Close,
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
  MoreFilled,
  Brush,
  Picture
} from '@element-plus/icons-vue'
import {
  useAiChatStore,
  type ContextNote,
  type MessageType,
  type ToolPlanPreview
} from '@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat'
import { useCurrentNoteInfoStore } from '@/views/User/Main/components/Edit/Pinia/currentNoteInfo'
import AiMessageList from './components/AiMessageList.vue'
import AiSessionHistory from './components/AiSessionHistory.vue'
import { useContextNoteSearch } from './composables/useContextNoteSearch'

const aiChat = useAiChatStore()
const currentNoteInfo = useCurrentNoteInfoStore()
const inputText = ref('')
const selectedAction = ref<MessageType>('chat')
const messageListRef = ref<{ resetWindow: () => void } | null>(null)
const {
  noteSearchVisible,
  noteKeyword,
  noteSearching,
  noteSearchResults,
  openNoteSearch
} = useContextNoteSearch()
const quickMenuVisible = ref(false)
const historyVisible = ref(false)
const authorizeDialogVisible = ref(false)
const authorizeSubmitting = ref(false)
const sendPreparing = ref(false)
const pendingAuthorizePreview = ref<ToolPlanPreview | null>(null)
const pendingAuthorizeInput = ref('')
const pendingAuthorizeAction = ref<MessageType>('chat')
const pendingAuthorizeNoteId = ref<number | null>(null)
const pendingAuthorizeSessionId = ref<number | null>(null)
const pendingAuthorizeContextKey = ref('')
const pendingAuthorizeSelectedText = ref('')
const lastSendAt = ref(0)
const SEND_DEBOUNCE_MS = 500

const editor = defineModel<any>()
const hasSelectedText = computed(() => !!aiChat.getSelectedText().trim())
const isAiBusy = computed(() =>
  aiChat.loading || sendPreparing.value || authorizeSubmitting.value || authorizeDialogVisible.value
)

const getContextKey = () => aiChat.contextNotes.map(note => note.noteId).join(',')

const matchesAuthorizeSnapshot = (
  noteId: number | null,
  sessionId: number | null,
  contextKey: string,
  selectedText: string
) => currentNoteInfo.noteId === noteId
  && aiChat.activeSessionId === sessionId
  && getContextKey() === contextKey
  && aiChat.getSelectedText() === selectedText

const resetMessageWindow = () => {
  messageListRef.value?.resetWindow()
}

const insertCodeBlock = (code: string, language?: string) => {
  if (!editor?.value) return
  const content = {
    type: 'codeBlock',
    attrs: language ? { language } : {},
    content: code ? [{ type: 'text', text: code }] : []
  }
  editor.value.chain().focus().insertContent(content).run()
  ElMessage.success('代码块已插入到笔记')
}

const openNoteSelector = () => {
  if (isAiBusy.value) return
  quickMenuVisible.value = false
  openNoteSearch()
}

const addCurrentNote = async () => {
  if (isAiBusy.value) return
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
  if (isAiBusy.value) return
  try {
    await aiChat.addContextNote(note)
    noteSearchVisible.value = false
  } catch (error) {
    console.warn('[NoteAI] add context note failed', error)
    ElMessage.error('关联笔记失败')
  }
}

const removeContextNote = async (noteId: number) => {
  if (isAiBusy.value) return
  try {
    await aiChat.removeContextNote(noteId)
  } catch (error) {
    console.warn('[NoteAI] remove context note failed', error)
    ElMessage.error('移除关联笔记失败')
  }
}

const actionLabelMap: Record<MessageType, string> = {
  chat: '智能指令',
  explain: '解释',
  polish: '润色',
  summary: '摘要',
  agent_update_summary: '修改简介',
  agent_generate_summary_to_note: '生成摘要',
  agent_update_title: '修改标题',
  agent_update_cover: '修改封面'
}

const requireCurrentNoteForAgent = (action: string) => {
  if (!action.startsWith('agent_')) {
    return true
  }
  if (currentNoteInfo.noteId) {
    return true
  }
  ElMessage.warning('请先打开一篇笔记，再使用笔记 agent 功能')
  return false
}

const selectQuickAction = (action: MessageType) => {
  if (isAiBusy.value) return
  if (!requireCurrentNoteForAgent(action)) {
    return
  }
  quickMenuVisible.value = false
  selectedAction.value = action
}

const resetAuthorizeDialog = () => {
  authorizeDialogVisible.value = false
  authorizeSubmitting.value = false
  pendingAuthorizePreview.value = null
  pendingAuthorizeInput.value = ''
  pendingAuthorizeAction.value = 'chat'
  pendingAuthorizeNoteId.value = null
  pendingAuthorizeSessionId.value = null
  pendingAuthorizeContextKey.value = ''
  pendingAuthorizeSelectedText.value = ''
}

const executeSend = async (
  text: string,
  action: MessageType,
  preview: ToolPlanPreview | null = null
) => {
  const options: { allowCurrentNoteWrite?: boolean; plannedToolName?: string; plannedToolArgumentsJson?: string } = {}
  if (preview?.requiresConfirmation && preview.writeTool) {
    options.allowCurrentNoteWrite = true
    options.plannedToolName = preview.tool
    options.plannedToolArgumentsJson = preview.argumentsJson
  }

  await aiChat.sendMessage(text, action, options)
  inputText.value = ''
  if (selectedAction.value !== 'chat') {
    selectedAction.value = 'chat'
  }
}

const confirmAuthorizeAndSend = async () => {
  if (!pendingAuthorizePreview.value || authorizeSubmitting.value || aiChat.loading) {
    return
  }

  if (!matchesAuthorizeSnapshot(
    pendingAuthorizeNoteId.value,
    pendingAuthorizeSessionId.value,
    pendingAuthorizeContextKey.value,
    pendingAuthorizeSelectedText.value
  )) {
    ElMessage.warning('笔记、会话或上下文已变化，请重新发起 AI 指令')
    resetAuthorizeDialog()
    return
  }

  authorizeSubmitting.value = true
  try {
    await executeSend(
      pendingAuthorizeInput.value,
      pendingAuthorizeAction.value,
      pendingAuthorizePreview.value
    )
    resetAuthorizeDialog()
  } finally {
    authorizeSubmitting.value = false
  }
}

const send = async () => {
  if (aiChat.loading || sendPreparing.value) return
  if (Date.now() - lastSendAt.value < SEND_DEBOUNCE_MS) return
  if (!inputText.value.trim() && selectedAction.value === 'chat') return
  if (['explain', 'polish', 'summary'].includes(selectedAction.value) && !hasSelectedText.value && !inputText.value.trim()) {
    ElMessage.warning('请先选中文本或输入内容')
    return
  }
  if (!requireCurrentNoteForAgent(selectedAction.value)) {
    return
  }
  lastSendAt.value = Date.now()
  resetMessageWindow()

  sendPreparing.value = true
  try {
    if (!selectedAction.value.startsWith('agent_')) {
      await executeSend(inputText.value, selectedAction.value, null)
      return
    }

    const previewNoteId = currentNoteInfo.noteId
    const previewSessionId = aiChat.activeSessionId
    const previewContextKey = getContextKey()
    const previewSelectedText = aiChat.getSelectedText()
    const previewInput = inputText.value
    const previewAction = selectedAction.value
    let preview: ToolPlanPreview | null = null
    try {
      preview = await aiChat.previewToolPlan(previewInput, previewAction)
    } catch (error) {
      console.warn('[NoteAI] preview tool plan failed, fallback to direct send', error)
    }

    if (
      !matchesAuthorizeSnapshot(previewNoteId, previewSessionId, previewContextKey, previewSelectedText)
      || inputText.value !== previewInput
      || selectedAction.value !== previewAction
    ) {
      ElMessage.warning('笔记、会话或上下文已变化，请重新发起 AI 指令')
      return
    }

    if (preview?.requiresConfirmation && preview.writeTool) {
      if (!currentNoteInfo.noteId) {
        ElMessage.warning('当前没有打开的笔记，无法执行 AI 写入')
        return
      }
      pendingAuthorizePreview.value = preview
      pendingAuthorizeInput.value = previewInput
      pendingAuthorizeAction.value = previewAction
      pendingAuthorizeNoteId.value = previewNoteId
      pendingAuthorizeSessionId.value = previewSessionId
      pendingAuthorizeContextKey.value = previewContextKey
      pendingAuthorizeSelectedText.value = previewSelectedText
      authorizeDialogVisible.value = true
      return
    }

    await executeSend(previewInput, previewAction, null)
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
      @insert-code-block="insertCodeBlock"
    />

    <!-- Unified Composer -->
    <div class="composer-container">
      <div class="composer-box" :class="{ 'is-focused': quickMenuVisible }">
        <!-- Context Tags (Inside Composer) -->
        <div v-if="aiChat.contextNotes.length" class="composer-contexts">
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
            :placeholder="selectedAction === 'chat' ? '输入指令，Enter 发送...' : `当前模式: ${actionLabelMap[selectedAction]}，可输入额外要求`"
            @keydown.enter.exact.prevent="send"
        />

        <div class="composer-toolbar">
          <div class="toolbar-left">
            <!-- 附件工具 -->
            <el-dropdown trigger="click" placement="top-start">
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

            <!-- 快捷指令工具 -->
            <el-dropdown trigger="click" placement="top-start" @command="selectQuickAction">
              <el-button text round size="small" class="mode-btn" :disabled="isAiBusy">
                <el-icon><MoreFilled /></el-icon>
                {{ actionLabelMap[selectedAction] }}
              </el-button>
              <template #dropdown>
                <el-dropdown-menu class="minimal-menu">
                  <el-dropdown-item command="chat"><el-icon><ChatLineSquare /></el-icon> 对话 (默认)</el-dropdown-item>
                  <el-dropdown-item command="explain" divided><el-icon><Document /></el-icon> 解释选中内容</el-dropdown-item>
                  <el-dropdown-item command="polish"><el-icon><Edit /></el-icon> 润色选中内容</el-dropdown-item>
                  <el-dropdown-item command="summary"><el-icon><Document /></el-icon> 生成摘要</el-dropdown-item>
                  <el-dropdown-item command="agent_update_summary" divided><el-icon><Brush /></el-icon> Agent 修改简介</el-dropdown-item>
                  <el-dropdown-item command="agent_generate_summary_to_note"><el-icon><Document /></el-icon> Agent 生成摘要并写入</el-dropdown-item>
                  <el-dropdown-item command="agent_update_title"><el-icon><Edit /></el-icon> Agent 修改标题</el-dropdown-item>
                  <el-dropdown-item command="agent_update_cover"><el-icon><Picture /></el-icon> Agent 修改封面</el-dropdown-item>
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
              :disabled="sendPreparing || (!inputText.trim() && !hasSelectedText)"
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

    <el-dialog
      v-model="authorizeDialogVisible"
      width="460px"
      class="ai-authorize-dialog"
      :show-close="false"
      align-center
      @closed="resetAuthorizeDialog"
    >
      <template #header>
        <div class="authorize-header">
          <div class="authorize-icon">
            <el-icon><Edit /></el-icon>
          </div>
          <div>
            <div class="authorize-title">授权 AI 编辑当前笔记</div>
            <div class="authorize-subtitle">本次操作会写入你当前打开的笔记</div>
          </div>
        </div>
      </template>

      <div class="authorize-body">
        <div class="authorize-card">
          <div class="authorize-label">目标笔记</div>
          <div class="authorize-value">{{ currentNoteInfo.noteName || '当前笔记' }}</div>
          <div class="authorize-meta">ID: {{ currentNoteInfo.noteId || '-' }}</div>
        </div>

        <div class="authorize-card" v-if="pendingAuthorizePreview">
          <div class="authorize-label">计划操作</div>
          <div class="authorize-value">{{ pendingAuthorizePreview.summary }}</div>
          <div class="authorize-meta">工具: {{ pendingAuthorizePreview.tool }}</div>
        </div>

        <div class="authorize-tip">
          AI 只会修改当前打开的笔记，不会写入其他笔记。你可以在执行后继续检查并手动撤销。
        </div>
      </div>

      <template #footer>
        <div class="authorize-footer">
          <el-button @click="resetAuthorizeDialog">取消</el-button>
          <el-button type="primary" :loading="authorizeSubmitting" @click="confirmAuthorizeAndSend">
            允许本次编辑
          </el-button>
        </div>
      </template>
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

.authorize-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.authorize-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eff6ff;
  color: #2563eb;
  font-size: 18px;
}

.authorize-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.authorize-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.authorize-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.authorize-card {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  background: #f9fafb;
}

.authorize-label {
  font-size: 12px;
  color: #6b7280;
}

.authorize-value {
  margin-top: 6px;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  line-height: 1.5;
}

.authorize-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #6b7280;
}

.authorize-tip {
  padding: 12px 14px;
  border-radius: 12px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 12px;
  line-height: 1.6;
}

.authorize-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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
