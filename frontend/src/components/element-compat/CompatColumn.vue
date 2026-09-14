<!--
@file CompatColumn
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus el-table-column 承接旧 Column field/header/body 语义。
@logic 1. field/header 映射为 prop/label；2. selectionMode 映射为 selection 列；3. 将旧百分比列宽换算为最小像素宽度，避免 Element Plus 将其误解为极窄固定宽度。
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

const declaredWidth = computed(() => {
  const match = props.headerStyle?.match(/width:\s*([^;]+)/)
  return match?.[1]?.trim()
})

const width = computed(() => {
  const value = declaredWidth.value
  return value && !value.endsWith('%') ? value : undefined
})

const minWidth = computed(() => {
  const value = declaredWidth.value
  if (!value?.endsWith('%')) return undefined

  const percentage = Number.parseFloat(value)
  if (!Number.isFinite(percentage)) return undefined
  return Math.max(100, Math.round(percentage * 9))
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
      :min-width="minWidth"
      :fixed="position === 'fixed' || frozen"
  >
    <template v-if="$slots.body" #default="scope">
      <slot name="body" :data="scope?.row ?? {}" :field="field" />
    </template>
    <template v-else-if="$slots.default" #default="scope">
      <slot :data="scope?.row ?? {}" :field="field" />
    </template>
  </el-table-column>
</template>
