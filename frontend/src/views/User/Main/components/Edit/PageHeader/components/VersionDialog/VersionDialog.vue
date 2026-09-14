<!--
@file NoteVersionDialog
@project SlothNote
@module 用户端 / 笔记历史版本
@description 展示笔记历史版本、按需加载版本详情并执行恢复。
@logic 1. 对版本列表和详情分别分配请求序号；2. 笔记或选中版本变化时仅提交最新响应；3. 恢复前后校验笔记与版本仍一致。
@dependencies Service: noteVersion, Store: currentNoteInfo/SaveNoteState, Editor
@index_tags 历史版本, 版本详情, 恢复笔记, 请求竞态
@author holic512
-->
<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCurrentNoteInfoStore } from '@/views/User/Main/components/Edit/Pinia/currentNoteInfo'
import { useSaveNoteState } from '@/views/User/Main/components/Edit/Pinia/SaveNoteState'
import { fetchNoteVersionDetail, fetchNoteVersions, restoreNoteVersion, type NoteVersionDetail, type NoteVersionRow } from '../../service/noteVersion'

const visible = defineModel<boolean>('visible', { required: true })
const editor = defineModel<any>('editor')
const currentNoteInfo = useCurrentNoteInfoStore()
const saveState = useSaveNoteState()

const loading = ref(false)
const versions = ref<NoteVersionRow[]>([])
const selectedVersionId = ref<number | null>(null)
const detail = ref<NoteVersionDetail | null>(null)
let versionsRequestId = 0
let detailRequestId = 0

const selectedVersion = computed(() => versions.value.find(item => item.id === selectedVersionId.value) || null)

const loadVersions = async () => {
  const noteId = currentNoteInfo.noteId
  const requestId = ++versionsRequestId
  detailRequestId += 1

  if (!noteId) {
    versions.value = []
    selectedVersionId.value = null
    detail.value = null
    return
  }

  versions.value = []
  selectedVersionId.value = null
  detail.value = null
  loading.value = true
  try {
    const nextVersions = await fetchNoteVersions(noteId)
    if (requestId !== versionsRequestId || !visible.value || noteId !== currentNoteInfo.noteId) return

    versions.value = nextVersions
    selectedVersionId.value = nextVersions[0]?.id ?? null
  } finally {
    if (requestId === versionsRequestId) loading.value = false
  }
}

const loadDetail = async () => {
  const noteId = currentNoteInfo.noteId
  const versionId = selectedVersionId.value
  const requestId = ++detailRequestId

  if (!noteId || !versionId) {
    detail.value = null
    return
  }

  const nextDetail = await fetchNoteVersionDetail(noteId, versionId)
  if (
    requestId !== detailRequestId
    || !visible.value
    || noteId !== currentNoteInfo.noteId
    || versionId !== selectedVersionId.value
  ) return

  detail.value = nextDetail
}

const restore = async () => {
  const noteId = currentNoteInfo.noteId
  const versionId = selectedVersionId.value
  if (!noteId || !versionId || detail.value?.id !== versionId) {
    return
  }

  await ElMessageBox.confirm(
    `确定恢复到 V${detail.value.versionNo} 吗？当前内容将被该版本覆盖。`,
    '恢复历史版本',
    { type: 'warning' }
  )

  if (noteId !== currentNoteInfo.noteId || versionId !== selectedVersionId.value) return

  const response = await restoreNoteVersion(noteId, versionId)
  if (response?.status !== 200) {
    ElMessage.error(response?.message || '恢复失败')
    return
  }

  try {
    const parsed = JSON.parse(detail.value.contentJson)
    editor.value?.commands?.setContent(parsed)
    saveState.saveContent()
    ElMessage.success('历史版本恢复成功')
    visible.value = false
  } catch {
    ElMessage.error('恢复成功，但内容解析失败')
  }
}

watch([visible, () => currentNoteInfo.noteId], async ([newVisible]) => {
  if (newVisible) {
    await loadVersions()
  } else {
    versionsRequestId += 1
    detailRequestId += 1
    loading.value = false
  }
})

watch(selectedVersionId, async () => {
  await loadDetail()
})

onBeforeUnmount(() => {
  versionsRequestId += 1
  detailRequestId += 1
})
</script>

<template>
  <el-dialog v-model="visible" title="历史版本" width="880px" destroy-on-close>
    <div v-loading="loading" class="version-layout">
      <div class="version-list">
        <div
          v-for="item in versions"
          :key="item.id"
          :class="['version-item', { active: item.id === selectedVersionId }]"
          @click="selectedVersionId = item.id"
        >
          <div class="version-head">
            <span class="version-no">V{{ item.versionNo }}</span>
            <span class="version-source">{{ item.sourceType }}</span>
          </div>
          <div class="version-time">{{ new Date(item.createdAt).toLocaleString() }}</div>
          <div class="version-preview">{{ item.contentPreview || '无内容预览' }}</div>
        </div>
        <el-empty v-if="!versions.length" description="暂无历史版本" :image-size="80" />
      </div>

      <div class="version-detail">
        <template v-if="detail">
          <div class="detail-meta">
            <span>当前查看：V{{ detail.versionNo }}</span>
            <span>{{ new Date(detail.createdAt).toLocaleString() }}</span>
            <span>{{ detail.sourceType }}</span>
          </div>
          <pre class="detail-preview">{{ detail.contentPreview }}</pre>
          <el-scrollbar max-height="420px">
            <pre class="detail-json">{{ detail.contentJson }}</pre>
          </el-scrollbar>
        </template>
        <el-empty v-else description="请选择一个版本" :image-size="80" />
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" :disabled="!selectedVersion" @click="restore">恢复此版本</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.version-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 16px;
  min-height: 460px;
}

.version-list,
.version-detail {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  background: #fff;
}

.version-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 520px;
  overflow: auto;
}

.version-item {
  padding: 10px 12px;
  border: 1px solid #edf2f7;
  border-radius: 10px;
  cursor: pointer;
  background: #f8fafc;
}

.version-item.active {
  border-color: #93c5fd;
  background: #eff6ff;
}

.version-head,
.detail-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.version-no {
  font-weight: 700;
}

.version-source,
.version-time,
.detail-meta {
  font-size: 12px;
  color: #6b7280;
}

.version-preview,
.detail-preview,
.detail-json {
  margin-top: 8px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  font-size: 13px;
}

.detail-preview {
  padding: 10px;
  background: #f8fafc;
  border-radius: 8px;
}

.detail-json {
  margin: 0;
  padding: 12px;
  background: #111827;
  color: #e5e7eb;
  border-radius: 8px;
}
</style>
