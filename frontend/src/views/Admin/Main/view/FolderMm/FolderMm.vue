<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from 'vue';
import axios from '../../../../../axios'; // 请确认路径
import {calculateRows} from './components/TableView/calculateRows';
import {ElMessage} from 'element-plus';
import {fetchUserOptions} from './components/TableView/userOptions';
import {BatchDeleteFolder} from './components/TableView/batchDeleteFolder';
import {searchFolders} from './components/TableView/searchFolders';
import AddFolder from './components/AddFolder/addFolder.vue';
import FolderDetail from './components/FolderDetail/folderDetail.vue';
import {getMaxPage, useLatestRequest} from '../../composables/useAdminListRequest';

// --- 响应式折叠控制 ---
const showFilters = ref(false);

const value1 = ref<string | null>(null);
const isDeletedFilter = ref<number | undefined>(undefined);
const parentIdFilter = ref<number | undefined>(undefined);
const userIdFilter = ref<number | undefined>(undefined);
const userOptions = ref<any[]>([]);

const minHeight = 720;
const stepHeight = 45;
let nowRow = ref(10);
const folderCount = ref(0);
const maxPage = ref(1);
const nowPage = ref(1);
const products = ref<any[]>([]);
const selectedProduct = ref<any[]>([]);
const listRequest = useLatestRequest();

const loadFolders = async (page = nowPage.value) => {
  const pageSize = nowRow.value;
  const requestedPage = Math.max(1, page);
  nowPage.value = requestedPage;
  const filters = {
    q: value1.value || undefined,
    isDeleted: isDeletedFilter.value,
    userId: userIdFilter.value,
    parentId: parentIdFilter.value,
  };

  return listRequest.runLatest(
      async (signal) => {
        const requestPage = (pageNum: number) => searchFolders({
          ...filters,
          pageNum,
          pageSize,
        }, signal);
        let data = await requestPage(requestedPage);
        const resolvedMaxPage = getMaxPage(data.total, pageSize);
        const resolvedPage = Math.min(requestedPage, resolvedMaxPage);
        if (resolvedPage !== requestedPage) data = await requestPage(resolvedPage);
        return {data, resolvedMaxPage, resolvedPage};
      },
      ({data, resolvedMaxPage, resolvedPage}) => {
        folderCount.value = data.total;
        maxPage.value = resolvedMaxPage;
        nowPage.value = resolvedPage;
        products.value = data.list;
        selectedProduct.value = [];
      },
  );
};

onMounted(async () => {
  nowRow.value = calculateRows(minHeight, stepHeight);
  await Promise.all([
    loadFolders(),
    fetchUserOptions(undefined, 50).then((options) => {
      userOptions.value = options;
    }),
  ]);
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (resizeTimeout) clearTimeout(resizeTimeout);
});

const DEBOUNCE_DELAY = 100;
let resizeTimeout: ReturnType<typeof setTimeout> | undefined;
const handleResize = () => {
  if (resizeTimeout) clearTimeout(resizeTimeout);
  resizeTimeout = setTimeout(async () => {
    resizeTimeout = undefined;
    const rows = calculateRows(minHeight, stepHeight);
    if (rows !== nowRow.value) {
      nowRow.value = rows;
      await loadFolders();
    }
  }, DEBOUNCE_DELAY);
};

const dynamicHeight = computed(() => `${475 + (nowRow.value - 10) * 45}px`);

enum pageTurn { FirstPage, PreviousPage, NextPage, LastPage }
const turnPage = async (turn: pageTurn) => {
  switch (turn) {
    case pageTurn.FirstPage:
      if (nowPage.value != 1) {
        await loadFolders(1);
      } else {
        ElMessage.warning('已经是第一页了');
      }
      break;
    case pageTurn.PreviousPage:
      if (nowPage.value > 1) {
        await loadFolders(nowPage.value - 1);
      } else {
        ElMessage.warning('已经是第一页了');
      }
      break;
    case pageTurn.NextPage:
      if (nowPage.value < maxPage.value) {
        await loadFolders(nowPage.value + 1);
      } else {
        ElMessage.warning('已经是最后一页了');
      }
      break;
    case pageTurn.LastPage:
      if (nowPage.value != maxPage.value) {
        await loadFolders(maxPage.value);
      } else {
        ElMessage.warning('已经是最后一页了');
      }
      break;
    default:
      console.error(`Unknown turn: ${turn}`);
  }
};

const refresh = async () => {
  const committed = await loadFolders();
  if (committed) ElMessage.success('刷新成功');
};

const batchDelete = async () => {
  if (!selectedProduct.value || selectedProduct.value.length === 0) {
    ElMessage.warning('选择为空');
    return;
  }
  const ids = selectedProduct.value.map((p: any) => p.id);
  const status = await BatchDeleteFolder(ids);
  if (status === 200) {
    ElMessage.success('删除成功');
    await loadFolders();
  } else {
    ElMessage.error('无法连接服务器');
  }
};

const doSearch = async () => {
  nowPage.value = 1;
  await loadFolders(1);
};

const addFolderVisible = ref<boolean>(false);
const folderDetailVisible = ref<boolean>(false);
const currentFolderId = ref<number | null>(null);
const openDetail = (id: number) => {
  currentFolderId.value = id;
  folderDetailVisible.value = true;
};

const handleDeleteFolder = async (id: number) => {
  const response = await axios.delete('admin/folderMm/delete', {params: {id}});
  if (response.data.status === 200) {
    ElMessage.success('删除成功');
    await loadFolders();
  } else {
    ElMessage.error('无法连接服务器');
  }
};

const getDeletedMsg = (d: number) => (d === 1 ? '已删除' : '正常');
const getDeletedType = (d: number) => (d === 1 ? 'danger' : 'success');

</script>

<template>
  <el-scrollbar height="100%">
    <div class="common-layout">
      <!-- 响应式工具栏 -->
      <div class="responsive-toolbar">
        <!-- 第一行：常用操作 -->
        <div class="toolbar-top">
          <!-- 左侧：搜索 + 筛选开关 -->
          <div class="group-left">
            <IconField>
              <InputIcon icon="Search" class="custom-icon"/>
              <InputText v-model="value1" placeholder="Search Folder" class="custom-input"/>
            </IconField>

            <!-- 筛选开关按钮 -->
            <Button
                :icon="showFilters ? 'FilterSlash' : 'Filter'"
                :severity="showFilters ? 'primary' : 'secondary'"
                outlined
                size="small"
                @click="showFilters = !showFilters"
                v-tooltip="'高级筛选'"
            />

            <Button icon="Search" severity="secondary" outlined size="small"
                    @click="doSearch"
                    v-tooltip.bottom="{ value: '搜索', showDelay: 1000, hideDelay: 300 }"/>
          </div>

          <!-- 右侧：增删改 + 分页 -->
          <div class="group-right">
            <Button icon="Plus" severity="secondary" outlined size="small"
                    v-tooltip.bottom="{ value: '添加文件夹', showDelay: 1000, hideDelay: 300 }"
                    @click="addFolderVisible = true"/>

            <Button icon="Trash" severity="secondary" outlined size="small"
                    @click="batchDelete"
                    v-tooltip.bottom="{ value: '删除选中文件夹', showDelay: 1000, hideDelay: 300 }"/>

            <Button icon="Spinner" severity="secondary" outlined size="small"
                    @click="refresh"
                    v-tooltip.bottom="{ value: '刷新', showDelay: 1000, hideDelay: 300 }"/>

            <div class="pagination-controls">
              <el-divider direction="vertical" class="hidden-xs-only"/>
              <Tag severity="info">文件夹数: {{ folderCount }}</Tag>
              <Tag class="page-tag">页: {{ nowPage }}/{{ maxPage }}</Tag>

              <div class="page-btns">
                <Button icon="AngleDoubleLeft" severity="secondary" text size="small"
                        @click="turnPage(0)" />
                <Button icon="AngleLeft" severity="secondary" text size="small"
                        @click="turnPage(1)" />
                <Button icon="AngleRight" severity="secondary" text size="small"
                        @click="turnPage(2)" />
                <Button icon="AngleDoubleRight" severity="secondary" text size="small"
                        @click="turnPage(3)" />
              </div>
            </div>
          </div>
        </div>

        <!-- 第二行：折叠筛选面板 -->
        <transition name="fade-slide">
          <div v-if="showFilters" class="toolbar-filter-panel">
            <el-select v-model="isDeletedFilter" placeholder="删除状态" style="width: 120px" clearable>
              <el-option label="正常" :value="0" />
              <el-option label="已删除" :value="1" />
            </el-select>

            <el-input-number v-model="parentIdFilter" :min="0" :step="1" placeholder="父ID" controls-position="right" style="width: 120px" />

            <el-select v-model="userIdFilter" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
              <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
            </el-select>

            <Button label="应用筛选" icon="Check" size="small" outlined @click="doSearch" />
          </div>
        </transition>
      </div>

      <div class="table-container">
        <DataTable v-model:selection="selectedProduct" :value="products" stripedRows dataKey="id"
                   tableStyle="min-width: 950px;" size="small" :style="{ minHeight: dynamicHeight }">
          <Column selectionMode="multiple" headerStyle="width: 50px" position="fixed"></Column>
          <Column field="id" header="ID" headerStyle="width: 60px"></Column>
          <Column field="folderName" header="文件夹名" headerStyle="width: 18%"></Column>
          <Column field="userId" header="用户ID" headerStyle="width: 10%"></Column>
          <Column field="parentId" header="父ID" headerStyle="width: 10%"></Column>
          <Column field="description" header="简介" headerStyle="width: 20%"></Column>
          <Column field="folderAvatar" header="头像" headerStyle="width: 8%"></Column>
          <Column field="isDeleted" header="状态" headerStyle="width: 10%">
            <template #body="{ data }">
              <Tag :value="getDeletedMsg(data.isDeleted)" :severity="getDeletedType(data.isDeleted)"/>
            </template>
          </Column>
          <Column header="更多" headerStyle="width: 120px">
            <template #body="{ data }">
              <div style="display: flex; gap: 6px; align-items: center;">
                <Button type="button" icon="Eye" rounded outlined style=" height: 32px;width: 32px" @click="openDetail(data.id)"/>
                <Button type="button" icon="Trash" rounded outlined style=" height: 32px;width: 32px" @click="handleDeleteFolder(data.id)"/>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>
  </el-scrollbar>

  <AddFolder v-model="addFolderVisible"/>
  <FolderDetail v-model="folderDetailVisible" v-model:folderId="currentFolderId"/>
</template>

<style scoped>
.common-layout {
  height: 100%;
  padding-left: 1px;
  padding-right: 15px;
  background-color: white;
}

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
