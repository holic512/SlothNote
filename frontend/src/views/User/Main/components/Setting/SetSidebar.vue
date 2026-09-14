<!--
@file UserSettingSidebar
@project SlothNote
@module 用户端 / 设置侧边栏
@description 提供账号、AI 权限与系统设置面板切换入口。
@logic 1. 通过 defineModel 同步当前激活面板；2. 渲染设置分类导航；3. 使用 Element Plus 图标替代 PrimeIcons。
@dependencies ElementPlus: el-icon, Vue: defineModel
@index_tags 设置侧边栏, 账号设置, AI权限, 系统设置, ElementPlus图标
@author holic512
-->
<script setup lang="ts">
const activePanel = defineModel<'account' | 'ai' | 'system'>({default: 'account'})
</script>

<template>
  <el-text size="small" tag="b">账号</el-text>

  <el-divider style="margin: 4px 0 4px 0"/>

  <div class="sidebar-div" :class="{ active: activePanel === 'account' }" @click="activePanel = 'account'">
    <el-icon class="setting-icon"><User /></el-icon>
    <el-text style="color: #000;">我的账号</el-text>
  </div>

  <el-text size="small" tag="b" style="margin-top: 12px;">系统</el-text>

  <el-divider style="margin: 4px 0 4px 0"/>

  <div class="sidebar-div" :class="{ active: activePanel === 'ai' }" @click="activePanel = 'ai'">
    <el-icon class="setting-icon"><MagicStick /></el-icon>
    <el-text style="color: #000;">AI 权限</el-text>
  </div>

  <div class="sidebar-div" :class="{ active: activePanel === 'system' }" @click="activePanel = 'system'">
    <el-icon class="setting-icon"><Setting /></el-icon>
    <el-text style="color: #000;">系统设置</el-text>
  </div>

</template>

<style scoped>
.sidebar-div {
  width: 100%;
  height: 25px;
  display: flex;
  align-items: center;
  border-radius: 4px; /* 添加圆角 */
  padding: 4px 2px 4px 2px; /* 增加内边距以增强按钮的点击区域 */
  transition: background-color 0.1s ease; /* 添加过渡效果 */

  user-select: none;
}

.sidebar-div:hover {
  background-color: #EFEFED;
}

.sidebar-div.active {
  background-color: #E6F1FC;
}

.setting-icon {
  margin: 1px 4px 0 2px;
  font-size: 14px;
}
</style>
