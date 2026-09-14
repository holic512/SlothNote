import {beforeEach, describe, expect, it, vi} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'

const http = vi.hoisted(() => ({
  get: vi.fn(),
  defaults: {baseURL: 'https://api.example/'},
}))

vi.mock('@/axios', () => ({
  default: http,
  handleHttpAuthStatus: vi.fn(),
}))
vi.mock('@/pinia/token', () => ({
  tokenStore: () => ({getUserToken: () => 'test-token'}),
}))

import {useAiChatStore} from './AiChat'
import {useCurrentNoteInfoStore} from '@/views/User/Main/components/Edit/Pinia/currentNoteInfo'

const sseResponse = (events: object[]) => {
  const encoder = new TextEncoder()
  const body = new ReadableStream({
    start(controller) {
      controller.enqueue(encoder.encode(events.map(event => `data: ${JSON.stringify(event)}\n\n`).join('')))
      controller.close()
    },
  })

  return {ok: true, body, status: 200}
}

describe('useAiChatStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    http.get.mockReset()
    http.get.mockResolvedValue({data: {data: []}})
  })

  it('uses the server-reported write target when publishing an AI note mutation', async () => {
    const currentNote = useCurrentNoteInfoStore()
    currentNote.setNoteInfo(9, '当前显示的笔记', [], '', null)
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(sseResponse([
      {type: 'session', sessionId: 1, userMessageId: 100, assistantMessageId: 101},
      {type: 'status', assistantMessageId: 101, status: 'editing_note', label: '正在追加内容'},
      {
        type: 'tool_result',
        assistantMessageId: 101,
        tool: 'append_to_current_note',
        writeTool: true,
        noteId: 42,
        success: true,
        summary: '已追加到当前笔记末尾。',
      },
      {type: 'done', assistantMessageId: 101, status: 'completed'},
    ])))

    const store = useAiChatStore()
    await store.sendMessage('追加一段内容', true)

    expect(store.lastNoteMutation).toMatchObject({
      noteId: 42,
      summary: '已追加到当前笔记末尾。',
    })
    expect(store.getTimeline(101)).toEqual(expect.arrayContaining([
      expect.objectContaining({kind: 'status', label: '正在追加内容'}),
      expect.objectContaining({kind: 'tool_result', writeTool: true, success: true}),
    ]))
  })
})
