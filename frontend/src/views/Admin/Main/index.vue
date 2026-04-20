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

const router = useRouter()
const route = useRoute()
const isCollapsed = ref(false)

type MenuItem = {
  index: string
  title: string
  icon: unknown
  alias?: string[]
}

const menuItems: MenuItem[] = [
  { index: '/admin/main/home', title: '仪表盘', icon: Grid, alias: ['/admin/main', '/admin/main/dashboardMm'] },
  { index: '/admin/main/userMm', title: '用户管理', icon: User },
  { index: '/admin/main/noteMm', title: '笔记管理', icon: Document },
  { index: '/admin/main/commentMm', title: '评论管理', icon: ChatDotRound },
  { index: '/admin/main/folderMm', title: '文件夹管理', icon: Folder },
  { index: '/admin/main/favoriteMm', title: '收藏管理', icon: Star },
  { index: '/admin/main/todoMm', title: '待办管理', icon: Checked },
  { index: '/admin/main/aiMm', title: 'AI记录', icon: Cpu },
  { index: '/admin/main/setting', title: '系统设置', icon: Setting },
]

const activeMenu = computed(() => {
  const matched = menuItems.find((item) => item.index === route.path || item.alias?.includes(route.path))
  return matched?.index ?? '/admin/main/home'
})

const currentTitle = computed(() => (route.meta.title as string) ?? '管理后台')
const currentSubtitle = computed(() => (route.meta.subtitle as string) ?? '')

const handleSelect = (index: string) => {
  if (index !== route.path) {
    router.push(index)
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
    await router.push('/admin/auth/login')
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
      <el-aside class="admin-aside" :class="{ 'is-collapsed': isCollapsed }" :width="isCollapsed ? '64px' : '240px'">
        <div class="brand">
          <div class="brand-mark">
            <el-icon><Monitor /></el-icon>
          </div>
          <div v-show="!isCollapsed" class="brand-text">
            <strong>SlothNote</strong>
            <span>Admin Console</span>
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
            <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>
                <span>{{ item.title }}</span>
              </template>
            </el-menu-item>
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
              <h1 class="page-title">{{ currentTitle }}</h1>
              <el-tag v-if="currentSubtitle" type="info" effect="plain" size="small" round class="page-subtitle">
                {{ currentSubtitle }}
              </el-tag>
            </div>
          </div>

          <div class="header-right">
            <div class="admin-badge">
              <span class="dot"></span>
              Super Admin
            </div>
            <el-button type="danger" plain round size="default" class="logout-btn" @click="handleLogout">
              <el-icon class="el-icon--left"><SwitchButton /></el-icon>退出登录
            </el-button>
          </div>
        </el-header>

        <!-- 内容区域 -->
        <el-main class="admin-main">
          <el-scrollbar>
            <div class="admin-content-wrapper">
              <div class="admin-content">
                <router-view v-slot="{ Component, route: currentRoute }">
                  <transition name="fade-transform" mode="out-in">
                    <component :is="Component" :key="currentRoute.fullPath" />
                  </transition>
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
  background-color: #f4f6f8; /* 更柔和的现代灰底色 */
}

.admin-layout {
  height: 100%;
}

/* ================= 侧边栏 ================= */
.admin-aside {
  background: #ffffff;
  border-right: 1px solid #edf2f7;
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.2, 0, 0, 1);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.02);
  z-index: 10;
}

.brand {
  height: 64px; /* 与 header 保持一致高度 */
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  border-bottom: 1px solid #edf2f7;
  overflow: hidden;
  white-space: nowrap;
}

.admin-aside.is-collapsed .brand {
  justify-content: center;
  padding: 0;
}

.brand-mark {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff, #337ecc);
  color: #ffffff;
  font-size: 18px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.brand-text strong {
  color: #1f2937;
  font-size: 16px;
  line-height: 1.2;
}

.brand-text span {
  color: #9ca3af;
  font-size: 12px;
  line-height: 1;
}

/* 菜单样式优化 */
.menu-scroll {
  flex: 1;
}

.admin-menu {
  border-right: none;
  padding: 12px 8px;
}

.admin-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin-bottom: 6px;
  border-radius: 8px;
  color: #4b5563;
  transition: all 0.2s;
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: #f3f4f6;
  color: #111827;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background-color: #ebf5ff;
  color: #409eff;
  font-weight: 600;
}

/* ================= 主体与 Header ================= */
.admin-main-layout {
  display: flex;
  flex-direction: column;
  background-color: #f4f6f8;
}

.admin-header {
  height: 64px;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #edf2f7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.02);
  z-index: 5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

/* 将原来错位的换行改为水平排布 */
.collapse-trigger {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 6px;
  color: #4b5563;
  transition: background 0.2s;
}

.collapse-trigger:hover {
  background: #f3f4f6;
  color: #111827;
}

.page-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  line-height: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.admin-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #4b5563;
  font-weight: 500;
}

.admin-badge .dot {
  width: 8px;
  height: 8px;
  background-color: #10b981;
  border-radius: 50%;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
}

.logout-btn {
  border: none;
  background: #fef2f2;
}
.logout-btn:hover {
  background: #fee2e2;
}

/* ================= 内容区 ================= */
.admin-main {
  padding: 0; /* padding 移到内部包装器，配合滚动条 */
  overflow: hidden; /* 让 el-scrollbar 接管滚动 */
  display: flex;
  flex-direction: column;
}

.admin-content-wrapper {
  padding: 24px;
  min-height: 100%;
  box-sizing: border-box;
}

.admin-content {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  padding: 24px;
  min-height: calc(100vh - 64px - 48px); /* 100vh - header高度 - wrapper的上下padding */
}

/* 路由切换动画 */
.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-15px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(15px);
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
    gap: 12px;
  }

  .page-subtitle, .admin-badge {
    display: none;
  }

  .admin-content-wrapper {
    padding: 16px;
  }

  .admin-content {
    min-height: calc(100vh - 64px - 32px);
    padding: 16px;
  }
}
</style>