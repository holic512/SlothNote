<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import axios from '../../../../../axios'; // 请确认路径
import {ElMessage} from 'element-plus';
import {calculateRows} from '../FolderMm/components/TableView/calculateRows';
import {fetchUserOptions} from '../FolderMm/components/TableView/userOptions';
import {searchCategories, batchDeleteCategories, batchRestoreCategories} from './components/categoryApi';
import {searchTodos, batchDeleteTodos, batchRestoreTodos, batchEnableTodos, batchDisableTodos} from './components/todoApi';
import AddTodoCategory from './components/AddTodoCategory/addTodoCategory.vue';
import AddTodo from './components/AddTodo/addTodo.vue';
import TodoCategoryDetail from './components/CategoryDetail/todoCategoryDetail.vue';
import TodoDetail from './components/TodoDetail/todoDetail.vue';
import {getMaxPage, useLatestRequest} from '../../composables/useAdminListRequest';

const activeTab = ref<'category' | 'todo'>('category');

// --- 响应式折叠控制 ---
const showCategoryFilters = ref(false);
const showTodoFilters = ref(false);

// Category Filters
const catQ = ref<string | null>(null);
const catDeleted = ref<boolean | undefined>(undefined);
const catType = ref<number | undefined>(undefined);
const catUserId = ref<number | undefined>(undefined);
const userOptions = ref<any[]>([]);

// Todo Filters
const todoQ = ref<string | null>(null);
const todoDeleted = ref<boolean | undefined>(undefined);
const todoStatus = ref<number | undefined>(undefined);
const todoUserId = ref<number | undefined>(undefined);
const todoCategoryId = ref<number | undefined>(undefined);

const minHeight = 720;
const stepHeight = 45;
const nowRow = ref(10);
const categoryCount = ref(0);
const todoCount = ref(0);
const categoryMaxPage = ref(1);
const todoMaxPage = ref(1);
const categoryPage = ref(1);
const todoPage = ref(1);
const categoryRows = ref<any[]>([]);
const todoRows = ref<any[]>([]);
const selectedCategory = ref<any[]>([]);
const selectedTodo = ref<any[]>([]);

const categoryRequest = useLatestRequest();
const todoRequest = useLatestRequest();

const loadCategory = async (page = categoryPage.value) => {
  const pageSize = nowRow.value;
  const requestedPage = Math.max(1, page);
  categoryPage.value = requestedPage;
  const filters = {
    q: catQ.value || undefined,
    isDeleted: catDeleted.value,
    userId: catUserId.value,
    type: catType.value,
  };

  return categoryRequest.runLatest(
      async (signal) => {
        const requestPage = (pageNum: number) => searchCategories({
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
        categoryCount.value = data.total;
        categoryMaxPage.value = maxPage;
        categoryPage.value = resolvedPage;
        categoryRows.value = data.list;
        selectedCategory.value = [];
      },
  );
};

const loadTodo = async (page = todoPage.value) => {
  const pageSize = nowRow.value;
  const requestedPage = Math.max(1, page);
  todoPage.value = requestedPage;
  const filters = {
    q: todoQ.value || undefined,
    isDeleted: todoDeleted.value,
    status: todoStatus.value,
    userId: todoUserId.value,
    categoryId: todoCategoryId.value,
  };

  return todoRequest.runLatest(
      async (signal) => {
        const requestPage = (pageNum: number) => searchTodos({
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
        todoCount.value = data.total;
        todoMaxPage.value = maxPage;
        todoPage.value = resolvedPage;
        todoRows.value = data.list;
        selectedTodo.value = [];
      },
  );
};

const loadActiveTab = async () => {
  if (activeTab.value === 'category') {
    return loadCategory();
  }
  return loadTodo();
};

onMounted(async () => {
  nowRow.value = calculateRows(minHeight, stepHeight);
  await Promise.all([
    loadCategory(),
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
    categoryMaxPage.value = getMaxPage(categoryCount.value, rows);
    todoMaxPage.value = getMaxPage(todoCount.value, rows);
    categoryPage.value = Math.min(categoryPage.value, categoryMaxPage.value);
    todoPage.value = Math.min(todoPage.value, todoMaxPage.value);
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
  const currentPage = activeTab.value === 'category' ? categoryPage.value : todoPage.value;
  const maxPage = activeTab.value === 'category' ? categoryMaxPage.value : todoMaxPage.value;
  let targetPage = currentPage;

  if (turn === pageTurn.FirstPage) targetPage = 1;
  if (turn === pageTurn.PreviousPage) targetPage = Math.max(1, currentPage - 1);
  if (turn === pageTurn.NextPage) targetPage = Math.min(maxPage, currentPage + 1);
  if (turn === pageTurn.LastPage) targetPage = maxPage;

  if (targetPage === currentPage) {
    ElMessage.warning(turn === pageTurn.FirstPage || turn === pageTurn.PreviousPage ? '已经是第一页了' : '已经是最后一页了');
    return;
  }

  if (activeTab.value === 'category') {
    await loadCategory(targetPage);
  } else {
    await loadTodo(targetPage);
  }
};

const refreshCategory = () => loadCategory();
const refreshTodo = () => loadTodo();

const refresh = async () => {
  const committed = activeTab.value === 'category' ? await refreshCategory() : await refreshTodo();
  if (committed) ElMessage.success('刷新成功');
};

const batchDeleteCategory = async () => {
  if (!selectedCategory.value || selectedCategory.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedCategory.value.map((p: any) => p.id);
  const s = await batchDeleteCategories(ids);
  if (s===200) { ElMessage.success('删除成功'); await refreshCategory(); } else { ElMessage.error('无法连接服务器'); }
};

const batchRestoreCategory = async () => {
  if (!selectedCategory.value || selectedCategory.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedCategory.value.map((p: any) => p.id);
  const s = await batchRestoreCategories(ids);
  if (s===200) { ElMessage.success('恢复成功'); await refreshCategory(); } else { ElMessage.error('无法连接服务器'); }
};

const batchDeleteTodo = async () => {
  if (!selectedTodo.value || selectedTodo.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedTodo.value.map((p: any) => p.id);
  const s = await batchDeleteTodos(ids);
  if (s===200) { ElMessage.success('删除成功'); await refreshTodo(); } else { ElMessage.error('无法连接服务器'); }
};

const batchEnableTodo = async () => {
  if (!selectedTodo.value || selectedTodo.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedTodo.value.map((p: any) => p.id);
  const s = await batchEnableTodos(ids);
  if (s===200) { ElMessage.success('启用成功'); await loadTodo(); } else { ElMessage.error('无法连接服务器'); }
};

const batchDisableTodo = async () => {
  if (!selectedTodo.value || selectedTodo.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedTodo.value.map((p: any) => p.id);
  const s = await batchDisableTodos(ids);
  if (s===200) { ElMessage.success('禁用成功'); await loadTodo(); } else { ElMessage.error('无法连接服务器'); }
};

const batchRestoreTodo = async () => {
  if (!selectedTodo.value || selectedTodo.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selectedTodo.value.map((p: any) => p.id);
  const s = await batchRestoreTodos(ids);
  if (s===200) { ElMessage.success('恢复成功'); await refreshTodo(); } else { ElMessage.error('无法连接服务器'); }
};

const doCategorySearch = async () => {
  categoryPage.value = 1;
  await loadCategory(1);
};

const doTodoSearch = async () => {
  todoPage.value = 1;
  await loadTodo(1);
};

const handleDeleteCategory = async (id: number) => {
  const response = await axios.delete('admin/todoMm/category/delete', {params: {id}});
  if (response.data.status === 200) {
    ElMessage.success('删除成功');
    await refreshCategory();
  } else {
    ElMessage.error('无法连接服务器');
  }
};

const handleDeleteTodo = async (id: number) => {
  const response = await axios.delete('admin/todoMm/todo/delete', {params: {id}});
  if (response.data.status === 200) {
    ElMessage.success('删除成功');
    await refreshTodo();
  } else {
    ElMessage.error('无法连接服务器');
  }
};

const addCategoryVisible = ref<boolean>(false);
const addTodoVisible = ref<boolean>(false);
const categoryDetailVisible = ref<boolean>(false);
const todoDetailVisible = ref<boolean>(false);
const currentCategoryId = ref<number | null>(null);
const currentTodoId = ref<number | null>(null);
const openCategoryDetail = (id: number) => { currentCategoryId.value = id; categoryDetailVisible.value = true; };
const openTodoDetail = (id: number) => { currentTodoId.value = id; todoDetailVisible.value = true; };

</script>

<template>
  <el-scrollbar height="100%">
    <div class="common-layout">
      <el-tabs v-model="activeTab" type="card">
        <!-- ==================== CATEGORY TAB ==================== -->
        <el-tab-pane label="待做分类" name="category">
          <div class="responsive-toolbar">
            <!-- 第一行 -->
            <div class="toolbar-top">
              <div class="group-left">
                <IconField>
                  <InputIcon icon="Search" class="custom-icon"/>
                  <InputText v-model="catQ" placeholder="Search Name" class="custom-input"/>
                </IconField>
                <!-- 筛选开关 -->
                <Button 
                  :icon="showCategoryFilters ? 'FilterSlash' : 'Filter'" 
                  :severity="showCategoryFilters ? 'primary' : 'secondary'" 
                  outlined 
                  size="small" 
                  @click="showCategoryFilters = !showCategoryFilters"
                />
                <Button icon="Search" severity="secondary" outlined size="small" @click="doCategorySearch"/>
              </div>

              <div class="group-right">
                <Button icon="Plus" severity="secondary" outlined size="small" @click="addCategoryVisible = true"/>
                <Button icon="Trash" severity="secondary" outlined size="small" @click="batchDeleteCategory"/>
                <Button icon="Refresh" severity="secondary" outlined size="small" @click="batchRestoreCategory"/>
                <Button icon="Spinner" severity="secondary" outlined size="small" @click="refresh"/>
                
                <div class="pagination-controls">
                  <el-divider direction="vertical" class="hidden-xs-only"/>
                  <Tag severity="info">数量: {{ categoryCount }}</Tag>
                  <Tag class="page-tag">页: {{ categoryPage }}/{{ categoryMaxPage }}</Tag>
                  <div class="page-btns">
                    <Button icon="AngleDoubleLeft" severity="secondary" text size="small" @click="turnPage(0)"/>
                    <Button icon="AngleLeft" severity="secondary" text size="small" @click="turnPage(1)"/>
                    <Button icon="AngleRight" severity="secondary" text size="small" @click="turnPage(2)"/>
                    <Button icon="AngleDoubleRight" severity="secondary" text size="small" @click="turnPage(3)"/>
                  </div>
                </div>
              </div>
            </div>

            <!-- 折叠筛选 -->
            <transition name="fade-slide">
              <div v-if="showCategoryFilters" class="toolbar-filter-panel">
                <el-select v-model="catDeleted" placeholder="删除状态" style="width: 120px" clearable>
                  <el-option label="正常" :value="false" />
                  <el-option label="已删除" :value="true" />
                </el-select>
                <el-input-number v-model="catType" :min="0" :step="1" placeholder="类型" controls-position="right" style="width: 120px" />
                <el-select v-model="catUserId" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
                  <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
                </el-select>
                <Button label="应用筛选" icon="Check" size="small" outlined @click="doCategorySearch" />
              </div>
            </transition>
          </div>

          <div class="table-container">
            <DataTable v-model:selection="selectedCategory" :value="categoryRows" stripedRows dataKey="id" tableStyle="min-width: 900px;" size="small" :style="{ minHeight: dynamicHeight }">
              <Column selectionMode="multiple" headerStyle="width: 50px" position="fixed"></Column>
              <Column field="id" header="ID" headerStyle="width: 60px"></Column>
              <Column field="name" header="名称" headerStyle="width: 20%"></Column>
              <Column field="userId" header="用户ID" headerStyle="width: 10%"></Column>
              <Column field="type" header="类型" headerStyle="width: 10%"></Column>
              <Column field="isDeleted" header="状态" headerStyle="width: 10%">
                <template #body="{ data }">
                  <Tag :value="data.isDeleted ? '已删除' : '正常'" :severity="data.isDeleted ? 'danger' : 'success'"/>
                </template>
              </Column>
              <Column header="更多" headerStyle="width: 120px">
                <template #body="{ data }">
                  <div style="display: flex; gap: 6px; align-items: center;">
                    <Button type="button" icon="Eye" rounded outlined style=" height: 32px;width: 32px" @click="openCategoryDetail(data.id)"/>
                    <Button type="button" icon="Trash" rounded outlined style=" height: 32px;width: 32px" @click="handleDeleteCategory(data.id)"/>
                  </div>
                </template>
              </Column>
            </DataTable>
          </div>
        </el-tab-pane>

        <!-- ==================== TODO TAB ==================== -->
        <el-tab-pane label="待做记录" name="todo">
          <div class="responsive-toolbar">
            <!-- 第一行 -->
            <div class="toolbar-top">
              <div class="group-left">
                <IconField>
                  <InputIcon icon="Search" class="custom-icon"/>
                  <InputText v-model="todoQ" placeholder="Search Title" class="custom-input"/>
                </IconField>
                <!-- 筛选开关 -->
                <Button 
                  :icon="showTodoFilters ? 'FilterSlash' : 'Filter'" 
                  :severity="showTodoFilters ? 'primary' : 'secondary'" 
                  outlined 
                  size="small" 
                  @click="showTodoFilters = !showTodoFilters"
                />
                <Button icon="Search" severity="secondary" outlined size="small" @click="doTodoSearch"/>
              </div>

              <div class="group-right">
                <Button icon="Plus" severity="secondary" outlined size="small" @click="addTodoVisible = true"/>
                <Button icon="Trash" severity="secondary" outlined size="small" @click="batchDeleteTodo"/>
                <Button icon="Check" severity="secondary" outlined size="small" @click="batchEnableTodo"/>
                <Button icon="Ban" severity="secondary" outlined size="small" @click="batchDisableTodo"/>
                <Button icon="Refresh" severity="secondary" outlined size="small" @click="batchRestoreTodo"/>
                <Button icon="Spinner" severity="secondary" outlined size="small" @click="refresh"/>
                
                <div class="pagination-controls">
                  <el-divider direction="vertical" class="hidden-xs-only"/>
                  <Tag severity="info">数量: {{ todoCount }}</Tag>
                  <Tag class="page-tag">页: {{ todoPage }}/{{ todoMaxPage }}</Tag>
                  <div class="page-btns">
                    <Button icon="AngleDoubleLeft" severity="secondary" text size="small" @click="turnPage(0)"/>
                    <Button icon="AngleLeft" severity="secondary" text size="small" @click="turnPage(1)"/>
                    <Button icon="AngleRight" severity="secondary" text size="small" @click="turnPage(2)"/>
                    <Button icon="AngleDoubleRight" severity="secondary" text size="small" @click="turnPage(3)"/>
                  </div>
                </div>
              </div>
            </div>

            <!-- 折叠筛选 -->
            <transition name="fade-slide">
              <div v-if="showTodoFilters" class="toolbar-filter-panel">
                <el-select v-model="todoStatus" placeholder="状态" style="width: 110px" clearable>
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
                <el-select v-model="todoDeleted" placeholder="删除状态" style="width: 110px" clearable>
                  <el-option label="正常" :value="false" />
                  <el-option label="已删除" :value="true" />
                </el-select>
                <el-input-number v-model="todoCategoryId" :min="0" :step="1" placeholder="分类ID" controls-position="right" style="width: 120px" />
                <el-select v-model="todoUserId" placeholder="选择用户" style="width: 200px" filterable remote clearable :remote-method="async (q:string)=>{ userOptions = await fetchUserOptions(q, 50) }" :reserve-keyword="true">
                  <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
                </el-select>
                 <Button label="应用筛选" icon="Check" size="small" outlined @click="doTodoSearch" />
              </div>
            </transition>
          </div>

          <div class="table-container">
            <DataTable v-model:selection="selectedTodo" :value="todoRows" stripedRows dataKey="id" tableStyle="min-width: 1000px;" size="small" :style="{ minHeight: dynamicHeight }">
              <Column selectionMode="multiple" headerStyle="width: 50px" position="fixed"></Column>
              <Column field="id" header="ID" headerStyle="width: 60px"></Column>
              <Column field="title" header="标题" headerStyle="width: 20%"></Column>
              <Column field="userId" header="用户ID" headerStyle="width: 10%"></Column>
              <Column field="categoryId" header="分类ID" headerStyle="width: 10%"></Column>
              <Column field="status" header="状态" headerStyle="width: 10%">
                <template #body="{ data }">
                  <Tag :value="data.status===1 ? '启用' : '禁用'" :severity="data.status===1 ? 'success' : 'secondary'"/>
                </template>
              </Column>
              <Column field="isDeleted" header="删除" headerStyle="width: 10%">
                <template #body="{ data }">
                  <Tag :value="data.isDeleted ? '已删除' : '正常'" :severity="data.isDeleted ? 'danger' : 'success'"/>
                </template>
              </Column>
              <Column header="更多" headerStyle="width: 120px">
                <template #body="{ data }">
                  <div style="display: flex; gap: 6px; align-items: center;">
                    <Button type="button" icon="Eye" rounded outlined style=" height: 32px;width: 32px" @click="openTodoDetail(data.id)"/>
                    <Button type="button" icon="Trash" rounded outlined style=" height: 32px;width: 32px" @click="handleDeleteTodo(data.id)"/>
                  </div>
                </template>
              </Column>
            </DataTable>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-scrollbar>

  <AddTodoCategory v-model="addCategoryVisible"/>
  <AddTodo v-model="addTodoVisible"/>
  <TodoCategoryDetail v-model="categoryDetailVisible" v-model:categoryId="currentCategoryId"/>
  <TodoDetail v-model="todoDetailVisible" v-model:todoId="currentTodoId"/>
</template>

<style scoped>
.common-layout { height: 100%; padding-left: 1px; padding-right: 15px; background-color: white; }

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
