<!--
@file UserSidebar
@project SlothNote
@module 用户端 / 主侧边栏
@description 提供用户端主导航、笔记树入口和设置弹窗入口。
@logic 1. 控制左侧面板折叠；2. 导航到首页、搜索、待办、收藏；3. 挂载笔记树和相关右侧操作弹窗。
@dependencies Store: useUserPreferencesStore/useSearchDialogStore, Component: NoteTree/Rename/Details/Description
@index_tags 用户侧边栏, 笔记树入口, 用户导航, ElementPlus图标
@author holic512
-->
<script setup lang="ts">
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

        </div>
      </div>

      <!--      收缩侧边栏 箭头  -->
      <div>
        <Button class="sidebar-button" text icon="AngleDoubleLeft" size="small"
                @click="LeftPanelState.toggleLeftPanel()"/>
      </div>

      <div>
        <Button class="sidebar-button" text icon="PenToSquare" size="small"/>
      </div>

    </div>


    <el-divider style="margin: 4px"/>


    <!--    工具栏   -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/home')">
      <div class="sidebar-icon">
        <el-icon class="sidebar-svg-icon"><HomeFilled /></el-icon>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>主页</el-text>
      </div>
    </div>

    <div class="sidebar-div" style="margin-bottom: 1px;" @click="openSearch">
      <div class="sidebar-icon">
        <el-icon class="sidebar-svg-icon"><Search /></el-icon>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>搜索</el-text>
      </div>
    </div>


    <div class="sidebar-div" style="margin-bottom: 1px;" @click="router.push('/user/main/todolist')">
      <div class="sidebar-icon">
        <el-icon class="sidebar-svg-icon"><Tickets /></el-icon>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>待做</el-text>
      </div>
    </div>

    <div class="sidebar-div" style="margin-bottom: 6px;">
      <div class="sidebar-icon">
        <el-icon class="sidebar-svg-icon"><MessageBox /></el-icon>
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
        <el-icon class="sidebar-svg-icon"><StarFilled /></el-icon>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>收藏</el-text>
      </div>
    </div>

    <!--  账号  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="UserSettingVisible = true ">
      <div class="sidebar-icon">
        <el-icon class="sidebar-svg-icon"><User /></el-icon>
      </div>
      <div style="display: flex;  align-items: center;">
        <el-text>账号</el-text>
      </div>
    </div>

    <!--  设置  -->
    <div class="sidebar-div" style="margin-bottom: 1px;" @click="UserSettingVisible = true ">
      <div class="sidebar-icon">
        <el-icon class="sidebar-svg-icon"><Setting /></el-icon>
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

.sidebar-svg-icon {
  color: #708090;
  font-size: 16px;
}

</style>
