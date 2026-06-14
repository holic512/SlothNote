<!--
@file CompatInputIcon
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus 图标承接旧 InputIcon 位置。
@logic 1. 默认显示搜索图标；2. 支持通过 icon 指定图标；3. 保持搜索框前缀视觉。
@dependencies ElementPlus: el-icon, Utility: resolveCompatIcon
@index_tags InputIcon迁移, el-icon, 搜索图标
@author holic512
-->
<script setup lang="ts">
import {computed} from 'vue'
import {Search} from '@element-plus/icons-vue'
import {resolveCompatIcon} from './iconMap'

const props = defineProps<{ icon?: string }>()
const resolvedIcon = computed(() => resolveCompatIcon(props.icon) || Search)
</script>

<template>
  <el-icon class="compat-input-icon">
    <component :is="resolvedIcon" />
  </el-icon>
</template>

<style scoped>
.compat-input-icon {
  color: var(--sn-text-muted);
}
</style>
