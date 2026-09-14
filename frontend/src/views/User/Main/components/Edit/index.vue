<!--
@file UserNoteEditPage
@project SlothNote
@module 用户端 / 笔记编辑页
@description 承载笔记编辑器页面，负责创建 Tiptap Editor、同步路由笔记信息并加载正文内容。
@logic 1. 创建并 provide editor 实例；2. 规范化 noteId 路由并仅提交最新元数据/正文请求；3. 对 AI 修改与窗口尺寸变化进行同步。
@dependencies Component: TipTap/PageHeader/PageRight, Store: currentNoteInfo/SaveNoteState, Service: GetNoteContent/getNoteShareInfo
@index_tags 用户笔记, 编辑器入口, Tiptap, 路由同步, 笔记内容加载
@author holic512
-->
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
import {provideNoteEditorContext} from "@/views/User/Main/components/Edit/editor/editorContext";


// 创建 editor 实例
const editor = createEditorInstance();
provideNoteEditorContext(editor);

// 当前笔记实例
const currentNoteInfo = useCurrentNoteInfoStore();
const aiChat = useAiChatStore();
const saveNoteState = useSaveNoteState();
const route = useRoute();
const router = useRouter();

let lastLoadRequestId = 0;
let lastRouteSyncRequestId = 0;

const normalizeNoteId = (value: unknown): number | null => {
  if (typeof value !== "string" || value.trim() === "") {
    return null;
  }

  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

const syncNoteInfoFromRoute = async () => {
  const routeNoteId = normalizeNoteId(route.query.noteId ?? route.query.id);

  if (routeNoteId == null) {
    return;
  }

  if (route.query.noteId == null) {
    const nextQuery = {...route.query};
    delete nextQuery.id;
    await router.replace({
      path: route.path,
      query: {...nextQuery, noteId: String(routeNoteId)},
    });
  }

  if (currentNoteInfo.noteId === routeNoteId) return;

  const requestId = ++lastRouteSyncRequestId;

  const shareInfo = await getNoteShareInfo(routeNoteId);

  if (requestId !== lastRouteSyncRequestId) return;

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
    try {
      const parsedContent = JSON.parse(context.content);
      editor.value.commands.setContent(parsedContent, false);
    } catch (error) {
      console.error(error);
      ElMessage.error("笔记内容解析失败，已保留空白编辑器");
      editor.value.commands.clearContent(false);
    }
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
    () => [route.query.noteId, route.query.id],
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
  lastRouteSyncRequestId += 1;
  lastLoadRequestId += 1;
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
