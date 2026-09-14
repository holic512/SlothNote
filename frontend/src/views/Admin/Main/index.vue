<!--
@file AdminMainLayout
@project SlothNote
@module 管理后台 / 主框架
@description 提供管理后台侧边栏、顶部栏和子路由页面承载容器。
@logic 1. 根据当前路由同步菜单选中态与页面分类；2. 菜单点击触发路由跳转；3. 缓存已访问管理页，保持切换期间内容稳定。
@dependencies VueRouter: useRoute/useRouter, Pinia: tokenStore, ElementPlus: Menu/Container/Icon
@index_tags 后台布局, 菜单切换, 路由视图, 异步页面加载
@author holic512
-->
<script setup lang="ts">
import {
  Expand,
  ChatDotRound,
  Checked,
  Fold,
  Cpu,
  Document,
  Folder,
  Grid,
  Monitor,
  Setting,
  Star,
  User,
  SwitchButton
} from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { tokenStore } from '@/pinia/token'
import { ROUTE_PATHS } from '@/router/paths'

const router = useRouter()
const route = useRoute()
const isCollapsed = ref(false)
type MenuItem = {
  index: string
  title: string
  icon: unknown
  alias?: string[]
}

type MenuGroup = {
  title: string
  items: MenuItem[]
}

const menuGroups: MenuGroup[] = [
  {
    title: '系统概览',
    items: [
      { index: '/admin/main/home', title: '仪表盘', icon: Grid, alias: ['/admin/main', '/admin/main/dashboardMm'] },
    ],
  },
  {
    title: '内容与用户',
    items: [
      { index: '/admin/main/userMm', title: '用户管理', icon: User },
      { index: '/admin/main/noteMm', title: '笔记管理', icon: Document },
      { index: '/admin/main/commentMm', title: '评论管理', icon: ChatDotRound },
      { index: '/admin/main/folderMm', title: '文件夹管理', icon: Folder },
      { index: '/admin/main/favoriteMm', title: '收藏管理', icon: Star },
      { index: '/admin/main/todoMm', title: '待办管理', icon: Checked },
    ],
  },
  {
    title: '平台运行',
    items: [
      { index: '/admin/main/aiMm', title: 'AI 记录', icon: Cpu },
      { index: '/admin/main/setting', title: '系统设置', icon: Setting },
    ],
  },
]

const menuItems = menuGroups.flatMap((group) => group.items)
const cachedAdminViewNames = ['DashboardMm', 'UserMm', 'NoteMm', 'CommentMm', 'FolderMm', 'FavoriteMm', 'TodoMm', 'AiMm', 'Setting']

const activeMenu = computed(() => {
  const matched = menuItems.find((item) => item.index === route.path || item.alias?.includes(route.path))
  return matched?.index ?? '/admin/main/home'
})

const currentTitle = computed(() => (route.meta.title as string) ?? '管理后台')
const currentCategory = computed(() => (route.meta.category as string) ?? '管理空间')
const currentSubtitle = computed(() => (route.meta.subtitle as string) ?? '')

const handleSelect = async (index: string) => {
  if (index === route.path) {
    return
  }

  try {
    await router.push(index)
  } catch (error) {
    ElMessage.error('页面切换失败，请稍后重试')
  }
}

const toggleAside = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确认退出当前管理员登录状态？', '退出登录', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    })

    tokenStore().clearAdminToken()
    await router.push(ROUTE_PATHS.adminLogin)
    ElMessage.success('已退出登录')
  } catch (error) {
    // Cancelled
  }
}

</script>

<template>
  <div class="admin-shell">
    <el-container class="admin-layout">
      <!-- 侧边栏 -->
      <el-aside class="admin-aside" :class="{ 'is-collapsed': isCollapsed }" :width="isCollapsed ? '56px' : '216px'">
        <div class="brand">
          <div class="brand-mark">
            <el-icon><Monitor /></el-icon>
          </div>
          <div v-show="!isCollapsed" class="brand-text">
            <strong>SlothNote</strong>
            <span>管理空间</span>
          </div>
        </div>

        <el-scrollbar class="menu-scroll">
          <el-menu
              :default-active="activeMenu"
              :collapse="isCollapsed"
              :collapse-transition="false"
              class="admin-menu"
              @select="handleSelect"
          >
            <template v-for="group in menuGroups" :key="group.title">
              <p v-show="!isCollapsed" class="menu-group-title">{{ group.title }}</p>
              <el-menu-item v-for="item in group.items" :key="item.index" :index="item.index">
                <el-icon><component :is="item.icon" /></el-icon>
                <template #title>
                  <span>{{ item.title }}</span>
                </template>
              </el-menu-item>
            </template>
          </el-menu>
        </el-scrollbar>
      </el-aside>

      <!-- 右侧主体 -->
      <el-container class="admin-main-layout">
        <!-- 顶部 Header -->
        <el-header class="admin-header">
          <div class="header-left">
            <div class="collapse-trigger" @click="toggleAside">
              <el-icon :size="20">
                <component :is="isCollapsed ? Expand : Fold" />
              </el-icon>
            </div>

            <div class="page-header-info">
              <span class="page-category">{{ currentCategory }}</span>
              <h1 class="page-title">{{ currentTitle }}</h1>
              <span v-if="currentSubtitle" class="page-subtitle">{{ currentSubtitle }}</span>
            </div>
          </div>

          <div class="header-right">
            <div class="admin-badge">
              <span class="dot"></span>
              管理员会话
            </div>
            <el-button type="danger" plain round size="small" class="logout-btn" @click="handleLogout">
              <el-icon class="el-icon--left"><SwitchButton /></el-icon>退出登录
            </el-button>
          </div>
        </el-header>

        <!-- 内容区域 -->
        <el-main class="admin-main">
          <el-scrollbar>
            <div class="admin-content-wrapper">
              <div class="admin-content">
                <router-view v-slot="{ Component }">
                  <KeepAlive :include="cachedAdminViewNames" :max="9">
                    <component :is="Component" v-if="Component" :key="route.name" />
                  </KeepAlive>
                </router-view>
              </div>
            </div>
          </el-scrollbar>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
/* 基础重置与外壳 */
.admin-shell {
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: var(--sn-bg-page);
}

.admin-layout {
  height: 100%;
}

/* ================= 侧边栏 ================= */
.admin-aside {
  background: var(--sn-bg-surface);
  border-right: 1px solid var(--sn-border);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
  box-shadow: none;
  z-index: 10;
}

.brand {
  height: 52px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  border-bottom: 1px solid var(--sn-border);
  overflow: hidden;
  white-space: nowrap;
}

.admin-aside.is-collapsed .brand {
  justify-content: center;
  padding: 0;
}

.brand-mark {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: var(--sn-radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--sn-color-black);
  color: #ffffff;
  font-size: 16px;
  box-shadow: none;
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-text strong {
  color: var(--sn-text-primary);
  font-size: 15px;
  line-height: 1.2;
}

.brand-text span {
  color: var(--sn-text-muted);
  font-size: 11px;
  line-height: 1;
}

/* 菜单样式优化 */
.menu-scroll {
  flex: 1;
}

.admin-menu {
  border-right: none;
  padding: 12px 8px 16px;
  background: transparent;
}

.menu-group-title {
  margin: 14px 8px 6px;
  color: var(--sn-text-muted);
  font-size: 11px;
  font-weight: 500;
  line-height: 1.2;
}

.menu-group-title:first-child {
  margin-top: 2px;
}

.admin-menu :deep(.el-menu-item) {
  height: 38px;
  line-height: 38px;
  margin-bottom: 2px;
  border-radius: var(--sn-radius-base);
  color: var(--sn-text-regular);
  transition: all 0.2s;
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: var(--sn-bg-muted);
  color: var(--sn-text-primary);
}

.admin-menu :deep(.el-menu-item.is-active) {
  background-color: var(--sn-color-black);
  color: var(--sn-color-white);
  font-weight: 500;
}

/* ================= 主体与 Header ================= */
.admin-main-layout {
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  background-color: var(--sn-bg-page);
}

.admin-header {
  height: 52px;
  padding: 0 16px;
  background: var(--sn-bg-surface);
  border-bottom: 1px solid var(--sn-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: none;
  z-index: 5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 将原来错位的换行改为水平排布 */
.collapse-trigger {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 6px;
  color: var(--sn-text-regular);
  transition: background 0.2s;
}

.collapse-trigger:hover {
  background: var(--sn-bg-muted);
  color: var(--sn-text-primary);
}

.page-header-info {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  min-height: 28px;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
}

.page-category {
  color: var(--sn-text-muted);
  font-size: 11px;
  font-weight: 500;
  line-height: 1;
  flex: 0 0 auto;
}

.page-title {
  margin: 0;
  padding-left: 10px;
  margin-left: 10px;
  border-left: 1px solid var(--sn-border-strong);
  font-size: 16px;
  font-weight: 600;
  color: var(--sn-text-primary);
  line-height: 1;
  flex: 0 0 auto;
}

.page-subtitle {
  min-width: 0;
  max-width: min(42vw, 540px);
  margin-left: 10px;
  padding-left: 10px;
  overflow: hidden;
  border-left: 1px solid var(--sn-border);
  color: var(--sn-text-muted);
  font-size: 12px;
  line-height: 1;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-badge {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  color: var(--sn-text-regular);
  font-weight: 500;
}

.admin-badge .dot {
  width: 7px;
  height: 7px;
  background-color: #22a06b;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(34, 160, 107, 0.14);
}

.logout-btn {
  --el-button-text-color: var(--sn-text-regular);
  --el-button-bg-color: var(--sn-bg-muted);
  --el-button-border-color: var(--sn-border);
}
.logout-btn:hover {
  --el-button-bg-color: var(--sn-gray-200);
  --el-button-border-color: var(--sn-border-strong);
}

/* ================= 内容区 ================= */
.admin-main {
  min-height: 0;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.admin-content-wrapper {
  padding: 16px;
  min-height: 100%;
  box-sizing: border-box;
}

.admin-content {
  min-height: calc(100vh - 52px - 32px);
}

.admin-main :deep(> .el-scrollbar) {
  min-height: 0;
  flex: 1;
}

/* ================= 移动端适配 ================= */
@media (max-width: 768px) {
  .admin-aside {
    position: absolute;
    height: 100vh;
    left: 0;
    transform: translateX(0);
  }

  .admin-aside.is-collapsed {
    transform: translateX(-100%);
  }

  .header-left {
    gap: 8px;
  }

  .page-subtitle, .admin-badge {
    display: none;
  }

  .admin-content-wrapper {
    padding: 12px;
  }

  .admin-content {
    min-height: calc(100vh - 52px - 24px);
  }
}

@media (max-width: 480px) {
  .page-category {
    display: none;
  }

  .page-title {
    margin-left: 0;
    padding-left: 0;
    border-left: 0;
  }
}
</style>
