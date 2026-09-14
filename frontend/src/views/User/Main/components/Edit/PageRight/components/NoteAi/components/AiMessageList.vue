<!--
@file AiMessageList
@project SlothNote
@module 用户端 / 笔记 AI 消息列表
@description 展示有界 AI 消息窗口、工具时间线、Markdown 内容和消息级操作。
@logic 1. 委托 composable 维护消息窗口及缓存；2. 会话切换重置窗口；3. 最新窗口新增消息时滚动到底部；4. 将代码块插入意图上抛。
@dependencies Composable: useAiMessagePresentation, Component: SaveSummaryButton, Types: AiChat
@index_tags AI 消息列表, 会话窗口, Markdown, 代码块
@author holic512
-->
<script setup lang="ts">
import { nextTick, ref, toRef, watch } from 'vue'
import { MagicStick, Plus } from '@element-plus/icons-vue'
import type { AiTimelineItem, ChatMessage } from '../service/AiChat'
import { useAiMessagePresentation } from '../composables/useAiMessagePresentation'
import SaveSummaryButton from './SaveSummaryButton.vue'

const props = defineProps<{
  messages: ChatMessage[]
  activeSessionId: number | null
  getTimeline: (messageId: number) => AiTimelineItem[]
}>()

const emit = defineEmits<{
  'insert-code-block': [code: string, language?: string]
}>()

const scrollbarRef = ref<{ wrapRef?: HTMLElement } | null>(null)
const {
  windowSize,
  messageWindow,
  isLatestWindow,
  resetWindow,
  showEarlierMessages,
  showNewerMessages,
  renderMarkdown,
  extractCodeBlocks,
  isSummaryMessage,
  timelineLabel
} = useAiMessagePresentation(toRef(props, 'messages'))

const scrollToBottom = () => {
  const wrap = scrollbarRef.value?.wrapRef
  if (!wrap) return
  nextTick(() => {
    wrap.scrollTo({
      top: wrap.scrollHeight,
      behavior: 'smooth'
    })
  })
}

const showNewer = () => {
  showNewerMessages()
  if (isLatestWindow.value) {
    scrollToBottom()
  }
}

watch(() => props.messages.map(message => message.id).join(','), () => {
  if (isLatestWindow.value) {
    scrollToBottom()
  }
})

watch(() => props.activeSessionId, resetWindow)

defineExpose({ resetWindow, scrollToBottom })
</script>

<template>
  <el-scrollbar ref="scrollbarRef" class="chat-viewport">
    <div v-if="!messages.length" class="empty-state">
      <div class="empty-icon"><el-icon><MagicStick /></el-icon></div>
      <h3>有什么可以帮你的？</h3>
      <p>你可以直接提问，或选择笔记内容让我为你解释和润色。</p>
    </div>

    <div v-else class="message-list">
      <div v-if="messageWindow.earlierCount || messageWindow.newerCount" class="message-window-controls">
        <el-button v-if="messageWindow.earlierCount" text size="small" @click="showEarlierMessages">
          查看更早的 {{ Math.min(messageWindow.earlierCount, windowSize) }} 条
        </el-button>
        <el-button v-if="messageWindow.newerCount" text size="small" @click="showNewer">
          查看更新的 {{ Math.min(messageWindow.newerCount, windowSize) }} 条
        </el-button>
      </div>

      <div v-for="message in messageWindow.messages" :key="message.id" :class="['message', message.role]">
        <div v-if="message.role === 'assistant'" class="msg-avatar ai">
          <el-icon><MagicStick /></el-icon>
        </div>

        <div class="msg-content">
          <div v-if="message.role === 'assistant' && getTimeline(message.id).length" class="timeline-list">
            <div
              v-for="item in getTimeline(message.id)"
              :key="item.id"
              :class="['timeline-item', item.kind, { success: item.success, failed: item.success === false }]"
            >
              {{ timelineLabel(item) }}
            </div>
          </div>
          <div v-if="message.role === 'user'" class="bubble user">
            {{ message.content }}
          </div>
          <div v-else class="bubble ai markdown-body" v-html="renderMarkdown(message)"></div>

          <div v-if="message.role === 'assistant' && message.status === 'completed'" class="msg-actions">
            <template v-for="(block, index) in extractCodeBlocks(message)" :key="`${message.id}-${index}`">
              <el-button size="small" text @click="emit('insert-code-block', block.code, block.language)">
                <el-icon><Plus /></el-icon>插入代码块{{ block.language ? ` (${block.language})` : '' }}
              </el-button>
            </template>
            <SaveSummaryButton v-if="isSummaryMessage(message)" :summary="message.content" />
          </div>
        </div>
      </div>
    </div>
  </el-scrollbar>
</template>

<style scoped lang="scss">
.chat-viewport {
  flex: 1;
  min-height: 0;
  padding: 0 16px;

  :deep(.el-scrollbar__wrap),
  :deep(.el-scrollbar__view) {
    height: 100%;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #6b7280;
  text-align: center;
  margin-top: 6vh;

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
    margin: 0 0 8px;
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

.message-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px 0;
}

.message-window-controls {
  display: flex;
  justify-content: center;
  gap: 6px;
  min-height: 28px;
  color: #6b7280;
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

.timeline-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.timeline-item {
  font-size: 11px;
  line-height: 1;
  padding: 6px 8px;
  border-radius: 999px;
  background: #eef2ff;
  color: #4338ca;

  &.tool_call {
    background: #eff6ff;
    color: #1d4ed8;
  }

  &.tool_result.success {
    background: #ecfdf5;
    color: #047857;
  }

  &.tool_result.failed {
    background: #fef2f2;
    color: #b91c1c;
  }
}

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
    padding: 2px 0;
  }
}

.markdown-body {
  :deep(p) { margin: 0 0 12px; }
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
</style>
