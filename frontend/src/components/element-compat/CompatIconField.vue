<!--
@file CompatIconField
@project SlothNote
@module 前端组件 / 旧组件迁移兼容层
@description 为旧 IconField 提供轻量布局容器。
@logic 1. 提供 inline-flex 搜索框布局；2. 插槽承载 InputIcon 和 InputText；3. 不引入旧组件库依赖。
@dependencies CSS Flexbox
@index_tags IconField迁移, 搜索框布局, ElementPlus兼容
@author holic512
-->
<template>
  <span class="compat-icon-field">
    <slot />
  </span>
</template>

<style scoped>
.compat-icon-field {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
</style>
