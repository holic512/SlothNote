<!--
@file AdminDashboardPage
@project SlothNote
@module 管理后台 / 仪表盘
@description 展示系统指标、图表和近期管理数据列表。
@logic 1. 加载仪表盘指标与近期数据；2. 初始化并维护 ECharts 图表；3. 支持按评论、待办、笔记分类筛选与批量操作。
@dependencies API: admin/dashboard/*, Component: Element Plus table compatibility, Library: ECharts
@index_tags 仪表盘, 后台首页, 数据统计, 近期数据, 单选筛选
@author holic512
-->
<script setup lang="ts">
import { onMounted, ref, computed, watch, onUnmounted, nextTick } from "vue";
// Element Plus
import { ElMessage } from "element-plus";
// ECharts（按需注册，避免仪表盘引入完整图表库）
import * as echarts from 'echarts/core';
import { BarChart, PieChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TitleComponent, TooltipComponent } from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';
// API & Utils (保持原样)
import { fetchMetrics, fetchRecent, todoDelete, todoBatchDelete, todoBatchEnable, todoBatchDisable } from "./components/api";
import { debounceImmediate } from "@/util/debounce";

echarts.use([
  BarChart,
  PieChart,
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
  CanvasRenderer,
]);

type DashboardMetrics = {
  userCount: number;
  noteCount: number;
  folderCount: number;
  commentCount: number;
  favoriteNoteCount: number;
  favoriteFolderCount: number;
  todoCount: number;
}

type MetricCard = {
  key: string;
  label: string;
  value: number | string;
  icon: string;
  tone: string;
  helper?: string;
}

// --- 状态定义 ---
const metrics = ref<Partial<DashboardMetrics>>({});
const category = ref<string>('comment');
const q = ref<string>('');
const userIdFilter = ref<number | undefined>(undefined);
const deletedFilter = ref<boolean | undefined>(undefined);
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

const toCount = (value?: number) => value ?? 0;
const formatRatio = (value: number) => `${Number.isFinite(value) ? value.toFixed(1) : '0.0'}%`;

const contentTotal = computed(() => (
  toCount(metrics.value.noteCount) +
  toCount(metrics.value.folderCount) +
  toCount(metrics.value.commentCount) +
  toCount(metrics.value.todoCount)
));

const favoriteTotal = computed(() => (
  toCount(metrics.value.favoriteNoteCount) +
  toCount(metrics.value.favoriteFolderCount)
));

const avgNotesPerUser = computed(() => {
  const users = toCount(metrics.value.userCount);
  return users === 0 ? '0.0' : (toCount(metrics.value.noteCount) / users).toFixed(1);
});

const commentCoverage = computed(() => {
  const notes = toCount(metrics.value.noteCount);
  return formatRatio(notes === 0 ? 0 : (toCount(metrics.value.commentCount) / notes) * 100);
});

const favoriteCoverage = computed(() => {
  const base = toCount(metrics.value.noteCount) + toCount(metrics.value.folderCount);
  return formatRatio(base === 0 ? 0 : (favoriteTotal.value / base) * 100);
});

const metricCards = computed<MetricCard[]>(() => [
  { key: 'user', label: '用户总数', value: toCount(metrics.value.userCount), icon: 'Users', tone: 'blue', helper: '平台注册账户' },
  { key: 'note', label: '笔记总数', value: toCount(metrics.value.noteCount), icon: 'Book', tone: 'green', helper: '已创建笔记' },
  { key: 'folder', label: '文件夹数', value: toCount(metrics.value.folderCount), icon: 'Folder', tone: 'slate', helper: '笔记归档结构' },
  { key: 'comment', label: '评论总数', value: toCount(metrics.value.commentCount), icon: 'Comments', tone: 'amber', helper: '内容互动量' },
  { key: 'todo', label: '待办事项', value: toCount(metrics.value.todoCount), icon: 'CheckSquare', tone: 'violet', helper: '用户任务记录' },
  { key: 'favoriteNote', label: '收藏笔记', value: toCount(metrics.value.favoriteNoteCount), icon: 'Star', tone: 'cyan', helper: '被收藏内容' },
  { key: 'favoriteFolder', label: '收藏文件夹', value: toCount(metrics.value.favoriteFolderCount), icon: 'Bookmark', tone: 'rose', helper: '被收藏目录' },
  { key: 'content', label: '内容总量', value: contentTotal.value, icon: 'Database', tone: 'indigo', helper: '笔记/评论/文件夹/待办' },
]);

const dashboardFacts = computed(() => [
  { label: '收藏总量', value: favoriteTotal.value },
  { label: '收藏覆盖', value: favoriteCoverage.value },
  { label: '人均笔记', value: avgNotesPerUser.value },
  { label: '评论覆盖', value: commentCoverage.value },
]);

// --- 生命周期 & 加载 ---
onMounted(async () => {
  // 并行加载数据，提高速度
  const [metricsData] = await Promise.all([
    fetchMetrics().catch(() => undefined),
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
    pieChartInst = pieChartInst ?? echarts.init(chartPieRef.value);
    pieChartInst.setOption({
      title: { text: '数据分布', left: 'center', textStyle: { fontSize: 14, fontWeight: 600, color: '#1f2937' } },
      tooltip: { trigger: 'item' },
      color: ['#3b82f6', '#22c55e', '#64748b', '#f59e0b'],
      legend: { bottom: '0%', left: 'center', itemWidth: 10, itemHeight: 10, textStyle: { color: '#64748b' } },
      series: [{
        name: '数据统计',
        type: 'pie',
        radius: ['48%', '70%'],
        itemStyle: { borderRadius: 5, borderColor: '#fff', borderWidth: 2 },
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
    barChartInst = barChartInst ?? echarts.init(chartBarRef.value);
    barChartInst.setOption({
      title: { text: '收藏转化情况', left: 'center', textStyle: { fontSize: 14, fontWeight: 600, color: '#1f2937' } },
      tooltip: { trigger: 'axis' },
      legend: { top: 28, itemWidth: 10, itemHeight: 10, textStyle: { color: '#64748b' } },
      grid: { left: '3%', right: '4%', top: 72, bottom: '8%', containLabel: true },
      xAxis: { type: 'category', data: ['笔记', '文件夹'], axisTick: { show: false }, axisLine: { lineStyle: { color: '#d7dde6' } } },
      yAxis: { type: 'value', splitLine: { lineStyle: { color: '#edf1f5' } } },
      series: [
        {
          name: '总数',
          type: 'bar',
          data: [metrics.value.noteCount || 0, metrics.value.folderCount || 0],
          barWidth: 28,
          itemStyle: { color: '#60a5fa', borderRadius: [4, 4, 0, 0] }
        },
        {
          name: '被收藏数',
          type: 'bar',
          data: [metrics.value.favoriteNoteCount || 0, metrics.value.favoriteFolderCount || 0],
          barWidth: 28,
          itemStyle: { color: '#f59e0b', borderRadius: [4, 4, 0, 0] }
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
  try {
    const data = await fetchRecent({
      category: category.value,
      q: q.value || undefined,
      userId: userIdFilter.value,
      isDeleted: deletedFilter.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });
    list.value = data?.list ?? [];
    total.value = data?.total ?? 0;
  } catch {
    list.value = [];
    total.value = 0;
  }
}

const handleDebouncedLoad = debounceImmediate(load, 300);

const changeCategory = async () => {
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
      <!-- 2. 关键指标卡片区 (Grid Layout) -->
      <div class="metrics-grid">
        <div v-for="item in metricCards" :key="item.key" class="metric-card" :class="`tone-${item.tone}`">
          <div class="metric-icon">
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
          </div>
          <div class="metric-info">
            <span class="label">{{ item.label }}</span>
            <span class="value">{{ item.value }}</span>
            <span class="helper">{{ item.helper }}</span>
          </div>
        </div>
      </div>

      <div class="facts-strip">
        <div v-for="fact in dashboardFacts" :key="fact.label" class="fact-item">
          <span>{{ fact.label }}</span>
          <strong>{{ fact.value }}</strong>
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
        <div class="panel-toolbar">
          <div class="toolbar-left">
            <el-radio-group v-model="category" @change="changeCategory" size="small">
              <el-radio-button value="comment">评论</el-radio-button>
              <el-radio-button value="todo">待办</el-radio-button>
              <el-radio-button value="note">笔记</el-radio-button>
            </el-radio-group>

            <IconField class="search-field">
              <InputIcon icon="Search"/>
              <InputText v-model="q" placeholder="搜索关键词..." class="compact-input" @keydown.enter="handleDebouncedLoad"/>
            </IconField>
          </div>

          <div class="toolbar-right">
            <div class="filter-group">
               <el-input v-model.number="userIdFilter" placeholder="用户ID" size="small" style="width: 100px"/>
               <el-select v-model="deletedFilter" placeholder="状态" size="small" style="width: 100px" clearable>
                <el-option label="有效" :value="false"/>
                <el-option label="已删除" :value="true"/>
              </el-select>
              <Button icon="Filter" rounded outlined size="small" @click="handleDebouncedLoad"/>
            </div>

            <div class="action-group" v-if="selected.length > 0">
              <Button v-if="category === 'todo'" label="启用" icon="Check" severity="success" size="small" @click="enableSelected" text bg/>
              <Button v-if="category === 'todo'" label="禁用" icon="Ban" severity="warning" size="small" @click="disableSelected" text bg/>
              <Button label="删除" icon="Trash" severity="danger" size="small" @click="batchDelete" text bg/>
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
              <Button icon="Trash" text rounded severity="danger" size="small" 
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
  background: transparent;
  height: 100%;
}

.dashboard-container {
  width: 100%;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* Metric Cards */
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  gap: 10px;
}
.metric-card {
  background: var(--sn-bg-surface);
  border: 1px solid var(--sn-border);
  border-radius: var(--sn-radius-lg);
  padding: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: var(--sn-shadow-sm);
  transition: border-color 0.2s, box-shadow 0.2s;
}
.metric-card:hover {
  border-color: var(--sn-border-strong);
  box-shadow: var(--sn-shadow-md);
}
.metric-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--sn-radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
}

.tone-blue .metric-icon,
.tone-green .metric-icon,
.tone-slate .metric-icon,
.tone-amber .metric-icon,
.tone-violet .metric-icon,
.tone-cyan .metric-icon,
.tone-rose .metric-icon,
.tone-indigo .metric-icon { background: var(--sn-bg-muted); color: var(--sn-text-regular); }

.metric-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.metric-info .label {
  color: var(--sn-text-regular);
  font-size: 12px;
  font-weight: 500;
  line-height: 1.2;
}
.metric-info .value {
  color: var(--sn-text-primary);
  font-size: 20px;
  font-weight: 700;
  line-height: 1.25;
}
.metric-info .helper {
  overflow: hidden;
  color: var(--sn-text-muted);
  font-size: 11px;
  line-height: 1.2;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.facts-strip {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid var(--sn-border);
  border-radius: var(--sn-radius-lg);
  background: var(--sn-bg-muted);
}

.fact-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
  color: var(--sn-text-muted);
  font-size: 12px;
}

.fact-item strong {
  color: var(--sn-text-primary);
  font-size: 14px;
  font-weight: 700;
}

/* Charts */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.chart-container {
  background: var(--sn-bg-surface);
  border: 1px solid var(--sn-border);
  border-radius: var(--sn-radius-lg);
  padding: 12px;
  box-shadow: var(--sn-shadow-sm);
}
.echart-instance {
  width: 100%;
  height: 260px;
}

/* Data Panel */
.data-panel {
  background: var(--sn-bg-surface);
  border: 1px solid var(--sn-border);
  border-radius: var(--sn-radius-lg);
  box-shadow: var(--sn-shadow-sm);
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.panel-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  border-bottom: 1px solid var(--sn-border);
  padding-bottom: 10px;
}
.toolbar-left, .toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search-field {
  width: 210px;
}
.filter-group, .action-group {
  display: flex;
  align-items: center;
  gap: 8px;
}
.action-group {
  padding-left: 8px;
  border-left: 1px solid var(--sn-border);
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
  margin-top: 4px;
}
.total-info {
  color: var(--sn-text-muted);
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
    flex-wrap: wrap;
  }
}
</style>
