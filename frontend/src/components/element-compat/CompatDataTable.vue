<!--
@file CompatDataTable
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus el-table 承接旧 DataTable 数据、选择和插槽语义。
@logic 1. value 映射为 data；2. selection-change 回写 v-model:selection；3. 保留旧表格最小宽度和滚动高度，透传子 Column 组件插槽。
@dependencies ElementPlus: el-table
@index_tags DataTable迁移, el-table, 多选表格, ElementPlus兼容
@author holic512
-->
<script setup lang="ts">
import {computed} from 'vue'
import type {CSSProperties} from 'vue'

const props = defineProps<{
  value?: any[]
  dataKey?: string
  size?: string
  stripedRows?: boolean
  scrollable?: boolean
  scrollHeight?: string
  tableStyle?: string
  selection?: any[]
}>()

const tableStyleValue = computed<CSSProperties>(() => {
  const minWidth = props.tableStyle?.match(/min-width:\s*([^;]+)/i)?.[1]?.trim()
  return minWidth ? {minWidth} : {}
})

const emit = defineEmits<{
  'update:selection': [value: any[]]
}>()
</script>

<template>
  <el-table
      :data="value || []"
      :row-key="dataKey"
      :size="size === 'small' ? 'small' : 'default'"
      :stripe="stripedRows"
      :height="scrollHeight"
      :style="tableStyleValue"
      @selection-change="emit('update:selection', $event)"
  >
    <slot />
  </el-table>
</template>
