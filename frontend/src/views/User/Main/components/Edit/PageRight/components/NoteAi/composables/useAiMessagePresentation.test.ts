import { effectScope, ref } from 'vue'
import { describe, expect, it, vi } from 'vitest'
import type { AiTimelineItem, ChatMessage } from '../service/AiChat'
import { getAiProgressLabel, useAiMessagePresentation } from './useAiMessagePresentation'

vi.mock('dompurify', () => ({
  default: { sanitize: (html: string) => html }
}))

const createMessage = (id: number, content = `message-${id}`): ChatMessage => ({
  id,
  role: 'assistant',
  messageType: 'chat',
  content,
  renderedContent: content,
  status: 'completed',
  createdAt: '2026-07-19T00:00:00Z'
})

describe('useAiMessagePresentation', () => {
  it('keeps rendering bounded while allowing navigation between old and new messages', () => {
    const scope = effectScope()
    const messages = ref(Array.from({ length: 100 }, (_, index) => createMessage(index + 1)))
    const presentation = scope.run(() => useAiMessagePresentation(messages))!

    expect(presentation.messageWindow.value.messages.map(message => message.id)).toEqual(
      Array.from({ length: 80 }, (_, index) => index + 21)
    )
    expect(presentation.messageWindow.value.earlierCount).toBe(20)
    expect(presentation.messageWindow.value.newerCount).toBe(0)

    presentation.showEarlierMessages()
    expect(presentation.messageWindow.value.messages.map(message => message.id)).toEqual(
      Array.from({ length: 20 }, (_, index) => index + 1)
    )
    expect(presentation.messageWindow.value.earlierCount).toBe(0)
    expect(presentation.messageWindow.value.newerCount).toBe(80)

    presentation.showNewerMessages()
    expect(presentation.isLatestWindow.value).toBe(true)
    expect(presentation.messageWindow.value.messages.at(-1)?.id).toBe(100)
    scope.stop()
  })

  it('renders Markdown code blocks without exposing editor insertion data', () => {
    const scope = effectScope()
    const message = createMessage(1, '```ts\nconst value = 1\n```')
    const messages = ref([message])
    const presentation = scope.run(() => useAiMessagePresentation(messages))!

    expect(presentation.renderMarkdown(message)).toContain('<pre><code class="language-ts">const value = 1')

    message.content = '```js\nconst value = 2\n```'
    message.renderedContent = message.content
    expect(presentation.renderMarkdown(message)).toContain('<pre><code class="language-js">const value = 2')
    scope.stop()
  })

  it('formats each timeline item kind without leaking display rules into the page component', () => {
    const scope = effectScope()
    const presentation = scope.run(() => useAiMessagePresentation(ref([])))!
    const items: AiTimelineItem[] = [
      { id: '1', kind: 'status', status: 'thinking', label: '正在分析', createdAt: '' },
      { id: '2', kind: 'tool_call', tool: 'search_note', createdAt: '' },
      { id: '3', kind: 'tool_result', success: false, createdAt: '' }
    ]

    expect(items.map(presentation.timelineLabel)).toEqual([
      '正在分析',
      '调用工具 search_note',
      '工具执行失败'
    ])
    scope.stop()
  })

  it('prefers the latest AI stage or concrete tool action while streaming', () => {
    expect(getAiProgressLabel([])).toBe('正在思考…')
    expect(getAiProgressLabel([
      { id: '1', kind: 'status', label: '正在规划是否需要调用工具', createdAt: '' },
      { id: '2', kind: 'tool_call', summary: '准备向当前笔记末尾追加内容。', createdAt: '' },
      { id: '3', kind: 'tool_result', summary: '已追加到当前笔记末尾。', success: true, createdAt: '' }
    ])).toBe('准备向当前笔记末尾追加内容。')
  })
})
