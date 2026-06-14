<!--
@file SaveNoteButton
@project SlothNote
@module 用户端 / 笔记页头
@description 提供页头保存按钮，触发当前 Tiptap 编辑器内容保存。
@logic 1. 读取保存状态决定按钮显示；2. 从编辑器上下文获取 Editor；3. 点击后调用 SaveNote 服务。
@dependencies Store: SaveNoteState, Service: SaveNote, Context: editorContext
@index_tags 笔记保存按钮, 页头, Tiptap, 保存状态
@author holic512
-->
<script setup lang="ts">

import IconSaveNote from "./IconSaveNote.vue";
import {useSaveNoteState} from "@/views/User/Main/components/Edit/Pinia/SaveNoteState";
import {SaveNote} from "@/views/User/Main/components/Edit/service/SaveNote";
import type {Editor} from "@tiptap/vue-3";
import {useNoteEditorContext} from "@/views/User/Main/components/Edit/editor/editorContext";

// 创建 存储 笔记保存状态 的Pinia
const editorState = useSaveNoteState()

// 获取父组件的 editor，保留 v-model 兼容，优先使用页面级 editor 上下文。
const editorModel = defineModel<Editor>()
const {editor} = useNoteEditorContext(editorModel)

</script>

<template>
  <el-tooltip
      v-if="!editorState.isSaved"
      class="box-item"
      effect="dark"
      content=" 保存 Ctrl + S"
      :show-after="500"
      placement="bottom"
  >

    <!--    <el-button text class="button" @click="editorState.saveContent()">-->
    <el-button text class="button" @click="SaveNote(editor)">
      <el-icon color="#E6A23C" size="18">
        <IconSaveNote/>
      </el-icon>
    </el-button>

  </el-tooltip>
</template>

<style scoped>
.button {
  width: 30px;
  height: 30px;
}
</style>
