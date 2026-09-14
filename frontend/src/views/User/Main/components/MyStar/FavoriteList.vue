<!--
@file UserFavoriteListPage
@project SlothNote
@module 用户端 / 收藏页面
@description 展示用户收藏分类与收藏笔记列表。
@logic 1. 加载收藏分类和分类下笔记并取消过时请求；2. KeepAlive 激活时刷新；3. 使用客户端分页限制单次 DOM 渲染数量。
@dependencies API: user/note/favorite/*, VueRouter: useRouter, ElementPlus: ElMessageBox
@index_tags 收藏页面, 收藏分类, KeepAlive, 路由切换性能
@author holic512
-->
<script setup lang="ts">
import {computed, onActivated, onBeforeUnmount, onDeactivated, onMounted, ref, watch} from 'vue';
import axios from '@/axios';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {navigateToNote} from '@/views/User/Main/components/Edit/service/noteNavigation';
import { 
  Folder as IconFolder, 
  Document, 
  Plus, 
  MoreFilled, 
  CollectionTag
} from '@element-plus/icons-vue';

defineOptions({
  name: 'UserFavoriteListPage'
})

// --- 类型定义 ---
interface Folder {
  id: number;
  name: string;
}

interface Note {
  id: number;
  noteId: number;
  folderId: number;
  noteRemark: string;
  updatedAt?: string; // 假设后端加了这个字段
  icon?: string;
  title?: string;
}

const router = useRouter();
const folders = ref<Folder[]>([]);
const selectedFolderId = ref<number>(0); // 0 代表 "未分类" 或 "全部"
const notes = ref<Note[]>([]);
const loading = ref(false);
const currentPage = ref(1);
const pageSize = 50;
let folderRequestId = 0;
let noteRequestId = 0;
let folderController: AbortController | null = null;
let noteController: AbortController | null = null;
let isActive = true;
let hasLoadedOnce = false;

// 模拟一个 "未分类/全部" 的固定选项
const defaultFolder = { id: 0, name: '默认收藏' };

// --- API 逻辑 ---

const fetchFolders = async () => {
  folderController?.abort();
  const controller = new AbortController();
  folderController = controller;
  const requestId = ++folderRequestId;
  try {
    const resp = await axios.get('user/note/favorite/folders', {signal: controller.signal});
    if (controller.signal.aborted || requestId !== folderRequestId || !isActive) return;
    if (resp.data?.status === 200) {
      folders.value = resp.data.data.map((f: any) => ({ id: f.id, name: f.folderName }));
    }
  } catch (e) {
    if (!controller.signal.aborted && requestId === folderRequestId && isActive) console.error(e);
  }
};

const fetchNotes = async () => {
  noteController?.abort();
  const controller = new AbortController();
  noteController = controller;
  const requestId = ++noteRequestId;
  const folderId = selectedFolderId.value;
  loading.value = true;
  try {
    const resp = await axios.get('user/note/favorite/notes', {
      params: {folderId},
      signal: controller.signal,
    });
    if (controller.signal.aborted || requestId !== noteRequestId || !isActive || folderId !== selectedFolderId.value) return;
    if (resp.data?.status === 200) {
      notes.value = resp.data.data || [];
    } else {
      notes.value = [];
    }
    currentPage.value = 1;
    hasLoadedOnce = true;
  } catch (e) {
    if (controller.signal.aborted || requestId !== noteRequestId || !isActive) return;
    notes.value = [];
  } finally {
    if (requestId === noteRequestId && isActive) loading.value = false;
  }
};

onMounted(async () => {
  isActive = true;
  await fetchFolders();
  await fetchNotes();
});

onActivated(() => {
  isActive = true;
  if (hasLoadedOnce) {
    void Promise.all([fetchFolders(), fetchNotes()]);
  }
});

const deactivateFavoriteList = () => {
  isActive = false;
  folderRequestId += 1;
  noteRequestId += 1;
  folderController?.abort();
  noteController?.abort();
  folderController = null;
  noteController = null;
};

onDeactivated(deactivateFavoriteList);
onBeforeUnmount(deactivateFavoriteList);

// 监听文件夹切换
watch(() => selectedFolderId.value, () => {
  if (isActive) void fetchNotes();
});

// --- 交互逻辑 ---

const handleSelectFolder = (id: number) => {
  selectedFolderId.value = id;
};

const openNote = async (noteId: number) => {
  await navigateToNote(router, {noteId});
};

const createFolder = async () => {
  try {
    const { value } = await ElMessageBox.prompt('输入新分类名称', '新建分类', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputPlaceholder: '例如：工作文档、稍后阅读...',
      customClass: 'notion-msg-box', // 可以自定义样式
    });
    if (!value) return;
    
    const resp = await axios.post('user/note/favorite/folders/add', { folderName: value });
    if (resp.data?.status === 200) {
      ElMessage.success('分类已创建');
      await fetchFolders();
      // 自动切换到新分类 (假设后端返回了新ID，这里简化处理重新拉取)
    } else {
      ElMessage.error('创建失败');
    }
  } catch (e) {
    // User cancelled
  }
};

// 获取当前选中的文件夹名称，用于显示在右侧标题
const currentFolderName = computed(() => {
  if (selectedFolderId.value === 0) return defaultFolder.name;
  const f = folders.value.find(i => i.id === selectedFolderId.value);
  return f ? f.name : '未知分类';
});

const pagedNotes = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  return notes.value.slice(start, start + pageSize);
});

// 随机生成个时间模拟一下
const getRandomDate = () => {
  return new Date().toLocaleDateString();
}
</script>

<template>
  <div class="notion-favorites-layout">
    
    <!-- 左侧侧边栏：分类列表 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <span class="sidebar-title">收藏夹</span>
      </div>
      
      <div class="folder-list">
        <!-- 默认分类 -->
        <div 
          class="folder-item" 
          :class="{ active: selectedFolderId === 0 }"
          @click="handleSelectFolder(0)"
        >
          <el-icon class="folder-icon"><CollectionTag /></el-icon>
          <span class="folder-name">{{ defaultFolder.name }}</span>
        </div>

        <!-- 用户分类 -->
        <div 
          v-for="f in folders" 
          :key="f.id" 
          class="folder-item"
          :class="{ active: selectedFolderId === f.id }"
          @click="handleSelectFolder(f.id)"
        >
          <el-icon class="folder-icon"><IconFolder /></el-icon>
          <span class="folder-name">{{ f.name }}</span>
        </div>

        <!-- 新建按钮 -->
        <div class="folder-item add-btn" @click="createFolder">
          <el-icon class="folder-icon"><Plus /></el-icon>
          <span class="folder-name">新建分类</span>
        </div>
      </div>
    </aside>

    <!-- 右侧主内容：笔记列表 -->
    <main class="main-content">
      <!-- 顶部 Banner / 标题区 -->
      <div class="content-header">
        <div class="header-icon">📂</div> <!-- 这里可以是动态的 Emoji -->
        <h1 class="header-title">{{ currentFolderName }}</h1>
        <div class="header-meta">{{ notes.length }} 个页面</div>
      </div>

      <!-- 列表区域 -->
      <div class="content-body" v-loading="loading">
        
        <!-- 表头 (伪) -->
        <div class="list-header-row">
          <div class="col-name">名称</div>
          <div class="col-meta">备注信息</div>
          <div class="col-action"></div>
        </div>

        <!-- 空状态 -->
        <div v-if="notes.length === 0 && !loading" class="empty-placeholder">
          此分类下暂无收藏笔记
        </div>

        <!-- 列表项 -->
        <div 
          v-for="note in pagedNotes"
          :key="note.id" 
          class="list-item"
          @click="openNote(note.noteId)"
        >
          <div class="col-name">
            <span v-if="note.icon" class="item-emoji">{{ note.icon }}</span>
            <el-icon v-else class="item-icon"><Document /></el-icon>
            <span class="item-text">{{ note.title || note.noteRemark || `笔记 #${note.noteId}` }}</span>
          </div>
          <div class="col-meta">
            <span class="tag">ID: {{ note.noteId }}</span>
            <!-- 模拟的更新时间 -->
            <span class="date">{{ note.updatedAt || getRandomDate() }}</span>
          </div>
          <div class="col-action">
            <!-- 悬停时显示的更多操作按钮 -->
            <div class="action-btn" @click.stop>
              <el-icon><MoreFilled /></el-icon>
            </div>
          </div>
        </div>

        <el-pagination
            v-if="notes.length > pageSize"
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="notes.length"
            layout="prev, pager, next"
            class="favorite-pagination"
        />

      </div>
    </main>
  </div>
</template>

<style scoped>
/* --- 布局变量 --- */
:root {
  --n-bg-sidebar: #F7F7F5;
  --n-bg-main: #FFFFFF;
  --n-hover: #EFEFEF;
  --n-active: #E8E8E8;
  --n-text-primary: #37352F;
  --n-text-secondary: #9B9A97;
  --n-border: #E9E9E8;
}

.notion-favorites-layout {
  display: flex;
  height: calc(100vh - 40px); /* 减去可能的顶栏高度 */
  background: var(--n-bg-main);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
  color: #37352F;
  overflow: hidden;
}

/* --- 侧边栏 --- */
.sidebar {
  width: 240px;
  background: #F7F7F5;
  border-right: 1px solid #E9E9E8;
  display: flex;
  flex-direction: column;
  padding: 12px 0;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 0 14px;
  margin-bottom: 8px;
}
.sidebar-title {
  font-size: 12px;
  font-weight: 600;
  color: #9B9A97;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.folder-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 4px;
}

.folder-item {
  display: flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #5F5E5B;
  transition: background 0.1s, color 0.1s;
  margin-bottom: 1px;
}
.folder-item:hover {
  background: #EFEFEF;
  color: #37352F;
}
.folder-item.active {
  background: #E8E8E8;
  color: #37352F;
  font-weight: 500;
}
.folder-item.add-btn {
  color: #9B9A97;
  margin-top: 8px;
}
.folder-item.add-btn:hover {
  color: #37352F;
}

.folder-icon {
  margin-right: 8px;
  font-size: 16px;
}
.folder-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* --- 主内容区 --- */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  overflow: hidden;
}

.content-header {
  padding: 48px 60px 24px 60px; /* Notion 经典的宽padding */
  border-bottom: 1px solid #E9E9E8;
}
.header-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.header-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0;
  color: #37352F;
}
.header-meta {
  margin-top: 8px;
  color: #9B9A97;
  font-size: 14px;
}

/* --- 列表部分 --- */
.content-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 60px;
}

.list-header-row {
  display: flex;
  padding: 8px 4px;
  border-bottom: 1px solid #E9E9E8;
  color: #9B9A97;
  font-size: 12px;
  margin-bottom: 4px;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 8px 4px;
  border-bottom: 1px solid #F0F0F0;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.1s;
  border-radius: 3px;
}
.list-item:hover {
  background: #F7F7F5;
}
.list-item:last-child {
  border-bottom: none;
}

/* 列宽控制 */
.col-name {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #37352F;
  font-weight: 500;
}
.col-meta {
  width: 200px;
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: flex-end;
  color: #9B9A97;
  font-size: 13px;
}
.col-action {
  width: 40px;
  display: flex;
  justify-content: center;
}

.item-icon {
  color: #9B9A97;
  font-size: 16px;
}
.item-emoji {
  font-size: 18px;
  line-height: 18px;
}
.item-text {
  border-bottom: 1px solid transparent;
}

/* 标签和日期样式 */
.tag {
  background: #F0F0F0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}
.date {
  font-size: 12px;
}

.action-btn {
  opacity: 0;
  color: #9B9A97;
  padding: 4px;
  border-radius: 4px;
}
.action-btn:hover {
  background: #E9E9E8;
  color: #37352F;
}
.list-item:hover .action-btn {
  opacity: 1;
}

.empty-placeholder {
  padding: 40px;
  text-align: center;
  color: #D3D3D3;
  font-style: italic;
}

.favorite-pagination {
  justify-content: flex-end;
  padding: 16px 0 4px;
}
</style>
