<script setup lang="ts">
import {RightPageModeEnum, useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import {onMounted, ref, watch} from "vue";
import NoteComment from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/NoteComment.vue";
import NoteAI from "@/views/User/Main/components/Edit/PageRight/components/NoteAi/NoteAi.vue"
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
      RightPageWidth.value = 380; // Same width as comments or adjust as needed
      break;
  }

})
</script>

<template>
  <!-- 右侧边栏内容 -->
  <div class="right-sidebar" :style="{width: RightPageWidth+'px'}">

      <NoteComment v-if="rightPageState.model == RightPageModeEnum.comment"/>

      <NoteAI v-if="rightPageState.model == RightPageModeEnum.Ai" v-model="editor"/>
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
