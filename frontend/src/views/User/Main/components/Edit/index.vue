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
import {getNoteContent, type NoteContentPayload} from "@/views/User/Main/components/Edit/service/GetNoteContent";
import PageRight from "@/views/User/Main/components/Edit/PageRight/PageRight.vue";
import {getNoteShareInfo, type NoteShareInfo} from "@/views/User/Main/components/Edit/PageHeader/service/getNoteShareInfo";
import {ElMessage} from "element-plus";
import {useAiChatStore} from "@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat";
import {useSaveNoteState} from "@/views/User/Main/components/Edit/Pinia/SaveNoteState";
import {provideNoteEditorContext} from "@/views/User/Main/components/Edit/editor/editorContext";
import {useNoteTreeUpdate} from "@/views/User/Main/components/Sidebar/Pinia/isNoteTreeUpdated";


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

const normalizeNoteId = (value: unknown): number | null => {
  if (typeof value !== "string" || value.trim() === "") {
    return null;
  }

  const parsed = Number(value);
  return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
}

const getRouteNoteId = () => normalizeNoteId(route.query.noteId ?? route.query.id);

const isLatestLoad = (requestId: number, noteId: number) => {
  return requestId === lastLoadRequestId && getRouteNoteId() === noteId;
}

const clearCurrentNote = () => {
  currentNoteInfo.clearNoteInfo();
  editor.value?.commands.clearContent(false);
  saveNoteState.saveContent();
}

/**
 * 路由、AI 写入和版本恢复都必须经过同一个服务端新鲜加载流程，避免持久化 Pinia 或 HTTP 缓存覆盖最新笔记。
 */
const loadCurrentRouteNote = async () => {
  const routeNoteId = getRouteNoteId();
  const requestId = ++lastLoadRequestId;

  if (routeNoteId == null) {
    clearCurrentNote();
    return;
  }

  if (route.query.noteId == null) {
    const nextQuery = {...route.query};
    delete nextQuery.id;
    void router.replace({
      path: route.path,
      query: {...nextQuery, noteId: String(routeNoteId)},
    });
  }

  if (currentNoteInfo.noteId !== routeNoteId) {
    clearCurrentNote();
  }

  let shareInfo: NoteShareInfo | null;
  let context: NoteContentPayload | null;
  try {
    [shareInfo, context] = await Promise.all([
      getNoteShareInfo(routeNoteId),
      getNoteContent(routeNoteId),
    ]);
  } catch (error) {
    if (isLatestLoad(requestId, routeNoteId)) {
      console.error(error);
      ElMessage.error("笔记加载失败，请稍后重试");
    }
    return;
  }

  if (!isLatestLoad(requestId, routeNoteId)) return;

  if (shareInfo == null) {
    clearCurrentNote();
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
  useNoteTreeUpdate().patchNode("NOTE", shareInfo.noteId, {
    label: shareInfo.noteName,
    avatar: shareInfo.avatar,
    cover: shareInfo.cover,
  });

  if (!editor.value) {
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
  } else {
    editor.value?.commands.clearContent(false);
  }

  saveNoteState.saveContent();
}


// 钩子函数
onMounted(async () => {
  await loadCurrentRouteNote();
})

watch(
    () => [route.query.noteId, route.query.id],
    async () => {
      await loadCurrentRouteNote();
    }
)

watch(
    () => aiChat.lastNoteMutation,
    async () => {
  const routeNoteId = getRouteNoteId();
  if (
      !aiChat.lastNoteMutation
      || aiChat.lastNoteMutation.noteId !== currentNoteInfo.noteId
      || aiChat.lastNoteMutation.noteId !== routeNoteId
  ) {
    return;
  }
  await loadCurrentRouteNote();
  ElMessage.success(aiChat.lastNoteMutation.summary || "AI 已同步更新当前笔记");
}
)

const refreshAfterVersionRestore = async (noteId: number) => {
  if (noteId === currentNoteInfo.noteId && noteId === getRouteNoteId()) {
    await loadCurrentRouteNote();
  }
}

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
  lastLoadRequestId += 1;
  window.removeEventListener('resize', onWindowResize);
});


</script>
<template>
  <div class="common-layout">
    <el-container style="height: 100vh">

      <!--  标题头  -->
      <el-header class="common-header">
        <PageHeader v-model="editor" @version-restored="refreshAfterVersionRestore"/>
      </el-header>

      <!--  编辑器  -->
      <el-main :style="{height: mainHeight + 'px' }" style="padding: 0;display: flex">

        <TipTap v-model="editor"/>


        <PageRight/>


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
