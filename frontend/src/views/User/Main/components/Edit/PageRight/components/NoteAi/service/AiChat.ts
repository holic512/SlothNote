/**
 * @file AiChat
 * @project SlothNote
 * @module 用户端 / AI 会话状态
 * @description 管理 AI 会话、上下文、工具时间线及流式响应生命周期。
 * @logic 1. 以取消和序号隔离会话列表/详情请求；2. 按顺序同步上下文笔记快照；3. 发送 SSE 请求并节流合并增量；4. 同步工具执行、笔记变更和账号重置状态。
 * @dependencies Pinia, Axios, Store: token/currentNoteInfo
 * @index_tags AI 会话, SSE, 流式节流, 请求竞态, 上下文有序同步, 工具调用, 会话状态
 * @author holic512
 */
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import axios, { handleHttpAuthStatus } from '@/axios'
import { tokenStore } from '@/pinia/token'
import { useCurrentNoteInfoStore } from '@/views/User/Main/components/Edit/Pinia/currentNoteInfo'

export interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  messageType: 'chat' | 'explain' | 'polish' | 'summary'
  content: string
  renderedContent?: string
  status: 'completed' | 'streaming' | 'stopped' | 'failed'
  createdAt: string
}

export interface ChatSession {
  id: number
  title: string
  lastMessageAt: string
}

export interface ContextNote {
  noteId: number
  title: string
  summary?: string
  icon?: string | null
}

export interface ToolPlanPreview {
  tool: string
  argumentsJson: string
  requiresConfirmation: boolean
  writeTool: boolean
  summary: string
}

export interface AiTimelineItem {
  id: string
  kind: 'status' | 'tool_call' | 'tool_result'
  status?: string
  label?: string
  tool?: string
  summary?: string
  success?: boolean
  writeTool?: boolean
  createdAt: string
}

export type MessageType =
  | ChatMessage['messageType']
  | 'agent_update_summary'
  | 'agent_generate_summary_to_note'
  | 'agent_update_title'
  | 'agent_update_cover'

interface SendMessageOptions {
  allowCurrentNoteWrite?: boolean
  plannedToolName?: string
  plannedToolArgumentsJson?: string
}

const AI_DEBUG_PREFIX = '[NoteAI]'
const DEBUG_PREVIEW_LENGTH = 240
const STREAM_RENDER_INTERVAL_MS = 48

const debugLog = (...args: unknown[]) => {
  console.debug(AI_DEBUG_PREFIX, ...args)
}

const previewText = (value?: string | null) => {
  const normalized = (value || '').replace(/\s+/g, ' ').trim()
  if (normalized.length <= DEBUG_PREVIEW_LENGTH) {
    return normalized
  }
  return `${normalized.slice(0, DEBUG_PREVIEW_LENGTH)}...`
}

const buildRequestText = (messageType: MessageType, selectedText: string, inputText: string) => {
  const cleanSelectedText = selectedText.trim()
  const cleanInputText = inputText.trim()

  if (messageType === 'explain') {
    return cleanInputText || `请解释这段内容，尽量讲清概念、作用和上下文：\n\n${cleanSelectedText}`
  }
  if (messageType === 'polish') {
    return cleanInputText || `请润色这段内容，并说明你做了哪些优化：\n\n${cleanSelectedText}`
  }
  if (messageType === 'summary') {
    return cleanInputText || `请把这段内容概括成一段不超过 200 字的简介：\n\n${cleanSelectedText}`
  }
  if (messageType === 'agent_update_summary') {
    return cleanInputText || '请根据当前打开笔记的内容，生成一段适合作为笔记简介的摘要，并直接更新到当前笔记摘要。'
  }
  if (messageType === 'agent_generate_summary_to_note') {
    return cleanInputText || '请阅读当前打开笔记，先生成一段清晰摘要，再直接写入当前笔记摘要。'
  }
  if (messageType === 'agent_update_title') {
    return cleanInputText || '请根据当前打开笔记内容，为它拟一个更准确、简洁的标题，并直接更新当前笔记标题。'
  }
  if (messageType === 'agent_update_cover') {
    return cleanInputText || '请根据当前打开笔记内容和风格，从可用封面中选择一个最合适的封面，并直接更新当前笔记封面。'
  }
  return cleanInputText
}

const normalizeStoredMessageType = (messageType: MessageType): ChatMessage['messageType'] =>
  ['chat', 'explain', 'polish', 'summary'].includes(messageType)
    ? (messageType as ChatMessage['messageType'])
    : 'chat'

const nextTimelineId = () => `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
const nextTempMessageId = () => -Date.now() - Math.floor(Math.random() * 1000)
const WRITE_TOOLS = [
  'replace_selected_text',
  'append_to_current_note',
  'insert_after_selected_text',
  'update_current_note_title',
  'save_current_note_summary',
  'update_current_note_cover'
]

export const useAiChatStore = defineStore('aiChat', () => {
  const sessions = ref<ChatSession[]>([])
  const activeSessionId = ref<number | null>(null)
  const messages = ref<ChatMessage[]>([])
  const selectedText = ref('')
  const contextNotes = ref<ContextNote[]>([])
  const loading = ref(false)
  const streamingMessageId = ref<number | null>(null)
  const abortController = ref<AbortController | null>(null)
  const timelineByMessageId = ref<Record<number, AiTimelineItem[]>>({})
  const lastNoteMutation = ref<{ noteId: number; timestamp: number; summary: string } | null>(null)
  let sessionListRequestId = 0
  let sessionDetailRequestId = 0
  let sessionListController: AbortController | null = null
  let sessionDetailController: AbortController | null = null
  let contextSyncGeneration = 0
  let contextSyncQueue: Promise<void> = Promise.resolve()
  let contextMutationRevision = 0

  const activeSession = computed(() => sessions.value.find(session => session.id === activeSessionId.value) || null)

  const setSelectedText = (text: string) => {
    selectedText.value = text
  }

  const getSelectedText = () => selectedText.value

  const getTimeline = (messageId: number) => timelineByMessageId.value[messageId] || []

  const pushTimeline = (messageId: number, item: AiTimelineItem) => {
    timelineByMessageId.value = {
      ...timelineByMessageId.value,
      [messageId]: [...(timelineByMessageId.value[messageId] || []), item]
    }
  }

  const remapTimeline = (fromMessageId: number, toMessageId: number) => {
    if (fromMessageId === toMessageId || !timelineByMessageId.value[fromMessageId]) {
      return
    }
    timelineByMessageId.value = {
      ...timelineByMessageId.value,
      [toMessageId]: [...(timelineByMessageId.value[toMessageId] || []), ...timelineByMessageId.value[fromMessageId]]
    }
    delete timelineByMessageId.value[fromMessageId]
  }

  const setActiveSessionId = (sessionId: number | null) => {
    if (activeSessionId.value !== sessionId) {
      sessionDetailRequestId += 1
      sessionDetailController?.abort()
      sessionDetailController = null
    }
    activeSessionId.value = sessionId
  }

  const syncContextNotes = async () => {
    const sessionId = activeSessionId.value
    if (!sessionId) {
      return
    }

    const generation = contextSyncGeneration
    const noteIds = contextNotes.value.map(note => note.noteId)
    const syncTask = contextSyncQueue
      .catch(() => undefined)
      .then(async () => {
        if (generation !== contextSyncGeneration) return
        await axios.put(`user/ai/sessions/${sessionId}/context-notes`, { noteIds })
      })

    contextSyncQueue = syncTask
    await syncTask
  }

  const loadSessions = async () => {
    sessionListController?.abort()
    const controller = new AbortController()
    sessionListController = controller
    const requestId = ++sessionListRequestId

    try {
      const response = await axios.get('user/ai/sessions', { signal: controller.signal })
      if (controller.signal.aborted || requestId !== sessionListRequestId) return

      sessions.value = response.data?.data || []
      if (!activeSessionId.value && sessions.value.length > 0) {
        await loadSessionDetail(sessions.value[0].id)
      }
    } catch (error) {
      if (controller.signal.aborted || requestId !== sessionListRequestId) return
      throw error
    } finally {
      if (sessionListController === controller) sessionListController = null
    }
  }

  const loadSessionDetail = async (sessionId: number) => {
    sessionDetailController?.abort()
    const controller = new AbortController()
    sessionDetailController = controller
    const requestId = ++sessionDetailRequestId

    try {
      const response = await axios.get(`user/ai/sessions/${sessionId}/messages`, {
        signal: controller.signal
      })
      if (controller.signal.aborted || requestId !== sessionDetailRequestId) return

      const detail = response.data?.data
      activeSessionId.value = sessionId
      messages.value = (detail?.messages || []).map((message: ChatMessage) => ({
        ...message,
        renderedContent: message.content
      }))
      contextMutationRevision += 1
      contextNotes.value = detail?.contextNotes || []
    } catch (error) {
      if (controller.signal.aborted || requestId !== sessionDetailRequestId) return
      throw error
    } finally {
      if (sessionDetailController === controller) sessionDetailController = null
    }
  }

  const createEmptySession = () => {
    sessionDetailRequestId += 1
    sessionDetailController?.abort()
    sessionDetailController = null
    activeSessionId.value = null
    messages.value = []
    contextMutationRevision += 1
    contextNotes.value = []
    streamingMessageId.value = null
    selectedText.value = ''
    timelineByMessageId.value = {}
  }

  const resetClientState = () => {
    abortController.value?.abort()
    abortController.value = null
    sessionListRequestId += 1
    sessionDetailRequestId += 1
    sessionListController?.abort()
    sessionDetailController?.abort()
    sessionListController = null
    sessionDetailController = null
    contextSyncGeneration += 1
    contextSyncQueue = Promise.resolve()
    contextMutationRevision += 1
    sessions.value = []
    activeSessionId.value = null
    messages.value = []
    selectedText.value = ''
    contextNotes.value = []
    loading.value = false
    streamingMessageId.value = null
    timelineByMessageId.value = {}
    lastNoteMutation.value = null
  }

  const addContextNote = async (note: ContextNote) => {
    if (contextNotes.value.some(item => item.noteId === note.noteId)) {
      return
    }
    const previousNotes = contextNotes.value
    const mutationRevision = ++contextMutationRevision
    contextNotes.value = [...previousNotes, note]
    try {
      await syncContextNotes()
    } catch (error) {
      if (mutationRevision === contextMutationRevision) contextNotes.value = previousNotes
      throw error
    }
  }

  const removeContextNote = async (noteId: number) => {
    const previousNotes = contextNotes.value
    const mutationRevision = ++contextMutationRevision
    contextNotes.value = previousNotes.filter(note => note.noteId !== noteId)
    try {
      await syncContextNotes()
    } catch (error) {
      if (mutationRevision === contextMutationRevision) contextNotes.value = previousNotes
      throw error
    }
  }

  const clearAllSessions = async () => {
    sessionListRequestId += 1
    sessionDetailRequestId += 1
    sessionListController?.abort()
    sessionDetailController?.abort()
    sessionListController = null
    sessionDetailController = null
    contextSyncGeneration += 1
    contextSyncQueue = Promise.resolve()
    contextMutationRevision += 1
    await axios.delete('user/ai/sessions')
    sessions.value = []
    createEmptySession()
  }

  const deleteSession = async (sessionId: number) => {
    await axios.delete(`user/ai/sessions/${sessionId}`)
    sessions.value = sessions.value.filter(session => session.id !== sessionId)
    if (activeSessionId.value === sessionId) {
      sessionDetailRequestId += 1
      sessionDetailController?.abort()
      sessionDetailController = null
      if (sessions.value.length > 0) {
        await loadSessionDetail(sessions.value[0].id)
      } else {
        createEmptySession()
      }
    }
  }

  const stopChat = async () => {
    if (!abortController.value && !streamingMessageId.value) {
      return
    }
    const stoppedMessageId = streamingMessageId.value
    debugLog('stop requested', {
      sessionId: activeSessionId.value,
      assistantMessageId: stoppedMessageId
    })
    abortController.value?.abort()
    const assistantMessage = messages.value.find(message => message.id === stoppedMessageId)
    if (assistantMessage?.status === 'streaming') {
      assistantMessage.status = 'stopped'
      assistantMessage.renderedContent = assistantMessage.content
    }
    try {
      await axios.post('user/ai/stop', {
        assistantMessageId: stoppedMessageId,
        sessionId: activeSessionId.value
      })
    } finally {
      loading.value = false
      abortController.value = null
      streamingMessageId.value = null
    }
  }

  const previewToolPlan = async (inputText: string, messageType: MessageType = 'chat') => {
    const currentNoteInfo = useCurrentNoteInfoStore()
    const text = buildRequestText(messageType, selectedText.value, inputText)
    if (!text.trim()) {
      return null
    }
    const response = await axios.post('user/ai/tools/plan', {
      sessionId: activeSessionId.value,
      text,
      messageType,
      selectedText: selectedText.value || null,
      contextNoteIds: contextNotes.value.map(note => note.noteId),
      currentNoteId: currentNoteInfo.noteId,
      currentNoteTitle: currentNoteInfo.noteName || null,
      currentNoteCover: currentNoteInfo.cover || null
    })
    return response.data?.data as ToolPlanPreview | null
  }

  const sendMessage = async (inputText: string, messageType: MessageType = 'chat', options: SendMessageOptions = {}) => {
    const currentNoteInfo = useCurrentNoteInfoStore()
    const text = buildRequestText(messageType, selectedText.value, inputText)
    if (!text.trim()) {
      return
    }

    loading.value = true
    abortController.value = new AbortController()
    const requestPayload = {
      sessionId: activeSessionId.value,
      text,
      messageType,
      selectedText: selectedText.value || null,
      contextNoteIds: contextNotes.value.map(note => note.noteId),
      currentNoteId: currentNoteInfo.noteId,
      currentNoteTitle: currentNoteInfo.noteName || null,
      currentNoteCover: currentNoteInfo.cover || null,
      allowCurrentNoteWrite: options.allowCurrentNoteWrite === true,
      plannedToolName: options.plannedToolName || null,
      plannedToolArgumentsJson: options.plannedToolArgumentsJson || null
    }
    debugLog('send start', {
      sessionId: activeSessionId.value,
      messageType,
      textLength: text.length,
      contextNoteIds: contextNotes.value.map(note => note.noteId),
      currentNoteId: currentNoteInfo.noteId,
      selectedTextLength: selectedText.value.trim().length,
      textPreview: previewText(text)
    })

    let noteMutated = false
    const tempUserMessageId = nextTempMessageId()
    const tempAssistantMessageId = nextTempMessageId()
    let pendingDeltaMessageId: number | null = null
    let pendingDeltaContent = ''
    let streamRenderTimer: ReturnType<typeof setTimeout> | undefined

    const flushPendingDelta = () => {
      if (streamRenderTimer) {
        clearTimeout(streamRenderTimer)
        streamRenderTimer = undefined
      }
      if (pendingDeltaMessageId == null || !pendingDeltaContent) {
        pendingDeltaMessageId = null
        pendingDeltaContent = ''
        return
      }

      const assistantMessage = messages.value.find(message => message.id === pendingDeltaMessageId)
      if (assistantMessage) {
        assistantMessage.content += pendingDeltaContent
        assistantMessage.renderedContent = assistantMessage.content
      }
      pendingDeltaMessageId = null
      pendingDeltaContent = ''
    }

    const queueDelta = (messageId: number, content: string) => {
      if (!content) return
      if (pendingDeltaMessageId != null && pendingDeltaMessageId !== messageId) {
        flushPendingDelta()
      }
      pendingDeltaMessageId = messageId
      pendingDeltaContent += content
      if (!streamRenderTimer) {
        streamRenderTimer = setTimeout(flushPendingDelta, STREAM_RENDER_INTERVAL_MS)
      }
    }

    messages.value.push({
      id: tempUserMessageId,
      role: 'user',
      messageType: normalizeStoredMessageType(messageType),
      content: text,
      renderedContent: text,
      status: 'completed',
      createdAt: new Date().toISOString()
    })
    messages.value.push({
      id: tempAssistantMessageId,
      role: 'assistant',
      messageType: normalizeStoredMessageType(messageType),
      content: '',
      renderedContent: '',
      status: 'streaming',
      createdAt: new Date().toISOString()
    })
    streamingMessageId.value = tempAssistantMessageId

    try {
      const userToken = tokenStore().getUserToken()
      const response = await fetch(`${axios.defaults.baseURL}user/ai/chat`, {
        method: 'POST',
        headers: {
          Accept: 'text/event-stream',
          'Content-Type': 'application/json',
          satoken: userToken || ''
        },
        body: JSON.stringify(requestPayload),
        signal: abortController.value.signal
      })

      if (!response.ok || !response.body) {
        handleHttpAuthStatus(response.status, 'user/ai/chat')
        const errorText = await response.text().catch(() => '')
        throw new Error(errorText || `HTTP ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      const processChunk = (rawChunk: string) => {
        const lines = rawChunk.split(/\r?\n/)
        for (const line of lines) {
          if (!line.startsWith('data:')) {
            continue
          }
          const payloadText = line.slice(5).trim()
          if (!payloadText) {
            continue
          }

          let payload: any
          try {
            payload = JSON.parse(payloadText)
          } catch {
            continue
          }

          if (payload.type === 'session') {
            if (payload.sessionId) {
              activeSessionId.value = payload.sessionId
              if (!sessions.value.some(session => session.id === payload.sessionId)) {
                sessions.value.unshift({
                  id: payload.sessionId,
                  title: text.replace(/\s+/g, ' ').trim().slice(0, 24) || '新对话',
                  lastMessageAt: new Date().toISOString()
                })
              }
            }
            if (payload.userMessageId) {
              const tempUserMessage = messages.value.find(message => message.id === tempUserMessageId)
              if (tempUserMessage) {
                tempUserMessage.id = payload.userMessageId
              } else if (!messages.value.some(message => message.id === payload.userMessageId)) {
                messages.value.push({
                  id: payload.userMessageId,
                  role: 'user',
                  messageType: normalizeStoredMessageType(messageType),
                  content: text,
                  renderedContent: text,
                  status: 'completed',
                  createdAt: new Date().toISOString()
                })
              }
            }
            if (payload.assistantMessageId) {
              const tempAssistantMessage = messages.value.find(message => message.id === tempAssistantMessageId)
              if (tempAssistantMessage) {
                tempAssistantMessage.id = payload.assistantMessageId
                remapTimeline(tempAssistantMessageId, payload.assistantMessageId)
              } else if (!messages.value.some(message => message.id === payload.assistantMessageId)) {
                messages.value.push({
                  id: payload.assistantMessageId,
                  role: 'assistant',
                  messageType: normalizeStoredMessageType(messageType),
                  content: '',
                  renderedContent: '',
                  status: 'streaming',
                  createdAt: new Date().toISOString()
                })
              }
              streamingMessageId.value = payload.assistantMessageId
            }
          }

          if (payload.type === 'status' && payload.assistantMessageId) {
            pushTimeline(payload.assistantMessageId, {
              id: nextTimelineId(),
              kind: 'status',
              status: payload.status,
              label: payload.label,
              createdAt: new Date().toISOString()
            })
          }

          if (payload.type === 'tool_call' && payload.assistantMessageId) {
            pushTimeline(payload.assistantMessageId, {
              id: nextTimelineId(),
              kind: 'tool_call',
              tool: payload.tool,
              summary: payload.summary,
              writeTool: payload.writeTool,
              createdAt: new Date().toISOString()
            })
          }

          if (payload.type === 'tool_result' && payload.assistantMessageId) {
            pushTimeline(payload.assistantMessageId, {
              id: nextTimelineId(),
              kind: 'tool_result',
              tool: payload.tool,
              summary: payload.summary,
              success: !!payload.success,
              createdAt: new Date().toISOString()
            })
            if (payload.success && currentNoteInfo.noteId && WRITE_TOOLS.includes(payload.tool)) {
              noteMutated = true
              selectedText.value = ''
              if (payload.tool === 'update_current_note_title' && requestPayload.plannedToolArgumentsJson) {
                try {
                  const args = JSON.parse(requestPayload.plannedToolArgumentsJson)
                  if (typeof args?.title === 'string' && args.title.trim()) {
                    currentNoteInfo.noteName = args.title.trim()
                  }
                } catch {
                  // ignore invalid preview payload
                }
              }
              if (payload.tool === 'update_current_note_cover' && requestPayload.plannedToolArgumentsJson) {
                try {
                  const args = JSON.parse(requestPayload.plannedToolArgumentsJson)
                  currentNoteInfo.cover = typeof args?.cover === 'string' && args.cover.trim() ? args.cover.trim() : null
                } catch {
                  currentNoteInfo.cover = null
                }
              }
              lastNoteMutation.value = {
                noteId: currentNoteInfo.noteId,
                timestamp: Date.now(),
                summary: payload.summary || 'AI 已修改当前笔记'
              }
            }
          }

          if (payload.type === 'delta') {
            queueDelta(payload.assistantMessageId, payload.content || '')
          }

          if (payload.type === 'done') {
            flushPendingDelta()
            const assistantMessage = messages.value.find(message => message.id === payload.assistantMessageId)
            if (assistantMessage) {
              assistantMessage.status = payload.status || 'completed'
              assistantMessage.renderedContent = assistantMessage.content
            }
            streamingMessageId.value = null
          }

          if (payload.type === 'error') {
            flushPendingDelta()
            const assistantMessage = messages.value.find(message => message.id === payload.assistantMessageId)
            const errorMessage = payload.message || 'AI 回复失败，请重试。'
            if (assistantMessage) {
              assistantMessage.status = 'failed'
              assistantMessage.content = assistantMessage.content || errorMessage
              assistantMessage.renderedContent = assistantMessage.content
            } else {
              messages.value.push({
                id: Date.now(),
                role: 'assistant',
                messageType: normalizeStoredMessageType(messageType),
                content: errorMessage,
                renderedContent: errorMessage,
                status: 'failed',
                createdAt: new Date().toISOString()
              })
            }
            streamingMessageId.value = null
          }
        }
      }

      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          break
        }
        buffer += decoder.decode(value, { stream: true })
        const chunks = buffer.split(/\r?\n\r?\n/)
        buffer = chunks.pop() || ''

        for (const rawChunk of chunks) {
          processChunk(rawChunk)
        }
      }

      if (buffer.trim()) {
        processChunk(buffer)
      }
      flushPendingDelta()

      await loadSessions()
      if (noteMutated && currentNoteInfo.noteId && currentNoteInfo.noteName) {
        currentNoteInfo.noteName = currentNoteInfo.noteName
      }
    } catch (error: any) {
      flushPendingDelta()
      debugLog('send error', {
        name: error?.name,
        message: error?.message,
        sessionId: activeSessionId.value
      })
      if (error?.name !== 'AbortError') {
        const assistantMessage = streamingMessageId.value
          ? messages.value.find(message => message.id === streamingMessageId.value)
          : null
        if (assistantMessage) {
          assistantMessage.status = 'failed'
          assistantMessage.content = assistantMessage.content || 'AI 回复失败，请重试。'
          assistantMessage.renderedContent = assistantMessage.content
        } else {
          messages.value.push({
            id: Date.now(),
            role: 'assistant',
            messageType: normalizeStoredMessageType(messageType),
            content: 'AI 回复失败，请重试。',
            renderedContent: 'AI 回复失败，请重试。',
            status: 'failed',
            createdAt: new Date().toISOString()
          })
        }
      } else {
        messages.value = messages.value.filter(message => message.id !== tempUserMessageId && message.id !== tempAssistantMessageId)
      }
    } finally {
      flushPendingDelta()
      loading.value = false
      abortController.value = null
      streamingMessageId.value = null
    }
  }

  return {
    sessions,
    activeSessionId,
    activeSession,
    messages,
    selectedText,
    contextNotes,
    loading,
    streamingMessageId,
    timelineByMessageId,
    lastNoteMutation,
    setSelectedText,
    getSelectedText,
    getTimeline,
    setActiveSessionId,
    loadSessions,
    loadSessionDetail,
    createEmptySession,
    resetClientState,
    addContextNote,
    removeContextNote,
    clearAllSessions,
    deleteSession,
    previewToolPlan,
    sendMessage,
    stopChat
  }
})
