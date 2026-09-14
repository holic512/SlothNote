<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, ref, watch} from "vue";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId.js";
import {useRenameData} from "@/views/User/Main/components/Sidebar/Pinia/RenameData.js";

// 状态变量
const newName = ref("");
const emojiPickerVis = ref(false);
const rightSelect: any = useRightSelectNodeId();
const RenameData = useRenameData();
let renameTimer: ReturnType<typeof setTimeout> | undefined;
let renameController: AbortController | undefined;

// 位置计算
const position = computed(() => ({
  x: 160,
  y: rightSelect.selectY + 30,
}));

const positionEmoji = computed(() => {
  const browserHeight = window.innerHeight; // 获取浏览器的高度
  const offsetY = rightSelect.selectY + 30; // 表情选择器的初始 y 位置
  const emojiPickerHeight = 340; // 假设表情选择器的高度为 300px

  // 确保表情选择器不会超出顶部或底部
  const constrainedY = Math.min(
      Math.max(0, offsetY), // 不低于 0
      browserHeight - emojiPickerHeight // 不超出浏览器底部
  );

  return {
    x: 160,
    y: constrainedY,
  };
});


// 关闭对话框函数
const closeDialog = () => {
  RenameData.RenameIs = false;
};

// 点击外部区域时关闭对话框和表情选择器
const handleClickOutside = (event: MouseEvent) => {
  const dialog = document.querySelector(".rename-dialog");
  if (dialog && !dialog.contains(event.target as Node)) {
    closeDialog();
  }

  const dialog2 = document.querySelector(".s-rename-emoji-box");
  if (dialog2 && !dialog2.contains(event.target as Node)) {
    emojiPickerVis.value = false;
  }
};

// 生命周期钩子
onMounted(() => {
  document.addEventListener("mousedown", handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener("mousedown", handleClickOutside);
  if (renameTimer) clearTimeout(renameTimer);
  renameController?.abort();
});

// 表情选择器相关配置
import EmojiPicker, {EmojiExt} from "vue3-emoji-picker";
import "vue3-emoji-picker/css";
import {useNoteTreeUpdate} from "@/views/User/Main/components/Sidebar/Pinia/isNoteTreeUpdated";


const iln = {
  smileys_people: "笑脸与人物",
  animals_nature: "动物与自然",
  food_drink: "食物与饮品",
  activities: "活动",
  travel_places: "旅行与地点",
  objects: "物品",
  symbols: "符号",
  flags: "旗帜",
};

import {updateNoteAvatar} from "./Service/updateNoteAvatar";
import {updateNoteTitle} from "./Service/updateNoteTitle";
import {updateFolderTitle} from "@/views/User/Main/components/Sidebar/components/Rename/Service/updateFolderTitle";
import {updateFolderAvatar} from "@/views/User/Main/components/Sidebar/components/Rename/Service/updateFolderAvatar";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";

// 修改 笔记头像 操作
const onSelectEmoji = async (emoji: EmojiExt) => {
  // 接口返回状态-判断逻辑生效
  let status: any;
  if (rightSelect.data.type == 'NOTE') {
    status = await updateNoteAvatar(rightSelect.data.id, emoji.i);
  } else {
    status = await updateFolderAvatar(rightSelect.data.id, emoji.i);
  }

  // 当修改成功 执行刷新 策略
  if (status == 200) {
    useNoteTreeUpdate().patchNode(rightSelect.data.type, rightSelect.data.id, {avatar: emoji.i});
    rightSelect.data.avatar = emoji.i;

    // 关闭表情选择器
    emojiPickerVis.value = false;

    // 当修改的是当前的笔记 同时刷新heardPage
    const currentNoteInfo = useCurrentNoteInfoStore()
    if (rightSelect.data.type === 'NOTE' && currentNoteInfo.noteId == rightSelect.data.id) {
      currentNoteInfo.avatar = emoji.i;
    }

  }
};

// 移除 头像操作
const removeAvatarEmoji = async () => {
  let status: any;
  if (rightSelect.data.type == 'NOTE') {
    status = await updateNoteAvatar(rightSelect.data.id, "");
  } else {
    status = await updateFolderAvatar(rightSelect.data.id, "");
  }

  // 当修改成功 执行刷新 策略
  if (status == 200) {
    useNoteTreeUpdate().patchNode(rightSelect.data.type, rightSelect.data.id, {avatar: ''});
    rightSelect.data.avatar = '';

    // 关闭表情选择器
    emojiPickerVis.value = false;

    // 当修改的是当前的笔记 同时刷新heardPage
    const currentNoteInfo = useCurrentNoteInfoStore()
    if (rightSelect.data.type === 'NOTE' && currentNoteInfo.noteId == rightSelect.data.id) {
      currentNoteInfo.avatar = '';
    }

  }
}

// 输入停止后提交，取消过时请求，并在本地增量更新节点。
watch(() => newName.value, (value) => {
  if (!RenameData.RenameIs) return;

  if (renameTimer) clearTimeout(renameTimer);
  renameController?.abort();

  const nodeId = Number(rightSelect.data.id);
  const nodeType = rightSelect.data.type as 'NOTE' | 'FOLDER';
  const updateName = value.trim() || "新页面";

  renameTimer = setTimeout(async () => {
    const controller = new AbortController();
    renameController = controller;
    const status = nodeType === 'NOTE'
        ? await updateNoteTitle(nodeId, updateName, controller.signal)
        : await updateFolderTitle(nodeId, updateName, controller.signal);

    if (controller.signal.aborted || status !== 200) return;

    useNoteTreeUpdate().patchNode(nodeType, nodeId, {label: updateName});
    if (rightSelect.data.id === nodeId && rightSelect.data.type === nodeType) {
      rightSelect.data.label = updateName;
    }

    const currentNoteInfo = useCurrentNoteInfoStore();
    if (nodeType === 'NOTE' && currentNoteInfo.noteId === nodeId) {
      currentNoteInfo.noteName = updateName;
    }
  }, 400);
})


// 监听显示是否刷新 当显示刷新的 时候 重新复制 newName
watch(() => RenameData.RenameIs, () => {
  if (!RenameData.RenameIs) {
    if (renameTimer) clearTimeout(renameTimer);
    renameController?.abort();
    newName.value = "";
  }
})

</script>

<template>
  <!--  重命名  -->
  <div v-if="RenameData.RenameIs" class="rename-dialog"
       :style="{top: position.y+'px',left:position.x+'px'
  }"
  >
    <div class="dialog-content">
      <!--  按钮 显示当前文件的 图标-->
      <el-button class="rename-emoji-button" plain @click="emojiPickerVis = true">
        <!--  判断是否使用自定义图标 -->
        <div v-if="rightSelect.data.avatar == null || rightSelect.data.avatar ==''">
          <div v-if='rightSelect.data.type == "NOTE" ' style="margin-top: 4px">
            <el-icon size="16">
              <Notebook/>
            </el-icon>
          </div>
          <div v-else-if='rightSelect.data.type == "FOLDER" ' style="margin-top: 4px">
            <el-icon size="16">
              <Folder/>
            </el-icon>
          </div>
        </div>
        <div v-else style="font-size: 16px;padding-top: 2px">
          {{ rightSelect.data.avatar }}
        </div>

      </el-button>

      <el-input
          v-model="newName"
          type="text"
          :placeholder="rightSelect.data.label"
          class="rename-input"
      />
    </div>
  </div>

  <!--    表情选择器   -->
  <div v-show="emojiPickerVis"
       :style="{top: positionEmoji.y+'px',left:position.x+'px'}"
       class="s-rename-emoji-box"
  >
    <div style="border-bottom: 1px #EDEDEC solid;padding: 4px;display: flex;  height: 36px;">
      <el-text style="margin-left: 8px" size="small">修改头像</el-text>
      <div style="flex: 1"/>
      <el-button size="small" text @click="removeAvatarEmoji()">移除头像</el-button>
    </div>
    <EmojiPicker
        @select="onSelectEmoji"
        :group-names="iln"
        class="rename-emoji"
        :disable-skin-tones="true"
        :hide-group-icons="true"
        :hide-search="true"
    />
  </div>


</template>


<style scoped>
.rename-dialog {
  position: absolute;

  transform: translateX(-50%);
  width: 300px;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-radius: 14px;
  padding: 6px;
  z-index: 1000;
}

.s-rename-emoji-box {
  position: absolute;
  z-index: 1000; /* 确保表情选择器在其他内容之上 */
  background-color: white;
  border-radius: 4px;
  width: 290px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.rename-emoji {
  border-radius: 8px;
  box-shadow: 0 0;
}

.dialog-content {
  display: flex;
  gap: 2px;
}

.rename-input {
  flex: 1;
}

.rename-emoji-button {
  width: 32px;
  height: 32px;
}
</style>
