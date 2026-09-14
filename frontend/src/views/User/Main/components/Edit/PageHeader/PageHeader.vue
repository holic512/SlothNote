<!--
@file PageHeader
@project SlothNote
@module 用户端 / 笔记页头
@description 展示当前笔记路径与页头操作入口，包括保存、分享、评论、历史版本、收藏与 AI。
@logic 1. 读取当前笔记基础信息渲染面包屑；2. 根据笔记状态打开收藏与历史版本弹窗；3. 生成并复制分享链接。
@dependencies Store: currentNoteInfo/RightPageState/FavoriteDialogStore, Component: SaveNote/VersionDialog/MyStar
@index_tags 笔记页头, 分享链接, 历史版本, 收藏, Tiptap
@author holic512
-->
<script setup lang="ts">

import SaveNote from "@/views/User/Main/components/Edit/PageHeader/components/SaveNote/SaveNote.vue";
import SaveNoteState from "@/views/User/Main/components/Edit/PageHeader/components/SaveNoteState/SaveNoteState.vue";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {RightPageModeEnum, useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import Comment from "@/views/User/Main/components/Edit/PageHeader/components/Comment/comment.vue";
import Ai from "@/views/User/Main/components/Edit/PageHeader/components/Ai/Ai.vue";
import MyStar from "@/views/User/Main/components/MyStar/MyStar.vue";
import {useFavoriteDialogStore} from "@/views/User/Main/components/Edit/Pinia/FavoriteDialogStore";
import {ElMessage} from "element-plus";
import {ref} from "vue";
import VersionDialog from "@/views/User/Main/components/Edit/PageHeader/components/VersionDialog/VersionDialog.vue";
import {useRouter} from "vue-router";
import type {Editor} from "@tiptap/vue-3";
import {Connection} from "@element-plus/icons-vue";

const editor = defineModel<Editor>()
const emit = defineEmits<{
  'version-restored': [noteId: number]
}>()

// 获取当前笔记 的 基础信息
const currentNoteInfo = useCurrentNoteInfoStore()
const rightPageState = useRightPageState()
const favStore = useFavoriteDialogStore();
const versionDialogVisible = ref(false)
const router = useRouter();
const openFavorite = () => {
  if (currentNoteInfo.noteId != null) {
    favStore.open(currentNoteInfo.noteId);
  } else {
    ElMessage.warning("请先打开一篇笔记");
  }
}

const openVersionDialog = () => {
  if (currentNoteInfo.noteId != null) {
    versionDialogVisible.value = true
  } else {
    ElMessage.warning("请先打开一篇笔记");
  }
}

const copyShareLink = async () => {
  if (currentNoteInfo.noteId == null) {
    ElMessage.warning("请先打开一篇笔记");
    return;
  }

  const href = router.resolve({
    path: "/user/main/edit",
    query: {
      noteId: String(currentNoteInfo.noteId)
    }
  }).href;
  const shareLink = new URL(href, window.location.origin).toString();

  try {
    await navigator.clipboard.writeText(shareLink);
    ElMessage.success("分享链接已复制");
  } catch (error) {
    console.error(error);
    ElMessage.error("复制失败，请检查浏览器剪贴板权限");
  }
}

</script>

<template>
  <el-row justify="space-between" class="row-pageHeader">

    <!--    文件路径头   -->
    <el-col :span="8" class="el-col">
      <el-breadcrumb separator="/">

        <!--文件夹文本 -循环-->
        <el-breadcrumb-item v-for="(noteLocation) in currentNoteInfo.noteLocation">
          <el-text tag="b">{{ noteLocation }}</el-text>
        </el-breadcrumb-item>

        <el-breadcrumb-item>
          <el-text tag="b" style="display: flex;gap: 4px;align-items: center;">
            <!--  判断是否使用自定义图标 -->
            <el-icon size="18" v-if="currentNoteInfo.avatar == null">
              <Notebook/>
            </el-icon>
            <div v-else>
              {{ currentNoteInfo.avatar }}
            </div>
            <!--  文本 -->
            {{
              (currentNoteInfo.noteName == null || currentNoteInfo.noteName == '') ? "新建文档" : currentNoteInfo.noteName
            }}
          </el-text>
        </el-breadcrumb-item>


      </el-breadcrumb>

      <!--  文件保存 状态-->
      <SaveNoteState/>
    </el-col>

    <el-col :span="16" class="el-col el-col-right">

      <SaveNote v-model="editor"/>

      <el-tooltip
          class="box-item"
          effect="dark"
          content=" 分享 "
          :show-after="500"
          placement="bottom"
      >

        <el-button text class="button" @click="copyShareLink">
          <el-icon color="#000000" size="18">
            <Share/>
          </el-icon>
        </el-button>

      </el-tooltip>

      <el-tooltip
          class="box-item"
          effect="dark"
          content=" 知识星图 "
          :show-after="500"
          placement="bottom"
      >

        <el-button
            text
            class="button"
            @click="rightPageState.SwitchKnowledgeGraph"
            :style="{ backgroundColor: rightPageState.model === RightPageModeEnum.KnowledgeGraph ? '#F3F3F3' : 'transparent' }"
        >
          <el-icon color="#000000" size="18">
            <Connection/>
          </el-icon>
        </el-button>

      </el-tooltip>

      <!--  评论  -->
      <Comment/>

      <el-tooltip
          class="box-item"
          effect="dark"
          content=" 历史版本 "
          :show-after="500"
          placement="bottom"
      >

        <el-button text class="button" @click="openVersionDialog"> <!--历史版本-->
          <el-icon color="#000000" size="18">
            <Clock/>
          </el-icon>
        </el-button>

      </el-tooltip>

      <el-tooltip
          class="box-item"
          effect="dark"
          content=" 收藏 "
          :show-after="500"
          placement="bottom"
      >

        <el-button text class="button" @click="openFavorite">  <!--收藏-->
          <el-icon color="#000000" size="18">
            <Star/>
          </el-icon>
        </el-button>


      </el-tooltip>

      <Ai/>

      <MyStar/>

      <el-tooltip
          class="box-item"
          effect="dark"
          content=" 更多 "
          :show-after="500"
          placement="bottom"
      >

        <el-button text class="button"> <!--更多-->
          <el-icon color="#000000" size="18">
            <MoreFilled/>
          </el-icon>
        </el-button>

      </el-tooltip>

    </el-col>
  </el-row>

  <VersionDialog
      v-model:visible="versionDialogVisible"
      v-model:editor="editor"
      @restored="emit('version-restored', $event)"
  />
</template>

<style scoped>
.row-pageHeader {
  height: 100%;

  user-select: none;
}

.el-col {
  display: flex;
  align-items: center;
}

.el-col-right {
  justify-content: flex-end;
}

.button {
  width: 30px;
  height: 30px;
}

</style>
