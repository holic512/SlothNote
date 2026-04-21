<script setup lang="ts">

import TipTap from "@/views/User/Main/components/Edit/Main/TipTap.vue";
import PageHeader from "@/views/User/Main/components/Edit/PageHeader/PageHeader.vue";
import {onBeforeUnmount, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";

import {createEditorInstance} from "@/views/User/Main/components/Edit/editor/editor";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {getNoteContent} from "@/views/User/Main/components/Edit/service/GetNoteContent";
import PageRight from "@/views/User/Main/components/Edit/PageRight/PageRight.vue";
import {getNoteShareInfo} from "@/views/User/Main/components/Edit/PageHeader/service/getNoteShareInfo";
import {ElMessage} from "element-plus";
import {useAiChatStore} from "@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat";
import {useSaveNoteState} from "@/views/User/Main/components/Edit/Pinia/SaveNoteState";


// 创建 editor 实例
const editor = createEditorInstance();

// 当前笔记实例
const currentNoteInfo = useCurrentNoteInfoStore();
const aiChat = useAiChatStore();
const saveNoteState = useSaveNoteState();
const route = useRoute();
const router = useRouter();

let lastLoadRequestId = 0;

const normalizeNoteId = (value: unknown): number | null => {
  if (typeof value !== "string" || value.trim() === "") {
    return null;
  }

  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

const syncNoteInfoFromRoute = async () => {
  const routeNoteId = normalizeNoteId(route.query.noteId);

  if (routeNoteId == null || currentNoteInfo.noteId === routeNoteId) {
    return;
  }

  const shareInfo = await getNoteShareInfo(routeNoteId);

  if (shareInfo == null) {
    ElMessage.warning("分享链接对应的笔记不存在或无权访问");
    await router.replace({path: route.path});
    return;
  }

  currentNoteInfo.setNoteInfo(
      shareInfo.noteId,
      shareInfo.noteName,
      shareInfo.noteLocation,
      shareInfo.avatar,
      shareInfo.cover
  );
}

const applyEditorContent = async (noteId: number | null | undefined) => {
  const requestId = ++lastLoadRequestId;

  if (noteId == null || !editor.value) {
    editor.value?.commands.clearContent(false);
    return;
  }

  const context = await getNoteContent(noteId);

  if (requestId !== lastLoadRequestId || !editor.value) {
    return;
  }

  if (context?.content) {
    const parsedContent = JSON.parse(context.content);
    editor.value.commands.setContent(parsedContent, false);
    return;
  }

  editor.value.commands.clearContent(false);
}


// 钩子函数
onMounted(async () => {
  await syncNoteInfoFromRoute();
  await applyEditorContent(currentNoteInfo.noteId);
})

// 监听 当前笔记数据是否发生改变 -> 获取新笔记的  context
watch(
    () => [currentNoteInfo.noteId],
    async ([newNoteId]) => {
      await applyEditorContent(newNoteId);
    })

watch(
    () => route.query.noteId,
    async () => {
      await syncNoteInfoFromRoute();
    }
)

watch(
    () => aiChat.lastNoteMutation?.timestamp,
    async () => {
      if (!aiChat.lastNoteMutation || aiChat.lastNoteMutation.noteId !== currentNoteInfo.noteId) {
        return;
      }
      await applyEditorContent(currentNoteInfo.noteId);
      saveNoteState.saveContent();
      ElMessage.success(aiChat.lastNoteMutation.summary || "AI 已同步更新当前笔记");
    }
)

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
