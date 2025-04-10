<script setup>

import TipTap from "@/views/User/Main/components/Edit/Main/TipTap.vue";
import PageHeader from "@/views/User/Main/components/Edit/PageHeader/PageHeader.vue";
import {onBeforeUnmount, onMounted, ref, watch} from "vue";

import {createEditorInstance} from "@/views/User/Main/components/Edit/editor/editor.ts";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo.ts";
import {getNoteContent} from "@/views/User/Main/components/Edit/service/GetNoteContent.ts";
import PageRight from "@/views/User/Main/components/Edit/PageRight/PageRight.vue";


// 创建 editor 实例
const editor = createEditorInstance();

// 当前笔记实例
const currentNoteInfo = useCurrentNoteInfoStore();


// 钩子函数
onMounted(async () => {
  // 获取当前笔记的 context
  const context = await getNoteContent(currentNoteInfo.noteId);

  //当前笔记不为空
  if (context !== null) {
    const Json = context.content
    const parsedContent = JSON.parse(Json);    // 输入到 编辑器 中
    editor.value.commands.setContent(parsedContent);
  }
  // 当前笔记为空 清空当前 编辑器内容
  else {
    editor.value.commands.clearContent();
  }
})

// 监听 当前笔记数据是否发生改变 -> 获取新笔记的  context
watch(
    () => [currentNoteInfo.noteId],
    async ([newNoteId]) => {

      // 获取当前笔记的 context
      const context = await getNoteContent(newNoteId);

      // 当 当前笔记不为空
      if (context !== null) {
        const Json = context.content
        const parsedContent = JSON.parse(Json);    // 输入到 编辑器 中
        editor.value.commands.setContent(parsedContent);
      }
      // 当前笔记为空 清空当前 编辑器内容
      else {
        editor.value.commands.clearContent();
      }

    })

// ui  适配
const mainHeight = ref(window.innerHeight - 48);

// 窗口大小变化时重新计算面板宽度
const onWindowResize = () => {
  mainHeight.value = window.innerHeight - 48; // 减去分割线宽度
};

// 绑定和移除事件监听
onMounted(() => {
  window.addEventListener('resize', onWindowResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', onWindowResize);
});


</script>
<template>
  <div class="common-layout">
    <el-container style="height: 100vh">

      <!--  标题头  -->
      <el-header class="common-header">
        <PageHeader v-model="editor"/>
      </el-header>

      <!--  编辑器  -->
      <el-main :style="{height: mainHeight + 'px' }" style="padding: 0;display: flex">

        <TipTap v-model="editor"/>


        <PageRight v-model="editor"/>


      </el-main>

      <!--      <el-footer style="height: 32px;">-->
      <!--        尾部-->
      <!--      </el-footer>-->

    </el-container>
  </div>
</template>


<style scoped>
.common-header {
  border-bottom: 1px solid #F0F3F5;
  height: 38px;
}
</style>
