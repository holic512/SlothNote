<script setup lang="ts">

import TodoListTree from "@/views/User/Main/components/TodoList/TodoListTree/TodoListTree.vue";
import TodoMain from "@/views/User/Main/components/TodoList/TodoMain/TodoMain.vue";
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