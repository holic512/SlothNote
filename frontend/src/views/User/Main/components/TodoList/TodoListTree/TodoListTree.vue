<!--
@file TodoListTree
@project SlothNote
@module 用户端 / 待办侧栏
@description 渲染待办分类树入口，供待办主列表按分类切换。
@logic 1. 挂载 ClassTree 分类树；2. 保持侧栏结构轻量，避免未启用日历视图组件进入切换路径。
@dependencies Component: ClassTree
@index_tags 待办侧栏, 分类树, 路由切换性能, 轻量化
@author holic512
-->
<script setup lang="ts">
import ClassTree from "@/views/User/Main/components/TodoList/TodoListTree/ClassTree/ClassTree.vue";
</script>

<template>
  <div class="todo-list-tree">
    <ClassTree/>
  </div>
</template>

<style scoped>
.todo-list-tree {
  padding: 10px;
}
</style>
