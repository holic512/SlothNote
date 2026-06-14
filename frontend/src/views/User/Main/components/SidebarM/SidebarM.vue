<!--
@file UserCompactSidebar
@project SlothNote
@module 用户端 / 折叠侧边栏
@description 提供用户端折叠状态下的主导航图标入口。
@logic 1. 控制左侧面板展开；2. 跳转首页、待办并打开搜索；3. 使用 Element Plus 图标替代 PrimeIcons。
@dependencies Store: useUserPreferencesStore/useSearchDialogStore, VueRouter: useRouter, ElementPlus: el-icon
@index_tags 折叠侧边栏, 用户导航, ElementPlus图标
@author holic512
-->
<script setup lang="ts">

import {useUserPreferencesStore} from "@/views/User/Main/Pinia/userPreferencesStore";
import {useRouter} from "vue-router";
import {useSearchDialogStore} from './Pinia/SearchDialogStore';
// 左侧面板信息控制
const LeftPanelState = useUserPreferencesStore();

// 获取路由实例
const router = useRouter();
const searchStore = useSearchDialogStore();
const openSearch = () => { searchStore.open(); };
</script>

<template>
  <div class="sidebarM">


    <!--  用户头像  -->
    <div style="display: flex; justify-content: center; align-items: center; width: 38px">
      <el-avatar
          :size="24"
          src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
      />
    </div>

    <!--  缩放按钮  -->
    <div class="sidebar-div" @click="LeftPanelState.toggleLeftPanel()">
      <el-icon class="sidebar-svg-icon"><DArrowRight /></el-icon>
    </div>

    <!--  主页  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/home')">
      <el-icon class="sidebar-svg-icon"><HomeFilled /></el-icon>
    </div>

    <!--  搜索  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="openSearch">
      <el-icon class="sidebar-svg-icon"><Search /></el-icon>
    </div>

    <!--  待做  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/todolist')">
      <el-icon class="sidebar-svg-icon"><Tickets /></el-icon>
    </div>

    <!--  收件箱  -->
    <div class="sidebar-div" style="margin-bottom: 1px;">
      <el-icon class="sidebar-svg-icon"><MessageBox /></el-icon>
    </div>

  </div>

</template>

<style scoped>
.sidebarM {
  width: 48px;
  height: 100%;

  display: flex;
  flex-direction: column; /* 让子元素竖直排列 */
  align-items: center;

  padding-top: 11px;
  gap: 4px;

}


.sidebar-div {
  width: 32px;
  height: 32px;
  display: flex;
  justify-content: center;
  align-items: center;

  border-radius: 4px; /* 添加圆角 */
  padding: 4px 2px 4px 2px; /* 增加内边距以增强按钮的点击区域 */

  transition: background-color 0.1s ease; /* 添加过渡效果 */
  user-select: none;
}

.sidebar-div:hover {
  background-color: #EFEFED;
}

.sidebar-svg-icon {
  color: #708090;
  font-size: 16px;
}

</style>
