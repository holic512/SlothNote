<script setup lang="ts">
import Button from 'primevue/button';
import NoteTree from "./NoteTree/noteTree.vue";
import {useRouter} from "vue-router";
import Rename from "@/views/User/Main/components/Sidebar/components/Rename/Rename.vue";
import {useUserPreferencesStore} from "@/views/User/Main/Pinia/userPreferencesStore";
import Details from "@/views/User/Main/components/Sidebar/components/Details/Details.vue";
import Description from "@/views/User/Main/components/Sidebar/components/Description/Description.vue";
import {useSearchDialogStore} from "@/views/User/Main/components/SidebarM/Pinia/SearchDialogStore";

// 控制用户设置 显示
const UserSettingVisible = defineModel()

// 获取路由实例
const router = useRouter();

// 左侧面板信息控制
const LeftPanelState = useUserPreferencesStore();
const searchStore = useSearchDialogStore();
const openSearch = () => { searchStore.open(); };

</script>

<template>
  <div class="sidebar">
    <!--    头部内容  -->
    <div class="sidebar-div" style="margin-bottom: 6px">
      <div style="display: flex; justify-content: center; align-items: center; width: 38px">
        <el-avatar
            :size="24"
            src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"
        />
      </div>
      <div style="flex: 1; /* 占据剩余空间 */">
        <div style=" height: 100%; display: flex;  align-items: center; ">
          <el-text truncated>
            Holic
          </el-text>

          <!--          <i class="pi pi-angle-down" style="font-size: 1rem;color: #708090"/>-->
        </div>
      </div>

      <!--      收缩侧边栏 箭头  -->
      <div>
        <Button class="sidebar-button" text icon="pi pi-angle-double-left" size="small"
                @click="LeftPanelState.toggleLeftPanel()"/>
      </div>

      <div>
        <Button class="sidebar-button" text icon="pi pi-pen-to-square" size="small"/>
      </div>

    </div>


    <el-divider style="margin: 4px"/>


    <!--    工具栏   -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/home')">
      <div class="sidebar-icon">
        <i class="pi pi-home" style="font-size: 1rem;color: #708090"/>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>主页</el-text>
      </div>
    </div>

    <div class="sidebar-div" style="margin-bottom: 1px;" @click="openSearch">
      <div class="sidebar-icon">
        <i class="pi pi-search" style="font-size: 1rem;color: #708090"/>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>搜索</el-text>
      </div>
    </div>


    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/todolist')">
      <div class="sidebar-icon">
        <i class="pi pi-calendar" style="font-size: 1rem;color: #708090"/>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>待做</el-text>
      </div>
    </div>

    <div class="sidebar-div" style="margin-bottom: 6px;">
      <div class="sidebar-icon">
        <i class="pi pi-inbox" style="font-size: 1rem;color: #708090"/>
      </div>

      <div style="display: flex;  align-items: center;">
        <el-text>收件箱</el-text>
      </div>
    </div>
    <el-divider style="margin: 4px"/>

    <!--  笔记文件树  -->
    <note-tree/>

    <el-divider style="margin: 16px 4px 4px;"/>

    <!--    工具类    -->

    <!--  收藏  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/myStar')">
      <div class="sidebar-icon">
        <i class="pi pi-star-fill" style="font-size: 1rem;color: #708090"/>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>收藏</el-text>
      </div>
    </div>

    <!--  账号  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="UserSettingVisible = true ">
      <div class="sidebar-icon">
        <i class="pi pi-user" style="font-size: 1rem;color: #708090"/>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>账号</el-text>
      </div>
    </div>

    <!--  设置  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="UserSettingVisible = true ">
      <div class="sidebar-icon">
        <i class="pi pi-cog" style="font-size: 1rem;color: #708090"/>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>设置</el-text>
      </div>
    </div>
  </div>

  <!--重命名 动态框-->
  <Rename/>

  <!--详细信息 动态框-->
  <Details/>

  <!--编辑简介 动态框-->
  <Description/>

</template>

<style scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  user-select: none;
  height: 100%;
}

.sidebar-div {
  width: 100%;
  height: 30px;
  display: flex;
  align-items: center;
  border-radius: 4px; /* 添加圆角 */
  padding: 4px 2px 4px 2px; /* 增加内边距以增强按钮的点击区域 */
  transition: background-color 0.1s ease; /* 添加过渡效果 */
}

/* 控制按钮图标*/
.sidebar-icon {
  display: flex;
  justify-content: right;
  align-items: center;
  width: 26px;
  margin-right: 10px;
  margin-top: 1px;
}

.sidebar-div:hover {
  background-color: #EFEFED;
}

.sidebar-button {
  width: 24px;
  height: 24px;
}

.sidebar-button:hover {
  background-color: #f8f9fa;
}


</style>