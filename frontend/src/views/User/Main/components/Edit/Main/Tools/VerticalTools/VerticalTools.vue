<!--
@file EditorVerticalTools
@project SlothNote
@module 用户端 / 笔记编辑器工具栏
@description 提供编辑器右侧轻量快捷操作入口。
@logic 1. 展开或收起竖向工具按钮；2. 调用工具栏显示、加粗、斜体命令；3. 使用 Element Plus 替代旧 SpeedDial。
@dependencies Store: useUserPreferencesStore, Service: bold/italic, Component: IconBold/IconItalic/IconTopRight
@index_tags 编辑器工具栏, 竖向工具, ElementPlus按钮, SpeedDial迁移
@author holic512
-->
<script setup lang="ts">
import {ref} from 'vue';


import IconBold from "@/views/User/Main/components/Edit/Main/Tools/icon/IconBold.vue";
import IconItalic from "@/views/User/Main/components/Edit/Main/Tools/icon/IconItalic.vue";
import IconBottomLeft from "@/views/User/Main/components/Edit/Main/Tools/icon/IconTopRight.vue";
import {useUserPreferencesStore} from "@/views/User/Main/Pinia/userPreferencesStore";
import {bold} from "@/views/User/Main/components/Edit/Main/Tools/Service/bold";
import {italic} from "@/views/User/Main/components/Edit/Main/Tools/Service/Italic";

// 获取个性化实例
const PreferencesStore = useUserPreferencesStore()

// 获取editor的实例
const editor: any = defineModel()

const icons = {
  BottomLeft: IconBottomLeft,

  bold: IconBold,
  Italic: IconItalic,

};

const items = ref([
  {
    label: '切换状态栏',
    icon: 'BottomLeft',
    command: () => {
      PreferencesStore.toggleEditorToolbar()
    }
  },

  {
    label: '加粗',
    icon: 'bold',
    command: () => {
      bold(editor.value);
    }
  },

  {
    label: '斜体',
    icon: 'Italic',
    command: () => {
      italic(editor.value)
    }
  },

])

const resolveIcon = (iconKey) => icons[iconKey] || null;
const expanded = ref(false);

const runCommand = (command: () => void) => {
  command();
  expanded.value = false;
};

</script>

<template>
  <div class="vertical-tools">
    <el-button @click="expanded = !expanded" text circle>
      <el-icon size="16">
        <Plus/>
      </el-icon>
    </el-button>

    <transition name="vertical-tools-fade">
      <div v-if="expanded" class="vertical-tool-list">
        <el-tooltip
            v-for="item in items"
            :key="item.label"
            effect="dark"
            :content="item.label"
            :show-after="500"
            placement="right"
        >
          <el-button @click="runCommand(item.command)" text circle>
          <el-icon>
            <component :is="resolveIcon(item.icon)"/>
          </el-icon>
          </el-button>
        </el-tooltip>
      </div>
    </transition>
  </div>

</template>

<style scoped>
.vertical-tools {
  position: absolute;
  top: 25%;
  left: 25%;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.vertical-tool-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.vertical-tools-fade-enter-active,
.vertical-tools-fade-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}

.vertical-tools-fade-enter-from,
.vertical-tools-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
