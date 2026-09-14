<!--
@file PageRight
@project SlothNote
@module 用户端 / 笔记右侧栏
@description 根据右侧栏状态展示评论、AI 或知识星图面板。
@logic 1. 监听 RightPageState.model 调整栏宽；2. 按模式挂载对应功能组件；3. 关闭状态宽度归零。
@dependencies Store: RightPageState, Component: NoteComment/NoteAI/KnowledgeGraph
@index_tags 笔记右侧栏, AI, 评论, 知识星图
@author holic512
-->
<script setup lang="ts">
import {RightPageModeEnum, useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import {defineAsyncComponent, ref, watch} from "vue";

const NoteComment = defineAsyncComponent(() => import(
  "@/views/User/Main/components/Edit/PageRight/components/NoteComment/NoteComment.vue"
));
const NoteAI = defineAsyncComponent(() => import(
  "@/views/User/Main/components/Edit/PageRight/components/NoteAi/NoteAi.vue"
));
const KnowledgeGraph = defineAsyncComponent(() => import(
  "@/views/User/Main/components/Edit/PageRight/components/KnowledgeGraph/KnowledgeGraph.vue"
));
// 控制右侧边栏状态
const rightPageState = useRightPageState();

const editor = defineModel()

// 用于存储 侧边栏长度
let RightPageWidth = ref(0);

// 监听 rightPageState 的 model是否发生改变
watch(() => rightPageState.model, (newValue) => {
  switch (newValue) {
    case RightPageModeEnum.null:
      RightPageWidth.value = 0;
      break;
    case RightPageModeEnum.comment:
      RightPageWidth.value = 380;
      break;
    case RightPageModeEnum.Ai:
      RightPageWidth.value = 430;
      break;
    case RightPageModeEnum.KnowledgeGraph:
      RightPageWidth.value = 520;
      break;
  }

}, {immediate: true})
</script>

<template>
  <!-- 右侧边栏内容 -->
  <div class="right-sidebar" :style="{width: RightPageWidth+'px'}">

      <NoteComment v-if="rightPageState.model == RightPageModeEnum.comment"/>

      <NoteAI v-if="rightPageState.model == RightPageModeEnum.Ai" v-model="editor"/>

      <KnowledgeGraph v-if="rightPageState.model == RightPageModeEnum.KnowledgeGraph"/>
  </div>

</template>

<style scoped>
.right-sidebar {
  border-left: 1px #F0F3F5 solid;
  height: 100%;
  background-color: white;
  transition: width 0.15s ease-in-out; /* 设置平滑过渡动画 */
  overflow: hidden; /* 确保内容在隐藏时不显示 */
}

</style>
