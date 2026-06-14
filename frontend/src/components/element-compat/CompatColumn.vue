<!--
@file CompatColumn
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus el-table-column 承接旧 Column field/header/body 语义。
@logic 1. field/header 映射为 prop/label；2. selectionMode 映射为 selection 列；3. 将 body 插槽转换为 Element Plus default 插槽。
@dependencies ElementPlus: el-table-column
@index_tags Column迁移, el-table-column, 表格列, ElementPlus兼容
@author holic512
-->
<script setup lang="ts">
import {computed} from 'vue'

const props = defineProps<{
  field?: string
  header?: string
  selectionMode?: string
  headerStyle?: string
  position?: string
  frozen?: boolean
}>()

const width = computed(() => {
  const match = props.headerStyle?.match(/width:\s*([^;]+)/)
  return match?.[1]?.trim()
})
</script>

<template>
  <el-table-column
      v-if="selectionMode === 'multiple'"
      type="selection"
      :width="width || 50"
      :fixed="position === 'fixed' || frozen"
  />
  <el-table-column
      v-else
      :prop="field"
      :label="header"
      :width="width"
      :fixed="position === 'fixed' || frozen"
  >
    <template v-if="$slots.body" #default="scope">
      <slot name="body" :data="scope.row" :field="field" />
    </template>
    <template v-else-if="$slots.default" #default="scope">
      <slot :data="scope.row" :field="field" />
    </template>
  </el-table-column>
</template>
