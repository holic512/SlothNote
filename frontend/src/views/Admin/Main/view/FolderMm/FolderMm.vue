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
import fetchInitialPageData from './components/TableView/fetchInitialPageData';
import {fetchPageData} from './components/TableView/fetchPageData';
import {calculateRows} from './components/TableView/calculateRows';
import {ElMessage} from 'element-plus';
import {fetchUserOptions} from './components/TableView/userOptions';
import {BatchDeleteFolder} from './components/TableView/batchDeleteFolder';
import {searchFolders} from './components/TableView/searchFolders';
import AddFolder from './components/AddFolder/addFolder.vue';
import FolderDetail from './components/FolderDetail/folderDetail.vue';

// --- 响应式折叠控制 ---
const showFilters = ref(false);

const value1 = ref<string | null>(null);
const isDeletedFilter = ref<number | null>(null);
const parentIdFilter = ref<number | null>(null);
const userIdFilter = ref<number | null>(null);
const userOptions = ref<any[]>([]);

const minHeight = 720;
const stepHeight = 45;
let nowRow = ref(10);
const folderCount = ref(0);
const maxPage = ref(1);
const nowPage = ref(1);
const products = ref([]);

onMounted(async () => {
  nowRow.value = calculateRows(minHeight, stepHeight);
  await axios.get('admin/folderMm/getFolderCount').then((response) => {
    folderCount.value = response.data.data;
    maxPage.value = Math.ceil(folderCount.value / nowRow.value);
  });
  products.value = await fetchInitialPageData(nowRow.value);
  window.addEventListener('resize', handleResize);
  userOptions.value = await fetchUserOptions(undefined, 50);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});

const DEBOUNCE_DELAY = 100;
let resizeTimeout: ReturnType<typeof setTimeout>;
const handleResize = async () => {
  clearTimeout(resizeTimeout);
  resizeTimeout = setTimeout(async () => {
    const rows = calculateRows(minHeight, stepHeight);
    if (rows !== nowRow.value) {
      nowRow.value = rows;
      maxPage.value = Math.ceil(folderCount.value / nowRow.value);
      if (nowPage.value > maxPage.value) {
        nowPage.value = maxPage.value;
      }
      products.value = await fetchPageData(nowRow.value, nowPage.value);
    }
  }, DEBOUNCE_DELAY);
};

const dynamicHeight = computed(() => `${475 + (nowRow.value - 10) * 45}px`);

enum pageTurn { FirstPage, PreviousPage, NextPage, LastPage }
const turnPage = async (turn: pageTurn) => {
  switch (turn) {
    case pageTurn.FirstPage:
      if (nowPage.value != 1) {
        nowPage.value = 1;
        products.value = await fetchPageData(nowRow.value, nowPage.value);
      } else {
        ElMessage.warning('已经是第一页了');
      }
      break;
    case pageTurn.PreviousPage:
      if (nowPage.value > 1) {
        nowPage.value = nowPage.value - 1;
        products.value = await fetchPageData(nowRow.value, nowPage.value);
      } else {
        ElMessage.warning('已经是第一页了');
      }
      break;
    case pageTurn.NextPage:
      if (nowPage.value < maxPage.value) {
        nowPage.value = nowPage.value + 1;
        products.value = await fetchPageData(nowRow.value, nowPage.value);
      } else {
        ElMessage.warning('已经是最后一页了');
      }
      break;
    case pageTurn.LastPage:
      if (nowPage.value != maxPage.value) {
        nowPage.value = maxPage.value;
        products.value = await fetchPageData(nowRow.value, nowPage.value);
      } else {
        ElMessage.warning('已经是最后一页了');
      }
      break;
    default:
      console.error(`Unknown turn: ${turn}`);
  }
};

const refresh = async () => {
  await axios.get('admin/folderMm/getFolderCount').then((response) => {
    folderCount.value = response.data.data;
    maxPage.value = Math.ceil(folderCount.value / nowRow.value);
  });
  if (maxPage.value < nowPage.value) {
    products.value = await fetchPageData(nowRow.value, maxPage.value);
  } else {
    products.value = await fetchPageData(nowRow.value, nowPage.value);
  }
  ElMessage.success('刷新成功');
};

const selectedProduct = ref();

const batchDelete = async () => {
  if (!selectedProduct.value || selectedProduct.value.length === 0) {
    ElMessage.warning('选择为空');
    return;
  }
  const ids = selectedProduct.value.map((p: any) => p.id);
  const status = await BatchDeleteFolder(ids);
  if (status === 200) {
    ElMessage.success('删除成功');
    products.value = await fetchPageData(nowRow.value, nowPage.value);
  } else {
    ElMessage.error('无法连接服务器');
  }
};

const doSearch = async () => {
  const data = await searchFolders({
    q: value1.value || undefined,
    isDeleted: isDeletedFilter.value === null ? undefined : isDeletedFilter.value,
    userId: userIdFilter.value === null ? undefined : userIdFilter.value,
    parentId: parentIdFilter.value === null ? undefined : parentIdFilter.value,
    pageNum: nowPage.value,
    pageSize: nowRow.value,
  });
  products.value = data.list;
  folderCount.value = data.total;
  maxPage.value = Math.ceil(folderCount.value / nowRow.value);
};

const addFolderVisible = ref<boolean>(false);
const folderDetailVisible = ref<boolean>(false);
const currentFolderId = ref<number | null>(null);
const openDetail = (id: number) => {
  currentFolderId.value = id;
  folderDetailVisible.value = true;
};

const getDeletedMsg = (d: number) => (d === 1 ? '已删除' : '正常');
const getDeletedType = (d: number) => (d === 1 ? 'danger' : 'success');

</script>

<template>
  <el-scrollbar height="100%">
    <div class="common-layout">
      <!-- 标题区域 -->
      <div class="title-container">
        <div class="title">
          <h1>文件夹管理</h1>
          <p>对系统文件夹进行统一管理</p>
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
              <InputText v-model="value1" placeholder="Search Folder" class="custom-input"/>
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
                    v-tooltip.bottom="{ value: '添加文件夹', showDelay: 1000, hideDelay: 300 }"
                    @click="addFolderVisible = true"/>

            <Button icon="pi pi-trash" severity="secondary" outlined size="small"
                    @click="batchDelete"
                    v-tooltip.bottom="{ value: '删除选中文件夹', showDelay: 1000, hideDelay: 300 }"/>

            <Button icon="pi pi-spinner" severity="secondary" outlined size="small"
                    @click="refresh"
                    v-tooltip.bottom="{ value: '刷新', showDelay: 1000, hideDelay: 300 }"/>

            <div class="pagination-controls">
              <el-divider direction="vertical" class="hidden-xs-only"/>
              <Tag severity="info">文件夹数: {{ folderCount }}</Tag>
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

            <el-input-number v-model="parentIdFilter" :min="0" :step="1" placeholder="父ID" controls-position="right" style="width: 120px" />

            <el-select v-model="userIdFilter" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions.value = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
              <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
            </el-select>

            <Button label="应用筛选" icon="pi pi-check" size="small" outlined @click="doSearch" />
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
                <Button type="button" icon="pi pi-eye" rounded outlined style=" height: 32px;width: 32px" @click="openDetail(data.id)"/>
                <Button type="button" icon="pi pi-trash" rounded outlined style=" height: 32px;width: 32px" @click="(async()=>{ const response = await axios.delete('admin/folderMm/delete', { params: { id: data.id } }); if(response.data.status===200){ ElMessage.success('删除成功'); products.value = await fetchPageData(nowRow.value, nowPage.value) } else { ElMessage.error('无法连接服务器') } })()"/>
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

/* 标题区域 */
.title-container {
  margin-bottom: 10px;
}
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