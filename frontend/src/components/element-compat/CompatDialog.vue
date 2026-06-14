<!--
@file CompatDialog
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus el-dialog 承接旧 Dialog v-model:visible 语义。
@logic 1. visible 映射到 el-dialog v-model；2. header 映射 title；3. 支持 style.width 转换为 width。
@dependencies ElementPlus: el-dialog
@index_tags Dialog迁移, el-dialog, 弹窗, ElementPlus兼容
@author holic512
-->
<script setup lang="ts">
import {computed} from 'vue'

const visible = defineModel<boolean>('visible', {default: false})
const props = defineProps<{
  header?: string
  modal?: boolean
  draggable?: boolean
  style?: Record<string, string>
}>()

const dialogWidth = computed(() => props.style?.width || '520px')
</script>

<template>
  <el-dialog v-model="visible" :title="header" :width="dialogWidth" :draggable="draggable">
    <slot />
  </el-dialog>
</template>
