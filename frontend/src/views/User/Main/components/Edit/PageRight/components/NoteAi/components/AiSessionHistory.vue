<!--
@file AiSessionHistory
@project SlothNote
@module 用户端 / 笔记 AI 会话历史
@description 展示固定窗口内的 AI 历史会话，并向父组件上抛打开、删除和清空意图。
@logic 1. 每次打开历史面板回到最新窗口；2. 会话数量变化时校正窗口边界；3. 展示前后窗口导航。
@dependencies Vue, Element Plus Icons, Types: AiChat
@index_tags AI 会话, 历史记录, 有界窗口
@author holic512
-->
<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import type { ChatSession } from '../service/AiChat'

const SESSION_WINDOW_SIZE = 40

const props = defineProps<{
  sessions: ChatSession[]
  activeSessionId: number | null
  visible: boolean
}>()

const emit = defineEmits<{
  clear: []
  select: [sessionId: number]
  remove: [sessionId: number]
}>()

const windowStart = ref(0)
const visibleSessions = computed(() => props.sessions.slice(
  windowStart.value,
  windowStart.value + SESSION_WINDOW_SIZE
))
const newerSessionCount = computed(() => windowStart.value)
const olderSessionCount = computed(() => Math.max(
  0,
  props.sessions.length - windowStart.value - SESSION_WINDOW_SIZE
))

const showOlderSessions = () => {
  windowStart.value = Math.min(
    windowStart.value + SESSION_WINDOW_SIZE,
    Math.max(0, props.sessions.length - 1)
  )
}

const showNewerSessions = () => {
  windowStart.value = Math.max(0, windowStart.value - SESSION_WINDOW_SIZE)
}

watch(() => props.sessions.length, length => {
  if (!length) {
    windowStart.value = 0
    return
  }
  windowStart.value = Math.min(
    windowStart.value,
    Math.floor((length - 1) / SESSION_WINDOW_SIZE) * SESSION_WINDOW_SIZE
  )
})

watch(() => props.visible, visible => {
  if (visible) {
    windowStart.value = 0
  }
})
</script>

<template>
  <div class="history-panel">
    <div class="history-header">
      <span>最近对话</span>
      <el-button text size="small" type="danger" :disabled="!sessions.length" @click="emit('clear')">
        清空
      </el-button>
    </div>
    <div v-if="!sessions.length" class="history-empty">暂无历史记录</div>
    <div v-else class="history-list">
      <div
        v-for="session in visibleSessions"
        :key="session.id"
        :class="['history-item', { active: session.id === activeSessionId }]"
        @click="emit('select', session.id)"
      >
        <span class="history-title">{{ session.title }}</span>
        <el-icon class="history-del" @click.stop="emit('remove', session.id)"><Delete /></el-icon>
      </div>
      <div v-if="newerSessionCount || olderSessionCount" class="history-window-controls">
        <el-button v-if="newerSessionCount" text size="small" @click="showNewerSessions">
          更新的 {{ Math.min(newerSessionCount, SESSION_WINDOW_SIZE) }} 个
        </el-button>
        <el-button v-if="olderSessionCount" text size="small" @click="showOlderSessions">
          更早的 {{ Math.min(olderSessionCount, SESSION_WINDOW_SIZE) }} 个
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.history-panel {
  display: flex;
  flex-direction: column;
}

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

.history-window-controls {
  position: sticky;
  bottom: 0;
  display: flex;
  justify-content: center;
  gap: 6px;
  padding-top: 4px;
  background: #fff;
  color: #6b7280;
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
</style>
