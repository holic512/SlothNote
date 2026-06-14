<!--
@file CompatDataTable
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus el-table 承接旧 DataTable 数据、选择和插槽语义。
@logic 1. value 映射为 data；2. selection-change 回写 v-model:selection；3. 透传子 Column 组件插槽。
@dependencies ElementPlus: el-table
@index_tags DataTable迁移, el-table, 多选表格, ElementPlus兼容
@author holic512
-->
<script setup lang="ts">
defineProps<{
  value?: any[]
  dataKey?: string
  size?: string
  stripedRows?: boolean
  scrollable?: boolean
  scrollHeight?: string
  selection?: any[]
}>()

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
      @selection-change="emit('update:selection', $event)"
  >
    <slot />
  </el-table>
</template>
