<!--
@file UserTodoListPage
@project SlothNote
@module 用户端 / 待办页面
@description 承载待办分类树与待办主列表。
@logic 1. 左侧渲染分类树；2. 右侧渲染待办列表；3. 作为用户主布局 KeepAlive 缓存页面。
@dependencies Component: TodoListTree/TodoMain
@index_tags 待办页面, 分类树, KeepAlive, 路由切换性能
@author holic512
-->
<script setup lang="ts">

import TodoListTree from "@/views/User/Main/components/TodoList/TodoListTree/TodoListTree.vue";
import TodoMain from "@/views/User/Main/components/TodoList/TodoMain/TodoMain.vue";

defineOptions({
  name: 'UserTodoListPage'
})
</script>

<template>
  <div class="container">
    <div style="width: 18%;height: 100%" class="div1">
      <TodoListTree/>
    </div>
    <div style="width: 82%;height: 100%">
      <TodoMain/>
    </div>
  </div>
</template>

<style scoped>
.container {
  display: flex; /* 开启 flex 布局 */
  flex-direction: row; /* 主轴方向为列 */
  width: 100%;
  height: 100%;

  padding: 4px;

  /* 渐变背景 */
  background: linear-gradient(to top right, #ffffff, #f6f8fb);

}

.div1 {
  border-right: 1px solid #ddd; /* 边框 */
}


</style>
