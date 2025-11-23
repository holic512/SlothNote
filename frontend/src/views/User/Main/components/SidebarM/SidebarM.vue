<script setup lang="ts">

import Button from "primevue/button";
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
      <i class="pi pi-angle-double-right" style="font-size: 1rem;color: #708090"/>
    </div>

    <!--  主页  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/home')">
      <i class="pi pi-home" style="font-size: 1rem;color: #708090"/>
    </div>

    <!--  搜索  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="openSearch">
      <i class="pi pi-search" style="font-size: 1rem;color: #708090"/>
    </div>

    <!--  待做  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/todolist')">
      <i class="pi pi-calendar" style="font-size: 1rem;color: #708090"/>
    </div>

    <!--  收件箱  -->
    <div class="sidebar-div" style="margin-bottom: 1px;">
      <i class="pi pi-inbox" style="font-size: 1rem;color: #708090"/>
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

</style>