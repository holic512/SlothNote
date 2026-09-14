/**
 * @file useAiMessagePresentation
 * @project SlothNote
 * @module 用户端 / 笔记 AI 消息展示
 * @description 封装 AI 消息有界窗口、Markdown 净化缓存和工具时间线展示文本。
 * @logic 1. 仅暴露当前消息窗口；2. 按消息 ID 和源文本复用渲染结果；3. 对缓存执行固定上限淘汰；4. 提供时间线展示文本。
 * @dependencies Vue, MarkdownIt, DOMPurify, Types: AiChat
 * @index_tags AI 消息, Markdown 缓存, 有界窗口, 工具时间线
 * @author holic512
 */
import { computed, onScopeDispose, ref, watch, type Ref } from 'vue'
import MarkdownIt from 'markdown-it'
import DOMPurify from 'dompurify'
import type { AiTimelineItem, ChatMessage } from '../service/AiChat'

const DEFAULT_WINDOW_SIZE = 80
const DEFAULT_CACHE_LIMIT = 160
interface MessagePresentationOptions {
  windowSize?: number
  cacheLimit?: number
}

const trimOldestEntry = <T>(cache: Map<number, T>, limit: number) => {
  while (cache.size > limit) {
    const oldestKey = cache.keys().next().value as number | undefined
    if (oldestKey === undefined) return
    cache.delete(oldestKey)
  }
}

export const useAiMessagePresentation = (
  messages: Ref<ChatMessage[]>,
  options: MessagePresentationOptions = {}
) => {
  const windowSize = options.windowSize ?? DEFAULT_WINDOW_SIZE
  const cacheLimit = options.cacheLimit ?? DEFAULT_CACHE_LIMIT
  const windowOffset = ref(0)
  const markdown = new MarkdownIt({ html: false, breaks: true, linkify: true })
  const markdownCache = new Map<number, { source: string; html: string }>()

  const messageWindow = computed(() => {
    const total = messages.value.length
    const offset = Math.min(windowOffset.value, Math.max(0, total - 1))
    const end = Math.max(0, total - offset)
    const start = Math.max(0, end - windowSize)

    return {
      messages: messages.value.slice(start, end),
      earlierCount: start,
      newerCount: total - end
    }
  })

  const isLatestWindow = computed(() => windowOffset.value === 0)

  const resetWindow = () => {
    windowOffset.value = 0
  }

  const showEarlierMessages = () => {
    windowOffset.value = Math.min(
      windowOffset.value + windowSize,
      Math.max(0, messages.value.length - 1)
    )
  }

  const showNewerMessages = () => {
    windowOffset.value = Math.max(0, windowOffset.value - windowSize)
  }

  const renderMarkdown = (message: ChatMessage) => {
    const source = message.renderedContent ?? message.content ?? ''
    const cached = markdownCache.get(message.id)
    if (cached?.source === source) {
      return cached.html
    }

    const html = DOMPurify.sanitize(markdown.render(source))
    markdownCache.delete(message.id)
    markdownCache.set(message.id, { source, html })
    trimOldestEntry(markdownCache, cacheLimit)
    return html
  }

  const timelineLabel = (item: AiTimelineItem) => {
    if (item.kind === 'status') {
      return item.label || item.status || '处理中'
    }
    if (item.kind === 'tool_call') {
      return item.summary || `调用工具 ${item.tool}`
    }
    return item.summary || (item.success ? '工具执行完成' : '工具执行失败')
  }

  watch(() => messages.value.length, length => {
    if (!length) {
      resetWindow()
      return
    }
    windowOffset.value = Math.min(windowOffset.value, length - 1)
  })

  onScopeDispose(() => {
    markdownCache.clear()
  })

  return {
    windowSize,
    messageWindow,
    isLatestWindow,
    resetWindow,
    showEarlierMessages,
    showNewerMessages,
    renderMarkdown,
    timelineLabel
  }
}
