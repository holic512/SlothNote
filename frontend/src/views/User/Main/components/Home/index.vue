<!--
@file UserHomeDashboard
@project SlothNote
@module 用户端 / 首页仪表盘
@description 展示用户笔记、文件夹、收藏和待办概览数据。
@logic 1. 并发加载首页概览并取消过时请求；2. KeepAlive 激活时刷新数据；3. 渲染指标卡、状态分布和最近访问列表。
@dependencies API: user/dashboard/*, ElementPlus: el-icon
@index_tags 用户首页, 仪表盘, KeepAlive, 路由切换性能
@author holic512
-->
<script setup lang="ts">
import {computed, onActivated, onBeforeUnmount, onDeactivated, onMounted, ref} from 'vue';
import axios from '@/axios'; // 假设你的axios封装在这里
// 引入 Element Plus 图标
import { Document, Folder, Star, CircleCheck } from '@element-plus/icons-vue';

defineOptions({
  name: 'UserHomeDashboard'
})

// --- 类型定义 ---
interface Overview {
  notesTotal: number;
  foldersTotal: number;
  favoritesTotal: number;
  todosTotal: number;
  todosCompleted: number;
}

interface TodoStatus {
  incomplete: number;
  completed: number;
  deleted: number;
}

interface TodoWeekItem {
  date: string;
  count: number;
}

interface NoteItem {
  id: number;
  title: string;
  updatedAt: string;
  icon?: string; // 新增：建议后端返回 emoji，如 "📝"
}

// --- 状态数据 ---
// 模拟用户信息，建议从 store 或 接口获取
const userName = ref('Notioner'); 
const currentTime = ref(new Date());

const overview = ref<Overview>({ notesTotal: 0, foldersTotal: 0, favoritesTotal: 0, todosTotal: 0, todosCompleted: 0 });
const todoStatus = ref<TodoStatus>({ incomplete: 0, completed: 0, deleted: 0 });
const todoWeek = ref<TodoWeekItem[]>([]);
const recentNotes = ref<NoteItem[]>([]);
const loading = ref(true);
let dashboardRequestId = 0;
let dashboardController: AbortController | null = null;
let isActive = true;
let hasLoadedOnce = false;

// --- 数据加载 ---
const loadData = async () => {
  dashboardController?.abort();
  const controller = new AbortController();
  dashboardController = controller;
  const requestId = ++dashboardRequestId;
  loading.value = true;
  try {
    const [o, s, w, r] = await Promise.all([
      axios.get('user/dashboard/overview', {signal: controller.signal}),
      axios.get('user/dashboard/todoStatus', {signal: controller.signal}),
      axios.get('user/dashboard/todoWeek', {signal: controller.signal}),
      axios.get('user/dashboard/recentNotes', {signal: controller.signal}),
    ]);
    if (controller.signal.aborted || requestId !== dashboardRequestId || !isActive) return;
    overview.value = o.data.data || overview.value;
    todoStatus.value = s.data.data || todoStatus.value;
    todoWeek.value = w.data.data || [];
    recentNotes.value = r.data.data || [];
    hasLoadedOnce = true;
  } catch (e) {
    if (controller.signal.aborted || requestId !== dashboardRequestId || !isActive) return;
    console.error("Dashboard data load failed", e);
  } finally {
    if (requestId === dashboardRequestId && isActive) loading.value = false;
  }
};

onMounted(() => {
  isActive = true;
  void loadData();
});

onActivated(() => {
  isActive = true;
  if (hasLoadedOnce) void loadData();
});

const deactivateDashboard = () => {
  isActive = false;
  dashboardRequestId += 1;
  dashboardController?.abort();
  dashboardController = null;
};

onDeactivated(deactivateDashboard);
onBeforeUnmount(deactivateDashboard);

// --- 计算逻辑 ---
const maxWeekCount = computed(() => Math.max(1, ...todoWeek.value.map(i => i.count)));

// 格式化日期的简单函数 (模仿 Notion: "Oct 24, 2023")
const formatDate = (dateStr: string) => {
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
};

// 获取问候语
const greeting = computed(() => {
  const hour = currentTime.value.getHours();
  if (hour < 12) return '早上好';
  if (hour < 18) return '下午好';
  return '晚上好';
});
</script>

<template>
  <div class="notion-dashboard">
    <!-- 顶部封面区域 (Notion Cover) -->
    <div class="page-cover">
      <img src="https://images.unsplash.com/photo-1486312338219-ce68d2c6f44d?w=1200&auto=format&fit=crop&q=60" alt="Cover" />
    </div>

    <div class="content-container">
      <!-- 标题区 -->
      <div class="page-header">
        <div class="page-icon">🏠</div>
        <h1 class="page-title">{{ greeting }}，{{ userName }}</h1>
        <div class="page-desc">这是你的个人工作台概览。</div>
      </div>

      <!-- 核心指标 (Notion Callout 风格) -->
      <div class="grid-row">
        <div class="n-card kpi-block">
          <div class="kpi-icon-box bg-blue"><el-icon><Document /></el-icon></div>
          <div class="kpi-content">
            <div class="kpi-label">全部笔记</div>
            <div class="kpi-num">{{ overview.notesTotal }}</div>
          </div>
        </div>
        <div class="n-card kpi-block">
          <div class="kpi-icon-box bg-yellow"><el-icon><Folder /></el-icon></div>
          <div class="kpi-content">
            <div class="kpi-label">文件夹</div>
            <div class="kpi-num">{{ overview.foldersTotal }}</div>
          </div>
        </div>
        <div class="n-card kpi-block">
          <div class="kpi-icon-box bg-red"><el-icon><Star /></el-icon></div>
          <div class="kpi-content">
            <div class="kpi-label">收藏夹</div>
            <div class="kpi-num">{{ overview.favoritesTotal }}</div>
          </div>
        </div>
        <div class="n-card kpi-block">
          <div class="kpi-icon-box bg-green"><el-icon><CircleCheck /></el-icon></div>
          <div class="kpi-content">
            <div class="kpi-label">待办事项</div>
            <div class="kpi-num">{{ overview.todosTotal }}</div>
          </div>
        </div>
      </div>

      <!-- 中部区域：待办状态 & 趋势 -->
      <div class="grid-layout-2-1">
        
        <!-- 左侧：待办状态 (模仿 Notion Board 属性) -->
        <div class="n-section">
          <div class="section-header">
            <span class="section-emoji">✅</span> 待办状态分布
          </div>
          <div class="n-card status-card">
            <div class="status-row">
              <div class="status-label">
                <span class="status-dot done"></span> 已完成
              </div>
              <div class="status-bar-container">
                <div class="status-bar done" :style="{width: Math.min(100, (todoStatus.completed / Math.max(1, overview.todosTotal)) * 100) + '%'}"></div>
              </div>
              <div class="status-value">{{ todoStatus.completed }}</div>
            </div>

            <div class="status-row">
              <div class="status-label">
                <span class="status-dot todo"></span> 进行中
              </div>
              <div class="status-bar-container">
                <div class="status-bar todo" :style="{width: Math.min(100, (todoStatus.incomplete / Math.max(1, overview.todosTotal)) * 100) + '%'}"></div>
              </div>
              <div class="status-value">{{ todoStatus.incomplete }}</div>
            </div>

            <div class="status-row">
              <div class="status-label">
                <span class="status-dot deleted"></span> 已删除
              </div>
              <div class="status-bar-container">
                <div class="status-bar deleted" :style="{width: Math.min(100, (todoStatus.deleted / Math.max(1, overview.todosTotal)) * 100) + '%'}"></div>
              </div>
              <div class="status-value">{{ todoStatus.deleted }}</div>
            </div>
          </div>
        </div>

        <!-- 右侧：近7天趋势 (极简柱状图) -->
        <div class="n-section">
          <div class="section-header">
            <span class="section-emoji">📊</span> 本周动态
          </div>
          <div class="n-card chart-card">
            <div class="bar-chart">
              <div v-for="d in todoWeek" :key="d.date" class="bar-group">
                <div class="bar-track">
                  <div 
                    class="bar-fill" 
                    :style="{ height: (d.count / maxWeekCount) * 100 + '%' }"
                    :title="`${d.date}: ${d.count}`"
                  >
                    <span v-if="d.count > 0" class="bar-tooltip">{{ d.count }}</span>
                  </div>
                </div>
                <div class="bar-date">{{ d.date.slice(-2) }}日</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部：最近笔记 (模仿 Notion List Database) -->
      <div class="n-section" style="margin-top: 32px;">
        <div class="section-header">
          <span class="section-emoji">🕒</span> 最近访问
        </div>
        <div class="n-card list-view">
          <div class="list-header">
            <div class="col-title">名称</div>
            <div class="col-date">最后更新</div>
          </div>
          <div v-if="recentNotes.length === 0" class="empty-state">暂无最近笔记</div>
          
          <div 
            v-for="note in recentNotes" 
            :key="note.id" 
            class="list-item"
          >
            <div class="item-title">
              <span class="item-icon">{{ note.icon || '📄' }}</span>
              <span class="item-text">{{ note.title }}</span>
            </div>
            <div class="item-date">{{ formatDate(note.updatedAt) }}</div>
          </div>
          <div class="list-footer">
            + 查看全部笔记
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* --- 核心变量 --- */
:root {
  --n-bg: #FFFFFF;
  --n-text: #37352F;
  --n-text-light: #9B9A97;
  --n-border: #E9E9E8;
  --n-hover: #F7F7F5; /* Notion 悬停灰 */
  --n-blue: #E7F3F8;
  --n-yellow: #FBF3DB;
  --n-red: #FBE4E4;
  --n-green: #EDF3EC;
  --n-bar-blue: #5C9EB5;
}

.notion-dashboard {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol";
  color: #37352F;
  background-color: #FFFFFF;
  min-height: 100vh;
  padding-bottom: 60px;
}

/* --- Cover & Header --- */
.page-cover {
  height: 180px;
  width: 100%;
  overflow: hidden;
}
.page-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center 30%;
}

.content-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 0 48px; /* Notion 典型的左右留白 */
  position: relative;
}

.page-header {
  margin-top: -40px; /* 让图标盖住封面一部分 */
  margin-bottom: 32px;
}
.page-icon {
  font-size: 78px;
  line-height: 1;
  margin-bottom: 8px;
}
.page-title {
  font-size: 40px;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
}
.page-desc {
  color: #9B9A97;
  font-size: 16px;
  margin-top: 8px;
}

/* --- 通用卡片样式 (Notion Callout / Box) --- */
.n-card {
  border: 1px solid var(--n-border, #E9E9E8);
  border-radius: 4px; /* Notion 倒角很小 */
  background: white;
  transition: background 0.2s;
}

/* --- 第一行 Grid: KPI --- */
.grid-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.kpi-block {
  display: flex;
  align-items: center;
  padding: 16px;
  cursor: default;
}
.kpi-block:hover {
  background: var(--n-hover, #F7F7F5);
}

.kpi-icon-box {
  width: 36px;
  height: 36px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 18px;
}
.bg-blue { background: #E7F3F8; color: #367892; }
.bg-yellow { background: #FBF3DB; color: #D98836; }
.bg-red { background: #FBE4E4; color: #D44C47; }
.bg-green { background: #EDF3EC; color: #468156; }

.kpi-content {
  display: flex;
  flex-direction: column;
}
.kpi-label {
  font-size: 12px;
  color: #9B9A97;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  font-weight: 600;
}
.kpi-num {
  font-size: 20px;
  font-weight: 600;
  color: #37352F;
  margin-top: 2px;
}

/* --- 中间布局 --- */
.grid-layout-2-1 {
  display: grid;
  grid-template-columns: 5fr 7fr;
  gap: 24px;
}

.n-section {
  margin-bottom: 16px;
}
.section-header {
  font-size: 14px;
  font-weight: 600;
  color: #9B9A97;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  text-transform: uppercase;
}
.section-emoji {
  font-size: 16px;
  filter: grayscale(0.3); /* 让 emoji 稍微低调点 */
}

/* --- 待办状态条 --- */
.status-card {
  padding: 20px;
}
.status-row {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 14px;
}
.status-row:last-child { margin-bottom: 0; }

.status-label {
  width: 80px;
  display: flex;
  align-items: center;
  gap: 6px;
  color: #37352F;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.status-dot.done { background: #337EA9; }
.status-dot.todo { background: #D9730D; }
.status-dot.deleted { background: #EB5757; }

.status-bar-container {
  flex: 1;
  height: 6px;
  background: #F0F0F0;
  border-radius: 10px;
  margin: 0 12px;
  overflow: hidden;
}
.status-bar {
  height: 100%;
  border-radius: 10px;
  transition: width 0.5s ease;
}
.status-bar.done { background: #337EA9; }
.status-bar.todo { background: #D9730D; }
.status-bar.deleted { background: #EB5757; }

.status-value {
  width: 30px;
  text-align: right;
  color: #9B9A97;
  font-family: monospace;
}

/* --- 柱状图 --- */
.chart-card {
  padding: 20px;
  height: 164px; /* 对齐高度 */
  display: flex;
  align-items: flex-end;
}
.bar-chart {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}
.bar-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  justify-content: flex-end;
  gap: 8px;
}
.bar-track {
  width: 12px;
  height: 80%;
  background: transparent;
  display: flex;
  align-items: flex-end;
  border-radius: 4px;
}
.bar-fill {
  width: 100%;
  background: #E7F3F8; /* 浅蓝 */
  border-radius: 3px;
  transition: height 0.4s ease, background 0.2s;
  position: relative;
  min-height: 2px; /* 确保0也有个底 */
}
.bar-fill:hover {
  background: #337EA9; /* 深蓝 */
}
.bar-tooltip {
  position: absolute;
  top: -20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 10px;
  color: #37352F;
}
.bar-date {
  font-size: 12px;
  color: #9B9A97;
}

/* --- 列表视图 (Database View) --- */
.list-view {
  border: 1px solid #E9E9E8;
  display: flex;
  flex-direction: column;
}
.list-header {
  display: flex;
  padding: 8px 16px;
  border-bottom: 1px solid #E9E9E8;
  background: #FBFBFA;
  font-size: 12px;
  color: #9B9A97;
}
.col-title { flex: 1; }
.col-date { width: 120px; text-align: right; }

.list-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  border-bottom: 1px solid #F0F0F0;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.1s;
}
.list-item:last-of-type { border-bottom: none; }
.list-item:hover { background: #F7F7F5; }

.item-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}
.item-icon { font-size: 16px; }
.item-text { 
  border-bottom: 1px solid transparent; 
}
.item-date {
  width: 120px;
  text-align: right;
  color: #9B9A97;
  font-size: 13px;
}
.list-footer {
  padding: 8px 16px;
  color: #9B9A97;
  font-size: 13px;
  cursor: pointer;
  border-top: 1px solid #E9E9E8;
  transition: color 0.2s;
}
.list-footer:hover { color: #37352F; }
.empty-state {
  padding: 20px;
  text-align: center;
  color: #9B9A97;
  font-size: 13px;
}

/* --- 响应式 --- */
@media (max-width: 768px) {
  .grid-row { grid-template-columns: 1fr 1fr; }
  .grid-layout-2-1 { grid-template-columns: 1fr; }
  .content-container { padding: 0 20px; }
}
</style>
