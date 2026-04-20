<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import Tag from 'primevue/tag'
import { fetchAiSessionDetail, type AiSessionDetail } from '../../service/aiMm'

const visible = defineModel<boolean>('visible', { required: true })
const props = defineProps<{ sessionId?: number }>()

const loading = ref(false)
const detail = ref<AiSessionDetail | null>(null)

watch(() => props.sessionId, async (newId) => {
  if (!visible.value || !newId) {
    return
  }
  loading.value = true
  try {
    detail.value = await fetchAiSessionDetail(newId)
  } finally {
    loading.value = false
  }
}, { immediate: true })

watch(visible, async (newVisible) => {
  if (newVisible && props.sessionId) {
    loading.value = true
    try {
      detail.value = await fetchAiSessionDetail(props.sessionId)
    } finally {
      loading.value = false
    }
  }
})

const title = computed(() => detail.value?.session?.title || 'AI 会话详情')
</script>

<template>
  <el-dialog v-model="visible" :title="title" width="900px" destroy-on-close>
    <div v-loading="loading" class="detail-layout">
      <div v-if="detail" class="detail-grid">
        <div class="meta-card">
          <div class="meta-row"><strong>会话 ID:</strong> {{ detail.session.id }}</div>
          <div class="meta-row"><strong>用户:</strong> {{ detail.session.username || '-' }} ({{ detail.session.userId }})</div>
          <div class="meta-row"><strong>邮箱:</strong> {{ detail.session.email || '-' }}</div>
          <div class="meta-row"><strong>状态:</strong> {{ detail.session.isDeleted === 1 ? '已删除' : '有效' }}</div>
          <div class="meta-row"><strong>消息数:</strong> {{ detail.session.messageCount }}</div>
          <div class="meta-row"><strong>上下文笔记数:</strong> {{ detail.session.contextNoteCount }}</div>
        </div>

        <div class="context-card">
          <div class="section-title">上下文笔记</div>
          <div v-if="detail.contextNotes.length" class="context-list">
            <div v-for="note in detail.contextNotes" :key="note.noteId" class="context-item">
              <div class="context-title">{{ note.icon || '📝' }} {{ note.title }}</div>
              <div class="context-summary">{{ note.summary || '无摘要' }}</div>
            </div>
          </div>
          <el-empty v-else description="无上下文笔记" :image-size="72" />
        </div>
      </div>

      <div v-if="detail" class="message-section">
        <div class="section-title">消息记录</div>
        <el-scrollbar max-height="420px">
          <div class="message-list">
            <div v-for="message in detail.messages" :key="message.id" :class="['message-item', message.role]">
              <div class="message-head">
                <Tag :value="message.role" :severity="message.role === 'assistant' ? 'info' : 'contrast'" />
                <Tag :value="message.messageType" severity="secondary" />
                <Tag :value="message.status" severity="warn" />
                <span class="message-time">{{ new Date(message.createdAt).toLocaleString() }}</span>
              </div>
              <pre class="message-content">{{ message.content }}</pre>
            </div>
          </div>
        </el-scrollbar>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped>
.detail-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.meta-card,
.context-card,
.message-section {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 16px;
  background: #fff;
}

.meta-row {
  margin-bottom: 10px;
  color: #374151;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
}

.context-list,
.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.context-item,
.message-item {
  border: 1px solid #eef2f7;
  background: #f9fafb;
  border-radius: 10px;
  padding: 12px;
}

.context-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.context-summary {
  color: #6b7280;
  font-size: 13px;
  white-space: pre-wrap;
}

.message-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.message-time {
  font-size: 12px;
  color: #6b7280;
}

.message-content {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  font-size: 13px;
  line-height: 1.6;
}
</style>
