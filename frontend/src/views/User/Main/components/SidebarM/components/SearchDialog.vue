<script setup lang="ts">
import { ref, watch, nextTick } from 'vue';
import { searchNotes } from '../service/searchNotes'; // 请确保路径正确
import { useRouter } from 'vue-router';
import { useSearchDialogStore } from '../Pinia/searchDialogStore'; // 请确保路径正确
// 引入图标
import { Search, Document, Loading, Right } from '@element-plus/icons-vue';

// --- 类型定义 ---
interface SearchResult {
  noteId: number;
  title: string;
  summary?: string; // 简介
  snippet?: string; // 匹配片段
  icon?: string;    // 新增：笔记图标(emoji)
  path?: string;    // 新增：路径，如 "工作台 / 2023"
}

const store = useSearchDialogStore();
const keyword = ref('');
const loading = ref(false);
const results = ref<SearchResult[]>([]);
const router = useRouter();
const searchInputRef = ref();

// --- 逻辑处理 ---

const doSearch = async () => {
  if (!keyword.value.trim()) {
    results.value = [];
    return;
  }
  loading.value = true;
  try {
    // 这里假设 searchNotes 返回的是 Promise<SearchResult[]>
    const res = await searchNotes(keyword.value);
    // 兼容处理：如果接口没有返回 icon/path，前端补全结构以免报错
    results.value = (res || []).map((item: any) => ({
      ...item,
      path: item.path || '我的笔记', // 默认路径
      icon: item.icon || null
    }));
  } catch (e) {
    console.error(e);
    results.value = [];
  } finally {
    loading.value = false;
  }
}

// 防抖 (可选，防止输入过快频繁请求)
let timeout: any;
watch(() => keyword.value, (newVal) => {
  if (timeout) clearTimeout(timeout);
  timeout = setTimeout(() => {
    doSearch();
  }, 300); // 300ms 延迟
});

// 弹窗打开时自动聚焦
watch(() => store.visible, (val) => {
  if (val) {
    keyword.value = '';
    results.value = [];
    nextTick(() => {
      searchInputRef.value?.focus();
    });
  }
});

const openNote = (noteId: number) => {
  store.close();
  router.push(`/user/main/edit?id=${noteId}`); // 假设这是你的路由结构
}

// 处理高亮文字 (简单实现，如果后端没返回 HTML)
const getHighlightSnippet = (text: string, key: string) => {
  if (!text) return '';
  if (!key) return text;
  const regex = new RegExp(`(${key})`, 'gi');
  return text.replace(regex, '<span class="highlight">$1</span>');
}
</script>

<template>
  <!-- 
    class="notion-search-dialog": 用于覆盖 el-dialog 默认样式
    :show-close="false": 去掉默认关闭按钮
    :header="false": 去掉默认头部
  -->
  <el-dialog 
    :model-value="store.visible" 
    width="600px" 
    align-center 
    append-to-body 
    destroy-on-close 
    :show-close="false"
    class="notion-search-dialog"
    @close="store.close()"
  >
    <div class="search-container">
      
      <!-- 搜索头部：输入框 -->
      <div class="search-header">
        <el-icon class="search-icon"><Search /></el-icon>
        <input 
          ref="searchInputRef"
          v-model="keyword" 
          class="search-input"
          placeholder="搜索笔记、文档..." 
        />
        <div v-if="loading" class="loading-spinner">
           <el-icon class="is-loading"><Loading /></el-icon>
        </div>
        <div class="esc-hint" @click="store.close()">ESC</div>
      </div>

      <!-- 搜索结果区 -->
      <div class="search-body">
        
        <!-- 空状态 / 初始状态 -->
        <div v-if="!keyword && results.length === 0" class="empty-state">
          <div class="empty-text">输入关键词以开始搜索...</div>
        </div>

        <!-- 无结果状态 -->
        <div v-else-if="!loading && results.length === 0" class="empty-state">
          <div class="empty-text">未找到相关结果</div>
        </div>

        <!-- 结果列表 -->
        <el-scrollbar v-else max-height="400px" wrap-class="result-scrollbar">
          <div class="result-group-title" v-if="results.length > 0">最佳匹配</div>
          
          <div 
            v-for="(r, index) in results" 
            :key="r.noteId" 
            class="result-item" 
            @click="openNote(r.noteId)"
            tabindex="0"
          >
            <!-- 左侧图标 -->
            <div class="item-icon-box">
              <span v-if="r.icon" class="emoji-icon">{{ r.icon }}</span>
              <el-icon v-else><Document /></el-icon>
            </div>

            <!-- 中间内容 -->
            <div class="item-content">
              <div class="item-top">
                <span class="item-title" v-html="getHighlightSnippet(r.title, keyword)"></span>
              </div>
              <!-- 路径 / 面包屑 -->
              <div class="item-meta" v-if="r.path">
                {{ r.path }}
              </div>
              <!-- 匹配片段 -->
              <div class="item-snippet" v-if="r.snippet || r.summary">
                 <span v-html="getHighlightSnippet(r.snippet || r.summary || '', keyword)"></span>
              </div>
            </div>

            <!-- 右侧回车提示 (Hover时可见) -->
            <div class="item-action">
              <el-icon><Right /></el-icon>
            </div>
          </div>
        </el-scrollbar>
        
        <!-- 底部栏 (模仿 Notion footer) -->
        <div class="search-footer">
          <span class="footer-item">
            <kbd>↑</kbd> <kbd>↓</kbd> 选择
          </span>
          <span class="footer-item">
            <kbd>↵</kbd> 打开
          </span>
        </div>

      </div>
    </div>
  </el-dialog>
</template>

<style>
/* 全局覆盖 Dialog 样式，因为 append-to-body 导致 scoped 无法穿透到 el-dialog 容器 */
.notion-search-dialog.el-dialog {
  border-radius: 12px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.15) !important;
  background: #FFFFFF;
  padding: 0;
  overflow: hidden;
}
.notion-search-dialog .el-dialog__header {
  display: none; /* 彻底隐藏 element header */
}
.notion-search-dialog .el-dialog__body {
  padding: 0 !important;
  color: #37352F;
}
</style>

<style scoped>
/* --- 容器布局 --- */
.search-container {
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
}

/* --- 头部样式 --- */
.search-header {
  display: flex;
  align-items: center;
  padding: 16px 16px 16px 20px;
  border-bottom: 1px solid #E9E9E8;
}

.search-icon {
  font-size: 20px;
  color: #9B9A97;
  margin-right: 12px;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 18px;
  color: #37352F;
  background: transparent;
  height: 32px;
}
.search-input::placeholder {
  color: #9B9A97;
}

.esc-hint {
  font-size: 12px;
  color: #9B9A97;
  border: 1px solid #E9E9E8;
  border-radius: 4px;
  padding: 2px 6px;
  cursor: pointer;
  margin-left: 8px;
}
.esc-hint:hover {
  background: #F7F7F5;
  color: #37352F;
}

/* --- 结果列表样式 --- */
.search-body {
  background: #FBFBFA; /* 列表背景稍微灰一点点 */
  min-height: 100px;
}

.result-group-title {
  padding: 12px 16px 4px 16px;
  font-size: 12px;
  font-weight: 600;
  color: #9B9A97;
}

.result-item {
  display: flex;
  align-items: flex-start; /* 对齐顶部 */
  padding: 10px 16px;
  margin: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.1s;
  color: #37352F;
  border: 1px solid transparent; /* 预留边框位置防止跳动 */
}

/* 选中/悬停态 */
.result-item:hover, .result-item:focus {
  background: #FFFFFF; /* Notion 悬停时变白 */
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  border-color: #E9E9E8;
}

.item-icon-box {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  margin-top: 2px;
  color: #9B9A97;
  font-size: 18px;
}
.emoji-icon {
  font-size: 18px;
  line-height: 1;
}

.item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.item-top {
  display: flex;
  align-items: center;
}
.item-title {
  font-weight: 500;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.item-meta {
  font-size: 12px;
  color: #9B9A97;
  margin-top: 2px;
}

.item-snippet {
  font-size: 12px;
  color: #787774;
  margin-top: 4px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 限制显示2行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  opacity: 0.8;
}

/* 深度选择器处理 v-html 里的高亮 */
:deep(.highlight) {
  background: rgba(35, 131, 226, 0.2);
  border-bottom: 1px solid rgba(35, 131, 226, 0.4);
  color: #121212;
  border-radius: 2px;
  padding: 0 2px;
}

.item-action {
  opacity: 0;
  color: #9B9A97;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-left: 8px;
  margin-top: 4px;
}
.result-item:hover .item-action {
  opacity: 1;
}

/* --- 空状态 --- */
.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  color: #9B9A97;
  font-size: 14px;
}

/* --- 底部 Footer --- */
.search-footer {
  border-top: 1px solid #E9E9E8;
  padding: 8px 16px;
  display: flex;
  gap: 16px;
  background: #FFFFFF;
}
.footer-item {
  font-size: 12px;
  color: #9B9A97;
  display: flex;
  align-items: center;
  gap: 4px;
}
kbd {
  background: #F7F7F5;
  border: 1px solid #E9E9E8;
  border-radius: 3px;
  padding: 0 4px;
  font-family: monospace;
  font-size: 11px;
  min-width: 16px;
  text-align: center;
  box-shadow: 0 1px 0 rgba(0,0,0,0.05);
}
</style>