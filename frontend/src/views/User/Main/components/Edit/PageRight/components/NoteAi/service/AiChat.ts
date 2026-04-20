import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import axios from '@/axios'
import { tokenStore } from '@/pinia/token'

export interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  messageType: 'chat' | 'explain' | 'polish' | 'summary'
  content: string
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

type MessageType = ChatMessage['messageType']

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
  return cleanInputText
}

export const useAiChatStore = defineStore('aiChat', () => {
  const sessions = ref<ChatSession[]>([])
  const activeSessionId = ref<number | null>(null)
  const messages = ref<ChatMessage[]>([])
  const selectedText = ref('')
  const contextNotes = ref<ContextNote[]>([])
  const loading = ref(false)
  const streamingMessageId = ref<number | null>(null)
  const abortController = ref<AbortController | null>(null)

  const activeSession = computed(() => sessions.value.find(session => session.id === activeSessionId.value) || null)

  const setSelectedText = (text: string) => {
    selectedText.value = text
  }

  const getSelectedText = () => selectedText.value

  const setActiveSessionId = (sessionId: number | null) => {
    activeSessionId.value = sessionId
  }

  const syncContextNotes = async () => {
    if (!activeSessionId.value) {
      return
    }
    await axios.put(`user/ai/sessions/${activeSessionId.value}/context-notes`, {
      noteIds: contextNotes.value.map(note => note.noteId)
    })
  }

  const loadSessions = async () => {
    const response = await axios.get('user/ai/sessions')
    sessions.value = response.data?.data || []
    if (!activeSessionId.value && sessions.value.length > 0) {
      await loadSessionDetail(sessions.value[0].id)
    }
  }

  const loadSessionDetail = async (sessionId: number) => {
    const response = await axios.get(`user/ai/sessions/${sessionId}/messages`)
    const detail = response.data?.data
    activeSessionId.value = sessionId
    messages.value = detail?.messages || []
    contextNotes.value = detail?.contextNotes || []
  }

  const createEmptySession = () => {
    activeSessionId.value = null
    messages.value = []
    contextNotes.value = []
    streamingMessageId.value = null
    selectedText.value = ''
  }

  const addContextNote = async (note: ContextNote) => {
    if (contextNotes.value.some(item => item.noteId === note.noteId)) {
      return
    }
    contextNotes.value.push(note)
    await syncContextNotes()
  }

  const removeContextNote = async (noteId: number) => {
    contextNotes.value = contextNotes.value.filter(note => note.noteId !== noteId)
    await syncContextNotes()
  }

  const clearAllSessions = async () => {
    await axios.delete('user/ai/sessions')
    sessions.value = []
    createEmptySession()
  }

  const deleteSession = async (sessionId: number) => {
    await axios.delete(`user/ai/sessions/${sessionId}`)
    sessions.value = sessions.value.filter(session => session.id !== sessionId)
    if (activeSessionId.value === sessionId) {
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
    abortController.value?.abort()
    try {
      await axios.post('user/ai/stop', {
        assistantMessageId: streamingMessageId.value,
        sessionId: activeSessionId.value
      })
    } finally {
      const assistantMessage = messages.value.find(message => message.id === streamingMessageId.value)
      if (assistantMessage && assistantMessage.status === 'streaming') {
        assistantMessage.status = 'stopped'
      }
      loading.value = false
      abortController.value = null
      streamingMessageId.value = null
    }
  }

  const sendMessage = async (inputText: string, messageType: MessageType = 'chat') => {
    const text = buildRequestText(messageType, selectedText.value, inputText)
    if (!text.trim()) {
      return
    }

    loading.value = true
    abortController.value = new AbortController()

    try {
      const userToken = tokenStore().getUserToken()
      const response = await fetch(`${axios.defaults.baseURL}user/ai/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          satoken: userToken || ''
        },
        body: JSON.stringify({
          sessionId: activeSessionId.value,
          text,
          messageType,
          selectedText: selectedText.value || null,
          contextNoteIds: contextNotes.value.map(note => note.noteId)
        }),
        signal: abortController.value.signal
      })

      if (!response.ok || !response.body) {
        throw new Error(`HTTP ${response.status}`)
      }

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) {
          break
        }
        buffer += decoder.decode(value, { stream: true })
        const chunks = buffer.split('\n\n')
        buffer = chunks.pop() || ''

        for (const rawChunk of chunks) {
          const lines = rawChunk.split('\n')
          for (const line of lines) {
            if (!line.startsWith('data:')) {
              continue
            }
            const payloadText = line.slice(5).trim()
            if (!payloadText) {
              continue
            }
            const payload = JSON.parse(payloadText)

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
              if (payload.userMessageId && !messages.value.some(message => message.id === payload.userMessageId)) {
                messages.value.push({
                  id: payload.userMessageId,
                  role: 'user',
                  messageType,
                  content: text,
                  status: 'completed',
                  createdAt: new Date().toISOString()
                })
              }
              if (payload.assistantMessageId && !messages.value.some(message => message.id === payload.assistantMessageId)) {
                messages.value.push({
                  id: payload.assistantMessageId,
                  role: 'assistant',
                  messageType,
                  content: '',
                  status: 'streaming',
                  createdAt: new Date().toISOString()
                })
                streamingMessageId.value = payload.assistantMessageId
              }
            }

            if (payload.type === 'delta') {
              const assistantMessage = messages.value.find(message => message.id === payload.assistantMessageId)
              if (assistantMessage) {
                assistantMessage.content += payload.content || ''
              }
            }

            if (payload.type === 'done') {
              const assistantMessage = messages.value.find(message => message.id === payload.assistantMessageId)
              if (assistantMessage) {
                assistantMessage.status = payload.status || 'completed'
              }
              streamingMessageId.value = null
            }

            if (payload.type === 'error') {
              const assistantMessage = messages.value.find(message => message.id === payload.assistantMessageId)
              if (assistantMessage) {
                assistantMessage.status = 'failed'
                assistantMessage.content = assistantMessage.content || 'AI 回复失败，请重试。'
              } else {
                messages.value.push({
                  id: Date.now(),
                  role: 'assistant',
                  messageType,
                  content: 'AI 回复失败，请重试。',
                  status: 'failed',
                  createdAt: new Date().toISOString()
                })
              }
              streamingMessageId.value = null
            }
          }
        }
      }

      await loadSessions()
      if (activeSessionId.value) {
        await loadSessionDetail(activeSessionId.value)
      }
    } catch (error: any) {
      if (error?.name !== 'AbortError') {
        const assistantMessage = streamingMessageId.value
          ? messages.value.find(message => message.id === streamingMessageId.value)
          : null
        if (assistantMessage) {
          assistantMessage.status = 'failed'
          assistantMessage.content = assistantMessage.content || 'AI 回复失败，请重试。'
        } else {
          messages.value.push({
            id: Date.now(),
            role: 'assistant',
            messageType,
            content: 'AI 回复失败，请重试。',
            status: 'failed',
            createdAt: new Date().toISOString()
          })
        }
      }
    } finally {
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
    setSelectedText,
    getSelectedText,
    setActiveSessionId,
    loadSessions,
    loadSessionDetail,
    createEmptySession,
    addContextNote,
    removeContextNote,
    clearAllSessions,
    deleteSession,
    sendMessage,
    stopChat
  }
})
