<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref} from "vue";
import {calculateRows} from "./components/TableView/calculateRows";
import {getStatusMsg, getStatusType} from "./components/TableView/getStatusType";
import {debounceImmediate} from "@/util/debounce";
import {ElMessage} from "element-plus";
import {batchDeleteComments} from "./components/TableView/batchDeleteComments";
import {searchComments} from "./components/TableView/searchComments";
import {deleteComment} from "./components/TableView/deleteComment";
import AddComment from "./components/AddComment/addComment.vue";
import CommentDetail from "./components/CommentDetail/commentDetail.vue";
import {getMaxPage, useLatestRequest} from '../../composables/useAdminListRequest';

const showFilters = ref(false);
const keyword = ref<string | null>(null);
const noteIdFilter = ref<number | undefined>(undefined);
const userIdFilter = ref<number | undefined>(undefined);
const deletedFilter = ref<boolean | undefined>(undefined);
const topLevelOnly = ref<boolean>(false);

const minHeight = 720;
const stepHeight = 45;
let nowRow = ref(10);
const commentCount = ref(0);
const maxPage = ref(1);
const nowPage = ref(1);
const products = ref<any[]>([]);
const selected = ref<any[]>([]);
const listRequest = useLatestRequest();

const loadComments = async (page = nowPage.value) => {
  const pageSize = nowRow.value;
  const requestedPage = Math.max(1, page);
  nowPage.value = requestedPage;
  const filters = {
    q: keyword.value || undefined,
    noteId: noteIdFilter.value,
    userId: userIdFilter.value,
    isDeleted: deletedFilter.value,
    topLevelOnly: topLevelOnly.value || undefined,
  };

  return listRequest.runLatest(
      async (signal) => {
        const requestPage = (pageNum: number) => searchComments({
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
        commentCount.value = data.total;
        maxPage.value = resolvedMaxPage;
        nowPage.value = resolvedPage;
        products.value = data.list;
        selected.value = [];
      },
  );
};

onMounted(async () => {
  nowRow.value = calculateRows(minHeight, stepHeight);
  await loadComments();
  window.addEventListener('resize', handleResize);
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
      maxPage.value = getMaxPage(commentCount.value, rows);
      nowPage.value = Math.min(nowPage.value, maxPage.value);
      await loadComments();
    }
  }, DEBOUNCE_DELAY);
};

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  if (resizeTimeout) clearTimeout(resizeTimeout);
});

const dynamicHeight = computed(() => {
  return `${475 + (nowRow.value - 10) * 45}px`;
});

enum pageTurn { FirstPage, PreviousPage, NextPage, LastPage }
const turnPage = async (turn: pageTurn) => {
  switch (turn) {
    case pageTurn.FirstPage:
      if (nowPage.value != 1) { await loadComments(1); } else { ElMessage.warning("已经是第一页了") }
      break
    case pageTurn.PreviousPage:
      if (nowPage.value > 1) { await loadComments(nowPage.value - 1); } else { ElMessage.warning("已经是第一页了") }
      break
    case pageTurn.NextPage:
      if (nowPage.value < maxPage.value) { await loadComments(nowPage.value + 1); } else { ElMessage.warning("已经是最后一页了") }
      break
    case pageTurn.LastPage:
      if (nowPage.value != maxPage.value) { await loadComments(maxPage.value); } else { ElMessage.warning("已经是最后一页了") }
      break
  }
}
const handleDebouncedTurnPage = debounceImmediate(turnPage, 200)

const refresh = async () => {
  const committed = await loadComments();
  if (committed) ElMessage.success("刷新成功")
}
const handleDebouncedRefresh = debounceImmediate(refresh, 1000);

const batchDelete = async () => {
  if (!selected.value || selected.value.length === 0) { ElMessage.warning("选择为空"); return; }
  const ids = selected.value.map((p: any) => p.id);
  const status = await batchDeleteComments(ids);
  if (status === 200) { ElMessage.success("删除成功"); await refresh() } else { ElMessage.error("无法连接服务器") }
}
const handleDebouncedBatchDelete = debounceImmediate(batchDelete, 1000);

const doSearch = async () => {
  nowPage.value = 1;
  await loadComments(1);
};
const handleDebouncedSearch = debounceImmediate(doSearch, 500);

const addVisible = ref<boolean>(false);
const detailVisible = ref<boolean>(false);
const currentId = ref<number | null>(null);
const openDetail = (id: number) => { currentId.value = id; detailVisible.value = true; };

const handleDeleteComment = async (id: number) => {
  const status = await deleteComment(id);
  if (status === 200) {
    ElMessage.success('删除成功');
    await loadComments();
  } else {
    ElMessage.error('无法连接服务器');
  }
};
</script>

<template>
  <el-scrollbar height="100%">
    <div class="common-layout">
      <div class="responsive-toolbar">
        <div class="toolbar-top">
          <div class="group-left">
            <IconField>
              <InputIcon icon="Search" class="custom-icon"/>
              <InputText v-model="keyword" placeholder="Search Content" class="custom-input"/>
            </IconField>
            <Button :icon="showFilters ? 'FilterSlash' : 'Filter'" :severity="showFilters ? 'primary' : 'secondary'" outlined size="small" @click="showFilters = !showFilters" v-tooltip="'高级筛选'"/>
            <Button icon="Search" severity="secondary" outlined size="small" @click="handleDebouncedSearch" v-tooltip.bottom="{ value: '搜索', showDelay: 1000, hideDelay: 300 }"/>
          </div>
          <div class="group-right">
            <Button icon="Plus" severity="secondary" outlined size="small" v-tooltip.bottom="{ value: '添加评论', showDelay: 1000, hideDelay: 300 }" @click="addVisible = true"/>
            <Button icon="Trash" severity="secondary" outlined size="small" @click="handleDebouncedBatchDelete" v-tooltip.bottom="{ value: '删除选中评论', showDelay: 1000, hideDelay: 300 }"/>
            <Button icon="Spinner" severity="secondary" outlined size="small" @click="handleDebouncedRefresh" v-tooltip.bottom="{ value: '刷新', showDelay: 1000, hideDelay: 300 }"/>
            <div class="pagination-controls">
              <el-divider direction="vertical" class="hidden-xs-only"/>
              <Tag severity="info">评论数: {{ commentCount }}</Tag>
              <Tag class="page-tag">页: {{ nowPage }}/{{ maxPage }}</Tag>
              <div class="page-btns">
                <Button icon="AngleDoubleLeft" severity="secondary" text size="small" @click="handleDebouncedTurnPage(pageTurn.FirstPage)"/>
                <Button icon="AngleLeft" severity="secondary" text size="small" @click="handleDebouncedTurnPage(pageTurn.PreviousPage)"/>
                <Button icon="AngleRight" severity="secondary" text size="small" @click="handleDebouncedTurnPage(pageTurn.NextPage)"/>
                <Button icon="AngleDoubleRight" severity="secondary" text size="small" @click="handleDebouncedTurnPage(pageTurn.LastPage)"/>
              </div>
            </div>
          </div>
        </div>

        <transition name="fade-slide">
          <div v-if="showFilters" class="toolbar-filter-panel">
            <el-input v-model.number="noteIdFilter" placeholder="笔记ID" style="width: 120px"/>
            <el-input v-model.number="userIdFilter" placeholder="用户ID" style="width: 120px"/>
            <el-select v-model="deletedFilter" placeholder="状态" style="width: 120px" clearable>
              <el-option label="有效" :value="false"/>
              <el-option label="已删除" :value="true"/>
            </el-select>
            <el-checkbox v-model="topLevelOnly" label="仅顶层"/>
            <Button label="应用筛选" icon="Check" size="small" outlined @click="handleDebouncedSearch"/>
          </div>
        </transition>
      </div>

      <div class="table-container">
        <DataTable v-model:selection="selected" :value="products" stripedRows dataKey="id"
                   tableStyle="min-width: 850px;" size="small" :style="{ minHeight: dynamicHeight }">
          <Column selectionMode="multiple" headerStyle="width: 50px" position="fixed"></Column>
          <Column field="id" header="ID" headerStyle="width: 60px"></Column>
          <Column field="noteId" header="笔记ID" headerStyle="width: 12%"></Column>
          <Column field="userId" header="用户ID" headerStyle="width: 12%"></Column>
          <Column field="content" header="内容" headerStyle="width: 40%">
            <template #body="{ data }">
              <el-text>{{ (data.content || '').slice(0, 120) }}</el-text>
            </template>
          </Column>
          <Column field="parentId" header="父评论ID" headerStyle="width: 12%"></Column>
          <Column field="isDeleted" header="状态" headerStyle="width: 12%">
            <template #body="{ data }">
              <Tag :value="getStatusMsg(data.isDeleted)" :severity="getStatusType(data.isDeleted)"/>
            </template>
          </Column>
          <Column header="更多" headerStyle="width: 120px">
            <template #body="{ data }">
              <div style="display: flex; gap: 6px; align-items: center;">
                <Button type="button" icon="Eye" rounded outlined style=" height: 32px;width: 32px" @click="openDetail(data.id)"/>
                <Button type="button" icon="Trash" rounded outlined style=" height: 32px;width: 32px"
                        @click="handleDeleteComment(data.id)"/>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>
    </div>
  </el-scrollbar>

  <AddComment v-model="addVisible"/>
  <CommentDetail v-model="detailVisible" v-model:commentId="currentId"/>
</template>

<style scoped>
.common-layout { height: 100%; padding-left: 1px; padding-right: 15px; background-color: white; }
.responsive-toolbar { display: flex; flex-direction: column; gap: 10px; margin-bottom: 15px; }
.toolbar-top { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 10px; }
.group-left { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; }
.group-right { display: flex; align-items: center; flex-wrap: wrap; gap: 8px; justify-content: flex-end; }
.pagination-controls { display: flex; align-items: center; gap: 8px; }
.page-tag { min-width: 90px; text-align: center; }
.page-btns { display: flex; gap: 2px; }
.toolbar-filter-panel { background-color: #f8f9fa; border: 1px solid #e2e8f0; border-radius: 6px; padding: 10px; display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.custom-icon { font-size: 16px; }
.custom-input { font-size: 14px; padding: 5px 10px; height: 32px; width: 180px; }
.table-container { background-color: white; width: 100%; border-radius: 10px; box-shadow: 0 0 0 1px #D9D9D9; padding: 4px; margin-top: 5px; }
.fade-slide-enter-active, .fade-slide-leave-active { transition: all 0.3s ease; max-height: 200px; opacity: 1; overflow: hidden; }
.fade-slide-enter-from, .fade-slide-leave-to { max-height: 0; opacity: 0; padding-top: 0; padding-bottom: 0; margin-top: 0; border: none; }
@media (max-width: 768px) {
  .toolbar-top { flex-direction: column; align-items: stretch; }
  .group-left, .group-right { justify-content: space-between; width: 100%; }
  .custom-input { flex: 1; }
  .hidden-xs-only { display: none; }
  .pagination-controls { justify-content: space-between; width: 100%; margin-top: 5px; }
}
</style>
