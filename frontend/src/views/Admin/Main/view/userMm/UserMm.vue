<script setup lang="ts">
//组件加载
import Button from 'primevue/button';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from "primevue/inputtext";
import Tag from 'primevue/tag'

import {computed, onBeforeUnmount, onMounted, ref} from "vue";
// 表格组件
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import axios from "../../../../../axios"; // 请确认路径
import fetchInitialPageData from "./components/TableView/fetchInitialPageData";
import {fetchPageData} from "./components/TableView/fetchPageData";
import {calculateRows} from "./components/TableView/calculateRows";
import {getStatusMsg, getStatusType} from "./components/TableView/getStatusType";
import AddUser from "./components/AddUser/addUser.vue";
import {debounceImmediate} from "@/util/debounce";
import {ElMessage} from "element-plus";
import {BatchDeleteUser} from "./components/TableView/batchDeleteUser";
import OnlineUser from "./components/OnlineUser/OnlineUser.vue";
import {onlineUserCount} from "./components/OnlineUser/OnlineUserCount";
import {searchUsers} from "./components/TableView/searchUsers";
import {batchDisable, batchEnable, deleteUser} from "./components/TableView/batchUpdateStatus";
import UserDetail from "./components/UserDetail/userDetail.vue";

// --- 响应式折叠控制 ---
const showFilters = ref(false);

// 搜素框数据
const value1 = ref<string | null>(null);
const statusFilter = ref<number | null>(null);
const genderFilter = ref<string | null>(null);

// 计算表格显示条数
const minHeight = 720;  // 基准高度
const stepHeight = 45;  // 每个高度增加的间隔

// 当前行数
let nowRow = ref(10);

// 存储用户总数
const userCount = ref(0);

// 最大页数
const maxPage = ref(1);

// 当前页数
const nowPage = ref(1);

// 表格数据
const products = ref([]);

// 在线用户数目变量
const OUserCount = ref(0);

// 钩子函数
onMounted(async () => {
  // 获取当前尺寸 所能显示的 行数
  nowRow.value = calculateRows(minHeight, stepHeight);

  // 获取用户总数 并 计算最大页数
  await axios.get(
      "admin/userMm/getUserCount",
  ).then((response) => {
    userCount.value = response.data.data;
    maxPage.value = Math.ceil(userCount.value / nowRow.value);
  });

  // 获取初始数据 固定获取第一页的数据
  products.value = await fetchInitialPageData(nowRow.value);

  // 挂载 页面尺寸监听器
  window.addEventListener('resize', handleResize);

  // 获取在线用户数目
  OUserCount.value = await onlineUserCount()
})

// 在组件销毁时移除监听器
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});

// 监听窗口大小变化并防抖
const DEBOUNCE_DELAY = 100; // 防抖延时常量
let resizeTimeout: ReturnType<typeof setTimeout>;
const handleResize = async () => {
  clearTimeout(resizeTimeout);
  resizeTimeout = setTimeout(async () => {
    const rows = calculateRows(minHeight, stepHeight);

    if (rows !== nowRow.value) {
      // 更新行数和最大页数
      nowRow.value = rows;
      maxPage.value = Math.ceil(userCount.value / nowRow.value);

      // 调整当前页数，确保不会超出最大页数
      if (nowPage.value > maxPage.value) {
        nowPage.value = maxPage.value;
      }
      // 根据当前行数和页数重新获取数据
      products.value = await fetchPageData(nowRow.value, nowPage.value);
    }
  }, DEBOUNCE_DELAY);
};

// 计算 table 的动态高度 达到适配
const dynamicHeight = computed(() => {
  return `${475 + (nowRow.value - 10) * 45}px`;
});


// 制定分页逻辑
enum pageTurn { // 翻页行为枚举
  FirstPage,
  PreviousPage,
  NextPage,
  LastPage
}

const turnPage = async (turn: pageTurn) => {
  switch (turn) {
    case pageTurn.FirstPage:
      if (nowPage.value != 1) {
        nowPage.value = 1;
        products.value = await fetchPageData(nowRow.value, nowPage.value);
      } else {
        ElMessage.warning("已经是第一页了")
      }
      break

    case pageTurn.PreviousPage:
      if (nowPage.value > 1) {
        nowPage.value = nowPage.value - 1;
        products.value = await fetchPageData(nowRow.value, nowPage.value);
      } else {
        ElMessage.warning("已经是第一页了")
      }
      break

    case pageTurn.NextPage:
      if (nowPage.value < maxPage.value) {
        nowPage.value = nowPage.value + 1;
        products.value = await fetchPageData(nowRow.value, nowPage.value);
      } else {
        ElMessage.warning("已经是最后一页了")
      }
      break
    case pageTurn.LastPage:
      if (nowPage.value != maxPage.value) {
        nowPage.value = maxPage.value;
        products.value = await fetchPageData(nowRow.value, nowPage.value);
      } else {
        ElMessage.warning("已经是最后一页了")
      }
      break
    default:
      console.error(`Unknown turn: ${turn}`);
  }
}
const handleDebouncedTurnPage = debounceImmediate(turnPage, 200)

// 刷新逻辑
const refresh = async () => {
  await axios.get(
      "admin/userMm/getUserCount",
  ).then((response) => {
    userCount.value = response.data.data;
    maxPage.value = Math.ceil(userCount.value / nowRow.value);
  });
  // 当 重新获取后 最大页数 小于当前页数 则查询最后一页
  if (maxPage.value < nowPage.value) {
    products.value = await fetchPageData(nowRow.value, maxPage.value);
  } else {
    products.value = await fetchPageData(nowRow.value, nowPage.value);
  }

  ElMessage.success("刷新成功")
}
// 刷新逻辑 的 防抖函数
const handleDebouncedRefresh = debounceImmediate(refresh, 1000);

// 选择逻辑
const selectedProduct = ref();

// 批量删除/防抖
const batchDelete = async () => {
  if (selectedProduct.value == null) {
    ElMessage.warning("选择为空")
    return;
  }

  // 定义产品接口
  interface Product {
    id: number;
  }

  // 读取当前的 id 插入到ids
  const ids = selectedProduct.value.map((product: Product) => product.id);
  const status = await BatchDeleteUser(ids);
  if (status === 200) {
    ElMessage.success("删除成功")
  } else {
    ElMessage.error("无法连接服务器");
  }
}
const handleDebouncedBatchDelete = debounceImmediate(batchDelete, 1000);

const batchEnableR = async () => {
  if (!selectedProduct.value || selectedProduct.value.length === 0) {
    ElMessage.warning("选择为空");
    return;
  }
  const ids = selectedProduct.value.map((p: any) => p.id);
  const s = await batchEnable(ids);
  if (s === 200) {
    ElMessage.success("启用成功");
    products.value = await fetchPageData(nowRow.value, nowPage.value);
  } else {
    ElMessage.error("无法连接服务器");
  }
};
const handleDebouncedBatchEnable = debounceImmediate(batchEnableR, 500);

const batchDisableR = async () => {
  if (!selectedProduct.value || selectedProduct.value.length === 0) {
    ElMessage.warning("选择为空");
    return;
  }
  const ids = selectedProduct.value.map((p: any) => p.id);
  const s = await batchDisable(ids);
  if (s === 200) {
    ElMessage.success("禁用成功");
    products.value = await fetchPageData(nowRow.value, nowPage.value);
  } else {
    ElMessage.error("无法连接服务器");
  }
};
const handleDebouncedBatchDisable = debounceImmediate(batchDisableR, 500);

const doSearch = async () => {
  const data = await searchUsers({
    q: value1.value || undefined,
    status: statusFilter.value === null ? undefined : statusFilter.value,
    gender: genderFilter.value || undefined,
    pageNum: nowPage.value,
    pageSize: nowRow.value,
  });
  products.value = data.list;
  userCount.value = data.total;
  maxPage.value = Math.ceil(userCount.value / nowRow.value);
};
const handleDebouncedSearch = debounceImmediate(doSearch, 500);

const userDetailVisible = ref<boolean>(false);
const currentUserId = ref<number | null>(null);
const openDetail = (id: number) => {
  currentUserId.value = id;
  userDetailVisible.value = true;
};


// 控制添加用户页面
const addUserVisible = ref<boolean>(false);

// 控制在线用户页面
const onlineUserVisible = ref<boolean>(false);
</script>

<template>
  <el-scrollbar height="100%">
    <div class="common-layout">

      <!-- 标题区域：使用 flex 替代 el-row 以保持一致性 -->
      <div class="title-container">
        <div class="title-left">
          <h1>所有用户</h1>
          <p>这个列表可以对所有用户进行管理</p>
        </div>
        <div class="title-right">
          <Button type="button" badgeSeverity="contrast" outlined size="small"
                  style="width: 130px"
                  @click="onlineUserVisible = true"
          >
            <i class="pi pi-circle-fill" style="color: #22C55E; margin-right: 8px;"></i>
            <el-text tag="b">{{ OUserCount }}</el-text>
            <el-text>在线用户</el-text>
          </Button>
        </div>
      </div>

      <!-- 响应式工具栏 -->
      <div class="responsive-toolbar">
        <!-- 第一行：主要操作与常用搜索 -->
        <div class="toolbar-top">
          <!-- 左侧：搜索框 + 筛选开关 -->
          <div class="group-left">
            <IconField>
              <InputIcon class="pi pi-search custom-icon"/>
              <InputText v-model="value1" placeholder="Search Username" class="custom-input"/>
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
                    @click="handleDebouncedSearch"
                    v-tooltip.bottom="{ value: '搜索', showDelay: 1000, hideDelay: 300 }"/>
          </div>

          <!-- 右侧：增删改查操作 + 分页 -->
          <div class="group-right">
            <Button icon="pi pi-plus" severity="secondary" outlined size="small"
                    v-tooltip.bottom="{ value: '添加用户', showDelay: 1000, hideDelay: 300 }"
                    @click="addUserVisible = true"/>

            <Button icon="pi pi-trash" severity="secondary" outlined size="small"
                    @click="handleDebouncedBatchDelete"
                    v-tooltip.bottom="{ value: '删除选中用户', showDelay: 1000, hideDelay: 300 }"/>

            <Button icon="pi pi-check" severity="secondary" outlined size="small"
                    @click="handleDebouncedBatchEnable"
                    v-tooltip.bottom="{ value: '批量启用', showDelay: 1000, hideDelay: 300 }"/>

            <Button icon="pi pi-ban" severity="secondary" outlined size="small"
                    @click="handleDebouncedBatchDisable"
                    v-tooltip.bottom="{ value: '批量禁用', showDelay: 1000, hideDelay: 300 }"/>

            <Button icon="pi pi-spinner" severity="secondary" outlined size="small"
                    @click="handleDebouncedRefresh"
                    v-tooltip.bottom="{ value: '刷新', showDelay: 1000, hideDelay: 300 }"/>

            <div class="pagination-controls">
              <el-divider direction="vertical" class="hidden-xs-only"/>
              <Tag severity="info">用户数: {{ userCount }}</Tag>
              <Tag class="page-tag">页: {{ nowPage }}/{{ maxPage }}</Tag>

              <div class="page-btns">
                <Button icon="pi pi-angle-double-left" severity="secondary" text size="small"
                        @click="handleDebouncedTurnPage(pageTurn.FirstPage)"/>
                <Button icon="pi pi-angle-left" severity="secondary" text size="small"
                        @click="handleDebouncedTurnPage(pageTurn.PreviousPage)"/>
                <Button icon="pi pi-angle-right" severity="secondary" text size="small"
                        @click="handleDebouncedTurnPage(pageTurn.NextPage)"/>
                <Button icon="pi pi-angle-double-right" severity="secondary" text size="small"
                        @click="handleDebouncedTurnPage(pageTurn.LastPage)"/>
              </div>
            </div>
          </div>
        </div>

        <!-- 第二行：折叠筛选面板 -->
        <transition name="fade-slide">
          <div v-if="showFilters" class="toolbar-filter-panel">
            <el-select v-model="statusFilter" placeholder="状态" style="width: 120px" clearable>
              <el-option label="全部" :value="null"/>
              <el-option label="正常" :value="0"/>
              <el-option label="停用" :value="1"/>
              <el-option label="封禁" :value="2"/>
            </el-select>

            <el-select v-model="genderFilter" placeholder="性别" style="width: 120px" clearable>
              <el-option label="全部" :value="null"/>
              <el-option label="男" value="male"/>
              <el-option label="女" value="female"/>
              <el-option label="保密" value="secret"/>
            </el-select>

            <Button label="应用筛选" icon="pi pi-check" size="small" outlined @click="handleDebouncedSearch"/>
          </div>
        </transition>
      </div>

      <!--  表格  -->
      <div class="table-container">
        <DataTable v-model:selection="selectedProduct" :value="products" stripedRows dataKey="id"
                   tableStyle="min-width: 850px;" size="small" :style="{ minHeight: dynamicHeight }">
          <Column selectionMode="multiple" headerStyle="width: 50px" position="fixed"></Column>
          <Column field="id" header="id" headerStyle="width: 60px"></Column>

          <Column field="username" header="用户名" headerStyle="width: 22%">
            <template #body="{ data }">
              <div style="display: flex; align-items: center;">
                <el-avatar
                    :size="28"
                    :src="data.avatar"
                    shape="square"
                    style="margin-right: 8px"
                >
                  {{ data.username.charAt(0).toUpperCase() }}
                </el-avatar>
                <el-text tag="b">
                  {{ data.username }}
                </el-text>

              </div>

            </template>
          </Column>

          <Column field="uid" header="Uid" headerStyle="width: 16%"></Column>

          <Column field="email" header="邮箱地址" headerStyle="width: 32%"></Column>
          <Column field="status" header="状态" headerStyle="width: 12%">
            <template #body="{ data }">
              <Tag :value="getStatusMsg(data.status)" :severity="getStatusType(data.status)"/>
            </template>
          </Column>

          <Column header="更多" headerStyle="width: 120px">
            <template #body="{ data }">
              <div style="display: flex; gap: 6px; align-items: center;">
                <Button type="button" icon="pi pi-eye" rounded outlined style=" height: 32px;width: 32px"
                        @click="openDetail(data.id)"/>
                <Button type="button" icon="pi pi-trash" rounded outlined style=" height: 32px;width: 32px"
                        @click="(async()=>{ const s = await deleteUser(data.id); if(s===200){ ElMessage.success('删除成功'); products.value = await fetchPageData(nowRow.value, nowPage.value) } else { ElMessage.error('无法连接服务器') } })()"/>
              </div>
            </template>
          </Column>
        </DataTable>
      </div>

    </div>
  </el-scrollbar>

  <!--  添加用户页面  -->
  <AddUser v-model="addUserVisible"/>

  <UserDetail v-model="userDetailVisible" v-model:userId="currentUserId"/>

  <!--  在线用户页面  -->
  <OnlineUser v-model="onlineUserVisible"/>

</template>

<style scoped>
.common-layout {
  height: 100%;
  padding-left: 1px;
  padding-right: 15px;
  background-color: white;
}

/* 标题区域样式 */
.title-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.title-left h1 {
  color: #334155;
  font-size: 22px;
  margin: 0;
  font-weight: 700;
}

.title-left p {
  color: #64748b;
  font-size: 14px;
  margin: 0;
}

.title-right {
  display: flex;
  align-items: center;
}

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

/* 分页控制组 */
.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
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
  background-color: #f8f9fa;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

/* 图标和输入框微调 */
.custom-icon {
  font-size: 16px;
}

.custom-input {
  font-size: 14px;
  padding: 5px 10px;
  height: 32px;
  width: 180px;
}

.table-container {
  background-color: white;
  width: 100%;
  border-radius: 10px;
  box-shadow: 0 0 0 1px #D9D9D9;
  padding: 4px;
  margin-top: 5px;
}

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

  .custom-input {
    flex: 1;
  }

  .hidden-xs-only {
    display: none;
  }

  .pagination-controls {
    justify-content: space-between;
    width: 100%;
    margin-top: 5px;
  }
}
</style>