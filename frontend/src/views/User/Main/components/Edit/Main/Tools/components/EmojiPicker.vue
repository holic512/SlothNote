<!--
@file EmojiPicker
@project SlothNote
@module 用户端 / 编辑器工具栏
@description 提供表情选择弹层，并将选中的 emoji 插入到当前 Tiptap 编辑器。
@logic 1. 异步加载 vue3-emoji-picker；2. 接收 editor model；3. 选择 emoji 后插入内容并关闭下拉层。
@dependencies Tiptap: Editor, Component: vue3-emoji-picker, ElementPlus: DropdownInstance
@index_tags 表情选择器, Tiptap, 工具栏, emoji插入
@author holic512
-->
<script setup lang="ts">
import {ref} from "vue";
import type {ModelRef} from "vue";
import type {Editor} from "@tiptap/vue-3";
import type {DropdownInstance} from "element-plus";
import {defineAsyncComponent} from "vue";
import type {EmojiExt} from "vue3-emoji-picker";
import "../css/editTool-button.css";

import "vue3-emoji-picker/css";

const EmojiPickerPanel = defineAsyncComponent(() => import("vue3-emoji-picker"));

const editor: ModelRef<Editor | undefined> = defineModel();
const dropdown = ref<DropdownInstance>();

const groupNames = {
  smileys_people: "笑脸与人物",
  animals_nature: "动物与自然",
  food_drink: "食物与饮品",
  activities: "活动",
  travel_places: "旅行与地点",
  objects: "物品",
  symbols: "符号",
  flags: "旗帜",
};

const onSelectEmoji = (emoji: EmojiExt) => {
  editor.value?.chain().focus().insertContent(emoji.i).run();
  dropdown.value?.handleClose();
};
</script>

<template>
  <el-tooltip
      effect="dark"
      content=" 表情选择器 "
      :show-after="500"
      placement="bottom"
  >
    <el-dropdown
        ref="dropdown"
        trigger="click"
        :hide-on-click="false"
        placement="bottom-start"
        popper-class="emoji-dropdown-popper"
    >
      <el-button text class="tool-button tool-emoji-button">
        😀
      </el-button>

      <template #dropdown>
        <div class="tool-emoji-box">
          <div class="emoji-header">
            <el-text size="small">插入表情</el-text>
          </div>

          <EmojiPickerPanel
              @select="onSelectEmoji"
              :group-names="groupNames"
              class="insert-emoji"
              :disable-skin-tones="true"
              :hide-group-icons="true"
              :hide-search="true"
          />
        </div>
      </template>
    </el-dropdown>
  </el-tooltip>
</template>

<style scoped>
.tool-emoji-box {
  width: 290px;
  background-color: white;
}

.emoji-header {
  border-bottom: 1px solid #EDEDEC;
  padding: 8px 12px;
}

.insert-emoji {
  border-radius: 8px;
  box-shadow: none;
}

.tool-emoji-button {
  font-size: 18px;
}
</style>
