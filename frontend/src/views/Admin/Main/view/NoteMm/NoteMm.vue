<script setup lang="ts">
import Button from 'primevue/button';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import Tag from 'primevue/tag';
import {computed, onBeforeUnmount, onMounted, ref} from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import axios from '../../../../../axios'; // 请确认路径
import {ElMessage} from 'element-plus';
import {calculateRows} from '../FolderMm/components/TableView/calculateRows';
import {fetchUserOptions} from '../FolderMm/components/TableView/userOptions';
import AddNote from './components/AddNote/addNote.vue';
import NoteDetail from './components/NoteDetail/noteDetail.vue';

// --- 响应式折叠控制 ---
const showFilters = ref(false);

const q = ref<string | null>(null);
const userIdFilter = ref<number | null>(null);
const folderIdFilter = ref<number | null>(null);
const noteTypeFilter = ref<number | null>(null);
const isDeletedFilter = ref<number | null>(null);
const userOptions = ref<any[]>([]);

const minHeight = 720;
const stepHeight = 45;
let nowRow = ref(10);
const noteCount = ref(0);
const maxPage = ref(1);
const nowPage = ref(1);
const rows = ref([]);

onMounted(async () => {
  nowRow.value = calculateRows(minHeight, stepHeight);
  await axios.get('admin/noteMm/getCount').then((r)=>{ noteCount.value = r.data.data; maxPage.value = Math.ceil(noteCount.value / nowRow.value); });
  rows.value = await fetchPageData(nowRow.value, nowPage.value);
  userOptions.value = await fetchUserOptions(undefined, 50);
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); });

const DEBOUNCE_DELAY = 100;
let resizeTimeout: ReturnType<typeof setTimeout>;
const handleResize = async () => {
  clearTimeout(resizeTimeout);
  resizeTimeout = setTimeout(async () => {
    const rowsCount = calculateRows(minHeight, stepHeight);
    if (rowsCount !== nowRow.value) {
      nowRow.value = rowsCount;
      maxPage.value = Math.ceil(noteCount.value / nowRow.value);
      if (nowPage.value > maxPage.value) nowPage.value = maxPage.value;
      rows.value = await fetchPageData(nowRow.value, nowPage.value);
    }
  }, DEBOUNCE_DELAY);
};

const dynamicHeight = computed(() => `${475 + (nowRow.value - 10) * 45}px`);

enum pageTurn { FirstPage, PreviousPage, NextPage, LastPage }
const turnPage = async (turn: pageTurn) => {
  switch (turn) {
    case pageTurn.FirstPage:
      if (nowPage.value != 1) { nowPage.value = 1; rows.value = await fetchPageData(nowRow.value, nowPage.value); } else { ElMessage.warning('已经是第一页了') }
      break;
    case pageTurn.PreviousPage:
      if (nowPage.value > 1) { nowPage.value = nowPage.value - 1; rows.value = await fetchPageData(nowRow.value, nowPage.value); } else { ElMessage.warning('已经是第一页了') }
      break;
    case pageTurn.NextPage:
      if (nowPage.value < maxPage.value) { nowPage.value = nowPage.value + 1; rows.value = await fetchPageData(nowRow.value, nowPage.value); } else { ElMessage.warning('已经是最后一页了') }
      break;
    case pageTurn.LastPage:
      if (nowPage.value != maxPage.value) { nowPage.value = maxPage.value; rows.value = await fetchPageData(nowRow.value, nowPage.value); } else { ElMessage.warning('已经是最后一页了') }
      break;
  }
};

const refresh = async () => {
  await axios.get('admin/noteMm/getCount').then((r)=>{ noteCount.value = r.data.data; maxPage.value = Math.ceil(noteCount.value / nowRow.value); });
  if (maxPage.value < nowPage.value) rows.value = await fetchPageData(nowRow.value, maxPage.value);
  else rows.value = await fetchPageData(nowRow.value, nowPage.value);
  ElMessage.success('刷新成功');
};

const selected = ref();
const batchDelete = async () => {
  if (!selected.value || selected.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selected.value.map((p: any) => p.id);
  const r = await axios.post('admin/noteMm/batchDelete', ids);
  if (r.data.status===200) { ElMessage.success('删除成功'); rows.value = await fetchPageData(nowRow.value, nowPage.value); } else { ElMessage.error('无法连接服务器') }
};
const batchRestore = async () => {
  if (!selected.value || selected.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selected.value.map((p: any) => p.id);
  const r = await axios.post('admin/noteMm/batchRestore', ids);
  if (r.data.status===200) { ElMessage.success('恢复成功'); rows.value = await fetchPageData(nowRow.value, nowPage.value); } else { ElMessage.error('无法连接服务器') }
};

const doSearch = async () => {
  const resp = await axios.post('admin/noteMm/search', {
    q: q.value || undefined,
    userId: userIdFilter.value === null ? undefined : userIdFilter.value,
    folderId: folderIdFilter.value === null ? undefined : folderIdFilter.value,
    noteType: noteTypeFilter.value === null ? undefined : noteTypeFilter.value,
    isDeleted: isDeletedFilter.value === null ? undefined : isDeletedFilter.value,
    pageNum: nowPage.value,
    pageSize: nowRow.value,
  });
  const data = resp.data.data;
  rows.value = data.list;
  noteCount.value = data.total;
  maxPage.value = Math.ceil(noteCount.value / nowRow.value);
};

const previewVisible = ref<boolean>(false);
const previewContent = ref<string>('');
const openPreview = async (noteId: number) => {
  const r = await axios.get('admin/noteMm/content/get', { params: { noteId } });
  const content = r.data.data || '';
  try {
    const obj = JSON.parse(content);
    previewContent.value = JSON.stringify(obj, null, 2).slice(0, 800);
  } catch { previewContent.value = content.slice(0, 800); }
  previewVisible.value = true;
};

const addVisible = ref<boolean>(false);
const detailVisible = ref<boolean>(false);
const currentId = ref<number | null>(null);
const openDetail = (id: number) => { currentId.value = id; detailVisible.value = true; };

const fetchPageData = async (pageSize: number, pageNum: number) => {
  const r = await axios.get('admin/noteMm/fetchPageData', { params: { pageSize, pageNum } });
  return r.data.data;
}

</script>

<template>
  <el-scrollbar height="100%">
    <div class="common-layout">
      <!-- 标题区域 -->
      <div class="title-container">
        <div class="title">
          <h1>笔记管理</h1>
          <p>统一管理笔记基本信息与内容预览</p>
        </div>
      </div>

      <!-- 响应式工具栏 -->
      <div class="responsive-toolbar">
        <!-- 第一行：常用操作 -->
        <div class="toolbar-top">
          <!-- 左侧：搜索 + 筛选开关 -->
          <div class="group-left">
            <IconField>
              <InputIcon class="pi pi-search custom-icon"/>
              <InputText v-model="q" placeholder="Search Note" class="custom-input"/>
            </IconField>
            
            <!-- 筛选开关按钮 -->
            <Button 
              :icon="showFilters ? 'pi pi-filter-slash' : 'pi pi-filter'" 
              :severity="showFilters ? 'primary' : 'secondary'" 
              outlined 
              size="small" 
              @click="showFilters = !showFilters"
              v-tooltip="'高级筛选'"
            />
            
            <Button icon="pi pi-search" severity="secondary" outlined size="small"
                    @click="doSearch"
                    v-tooltip.bottom="{ value: '搜索', showDelay: 1000, hideDelay: 300 }"/>
          </div>

          <!-- 右侧：增删改 + 分页 -->
          <div class="group-right">
            <Button icon="pi pi-plus" severity="secondary" outlined size="small"
                    v-tooltip.bottom="{ value: '添加笔记', showDelay: 1000, hideDelay: 300 }"
                    @click="addVisible = true"/>

            <Button icon="pi pi-trash" severity="secondary" outlined size="small"
                    @click="batchDelete"
                    v-tooltip.bottom="{ value: '删除选中', showDelay: 1000, hideDelay: 300 }"/>
                    
            <Button icon="pi pi-refresh" severity="secondary" outlined size="small" 
                    @click="batchRestore"
                    v-tooltip.bottom="{ value: '恢复选中', showDelay: 1000, hideDelay: 300 }"/>

            <Button icon="pi pi-spinner" severity="secondary" outlined size="small"
                    @click="refresh"
                    v-tooltip.bottom="{ value: '刷新', showDelay: 1000, hideDelay: 300 }"/>

            <div class="pagination-controls">
              <el-divider direction="vertical" class="hidden-xs-only"/>
              <Tag severity="info">数量: {{ noteCount }}</Tag>
              <Tag class="page-tag">页: {{ nowPage }}/{{ maxPage }}</Tag>

              <div class="page-btns">
                <Button icon="pi pi-angle-double-left" severity="secondary" text size="small"
                        @click="turnPage(0)" />
                <Button icon="pi pi-angle-left" severity="secondary" text size="small"
                        @click="turnPage(1)" />
                <Button icon="pi pi-angle-right" severity="secondary" text size="small"
                        @click="turnPage(2)" />
                <Button icon="pi pi-angle-double-right" severity="secondary" text size="small"
                        @click="turnPage(3)" />
              </div>
            </div>
          </div>
        </div>

        <!-- 第二行：折叠筛选面板 -->
        <transition name="fade-slide">
          <div v-if="showFilters" class="toolbar-filter-panel">
             <el-select v-model="isDeletedFilter" placeholder="删除状态" style="width: 120px" clearable>
              <el-option label="全部" :value="null" />
              <el-option label="正常" :value="0" />
              <el-option label="已删除" :value="1" />
            </el-select>

            <el-input-number v-model="folderIdFilter" :min="0" :step="1" placeholder="文件夹ID" controls-position="right" style="width: 120px" />
            <el-input-number v-model="noteTypeFilter" :min="0" :step="1" placeholder="类型" controls-position="right" style="width: 120px" />
            
            <el-select v-model="userIdFilter" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions.value = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
              <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
            </el-select>

            <Button label="应用筛选" icon="pi pi-check" size="small" outlined @click="doSearch" />
          </div>
        </transition>
      </div>

      <div class="table-container">
        <DataTable v-model:selection="selected" :value="rows" stripedRows dataKey="id" tableStyle="min-width: 1000px;" size="small" :style="{ minHeight: dynamicHeight }">
          <Column selectionMode="multiple" headerStyle="width: 50px" position="fixed"></Column>
          <Column field="id" header="ID" headerStyle="width: 60px"></Column>
          <Column field="noteTitle" header="标题" headerStyle="width: 18%"></Column>
          <Column field="userId" header="用户ID" headerStyle="width: 10%"></Column>
          <Column field="folderId" header="文件夹ID" headerStyle="width: 10%"></Column>
          <Column field="noteType" header="类型" headerStyle="width: 10%"></Column>
          <Column field="isDeleted" header="状态" headerStyle="width: 10%">
            <template #body="{ data }">
              <Tag :value="data.isDeleted===1 ? '已删除' : '正常'" :severity="data.isDeleted===1 ? 'danger' : 'success'"/>
            </template>
          </Column>
          <Column header="更多" headerStyle="width: 140px">
            <template #body="{ data }">
              <div style="display: flex; gap: 6px; align-items: center;">
                <Button type="button" icon="pi pi-eye" rounded outlined style=" height: 32px;width: 32px" @click="openPreview(data.id)"/>
                <Button type="button" icon="pi pi-pencil" rounded outlined style=" height: 32px;width: 32px" @click="openDetail(data.id)"/>
                <Button type="button" icon="pi pi-trash" rounded outlined style=" height: 32px;width: 32px" @click="(async()=>{ const r = await axios.delete('admin/noteMm/delete', { params: { id: data.id } }); if(r.data.status===200){ ElMessage.success('删除成功'); rows.value = await fetchPageData(nowRow.value, nowPage.value) } else { ElMessage.error('无法连接服务器') } })()"/>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>

      <el-dialog v-model="previewVisible" title="内容预览" width="680px">
        <pre style="white-space: pre-wrap; word-break: break-word;">{{ previewContent }}</pre>
      </el-dialog>
      <AddNote v-model="addVisible"/>
      <NoteDetail v-model="detailVisible" v-model:noteId="currentId" />
    </div>
  </el-scrollbar>
</template>

<style scoped>
.common-layout { height: 100%; padding-left: 1px; padding-right: 15px; background-color: white; }

/* 标题区域 */
.title-container { margin-bottom: 10px; }
.title h1 { color: #334155; font-size: 22px; margin: 0; font-weight: 700; }
.title p { color: #64748b; font-size: 14px; margin: 0; }

/* --- 响应式 Toolbar 样式 --- */
.responsive-toolbar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

/* 顶部栏 */
.toolbar-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

/* 左侧组 */
.group-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

/* 右侧组 */
.group-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

/* 分页控制 */
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}
.page-tag { min-width: 90px; text-align: center; }
.page-btns { display: flex; gap: 2px; }

/* 筛选面板 */
.toolbar-filter-panel {
  background-color: #f8f9fa;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

/* 组件微调 */
.custom-icon { font-size: 16px; }
.custom-input { font-size: 14px; padding: 5px 10px; height: 32px; width: 180px; }

/* 表格容器 */
.table-container { 
  background-color: white; 
  width: 100%; 
  border-radius: 10px; 
  box-shadow: 0 0 0 1px #D9D9D9; 
  padding: 4px; 
  margin-top: 5px;
}

/* 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
  max-height: 200px;
  opacity: 1;
  overflow: hidden;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  max-height: 0;
  opacity: 0;
  padding-top: 0;
  padding-bottom: 0;
  margin-top: 0;
  border: none;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .toolbar-top {
    flex-direction: column;
    align-items: stretch;
  }
  .group-left, .group-right {
    justify-content: space-between;
    width: 100%;
  }
  .custom-input { flex: 1; }
  .hidden-xs-only { display: none; }
  .pagination-controls {
    justify-content: space-between;
    width: 100%;
    margin-top: 5px;
  }
}
</style>