<!--
@file CompatTag
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus el-tag 承接旧 Tag value/severity 语义。
@logic 1. 映射 severity 到 Element Plus type；2. 支持 value 属性和默认插槽；3. 保持小型状态标签外观。
@dependencies ElementPlus: el-tag
@index_tags Tag迁移, el-tag, 状态标签, ElementPlus兼容
@author holic512
-->
<script setup lang="ts">
import {computed} from 'vue'

const props = defineProps<{
  value?: string | number
  severity?: string
}>()

const tagType = computed(() => {
  if (props.severity === 'danger') return 'danger'
  if (props.severity === 'success') return 'success'
  if (props.severity === 'warning' || props.severity === 'warn') return 'warning'
  if (props.severity === 'info') return 'info'
  return 'info'
})
</script>

<template>
  <el-tag :type="tagType" effect="light">
    <slot>{{ value }}</slot>
  </el-tag>
</template>
