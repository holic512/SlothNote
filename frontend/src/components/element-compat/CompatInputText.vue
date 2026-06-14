<!--
@file CompatInputText
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus el-input 承接旧 InputText 语义。
@logic 1. 透传 v-model；2. 支持 placeholder/disabled/size；3. 保留输入事件透传。
@dependencies ElementPlus: el-input
@index_tags InputText迁移, el-input, 表单输入, ElementPlus兼容
@author holic512
-->
<script setup lang="ts">
const model = defineModel<string | number | null>()

defineProps<{
  placeholder?: string
  disabled?: boolean
  size?: string
}>()
</script>

<template>
  <el-input v-model="model" :placeholder="placeholder" :disabled="disabled" :size="size === 'small' ? 'small' : 'default'">
    <slot />
  </el-input>
</template>
