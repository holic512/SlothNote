<!--
@file AppRoot
@project SlothNote
@module 前端应用 / 根组件
@description 提供全局路由出口与 Element Plus 空值配置。
@logic 1. 通过 el-config-provider 统一管理组件库空值行为；2. 渲染全局 RouterView。
@dependencies VueRouter: RouterView, ElementPlus: el-config-provider
@index_tags 应用入口, 路由出口, ElementPlus配置, 空值配置
@author holic512
-->
<script setup lang="ts">
import {RouterView} from 'vue-router'

</script>

<template>
  <el-config-provider :empty-values="[null, undefined]">
    <RouterView/>
  </el-config-provider>
</template>

<style scoped>

</style>
