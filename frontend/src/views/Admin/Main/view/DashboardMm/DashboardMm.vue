<script setup lang="ts">
import { onMounted, ref, computed, watch, onUnmounted, nextTick } from "vue";
// PrimeVue Components
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import IconField from 'primevue/iconfield';
import InputIcon from 'primevue/inputicon';
import InputText from "primevue/inputtext";
import Tag from 'primevue/tag';
// Element Plus
import { ElMessage } from "element-plus";
// ECharts
import * as echarts from 'echarts';
// API & Utils (保持原样)
import { fetchMetrics, fetchRecent, todoDelete, todoBatchDelete, todoBatchEnable, todoBatchDisable } from "./components/api";
import { debounceImmediate } from "@/util/debounce";

// --- 状态定义 ---
const metrics = ref<any>({});
const category = ref<string>('comment');
const q = ref<string>('');
const userIdFilter = ref<number | null>(null);
const deletedFilter = ref<boolean | null>(null);
const pageNum = ref(1);
const pageSize = ref(10);
const total = ref(0);
const list = ref<any[]>([]);
const selected = ref<any[]>([]);

// ECharts DOM 引用
const chartPieRef = ref<HTMLElement | null>(null);
const chartBarRef = ref<HTMLElement | null>(null);
let pieChartInst: echarts.ECharts | null = null;
let barChartInst: echarts.ECharts | null = null;

// --- 生命周期 & 加载 ---
onMounted(async () => {
  // 并行加载数据，提高速度
  const [metricsData] = await Promise.all([
    fetchMetrics(),
    load()
  ]);
  metrics.value = metricsData || {};
  
  // 初始化图表
  nextTick(() => {
    initCharts();
  });
  
  window.addEventListener('resize', handleResize);
});

onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  pieChartInst?.dispose();
  barChartInst?.dispose();
});

// --- 图表逻辑 ---
const initCharts = () => {
  if (!metrics.value) return;

  // 1. 饼图：系统数据构成
  if (chartPieRef.value) {
    pieChartInst = echarts.init(chartPieRef.value);
    pieChartInst.setOption({
      title: { text: '数据分布', left: 'center', textStyle: { fontSize: 16 } },
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%', left: 'center' },
      series: [{
        name: '数据统计',
        type: 'pie',
        radius: ['40%', '70%'],
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        data: [
          { value: metrics.value.noteCount || 0, name: '笔记' },
          { value: metrics.value.commentCount || 0, name: '评论' },
          { value: metrics.value.folderCount || 0, name: '文件夹' },
          { value: metrics.value.todoCount || 0, name: '待办' }
        ]
      }]
    });
  }

  // 2. 柱状图：收藏比率分析
  if (chartBarRef.value) {
    barChartInst = echarts.init(chartBarRef.value);
    barChartInst.setOption({
      title: { text: '收藏转化情况', left: 'center', textStyle: { fontSize: 16 } },
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '10%', containLabel: true },
      xAxis: { type: 'category', data: ['笔记', '文件夹'] },
      yAxis: { type: 'value' },
      series: [
        {
          name: '总数',
          type: 'bar',
          data: [metrics.value.noteCount || 0, metrics.value.folderCount || 0],
          itemStyle: { color: '#91cc75' }
        },
        {
          name: '被收藏数',
          type: 'bar',
          data: [metrics.value.favoriteNoteCount || 0, metrics.value.favoriteFolderCount || 0],
          itemStyle: { color: '#fac858' }
        }
      ]
    });
  }
};

const handleResize = () => {
  pieChartInst?.resize();
  barChartInst?.resize();
};

// 监听 metrics 变化刷新图表
watch(metrics, () => {
  initCharts();
});

// --- 业务逻辑 (保持原有接口调用) ---

const load = async () => {
  const data = await fetchRecent({
    category: category.value,
    q: q.value || undefined,
    userId: userIdFilter.value === null ? undefined : userIdFilter.value,
    isDeleted: deletedFilter.value === null ? undefined : deletedFilter.value,
    pageNum: pageNum.value,
    pageSize: pageSize.value,
  });
  list.value = data.list;
  total.value = data.total;
}

const handleDebouncedLoad = debounceImmediate(load, 300);

const changeCategory = async (c: string) => {
  category.value = c;
  pageNum.value = 1;
  selected.value = []; // 切换分类清空选中
  await load();
}

// 批量操作逻辑保持不变
const enableSelected = async () => {
  if (category.value !== 'todo') { ElMessage.warning('当前类别不支持批量启用'); return; }
  if (!selected.value || selected.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selected.value.map((x: any) => x.id);
  const s = await todoBatchEnable(ids);
  if (s === 200) { ElMessage.success('批量启用成功'); await load(); } else { ElMessage.error('无法连接服务器'); }
}

const disableSelected = async () => {
  if (category.value !== 'todo') { ElMessage.warning('当前类别不支持批量禁用'); return; }
  if (!selected.value || selected.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selected.value.map((x: any) => x.id);
  const s = await todoBatchDisable(ids);
  if (s === 200) { ElMessage.success('批量禁用成功'); await load(); } else { ElMessage.error('无法连接服务器'); }
}

const batchDelete = async () => {
  if (!selected.value || selected.value.length === 0) { ElMessage.warning('选择为空'); return; }
  const ids = selected.value.map((x: any) => x.id);
  let s = 500;
  if (category.value === 'todo') s = await todoBatchDelete(ids);
  else if (category.value === 'comment') {
    // 动态导入保持不变
    const resp = await (await import("../CommentMm/components/TableView/batchDeleteComments"));
    s = await resp.batchDeleteComments(ids);
  } else { ElMessage.warning('该类别暂未支持批量删除'); return; }
  
  if (s === 200) { ElMessage.success('删除成功'); await load(); } else { ElMessage.error('无法连接服务器'); }
}

// 表头配置
const tableColumns = computed(() => {
  const common = ['id', 'isDeleted'];
  if (category.value === 'comment') return ['id', 'userId', 'content', 'noteId', ...common];
  if (category.value === 'todo') return ['id', 'user_id', 'title', 'status', ...common];
  if (category.value === 'note') return ['id', 'userId', 'noteTitle', 'noteSummary', ...common];
  return ['id'];
});
</script>

<template>
  <el-scrollbar height="100%" class="dashboard-bg">
    <div class="dashboard-container">
      
      <!-- 1. 顶部标题区 -->
      <div class="header-section">
        <div>
          <h1 class="main-title">系统仪表盘</h1>
          <p class="sub-title">实时监控数据指标与内容管理</p>
        </div>
        <div class="header-actions">
          <!-- 可以在这里放全局刷新按钮等 -->
          <Button icon="pi pi-refresh" rounded text @click="load" v-tooltip="'刷新列表'"/>
        </div>
      </div>

      <!-- 2. 关键指标卡片区 (Grid Layout) -->
      <div class="metrics-grid">
        <div class="metric-card user-card">
          <div class="metric-icon"><i class="pi pi-users"></i></div>
          <div class="metric-info">
            <span class="label">用户总数</span>
            <span class="value">{{ metrics?.userCount || 0 }}</span>
          </div>
        </div>
        <div class="metric-card note-card">
          <div class="metric-icon"><i class="pi pi-book"></i></div>
          <div class="metric-info">
            <span class="label">笔记总数</span>
            <span class="value">{{ metrics?.noteCount || 0 }}</span>
          </div>
        </div>
        <div class="metric-card comment-card">
          <div class="metric-icon"><i class="pi pi-comments"></i></div>
          <div class="metric-info">
            <span class="label">评论总数</span>
            <span class="value">{{ metrics?.commentCount || 0 }}</span>
          </div>
        </div>
        <div class="metric-card todo-card">
          <div class="metric-icon"><i class="pi pi-check-square"></i></div>
          <div class="metric-info">
            <span class="label">待办事项</span>
            <span class="value">{{ metrics?.todoCount || 0 }}</span>
          </div>
        </div>
      </div>

      <!-- 3. ECharts 图表区 -->
      <div class="charts-row">
        <div class="chart-container">
          <div ref="chartPieRef" class="echart-instance"></div>
        </div>
        <div class="chart-container">
          <div ref="chartBarRef" class="echart-instance"></div>
        </div>
      </div>

      <!-- 4. 数据管理列表区 -->
      <div class="data-panel">
        
        <!-- 工具栏 -->
        <div class="panel-toolbar">
          <div class="toolbar-left">
            <el-radio-group v-model="category" @change="changeCategory" size="small">
              <el-radio-button label="comment">评论管理</el-radio-button>
              <el-radio-button label="todo">待办管理</el-radio-button>
              <el-radio-button label="note">笔记管理</el-radio-button>
            </el-radio-group>
            
            <IconField class="search-field">
              <InputIcon class="pi pi-search"/>
              <InputText v-model="q" placeholder="搜索关键词..." class="p-inputtext-sm" @keydown.enter="handleDebouncedLoad"/>
            </IconField>
          </div>

          <div class="toolbar-right">
            <div class="filter-group">
               <el-input v-model.number="userIdFilter" placeholder="用户ID" size="small" style="width: 100px"/>
               <el-select v-model="deletedFilter" placeholder="状态" size="small" style="width: 100px" clearable>
                <el-option label="全部" :value="null"/>
                <el-option label="有效" :value="false"/>
                <el-option label="已删除" :value="true"/>
              </el-select>
              <Button icon="pi pi-filter" rounded outlined size="small" @click="handleDebouncedLoad"/>
            </div>

            <div class="action-group" v-if="selected.length > 0">
              <Button v-if="category === 'todo'" label="启用" icon="pi pi-check" severity="success" size="small" @click="enableSelected" text bg/>
              <Button v-if="category === 'todo'" label="禁用" icon="pi pi-ban" severity="warning" size="small" @click="disableSelected" text bg/>
              <Button label="删除" icon="pi pi-trash" severity="danger" size="small" @click="batchDelete" text bg/>
            </div>
          </div>
        </div>

        <!-- 表格 -->
        <DataTable v-model:selection="selected" :value="list" stripedRows dataKey="id" size="small" class="custom-datatable" scrollable scrollHeight="400px">
          <Column selectionMode="multiple" headerStyle="width: 40px"></Column>
          
          <!-- 动态列渲染，优化显示效果 -->
          <Column v-for="col in tableColumns" :key="col" :field="col" :header="col">
             <template #body="{ data, field }">
                <span v-if="field === 'isDeleted'">
                  <Tag :severity="data[field] ? 'danger' : 'success'" :value="data[field] ? '已删除' : '有效'"></Tag>
                </span>
                <span v-else-if="field === 'status' && category === 'todo'">
                   <Tag :severity="data[field] === 1 ? 'success' : 'warning'" :value="data[field] === 1 ? '完成' : '未完'"></Tag>
                </span>
                <span v-else class="text-truncate">{{ data[field] }}</span>
             </template>
          </Column>

          <Column header="操作" headerStyle="width: 80px" alignFrozen="right" frozen>
            <template #body="{ data }">
              <Button icon="pi pi-trash" text rounded severity="danger" size="small" 
                @click="(async()=>{ 
                  let s=500; 
                  if(category==='todo'){ s=await todoDelete(data.id) } 
                  else if(category==='comment'){ const resp = await (await import('../CommentMm/components/TableView/deleteComment')); s = await resp.deleteComment(data.id) } 
                  if(s===200){ ElMessage.success('删除成功'); await load() } else { ElMessage.error('无法连接服务器') } 
                })()"
              />
            </template>
          </Column>
        </DataTable>

        <div class="pagination-container">
          <span class="total-info">共 {{ total }} 条数据</span>
          <el-pagination 
            background 
            layout="prev, pager, next, sizes" 
            :total="total" 
            v-model:current-page="pageNum" 
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            @current-change="handleDebouncedLoad"
            @size-change="handleDebouncedLoad"
          />
        </div>
      </div>

    </div>
  </el-scrollbar>
</template>

<style scoped>
.dashboard-bg {
  background-color: #f8f9fa;
  height: 100%;
}

.dashboard-container {
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Header */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.main-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
}
.sub-title {
  color: #64748b;
  font-size: 14px;
  margin: 4px 0 0 0;
}

/* Metric Cards */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
}
.metric-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.05);
  transition: transform 0.2s;
}
.metric-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1);
}
.metric-icon {
  width: 50px;
  height: 50px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}
/* Card Colors */
.user-card .metric-icon { background: #e0f2fe; color: #0284c7; }
.note-card .metric-icon { background: #dcfce7; color: #16a34a; }
.comment-card .metric-icon { background: #fef9c3; color: #ca8a04; }
.todo-card .metric-icon { background: #f3e8ff; color: #9333ea; }

.metric-info {
  display: flex;
  flex-direction: column;
}
.metric-info .label {
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
}
.metric-info .value {
  color: #1e293b;
  font-size: 24px;
  font-weight: 700;
}

/* Charts */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.chart-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 4px rgb(0 0 0 / 0.05);
}
.echart-instance {
  width: 100%;
  height: 300px;
}

/* Data Panel */
.data-panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgb(0 0 0 / 0.05);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.panel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 15px;
}
.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.search-field {
  width: 200px;
}
.filter-group, .action-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.action-group {
  padding-left: 12px;
  border-left: 1px solid #e2e8f0;
}

/* Table & Pagination */
.text-truncate {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 300px;
}
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.total-info {
  color: #94a3b8;
  font-size: 13px;
}

/* Responsive */
@media (max-width: 1024px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
  .panel-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .toolbar-left, .toolbar-right {
    justify-content: space-between;
  }
}
</style>