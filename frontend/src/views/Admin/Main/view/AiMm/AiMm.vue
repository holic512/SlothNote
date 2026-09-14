<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { calculateRows } from '../FolderMm/components/TableView/calculateRows'
import {
  batchDeleteAiSessions,
  batchRestoreAiSessions,
  deleteAiSession,
  restoreAiSession,
  searchAiSessions,
  type AiSessionRow
} from './service/aiMm'
import AiSessionDetail from './components/AiSessionDetail/aiSessionDetail.vue'
import { getMaxPage, useLatestRequest } from '../../composables/useAdminListRequest'

const showFilters = ref(false)
const q = ref<string | null>(null)
const userIdFilter = ref<number | undefined>(undefined)
const usernameFilter = ref<string | null>(null)
const isDeletedFilter = ref<number | undefined>(undefined)

const minHeight = 720
const stepHeight = 45
const nowRow = ref(10)
const total = ref(0)
const maxPage = ref(1)
const nowPage = ref(1)
const rows = ref<AiSessionRow[]>([])
const selected = ref<AiSessionRow[]>([])
const listRequest = useLatestRequest()

const detailVisible = ref(false)
const currentId = ref<number | undefined>(undefined)

onMounted(async () => {
  nowRow.value = calculateRows(minHeight, stepHeight)
  await loadSessions()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (resizeTimeout) window.clearTimeout(resizeTimeout)
})

const dynamicHeight = computed(() => `${475 + (nowRow.value - 10) * 45}px`)

const RESIZE_DELAY = 100
let resizeTimeout: number | undefined
const handleResize = () => {
  if (resizeTimeout) window.clearTimeout(resizeTimeout)
  resizeTimeout = window.setTimeout(async () => {
    resizeTimeout = undefined
    const rowsCount = calculateRows(minHeight, stepHeight)
    if (rowsCount === nowRow.value) return
    nowRow.value = rowsCount
    await loadSessions()
  }, RESIZE_DELAY)
}

enum pageTurn { FirstPage, PreviousPage, NextPage, LastPage }

const turnPage = async (turn: pageTurn) => {
  switch (turn) {
    case pageTurn.FirstPage:
      nowPage.value = 1
      break
    case pageTurn.PreviousPage:
      nowPage.value = Math.max(1, nowPage.value - 1)
      break
    case pageTurn.NextPage:
      nowPage.value = Math.min(maxPage.value, nowPage.value + 1)
      break
    case pageTurn.LastPage:
      nowPage.value = maxPage.value
      break
  }
  await loadSessions()
}

const loadSessions = async (resetPage = false) => {
  if (resetPage) nowPage.value = 1

  const pageSize = nowRow.value
  const requestedPage = nowPage.value
  const filters = {
    q: q.value || undefined,
    userId: userIdFilter.value,
    username: usernameFilter.value || undefined,
    isDeleted: isDeletedFilter.value,
  }
  await listRequest.runLatest(
      async (signal) => {
        const requestPage = (pageNum: number) => searchAiSessions({
          ...filters,
          pageNum,
          pageSize,
        }, signal)

        let data = await requestPage(requestedPage)
        const resolvedMaxPage = getMaxPage(data.total, pageSize)
        const resolvedPage = Math.min(requestedPage, resolvedMaxPage)
        if (resolvedPage !== requestedPage) {
          data = await requestPage(resolvedPage)
        }
        return {data, resolvedMaxPage, resolvedPage}
      },
      ({data, resolvedMaxPage, resolvedPage}) => {
        total.value = data.total
        maxPage.value = resolvedMaxPage
        nowPage.value = resolvedPage
        rows.value = data.list
        selected.value = []
      },
  )
}

const doSearch = async () => {
  await loadSessions(true)
}

const refresh = async () => {
  await loadSessions()
  ElMessage.success('刷新成功')
}

const batchDelete = async () => {
  if (!selected.value.length) {
    ElMessage.warning('选择为空')
    return
  }
  const result = await batchDeleteAiSessions(selected.value.map(item => item.id))
  if (result.status === 200) {
    ElMessage.success('删除成功')
    await loadSessions()
  } else {
    ElMessage.error(result.message || '无法连接服务器')
  }
}

const batchRestore = async () => {
  if (!selected.value.length) {
    ElMessage.warning('选择为空')
    return
  }
  const result = await batchRestoreAiSessions(selected.value.map(item => item.id))
  if (result.status === 200) {
    ElMessage.success('恢复成功')
    await loadSessions()
  } else {
    ElMessage.error(result.message || '无法连接服务器')
  }
}

const handleSingleDelete = async (id: number) => {
  const result = await deleteAiSession(id)
  if (result.status === 200) {
    ElMessage.success('删除成功')
    await loadSessions()
  } else {
    ElMessage.error(result.message || '无法连接服务器')
  }
}

const handleSingleRestore = async (id: number) => {
  const result = await restoreAiSession(id)
  if (result.status === 200) {
    ElMessage.success('恢复成功')
    await loadSessions()
  } else {
    ElMessage.error(result.message || '无法连接服务器')
  }
}

const openDetail = (id: number) => {
  currentId.value = id
  detailVisible.value = true
}
</script>

<template>
  <el-scrollbar height="100%">
    <div class="common-layout admin-list-page">
      <div class="responsive-toolbar">
        <div class="toolbar-top">
          <div class="group-left">
            <IconField>
              <InputIcon icon="Search" class="custom-icon"/>
              <InputText v-model="q" placeholder="Search Session Title" class="custom-input" />
            </IconField>
            <Button :icon="showFilters ? 'FilterSlash' : 'Filter'" :severity="showFilters ? 'primary' : 'secondary'" outlined size="small" @click="showFilters = !showFilters" />
            <Button icon="Search" severity="secondary" outlined size="small" @click="doSearch" />
          </div>

          <div class="group-right">
            <Button icon="Trash" severity="secondary" outlined size="small" @click="batchDelete" />
            <Button icon="Refresh" severity="secondary" outlined size="small" @click="batchRestore" />
            <Button icon="Spinner" severity="secondary" outlined size="small" @click="refresh" />

            <div class="pagination-controls">
              <el-divider direction="vertical" />
              <Tag severity="info">数量: {{ total }}</Tag>
              <Tag class="page-tag">页: {{ nowPage }}/{{ maxPage }}</Tag>
              <div class="page-btns">
                <Button icon="AngleDoubleLeft" severity="secondary" text size="small" @click="turnPage(pageTurn.FirstPage)" />
                <Button icon="AngleLeft" severity="secondary" text size="small" @click="turnPage(pageTurn.PreviousPage)" />
                <Button icon="AngleRight" severity="secondary" text size="small" @click="turnPage(pageTurn.NextPage)" />
                <Button icon="AngleDoubleRight" severity="secondary" text size="small" @click="turnPage(pageTurn.LastPage)" />
              </div>
            </div>
          </div>
        </div>

        <transition name="fade-slide">
          <div v-if="showFilters" class="toolbar-filter-panel">
            <el-input v-model.number="userIdFilter" placeholder="用户ID" style="width: 120px" />
            <el-input v-model="usernameFilter" placeholder="用户名" style="width: 140px" />
            <el-select v-model="isDeletedFilter" placeholder="状态" style="width: 120px" clearable>
              <el-option label="有效" :value="0" />
              <el-option label="已删除" :value="1" />
            </el-select>
            <Button label="应用筛选" icon="Check" size="small" outlined @click="doSearch" />
          </div>
        </transition>
      </div>

      <div class="table-container">
        <DataTable v-model:selection="selected" :value="rows" stripedRows dataKey="id"
                   tableStyle="min-width: 980px;" size="small" :style="{ minHeight: dynamicHeight }">
          <Column selectionMode="multiple" headerStyle="width: 50px" />
          <Column field="id" header="ID" headerStyle="width: 70px" />
          <Column field="userId" header="用户ID" headerStyle="width: 90px" />
          <Column field="username" header="用户名" headerStyle="width: 130px" />
          <Column field="title" header="会话标题" headerStyle="width: 24%">
            <template #body="{ data }">
              <el-text>{{ data.title }}</el-text>
            </template>
          </Column>
          <Column field="messageCount" header="消息数" headerStyle="width: 90px" />
          <Column field="contextNoteCount" header="上下文笔记数" headerStyle="width: 120px" />
          <Column field="isDeleted" header="状态" headerStyle="width: 90px">
            <template #body="{ data }">
              <Tag :value="data.isDeleted === 1 ? '已删除' : '有效'" :severity="data.isDeleted === 1 ? 'danger' : 'success'" />
            </template>
          </Column>
          <Column field="lastMessageAt" header="最近消息时间" headerStyle="width: 180px">
            <template #body="{ data }">
              {{ data.lastMessageAt ? new Date(data.lastMessageAt).toLocaleString() : '-' }}
            </template>
          </Column>
          <Column header="更多" headerStyle="width: 140px">
            <template #body="{ data }">
              <div style="display: flex; gap: 6px; align-items: center;">
                <Button type="button" icon="Eye" rounded outlined style="height: 32px; width: 32px" @click="openDetail(data.id)" />
                <Button v-if="data.isDeleted !== 1" type="button" icon="Trash" rounded outlined style="height: 32px; width: 32px" @click="handleSingleDelete(data.id)" />
                <Button v-else type="button" icon="Refresh" rounded outlined style="height: 32px; width: 32px" @click="handleSingleRestore(data.id)" />
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>
  </el-scrollbar>

  <AiSessionDetail v-model:visible="detailVisible" :session-id="currentId" />
</template>

<style scoped>
.common-layout {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.responsive-toolbar {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.toolbar-top,
.group-left,
.group-right,
.pagination-controls,
.page-btns,
.toolbar-filter-panel {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-top {
  justify-content: space-between;
  flex-wrap: wrap;
}

.group-left,
.group-right,
.toolbar-filter-panel {
  flex-wrap: wrap;
}

.table-container {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.page-tag {
  background: #eff6ff;
}
</style>
