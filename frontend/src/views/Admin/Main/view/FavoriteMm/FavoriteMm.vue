<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import axios from '../../../../../axios'; // 请根据实际路径调整
import {ElMessage} from 'element-plus';
// 请确保以下导入路径正确
import {calculateRows} from '../FolderMm/components/TableView/calculateRows';
import {searchFavoriteFolders, batchDeleteFavoriteFolders, batchRestoreFavoriteFolders} from './components/folderApi';
import {searchFavoriteNotes, batchDeleteFavoriteNotes, batchEnableFavoriteNotes, batchDisableFavoriteNotes, batchRestoreFavoriteNotes} from './components/noteApi';
import {fetchUserOptions} from '../FolderMm/components/TableView/userOptions';
import AddFavoriteFolder from './components/AddFavoriteFolder/addFavoriteFolder.vue';
import AddFavoriteNote from './components/AddFavoriteNote/addFavoriteNote.vue';
import FavoriteFolderDetail from './components/FolderDetail/favoriteFolderDetail.vue';
import FavoriteNoteDetail from './components/NoteDetail/favoriteNoteDetail.vue';
import {getMaxPage, useLatestRequest} from '../../composables/useAdminListRequest';

const activeTab = ref<'folder' | 'note'>('folder');

// --- Filter Visibility States (新增：控制折叠面板) ---
const showFolderFilters = ref(false);
const showNoteFilters = ref(false);

// Folder Filters
const value1 = ref<string | null>(null);
const isDeletedFilter = ref<boolean | undefined>(undefined);
const parentIdFilter = ref<number | undefined>(undefined);
const userIdFilter = ref<number | undefined>(undefined);
const userOptions = ref<any[]>([]);

// Note Filters
const noteQ = ref<string | null>(null);
const noteUserIdFilter = ref<number | undefined>(undefined);
const noteFolderIdFilter = ref<number | undefined>(undefined);
const noteIdFilter = ref<number | undefined>(undefined); // 补充原代码中遗漏的定义
const noteStatusFilter = ref<boolean | undefined>(undefined);
const noteIsDeletedFilter = ref<boolean | undefined>(undefined);

const minHeight = 720;
const stepHeight = 45;
const nowRow = ref(10);

const folderCount = ref(0);
const noteCount = ref(0);
const folderMaxPage = ref(1);
const noteMaxPage = ref(1);
const folderPage = ref(1);
const notePage = ref(1);

const folderRows = ref<any[]>([]);
const noteRows = ref<any[]>([]);
const selectedFolder = ref<any[]>([]);
const selectedNote = ref<any[]>([]);

const folderRequest = useLatestRequest();
const noteRequest = useLatestRequest();

const loadFolder = async (page = folderPage.value) => {
  const pageSize = nowRow.value;
  const requestedPage = Math.max(1, page);
  folderPage.value = requestedPage;
  const filters = {
    q: value1.value || undefined,
    isDeleted: isDeletedFilter.value,
    userId: userIdFilter.value,
    parentId: parentIdFilter.value,
  };

  return folderRequest.runLatest(
      async (signal) => {
        const requestPage = (pageNum: number) => searchFavoriteFolders({
          ...filters,
          pageNum,
          pageSize,
        }, signal);
        let data = await requestPage(requestedPage);
        const maxPage = getMaxPage(data.total, pageSize);
        const resolvedPage = Math.min(requestedPage, maxPage);
        if (resolvedPage !== requestedPage) data = await requestPage(resolvedPage);
        return {data, maxPage, resolvedPage};
      },
      ({data, maxPage, resolvedPage}) => {
        folderCount.value = data.total;
        folderMaxPage.value = maxPage;
        folderPage.value = resolvedPage;
        folderRows.value = data.list;
        selectedFolder.value = [];
      },
  );
};

const loadNote = async (page = notePage.value) => {
  const pageSize = nowRow.value;
  const requestedPage = Math.max(1, page);
  notePage.value = requestedPage;
  const filters = {
    q: noteQ.value || undefined,
    isDeleted: noteIsDeletedFilter.value,
    favoriteStatus: noteStatusFilter.value,
    userId: noteUserIdFilter.value,
    favoriteFolderId: noteFolderIdFilter.value,
    noteId: noteIdFilter.value,
  };

  return noteRequest.runLatest(
      async (signal) => {
        const requestPage = (pageNum: number) => searchFavoriteNotes({
          ...filters,
          pageNum,
          pageSize,
        }, signal);
        let data = await requestPage(requestedPage);
        const maxPage = getMaxPage(data.total, pageSize);
        const resolvedPage = Math.min(requestedPage, maxPage);
        if (resolvedPage !== requestedPage) data = await requestPage(resolvedPage);
        return {data, maxPage, resolvedPage};
      },
      ({data, maxPage, resolvedPage}) => {
        noteCount.value = data.total;
        noteMaxPage.value = maxPage;
        notePage.value = resolvedPage;
        noteRows.value = data.list;
        selectedNote.value = [];
      },
  );
};

const loadActiveTab = async () => {
  if (activeTab.value === 'folder') {
    return loadFolder();
  }
  return loadNote();
};

onMounted(async () => {
  nowRow.value = calculateRows(minHeight, stepHeight);
  await Promise.all([
    loadFolder(),
    fetchUserOptions(undefined, 50).then((options) => {
      userOptions.value = options;
    }),
  ]);
  window.addEventListener('resize', handleResize);
});

watch(activeTab, async () => {
  await loadActiveTab();
});

const DEBOUNCE_DELAY = 100;
let resizeTimeout: ReturnType<typeof setTimeout> | undefined;
const handleResize = () => {
  if (resizeTimeout) clearTimeout(resizeTimeout);
  resizeTimeout = setTimeout(async () => {
    resizeTimeout = undefined;
    const rows = calculateRows(minHeight, stepHeight);
    if (rows === nowRow.value) return;

    nowRow.value = rows;
    folderMaxPage.value = getMaxPage(folderCount.value, rows);
    noteMaxPage.value = getMaxPage(noteCount.value, rows);
    folderPage.value = Math.min(folderPage.value, folderMaxPage.value);
    notePage.value = Math.min(notePage.value, noteMaxPage.value);
    await loadActiveTab();
  }, DEBOUNCE_DELAY);
};

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (resizeTimeout) clearTimeout(resizeTimeout);
});

const dynamicHeight = computed(() => `${475 + (nowRow.value - 10) * 45}px`);

enum pageTurn { FirstPage, PreviousPage, NextPage, LastPage }
const turnPage = async (turn: pageTurn) => {
  const currentPage = activeTab.value === 'folder' ? folderPage.value : notePage.value;
  const maxPage = activeTab.value === 'folder' ? folderMaxPage.value : noteMaxPage.value;
  let targetPage = currentPage;

  if (turn === pageTurn.FirstPage) targetPage = 1;
  if (turn === pageTurn.PreviousPage) targetPage = Math.max(1, currentPage - 1);
  if (turn === pageTurn.NextPage) targetPage = Math.min(maxPage, currentPage + 1);
  if (turn === pageTurn.LastPage) targetPage = maxPage;

  if (targetPage === currentPage) {
    ElMessage.warning(turn === pageTurn.FirstPage || turn === pageTurn.PreviousPage ? '已经是第一页了' : '已经是最后一页了');
    return;
  }

  if (activeTab.value === 'folder') {
    await loadFolder(targetPage);
  } else {
    await loadNote(targetPage);
  }
};

const refreshFolder = () => loadFolder();
const refreshNote = () => loadNote();

const refresh = async () => {
  const committed = activeTab.value === 'folder' ? await refreshFolder() : await refreshNote();
  if (committed) ElMessage.success('刷新成功');
};

const batchDeleteFolder = async () => {
  if (!selectedFolder.value || selectedFolder.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedFolder.value.map((p: any) => p.id);
  const s = await batchDeleteFavoriteFolders(ids);
  if (s===200) { ElMessage.success('删除成功'); await refreshFolder(); } else { ElMessage.error('无法连接服务器'); }
};

const batchRestoreFolder = async () => {
  if (!selectedFolder.value || selectedFolder.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedFolder.value.map((p: any) => p.id);
  const s = await batchRestoreFavoriteFolders(ids);
  if (s===200) { ElMessage.success('恢复成功'); await refreshFolder(); } else { ElMessage.error('无法连接服务器'); }
};

const batchDeleteNote = async () => {
  if (!selectedNote.value || selectedNote.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedNote.value.map((p: any) => p.id);
  const s = await batchDeleteFavoriteNotes(ids);
  if (s===200) { ElMessage.success('删除成功'); await refreshNote(); } else { ElMessage.error('无法连接服务器'); }
};

const batchEnableNote = async () => {
  if (!selectedNote.value || selectedNote.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedNote.value.map((p: any) => p.id);
  const s = await batchEnableFavoriteNotes(ids);
  if (s===200) { ElMessage.success('启用成功'); await loadNote(); } else { ElMessage.error('无法连接服务器'); }
};

const batchDisableNote = async () => {
  if (!selectedNote.value || selectedNote.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedNote.value.map((p: any) => p.id);
  const s = await batchDisableFavoriteNotes(ids);
  if (s===200) { ElMessage.success('禁用成功'); await loadNote(); } else { ElMessage.error('无法连接服务器'); }
};

const batchRestoreNote = async () => {
  if (!selectedNote.value || selectedNote.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedNote.value.map((p: any) => p.id);
  const s = await batchRestoreFavoriteNotes(ids);
  if (s===200) { ElMessage.success('恢复成功'); await refreshNote(); } else { ElMessage.error('无法连接服务器'); }
};

const doFolderSearch = async () => {
  folderPage.value = 1;
  await loadFolder(1);
};

const doNoteSearch = async () => {
  notePage.value = 1;
  await loadNote(1);
};

const handleDeleteFolder = async (id: number) => {
  const response = await axios.delete('admin/favoriteMm/folder/delete', {params: {id}});
  if (response.data.status === 200) {
    ElMessage.success('删除成功');
    await refreshFolder();
  } else {
    ElMessage.error('无法连接服务器');
  }
};

const handleDeleteNote = async (id: number) => {
  const response = await axios.delete('admin/favoriteMm/note/delete', {params: {id}});
  if (response.data.status === 200) {
    ElMessage.success('删除成功');
    await refreshNote();
  } else {
    ElMessage.error('无法连接服务器');
  }
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
    <div class="common-layout admin-list-page">
      <el-tabs v-model="activeTab" type="card">
        <!-- ================== FOLDER TAB ================== -->
        <el-tab-pane label="收藏文件夹" name="folder">
          <div class="responsive-toolbar">
            <!-- 第一行：搜索、开关、主要操作、分页 -->
            <div class="toolbar-top">
              <div class="group-left">
                <IconField>
                  <InputIcon icon="Search" class="custom-icon"/>
                  <InputText v-model="value1" placeholder="Search Name" class="custom-input"/>
                </IconField>
                <!-- 筛选开关按钮 -->
                <Button 
                  :icon="showFolderFilters ? 'FilterSlash' : 'Filter'" 
                  :severity="showFolderFilters ? 'primary' : 'secondary'" 
                  outlined 
                  size="small" 
                  @click="showFolderFilters = !showFolderFilters"
                  v-tooltip="'高级筛选'"
                />
                <Button icon="Search" severity="secondary" outlined size="small" @click="doFolderSearch"/>
              </div>

              <div class="group-right">
                <Button icon="Plus" severity="secondary" outlined size="small" @click="addFavoriteFolderVisible = true"/>
                <Button icon="Trash" severity="secondary" outlined size="small" @click="batchDeleteFolder"/>
                <Button icon="Refresh" severity="secondary" outlined size="small" @click="batchRestoreFolder"/>
                <Button icon="Spinner" severity="secondary" outlined size="small" @click="refresh"/>
                <div class="pagination-controls">
                  <el-divider direction="vertical" class="hidden-xs-only"/>
                  <Tag severity="info">数量: {{ folderCount }}</Tag>
                  <Tag class="page-tag">页: {{ folderPage }}/{{ folderMaxPage }}</Tag>
                  <div class="page-btns">
                    <Button icon="AngleDoubleLeft" severity="secondary" text size="small" @click="turnPage(0)"/>
                    <Button icon="AngleLeft" severity="secondary" text size="small" @click="turnPage(1)"/>
                    <Button icon="AngleRight" severity="secondary" text size="small" @click="turnPage(2)"/>
                    <Button icon="AngleDoubleRight" severity="secondary" text size="small" @click="turnPage(3)"/>
                  </div>
                </div>
              </div>
            </div>

            <!-- 第二行：折叠的筛选面板 -->
            <transition name="fade-slide">
              <div v-if="showFolderFilters" class="toolbar-filter-panel">
                 <el-select v-model="isDeletedFilter" placeholder="删除状态" style="width: 120px" clearable>
                  <el-option label="正常" :value="false" />
                  <el-option label="已删除" :value="true" />
                </el-select>
                <el-input-number v-model="parentIdFilter" :min="0" :step="1" placeholder="父ID" controls-position="right" style="width: 120px" />
                <el-select v-model="userIdFilter" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
                  <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
                </el-select>
                <!-- 在筛选栏也可以放一个搜索按钮，方便操作 -->
                <Button label="应用筛选" icon="Check" size="small" outlined @click="doFolderSearch" />
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
                    <Button type="button" icon="Eye" rounded outlined style=" height: 32px;width: 32px" @click="openFolderDetail(data.id)"/>
                    <Button type="button" icon="Trash" rounded outlined style=" height: 32px;width: 32px" @click="handleDeleteFolder(data.id)"/>
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
                  <InputIcon icon="Search" class="custom-icon"/>
                  <InputText v-model="noteQ" placeholder="Search Note" class="custom-input"/>
                </IconField>
                <!-- 筛选开关 -->
                <Button 
                  :icon="showNoteFilters ? 'FilterSlash' : 'Filter'" 
                  :severity="showNoteFilters ? 'primary' : 'secondary'" 
                  outlined 
                  size="small" 
                  @click="showNoteFilters = !showNoteFilters"
                />
                <Button icon="Search" severity="secondary" outlined size="small" @click="doNoteSearch"/>
              </div>

              <div class="group-right">
                <Button icon="Plus" severity="secondary" outlined size="small" @click="addFavoriteNoteVisible = true"/>
                <Button icon="Trash" severity="secondary" outlined size="small" @click="batchDeleteNote"/>
                <Button icon="Check" severity="secondary" outlined size="small" @click="batchEnableNote"/>
                <Button icon="Ban" severity="secondary" outlined size="small" @click="batchDisableNote"/>
                <Button icon="Refresh" severity="secondary" outlined size="small" @click="batchRestoreNote"/>
                <Button icon="Spinner" severity="secondary" outlined size="small" @click="refresh"/>
                
                <div class="pagination-controls">
                   <el-divider direction="vertical" class="hidden-xs-only"/>
                  <Tag severity="info">数量: {{ noteCount }}</Tag>
                  <Tag class="page-tag">页: {{ notePage }}/{{ noteMaxPage }}</Tag>
                  <div class="page-btns">
                    <Button icon="AngleDoubleLeft" severity="secondary" text size="small" @click="turnPage(0)"/>
                    <Button icon="AngleLeft" severity="secondary" text size="small" @click="turnPage(1)"/>
                    <Button icon="AngleRight" severity="secondary" text size="small" @click="turnPage(2)"/>
                    <Button icon="AngleDoubleRight" severity="secondary" text size="small" @click="turnPage(3)"/>
                  </div>
                </div>
              </div>
            </div>

            <!-- 折叠面板 -->
             <transition name="fade-slide">
              <div v-if="showNoteFilters" class="toolbar-filter-panel">
                <el-select v-model="noteStatusFilter" placeholder="状态" style="width: 110px" clearable>
                  <el-option label="已收藏" :value="true" />
                  <el-option label="取消" :value="false" />
                </el-select>
                <el-select v-model="noteIsDeletedFilter" placeholder="删除状态" style="width: 110px" clearable>
                  <el-option label="正常" :value="false" />
                  <el-option label="已删除" :value="true" />
                </el-select>
                <el-input-number v-model="noteFolderIdFilter" :min="0" :step="1" placeholder="收藏夹ID" controls-position="right" style="width: 120px" />
                <el-input-number v-model="noteIdFilter" :min="0" :step="1" placeholder="笔记ID" controls-position="right" style="width: 120px" />
                <el-select v-model="noteUserIdFilter" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
                  <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
                </el-select>
                <Button label="应用筛选" icon="Check" size="small" outlined @click="doNoteSearch" />
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
                    <Button type="button" icon="Eye" rounded outlined style=" height: 32px;width: 32px" @click="openNoteDetail(data.id)"/>
                    <Button type="button" icon="Trash" rounded outlined style=" height: 32px;width: 32px" @click="handleDeleteNote(data.id)"/>
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
