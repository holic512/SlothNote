<script setup lang="ts">
import Button from 'primevue/button';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from 'primevue/inputtext';
import Tag from 'primevue/tag';
import {computed, onBeforeUnmount, onMounted, ref} from 'vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import axios from '../../../../../axios'; // 请根据实际路径调整
import {ElMessage} from 'element-plus';
// 请确保以下导入路径正确
import {calculateRows} from '../FolderMm/components/TableView/calculateRows';
import {fetchFolderInitial, fetchFolderPageData, searchFavoriteFolders, batchDeleteFavoriteFolders, batchRestoreFavoriteFolders} from './components/folderApi';
import {fetchNoteInitial, fetchNotePageData, searchFavoriteNotes, batchDeleteFavoriteNotes, batchEnableFavoriteNotes, batchDisableFavoriteNotes, batchRestoreFavoriteNotes} from './components/noteApi';
import {fetchUserOptions} from '../FolderMm/components/TableView/userOptions';
import AddFavoriteFolder from './components/AddFavoriteFolder/addFavoriteFolder.vue';
import AddFavoriteNote from './components/AddFavoriteNote/addFavoriteNote.vue';
import FavoriteFolderDetail from './components/FolderDetail/favoriteFolderDetail.vue';
import FavoriteNoteDetail from './components/NoteDetail/favoriteNoteDetail.vue';

const activeTab = ref<'folder' | 'note'>('folder');

// --- Filter Visibility States (新增：控制折叠面板) ---
const showFolderFilters = ref(false);
const showNoteFilters = ref(false);

// Folder Filters
const value1 = ref<string | null>(null);
const isDeletedFilter = ref<boolean | null>(null);
const parentIdFilter = ref<number | null>(null);
const userIdFilter = ref<number | null>(null);
const userOptions = ref<any[]>([]);

// Note Filters
const noteQ = ref<string | null>(null);
const noteUserIdFilter = ref<number | null>(null);
const noteFolderIdFilter = ref<number | null>(null);
const noteIdFilter = ref<number | null>(null); // 补充原代码中遗漏的定义
const noteStatusFilter = ref<boolean | null>(null);
const noteIsDeletedFilter = ref<boolean | null>(null);

const minHeight = 720;
const stepHeight = 45;
let nowRow = ref(10);

const folderCount = ref(0);
const noteCount = ref(0);
const maxPage = ref(1);
const nowPage = ref(1);

const folderRows = ref([]);
const noteRows = ref([]);

// ... (onMounted, handleResize, turnPage, refresh 等逻辑保持不变) ...
// 为了节省篇幅，逻辑部分未变动，直接复用你原本的代码逻辑即可
// 注意：原代码中 noteIdFilter 似乎未定义，我在上面补上了

onMounted(async () => {
  nowRow.value = calculateRows(minHeight, stepHeight);
  await axios.get('admin/favoriteMm/folder/getCount').then((r)=>{ folderCount.value = r.data.data; });
  await axios.get('admin/favoriteMm/note/getCount').then((r)=>{ noteCount.value = r.data.data; });
  maxPage.value = Math.ceil((activeTab.value==='folder'?folderCount.value:noteCount.value) / nowRow.value);
  folderRows.value = await fetchFolderInitial(nowRow.value);
  noteRows.value = await fetchNoteInitial(nowRow.value);
  userOptions.value = await fetchUserOptions(undefined, 50);
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => { window.removeEventListener('resize', handleResize); });

const DEBOUNCE_DELAY = 100;
let resizeTimeout: ReturnType<typeof setTimeout>;
const handleResize = async () => {
  clearTimeout(resizeTimeout);
  resizeTimeout = setTimeout(async () => {
    const rows = calculateRows(minHeight, stepHeight);
    if (rows !== nowRow.value) {
      nowRow.value = rows;
      maxPage.value = Math.ceil((activeTab.value==='folder'?folderCount.value:noteCount.value) / nowRow.value);
      if (nowPage.value > maxPage.value) nowPage.value = maxPage.value;
      if (activeTab.value==='folder') folderRows.value = await fetchFolderPageData(nowRow.value, nowPage.value);
      else noteRows.value = await fetchNotePageData(nowRow.value, nowPage.value);
    }
  }, DEBOUNCE_DELAY);
};

const dynamicHeight = computed(() => `${475 + (nowRow.value - 10) * 45}px`);

enum pageTurn { FirstPage, PreviousPage, NextPage, LastPage }
const turnPage = async (turn: pageTurn) => {
  const load = async () => {
    if (activeTab.value==='folder') folderRows.value = await fetchFolderPageData(nowRow.value, nowPage.value);
    else noteRows.value = await fetchNotePageData(nowRow.value, nowPage.value);
  }
  switch (turn) {
    case pageTurn.FirstPage:
      if (nowPage.value != 1) { nowPage.value = 1; await load(); } else { ElMessage.warning('已经是第一页了'); }
      break;
    case pageTurn.PreviousPage:
      if (nowPage.value > 1) { nowPage.value = nowPage.value - 1; await load(); } else { ElMessage.warning('已经是第一页了'); }
      break;
    case pageTurn.NextPage:
      if (nowPage.value < maxPage.value) { nowPage.value = nowPage.value + 1; await load(); } else { ElMessage.warning('已经是最后一页了'); }
      break;
    case pageTurn.LastPage:
      if (nowPage.value != maxPage.value) { nowPage.value = maxPage.value; await load(); } else { ElMessage.warning('已经是最后一页了'); }
      break;
  }
};

const refresh = async () => {
  await axios.get('admin/favoriteMm/folder/getCount').then((r)=>{ folderCount.value = r.data.data; });
  await axios.get('admin/favoriteMm/note/getCount').then((r)=>{ noteCount.value = r.data.data; });
  maxPage.value = Math.ceil((activeTab.value==='folder'?folderCount.value:noteCount.value) / nowRow.value);
  if (maxPage.value < nowPage.value) {
    if (activeTab.value==='folder') folderRows.value = await fetchFolderPageData(nowRow.value, maxPage.value);
    else noteRows.value = await fetchNotePageData(nowRow.value, maxPage.value);
  } else {
    if (activeTab.value==='folder') folderRows.value = await fetchFolderPageData(nowRow.value, nowPage.value);
    else noteRows.value = await fetchNotePageData(nowRow.value, nowPage.value);
  }
  ElMessage.success('刷新成功');
};

const selectedFolder = ref();
const selectedNote = ref();

const batchDeleteFolder = async () => {
  if (!selectedFolder.value || selectedFolder.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedFolder.value.map((p: any) => p.id);
  const s = await batchDeleteFavoriteFolders(ids);
  if (s===200) { ElMessage.success('删除成功'); folderRows.value = await fetchFolderPageData(nowRow.value, nowPage.value); } else { ElMessage.error('无法连接服务器'); }
};

const batchRestoreFolder = async () => {
  if (!selectedFolder.value || selectedFolder.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedFolder.value.map((p: any) => p.id);
  const s = await batchRestoreFavoriteFolders(ids);
  if (s===200) { ElMessage.success('恢复成功'); folderRows.value = await fetchFolderPageData(nowRow.value, nowPage.value); } else { ElMessage.error('无法连接服务器'); }
};

const batchDeleteNote = async () => {
  if (!selectedNote.value || selectedNote.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedNote.value.map((p: any) => p.id);
  const s = await batchDeleteFavoriteNotes(ids);
  if (s===200) { ElMessage.success('删除成功'); noteRows.value = await fetchNotePageData(nowRow.value, nowPage.value); } else { ElMessage.error('无法连接服务器'); }
};

const batchEnableNote = async () => {
  if (!selectedNote.value || selectedNote.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedNote.value.map((p: any) => p.id);
  const s = await batchEnableFavoriteNotes(ids);
  if (s===200) { ElMessage.success('启用成功'); noteRows.value = await fetchNotePageData(nowRow.value, nowPage.value); } else { ElMessage.error('无法连接服务器'); }
};

const batchDisableNote = async () => {
  if (!selectedNote.value || selectedNote.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedNote.value.map((p: any) => p.id);
  const s = await batchDisableFavoriteNotes(ids);
  if (s===200) { ElMessage.success('禁用成功'); noteRows.value = await fetchNotePageData(nowRow.value, nowPage.value); } else { ElMessage.error('无法连接服务器'); }
};

const batchRestoreNote = async () => {
  if (!selectedNote.value || selectedNote.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedNote.value.map((p: any) => p.id);
  const s = await batchRestoreFavoriteNotes(ids);
  if (s===200) { ElMessage.success('恢复成功'); noteRows.value = await fetchNotePageData(nowRow.value, nowPage.value); } else { ElMessage.error('无法连接服务器'); }
};

const doFolderSearch = async () => {
  const data = await searchFavoriteFolders({
    q: value1.value || undefined,
    isDeleted: isDeletedFilter.value === null ? undefined : isDeletedFilter.value,
    userId: userIdFilter.value === null ? undefined : userIdFilter.value,
    parentId: parentIdFilter.value === null ? undefined : parentIdFilter.value,
    pageNum: nowPage.value,
    pageSize: nowRow.value,
  });
  folderRows.value = data.list;
  folderCount.value = data.total;
  maxPage.value = Math.ceil(folderCount.value / nowRow.value);
};

const doNoteSearch = async () => {
  const data = await searchFavoriteNotes({
    q: noteQ.value || undefined,
    isDeleted: noteIsDeletedFilter.value === null ? undefined : noteIsDeletedFilter.value,
    favoriteStatus: noteStatusFilter.value === null ? undefined : noteStatusFilter.value,
    userId: noteUserIdFilter.value === null ? undefined : noteUserIdFilter.value,
    favoriteFolderId: noteFolderIdFilter.value === null ? undefined : noteFolderIdFilter.value,
    pageNum: nowPage.value,
    pageSize: nowRow.value,
  });
  noteRows.value = data.list;
  noteCount.value = data.total;
  maxPage.value = Math.ceil(noteCount.value / nowRow.value);
};

const addFavoriteFolderVisible = ref<boolean>(false);
const addFavoriteNoteVisible = ref<boolean>(false);
const folderDetailVisible = ref<boolean>(false);
const noteDetailVisible = ref<boolean>(false);
const currentFolderId = ref<number | null>(null);
const currentNoteId = ref<number | null>(null);
const openFolderDetail = (id: number) => { currentFolderId.value = id; folderDetailVisible.value = true; };
const openNoteDetail = (id: number) => { currentNoteId.value = id; noteDetailVisible.value = true; };

</script>

<template>
  <el-scrollbar height="100%">
    <div class="common-layout">
      <div class="title-container">
        <div class="title">
          <h1>收藏管理</h1>
          <p>统一管理收藏文件夹与收藏记录</p>
        </div>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <!-- ================== FOLDER TAB ================== -->
        <el-tab-pane label="收藏文件夹" name="folder">
          <div class="responsive-toolbar">
            <!-- 第一行：搜索、开关、主要操作、分页 -->
            <div class="toolbar-top">
              <div class="group-left">
                <IconField>
                  <InputIcon class="pi pi-search custom-icon"/>
                  <InputText v-model="value1" placeholder="Search Name" class="custom-input"/>
                </IconField>
                <!-- 筛选开关按钮 -->
                <Button 
                  :icon="showFolderFilters ? 'pi pi-filter-slash' : 'pi pi-filter'" 
                  :severity="showFolderFilters ? 'primary' : 'secondary'" 
                  outlined 
                  size="small" 
                  @click="showFolderFilters = !showFolderFilters"
                  v-tooltip="'高级筛选'"
                />
                <Button icon="pi pi-search" severity="secondary" outlined size="small" @click="doFolderSearch"/>
              </div>

              <div class="group-right">
                <Button icon="pi pi-plus" severity="secondary" outlined size="small" @click="addFavoriteFolderVisible = true"/>
                <Button icon="pi pi-trash" severity="secondary" outlined size="small" @click="batchDeleteFolder"/>
                <Button icon="pi pi-refresh" severity="secondary" outlined size="small" @click="batchRestoreFolder"/>
                <Button icon="pi pi-spinner" severity="secondary" outlined size="small" @click="refresh"/>
                <div class="pagination-controls">
                  <el-divider direction="vertical" class="hidden-xs-only"/>
                  <Tag severity="info">数量: {{ folderCount }}</Tag>
                  <Tag class="page-tag">页: {{ nowPage }}/{{ maxPage }}</Tag>
                  <div class="page-btns">
                    <Button icon="pi pi-angle-double-left" severity="secondary" text size="small" @click="turnPage(0)"/>
                    <Button icon="pi pi-angle-left" severity="secondary" text size="small" @click="turnPage(1)"/>
                    <Button icon="pi pi-angle-right" severity="secondary" text size="small" @click="turnPage(2)"/>
                    <Button icon="pi pi-angle-double-right" severity="secondary" text size="small" @click="turnPage(3)"/>
                  </div>
                </div>
              </div>
            </div>

            <!-- 第二行：折叠的筛选面板 -->
            <transition name="fade-slide">
              <div v-if="showFolderFilters" class="toolbar-filter-panel">
                 <el-select v-model="isDeletedFilter" placeholder="删除状态" style="width: 120px" clearable>
                  <el-option label="全部" :value="null" />
                  <el-option label="正常" :value="false" />
                  <el-option label="已删除" :value="true" />
                </el-select>
                <el-input-number v-model="parentIdFilter" :min="0" :step="1" placeholder="父ID" controls-position="right" style="width: 120px" />
                <el-select v-model="userIdFilter" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions.value = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
                  <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
                </el-select>
                <!-- 在筛选栏也可以放一个搜索按钮，方便操作 -->
                <Button label="应用筛选" icon="pi pi-check" size="small" outlined @click="doFolderSearch" />
              </div>
            </transition>
          </div>

          <div class="table-container">
            <DataTable v-model:selection="selectedFolder" :value="folderRows" stripedRows dataKey="id" tableStyle="min-width: 950px;" size="small" :style="{ minHeight: dynamicHeight }">
              <Column selectionMode="multiple" headerStyle="width: 50px" position="fixed"></Column>
              <Column field="id" header="ID" headerStyle="width: 60px"></Column>
              <Column field="folderName" header="名称" headerStyle="width: 18%"></Column>
              <Column field="userId" header="用户ID" headerStyle="width: 10%"></Column>
              <Column field="parentId" header="父ID" headerStyle="width: 10%"></Column>
              <Column field="favoriteFolderDescription" header="简介" headerStyle="width: 24%"></Column>
              <Column field="isDeleted" header="状态" headerStyle="width: 10%">
                <template #body="{ data }">
                  <Tag :value="data.isDeleted ? '已删除' : '正常'" :severity="data.isDeleted ? 'danger' : 'success'"/>
                </template>
              </Column>
              <Column header="更多" headerStyle="width: 120px">
                <template #body="{ data }">
                  <div style="display: flex; gap: 6px; align-items: center;">
                    <Button type="button" icon="pi pi-eye" rounded outlined style=" height: 32px;width: 32px" @click="openFolderDetail(data.id)"/>
                    <Button type="button" icon="pi pi-trash" rounded outlined style=" height: 32px;width: 32px" @click="(async()=>{ const r = await axios.delete('admin/favoriteMm/folder/delete', { params: { id: data.id } }); if(r.data.status===200){ ElMessage.success('删除成功'); folderRows.value = await fetchFolderPageData(nowRow.value, nowPage.value) } else { ElMessage.error('无法连接服务器') } })()"/>
                  </div>
                </template>
              </Column>
            </DataTable>
          </div>
        </el-tab-pane>

        <!-- ================== NOTE TAB ================== -->
        <el-tab-pane label="收藏记录" name="note">
          <div class="responsive-toolbar">
             <div class="toolbar-top">
              <div class="group-left">
                <IconField>
                  <InputIcon class="pi pi-search custom-icon"/>
                  <InputText v-model="noteQ" placeholder="Search Note" class="custom-input"/>
                </IconField>
                <!-- 筛选开关 -->
                <Button 
                  :icon="showNoteFilters ? 'pi pi-filter-slash' : 'pi pi-filter'" 
                  :severity="showNoteFilters ? 'primary' : 'secondary'" 
                  outlined 
                  size="small" 
                  @click="showNoteFilters = !showNoteFilters"
                />
                <Button icon="pi pi-search" severity="secondary" outlined size="small" @click="doNoteSearch"/>
              </div>

              <div class="group-right">
                <Button icon="pi pi-plus" severity="secondary" outlined size="small" @click="addFavoriteNoteVisible = true"/>
                <Button icon="pi pi-trash" severity="secondary" outlined size="small" @click="batchDeleteNote"/>
                <Button icon="pi pi-check" severity="secondary" outlined size="small" @click="batchEnableNote"/>
                <Button icon="pi pi-ban" severity="secondary" outlined size="small" @click="batchDisableNote"/>
                <Button icon="pi pi-refresh" severity="secondary" outlined size="small" @click="batchRestoreNote"/>
                <Button icon="pi pi-spinner" severity="secondary" outlined size="small" @click="refresh"/>
                
                <div class="pagination-controls">
                   <el-divider direction="vertical" class="hidden-xs-only"/>
                  <Tag severity="info">数量: {{ noteCount }}</Tag>
                  <Tag class="page-tag">页: {{ nowPage }}/{{ maxPage }}</Tag>
                  <div class="page-btns">
                    <Button icon="pi pi-angle-double-left" severity="secondary" text size="small" @click="turnPage(0)"/>
                    <Button icon="pi pi-angle-left" severity="secondary" text size="small" @click="turnPage(1)"/>
                    <Button icon="pi pi-angle-right" severity="secondary" text size="small" @click="turnPage(2)"/>
                    <Button icon="pi pi-angle-double-right" severity="secondary" text size="small" @click="turnPage(3)"/>
                  </div>
                </div>
              </div>
            </div>

            <!-- 折叠面板 -->
             <transition name="fade-slide">
              <div v-if="showNoteFilters" class="toolbar-filter-panel">
                <el-select v-model="noteStatusFilter" placeholder="状态" style="width: 110px" clearable>
                  <el-option label="全部" :value="null" />
                  <el-option label="已收藏" :value="true" />
                  <el-option label="取消" :value="false" />
                </el-select>
                <el-select v-model="noteIsDeletedFilter" placeholder="删除状态" style="width: 110px" clearable>
                  <el-option label="全部" :value="null" />
                  <el-option label="正常" :value="false" />
                  <el-option label="已删除" :value="true" />
                </el-select>
                <el-input-number v-model="noteFolderIdFilter" :min="0" :step="1" placeholder="收藏夹ID" controls-position="right" style="width: 120px" />
                <el-input-number v-model="noteIdFilter" :min="0" :step="1" placeholder="笔记ID" controls-position="right" style="width: 120px" />
                <el-select v-model="noteUserIdFilter" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions.value = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
                  <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
                </el-select>
                <Button label="应用筛选" icon="pi pi-check" size="small" outlined @click="doNoteSearch" />
              </div>
             </transition>
          </div>

          <div class="table-container">
            <DataTable v-model:selection="selectedNote" :value="noteRows" stripedRows dataKey="id" tableStyle="min-width: 1000px;" size="small" :style="{ minHeight: dynamicHeight }">
              <Column selectionMode="multiple" headerStyle="width: 50px" position="fixed"></Column>
              <Column field="id" header="ID" headerStyle="width: 60px"></Column>
              <Column field="userId" header="用户ID" headerStyle="width: 10%"></Column>
              <Column field="noteId" header="笔记ID" headerStyle="width: 10%"></Column>
              <Column field="favoriteFolderId" header="收藏夹ID" headerStyle="width: 10%"></Column>
              <Column field="favoriteStatus" header="状态" headerStyle="width: 10%">
                <template #body="{ data }">
                  <Tag :value="data.favoriteStatus ? '已收藏' : '取消'" :severity="data.favoriteStatus ? 'success' : 'secondary'"/>
                </template>
              </Column>
              <Column field="noteRemark" header="备注" headerStyle="width: 24%"></Column>
              <Column field="isDeleted" header="删除" headerStyle="width: 10%">
                <template #body="{ data }">
                  <Tag :value="data.isDeleted ? '已删除' : '正常'" :severity="data.isDeleted ? 'danger' : 'success'"/>
                </template>
              </Column>
              <Column header="更多" headerStyle="width: 120px">
                <template #body="{ data }">
                  <div style="display: flex; gap: 6px; align-items: center;">
                    <Button type="button" icon="pi pi-eye" rounded outlined style=" height: 32px;width: 32px" @click="openNoteDetail(data.id)"/>
                    <Button type="button" icon="pi pi-trash" rounded outlined style=" height: 32px;width: 32px" @click="(async()=>{ const r = await axios.delete('admin/favoriteMm/note/delete', { params: { id: data.id } }); if(r.data.status===200){ ElMessage.success('删除成功'); noteRows.value = await fetchNotePageData(nowRow.value, nowPage.value) } else { ElMessage.error('无法连接服务器') } })()"/>
                  </div>
                </template>
              </Column>
            </DataTable>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-scrollbar>

  <AddFavoriteFolder v-model="addFavoriteFolderVisible"/>
  <AddFavoriteNote v-model="addFavoriteNoteVisible"/>
  <FavoriteFolderDetail v-model="folderDetailVisible" v-model:folderId="currentFolderId"/>
  <FavoriteNoteDetail v-model="noteDetailVisible" v-model:noteId="currentNoteId"/>
</template>

<style scoped>
/* 基础样式 */
.common-layout { height: 100%; padding-left: 1px; padding-right: 15px; background-color: white; }
.title-container { margin-bottom: 10px; }
.title h1 { color: #334155; font-size: 22px; margin: 0; font-weight: 700; }
.title p { color: #64748b; font-size: 14px; margin: 0; }
.custom-icon { font-size: 16px; }
.custom-input { font-size: 14px; padding: 5px 10px; height: 32px; width: 180px; }

/* --- 响应式 Toolbar 样式 --- */
.responsive-toolbar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

/* 顶部栏：两端对齐，支持换行 */
.toolbar-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap; /* 关键：允许换行 */
  gap: 10px;
}

/* 左侧组：搜索框等 */
.group-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

/* 右侧组：操作按钮 */
.group-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

/* 分页控制组 */
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap; /* 分页内部尽量不换行 */
}

.page-tag {
  min-width: 90px;
  text-align: center;
}

.page-btns {
  display: flex;
  gap: 2px;
}

/* 筛选面板样式 */
.toolbar-filter-panel {
  background-color: #f8f9fa; /* 浅灰色背景区分 */
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

/* 表格容器 */
.table-container { background-color: white; width: 100%; border-radius: 10px; box-shadow: 0 0 0 1px #D9D9D9; padding: 4px; margin-top: 5px; }

/* 动画效果 */
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

/* 移动端适配微调 */
@media (max-width: 768px) {
  .toolbar-top {
    flex-direction: column;
    align-items: stretch;
  }
  .group-left, .group-right {
    justify-content: space-between; /* 手机端撑满宽度 */
    width: 100%;
  }
  .custom-input {
    flex: 1; /* 搜索框自适应宽度 */
  }
  .hidden-xs-only {
    display: none;
  }
}
</style>