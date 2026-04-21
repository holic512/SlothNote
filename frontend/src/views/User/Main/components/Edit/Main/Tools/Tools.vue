<!--  用于 文本编辑器上方的 工具栏 -->
<script setup lang="ts">
import {computed, onBeforeUnmount, ref, watch} from "vue";
import type {Component} from "vue";
import type {Editor} from "@tiptap/vue-3";
import PlusMore from "./Plus/PlusMore.vue";
import SetText from "../BubbleMenu/SetText/SetText.vue";
import HighlightText from "./components/TextColor.vue";
import EmojiPicker from "./components/EmojiPicker.vue";
import InsertTable from "./components/InsertTable.vue";
import IconBold from "./icon/IconBold.vue";
import IconItalic from "./icon/IconItalic.vue";
import IconUnderline from "./icon/IconUnderline.vue";
import IconStrikethrough from "./icon/IconStrikethrough.vue";
import IconUnorderedList from "./icon/IconUnorderedList.vue";
import IconOrderedList from "./icon/IconOrderedList.vue";
import IconUndo from "./icon/IconUndo.vue";
import IconRedo from "./icon/IconRedo.vue";
import IconCode from "./Plus/Icon/blocks2/IconCode.vue";
import IconBlockquote from "./Plus/Icon/blocks1/IconBlockquote.vue";

const editor = defineModel<Editor>();
const editorStateVersion = ref(0);

type ToolbarAction = {
  key: string
  label: string
  icon: Component
  run: (instance: Editor) => void
  isActive?: (instance: Editor) => boolean
  isDisabled?: (instance: Editor) => boolean
};

const toolbarActions: ToolbarAction[] = [
  {
    key: "undo",
    label: "撤销",
    icon: IconUndo,
    run: (instance) => instance.chain().focus().undo().run(),
    isDisabled: (instance) => !instance.can().chain().focus().undo().run(),
  },
  {
    key: "redo",
    label: "重做",
    icon: IconRedo,
    run: (instance) => instance.chain().focus().redo().run(),
    isDisabled: (instance) => !instance.can().chain().focus().redo().run(),
  },
  {
    key: "bold",
    label: "加粗",
    icon: IconBold,
    run: (instance) => instance.chain().focus().toggleBold().run(),
    isActive: (instance) => instance.isActive("bold"),
  },
  {
    key: "italic",
    label: "斜体",
    icon: IconItalic,
    run: (instance) => instance.chain().focus().toggleItalic().run(),
    isActive: (instance) => instance.isActive("italic"),
  },
  {
    key: "underline",
    label: "下划线",
    icon: IconUnderline,
    run: (instance) => instance.chain().focus().toggleUnderline().run(),
    isActive: (instance) => instance.isActive("underline"),
  },
  {
    key: "strike",
    label: "删除线",
    icon: IconStrikethrough,
    run: (instance) => instance.chain().focus().toggleStrike().run(),
    isActive: (instance) => instance.isActive("strike"),
  },
  {
    key: "blockquote",
    label: "引用",
    icon: IconBlockquote,
    run: (instance) => instance.chain().focus().toggleBlockquote().run(),
    isActive: (instance) => instance.isActive("blockquote"),
  },
  {
    key: "codeBlock",
    label: "代码块",
    icon: IconCode,
    run: (instance) => instance.chain().focus().toggleCodeBlock().run(),
    isActive: (instance) => instance.isActive("codeBlock"),
  },
  {
    key: "bulletList",
    label: "无序排列",
    icon: IconUnorderedList,
    run: (instance) => instance.chain().focus().toggleBulletList().run(),
    isActive: (instance) => instance.isActive("bulletList"),
  },
  {
    key: "orderedList",
    label: "有序排列",
    icon: IconOrderedList,
    run: (instance) => instance.chain().focus().toggleOrderedList().run(),
    isActive: (instance) => instance.isActive("orderedList"),
  },
];

const actionGroups = computed(() => {
  editorStateVersion.value;
  const instance = editor.value;

  return [
    toolbarActions.slice(0, 2).map((action) => buildToolbarItem(action, instance)),
    toolbarActions.slice(2, 6).map((action) => buildToolbarItem(action, instance)),
    toolbarActions.slice(6).map((action) => buildToolbarItem(action, instance)),
  ];
});

function buildToolbarItem(action: ToolbarAction, instance: Editor | undefined) {
  return {
    ...action,
    active: instance ? action.isActive?.(instance) ?? false : false,
    disabled: instance ? action.isDisabled?.(instance) ?? false : true,
  };
}

const refreshToolbarState = () => {
  editorStateVersion.value += 1;
};

const removeEditorListeners = ref<(() => void) | null>(null);

const bindEditorListeners = (instance: Editor | undefined) => {
  removeEditorListeners.value?.();
  removeEditorListeners.value = null;

  if (!instance) {
    refreshToolbarState();
    return;
  }

  const events = ["selectionUpdate", "transaction", "focus", "blur"] as const;
  events.forEach((eventName) => instance.on(eventName, refreshToolbarState));
  refreshToolbarState();

  removeEditorListeners.value = () => {
    events.forEach((eventName) => instance.off(eventName, refreshToolbarState));
  };
};

watch(() => editor.value, (instance) => {
  bindEditorListeners(instance);
}, {immediate: true});

onBeforeUnmount(() => {
  removeEditorListeners.value?.();
});

const runToolbarAction = (action: ToolbarAction) => {
  if (!editor.value || action.disabled) {
    return;
  }

  action.run(editor.value);
};
</script>

<template>
  <div class="ToolsContainer">
    <div class="toolbar-scroll">
      <div class="toolbar-group">
        <template v-for="action in actionGroups[0]" :key="action.key">
          <el-tooltip effect="dark" :content="` ${action.label} `" :show-after="500" placement="bottom">
            <el-button
                text
                class="tool-button toolbar-button"
                :class="{ 'is-active': action.active }"
                :disabled="action.disabled"
                @click="runToolbarAction(action)"
            >
              <el-icon :color="action.active ? '#2383E2' : '#000000'" size="18">
                <component :is="action.icon"/>
              </el-icon>
            </el-button>
          </el-tooltip>
        </template>
      </div>

      <el-divider direction="vertical"/>

      <div class="toolbar-group">
        <SetText v-model="editor"/>
        <PlusMore v-model="editor"/>
        <InsertTable v-model="editor"/>
      </div>

      <el-divider direction="vertical"/>

      <div class="toolbar-group">
        <template v-for="action in actionGroups[1]" :key="action.key">
          <el-tooltip effect="dark" :content="` ${action.label} `" :show-after="500" placement="bottom">
            <el-button
                text
                class="tool-button toolbar-button"
                :class="{ 'is-active': action.active }"
                :disabled="action.disabled"
                @click="runToolbarAction(action)"
            >
              <el-icon :color="action.active ? '#2383E2' : '#000000'" size="18">
                <component :is="action.icon"/>
              </el-icon>
            </el-button>
          </el-tooltip>
        </template>
        <HighlightText v-model="editor"/>
      </div>

      <el-divider direction="vertical"/>

      <div class="toolbar-group">
        <template v-for="action in actionGroups[2]" :key="action.key">
          <el-tooltip effect="dark" :content="` ${action.label} `" :show-after="500" placement="bottom">
            <el-button
                text
                class="tool-button toolbar-button"
                :class="{ 'is-active': action.active }"
                :disabled="action.disabled"
                @click="runToolbarAction(action)"
            >
              <el-icon :color="action.active ? '#2383E2' : '#000000'" size="18">
                <component :is="action.icon"/>
              </el-icon>
            </el-button>
          </el-tooltip>
        </template>
        <EmojiPicker v-model="editor"/>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ToolsContainer {
  width: 100%;
  height: 36px;
  border-bottom: 1px solid #F0F3F5;
  display: flex;
  align-items: center;
  overflow: hidden;
  padding: 0 8px;
}

.toolbar-scroll {
  width: 100%;
  min-width: 0;
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none;
}

.toolbar-scroll::-webkit-scrollbar {
  display: none;
}

.toolbar-group {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  flex: 0 0 auto;
  white-space: nowrap;
}

.toolbar-button {
  transition: background-color 0.15s ease, color 0.15s ease;
}

.toolbar-button.is-active {
  background-color: #EEF6FF;
}

:deep(.el-divider--vertical) {
  flex: 0 0 auto;
  margin: 0 8px;
  height: 18px;
}
</style>
