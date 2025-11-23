<!--  用于 编辑内容 的页头  -->
<script setup lang="ts">

import SaveNote from "@/views/User/Main/components/Edit/PageHeader/components/SaveNote/SaveNote.vue";
import SaveNoteState from "@/views/User/Main/components/Edit/PageHeader/components/SaveNoteState/SaveNoteState.vue";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import Comment from "@/views/User/Main/components/Edit/PageHeader/components/Comment/comment.vue";
import Ai from "@/views/User/Main/components/Edit/PageHeader/components/Ai/Ai.vue";
import MyStar from "@/views/User/Main/components/MyStar/MyStar.vue";
import {useFavoriteDialogStore} from "@/views/User/Main/components/Edit/Pinia/FavoriteDialogStore";
import {ElMessage} from "element-plus";

const editor = defineModel()

// 获取当前笔记 的 基础信息
const currentNoteInfo = useCurrentNoteInfoStore()
const favStore = useFavoriteDialogStore();
const openFavorite = () => {
  if (currentNoteInfo.noteId != null) {
    favStore.open(currentNoteInfo.noteId);
  } else {
    ElMessage.warning("请先打开一篇笔记");
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

        <el-button text class="button">
          <el-icon color="#000000" size="18">
            <Share/>
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

        <el-button text class="button"> <!--历史版本-->
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