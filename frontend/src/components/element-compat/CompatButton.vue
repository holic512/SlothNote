<!--
@file CompatButton
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 用 Element Plus el-button 承接旧 Button 语义，降低批量迁移风险。
@logic 1. 映射 severity 到 Element Plus type；2. 使用显式原生按钮包装避免全局 Button 名称递归；3. 将旧 icon 名称解析为 Element Plus 图标。
@dependencies ElementPlus: el-button, Utility: resolveCompatIcon
@index_tags Button迁移, el-button, ElementPlus兼容, 图标映射
@author holic512
-->
<script setup lang="ts">
import {computed, defineComponent, h} from 'vue'
import type {Component} from 'vue'
import {ElButton} from 'element-plus'
import {resolveCompatIcon} from './iconMap'

const props = defineProps<{
  label?: string
  icon?: string | Component
  severity?: string
  size?: string
  outlined?: boolean
  text?: boolean
  rounded?: boolean
  loading?: boolean
  disabled?: boolean
  type?: string
}>()

const buttonType = computed(() => {
  if (props.severity === 'danger') return 'danger'
  if (props.severity === 'success') return 'success'
  if (props.severity === 'warning' || props.severity === 'warn') return 'warning'
  if (props.severity === 'info') return 'info'
  if (props.severity === 'primary') return 'primary'
  return ''
})

const nativeType = computed(() => {
  if (props.type === 'submit' || props.type === 'reset' || props.type === 'button') return props.type
  return 'button'
})

const buttonSize = computed(() => props.size === 'small' ? 'small' : props.size === 'large' ? 'large' : 'default')
const buttonIcon = computed(() => resolveCompatIcon(props.icon))

const nativeButtonTag = defineComponent({
  name: 'CompatNativeButtonTag',
  inheritAttrs: false,
  setup(_, {attrs, slots}) {
    return () => h('button', attrs, slots.default?.())
  },
})
</script>

<template>
  <ElButton
      :type="buttonType"
      :native-type="nativeType"
      :tag="nativeButtonTag"
      :size="buttonSize"
      :plain="outlined"
      :text="text"
      :circle="rounded && !label && !$slots.default"
      :round="rounded && Boolean(label || $slots.default)"
      :icon="buttonIcon"
      :loading="loading"
      :disabled="disabled"
  >
    <slot>{{ label }}</slot>
  </ElButton>
</template>
