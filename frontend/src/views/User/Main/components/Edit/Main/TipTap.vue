<script setup lang="ts">
import '/src/fonts/alibabaFy.css'

import Tools from "./Tools/Tools.vue";

import {Editor, EditorContent} from '@tiptap/vue-3'
import {onBeforeUnmount, onMounted, ref, ShallowRef, watch} from "vue";
import BubbleMenu from "@/views/User/Main/components/Edit/Main/BubbleMenu/BubbleMenu.vue";
import ToCItem from "@/views/User/Main/components/Edit/Main/ToCltem/ToCItem.vue";
import {useIndexItemsStore} from "@/views/User/Main/components/Edit/Pinia/IndexItems";
import {ElScrollbar} from "element-plus";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {updateNoteTitle} from "@/views/User/Main/components/Edit/Main/Service/updateNoteTitle";
import {useNoteTreeUpdate} from "@/views/User/Main/components/Sidebar/Pinia/isNoteTreeUpdated";
import SetCover from "@/views/User/Main/components/Edit/Main/SetCover/SetCover.vue";
import {useNoteCoverState} from "@/views/User/Main/components/Edit/Main/SetCover/paina/NoteCoverState";

const editor: ShallowRef<Editor | undefined> = defineModel()

// 焦点恢复到编译器
const focusOnParagraph = () => {
  // editor.value?.commands.focus(); // 将焦点设置到编辑器
}

// 存储目录的pinia
const IndexItemsStore = useIndexItemsStore();

// 存储滚筒条实例 用于目录跳转
const scrollbarRef = ref<InstanceType<typeof ElScrollbar>>()

// NoteCoverState 是否显示的 pinia
const NoteCoverState = useNoteCoverState();

// 获取当前笔记 的 基础信息
const currentNoteInfo = useCurrentNoteInfoStore()

// 定义input model
const InputNoteTitle = ref<string>();

// 定义 背景框
const noteCover = ref<string>();

let titleSyncTimer: ReturnType<typeof setTimeout> | undefined;

const syncNoteTitle = async (noteTitle: string) => {
  const nextTitle = noteTitle.trim();

  if (currentNoteInfo.noteId == null || !nextTitle || nextTitle === currentNoteInfo.noteName) {
    InputNoteTitle.value = currentNoteInfo.noteName;
    return;
  }

  const status = await updateNoteTitle(currentNoteInfo.noteId, nextTitle);
  if (status !== 200) {
    InputNoteTitle.value = currentNoteInfo.noteName;
    return;
  }

  currentNoteInfo.noteName = nextTitle;

  const isNoteTreeUpdated = useNoteTreeUpdate();
  isNoteTreeUpdated.UpdatedNoteTree();
}

const scheduleNoteTitleSync = (noteTitle: string) => {
  if (titleSyncTimer) {
    clearTimeout(titleSyncTimer);
  }

  titleSyncTimer = setTimeout(() => {
    void syncNoteTitle(noteTitle);
  }, 450);
}

// 钩子函数
onMounted(() => {
  SetupInfo()
})

onBeforeUnmount(() => {
  if (titleSyncTimer) {
    clearTimeout(titleSyncTimer);
  }
})

// 监听 当前笔记 是否改变
watch(() => currentNoteInfo.noteId, () => {
  if (titleSyncTimer) {
    clearTimeout(titleSyncTimer);
  }
  SetupInfo()
})

// 笔记内容加载
const SetupInfo = () => {
  // 更新input内容
  InputNoteTitle.value = currentNoteInfo.noteName;
}

// 监听 InputNoteTitle 是否改变 并执行重命名
watch(() => InputNoteTitle.value, async (newValue) => {
  if (currentNoteInfo.noteId != null && newValue != null) {
    scheduleNoteTitleSync(newValue)
  }
})

</script>

<template>
  <div style="position: relative; height: 100%; flex: 1;">
    <!--编辑器-->
    <div style="height: 100%; display: flex; flex-direction: column; width: 100%;">
      <!--  横装 工具栏-->
      <Tools v-model="editor"/>

      <!--  笔记内容  -->
      <el-scrollbar style="flex: 1;">

        <!--  封面  -->
        <img alt="1" :src="'/NoteCover/noteCover' + currentNoteInfo.cover + '.jpg'" style="height: 160px;width: 100%"
             v-if="currentNoteInfo.cover != null"/>

        <!--  当没有 封面 但是有图标的情况下-->
        <div v-if="currentNoteInfo.cover == null && currentNoteInfo.avatar != null" style="margin-top: 36px"/>

        <!-- 头像图标 -->
        <div
            style="height: 30px;width: 100%;display: flex;justify-content: center;align-items: center;margin-bottom: 4px">
          <div style="width: 720px;position: relative;top: -15px; /* 向上移动 50px */">
            <span style="font-size: 54px">{{ currentNoteInfo.avatar }}</span>
          </div>
        </div>

        <div class="container-tiptap">

          <!-- 功能 按钮部分 -->
          <div class="feature">
            <!--          <div class="feature-div">-->
            <!--            <el-text>-->
            <!--              😀 添加图标-->
            <!--            </el-text>-->
            <!--          </div>-->

            <div class="feature-div" @click="NoteCoverState.IsNoteCover()">
              <el-text>
                <el-icon>
                  <PictureFilled/>
                </el-icon>
                添加封面
              </el-text>
            </div>

          </div>


          <!-- 重命名部分 -->
          <input class="styled-input" placeholder="新建文档" v-model="InputNoteTitle">

        </div>


        <div class="editor-content" @click="focusOnParagraph" ref="scrollbarRef">
          <editor-content :editor="editor" class="tiptap-editor"/>
        </div>
      </el-scrollbar>
    </div>


    <!--  目录功能  -->
    <div style="
    position: absolute;
    right: 24px;
    top: 25%;
    text-align: right;
    z-index: 10;
  ">
      <el-popover
          placement="left"
          title="目录"
          :width="250"
          trigger="hover"
      >
        <template #reference>
          <div style="width: 24px;">
            <div v-for="item in IndexItemsStore.IndexItems" :key="item.id">
              <hr
                  v-if="item.level >= 1 && item.level <= 6"
                  :style="{ width: `${100 - (item.level - 1) * 10}%`,border: '1px solid #E3E2E0',margin: '4px 0 0 auto'}"
              />
            </div>
          </div>
        </template>
        <template #default>
          <ToCItem v-if="editor" :editor="editor" :items="IndexItemsStore.IndexItems" :scrollbarRef="scrollbarRef"/>
        </template>
      </el-popover>
    </div>
  </div>


  <!--  选中浮动菜单  -->
  <BubbleMenu v-model="editor"/>

  <!--  设置背景菜单  -->
  <SetCover/>
</template>


<style lang="scss">
.input-container {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.styled-input {
  width: 720px;
  height: 48px; /* 稍微增加高度 */
  font-size: 40px; /* 增大字体 */
  font-weight: bold;
  border: 0;
  outline: none; /* 去除聚焦时的默认蓝色边框 */
}

.styled-input::placeholder {
  color: #E0E0DF;
}

.container-tiptap {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  position: relative; /* 为了让功能部分在悬停时显示在容器上面 */
}

.feature {
  width: 720px;
  opacity: 0; /* 初始时隐藏功能部分 */
  transition: opacity 0.3s ease; /* 添加过渡效果 */

  display: flex;
  gap: 4px;
}

.container-tiptap:hover .feature {
  opacity: 1; /* ���悬停在最外层容器上时，显示功能部分 */
}

.feature-div {
  width: 92px;
  height: 28px;
  padding: 4px;

  display: flex;
  justify-content: center;
  align-items: center;

  border-radius: 4px;

  user-select: none;
}

.feature-div:hover {
  background-color: #EFEFED;
}

/* 样式用于编辑器的主要内容区域 */
.editor-content {
  cursor: text; /* 鼠标悬停时显示文本光标 */
  flex: 1;
  display: flex;
  justify-content: center;
  width: 100%;
  height: 100%;
}


/* 去掉文本选中时的轮廓 */
.editor-content *:focus {
  outline: none;
}

/* 去掉编辑器段落的默认选中样式 */
.editor-content p:focus {
  outline: none;
}

/* 设置编辑器内容的样式 */
.tiptap-editor {
  width: 750px; /* 设置编辑区域的度 */
  font-family: 'alibabaFy', serif; /* 使用自定义字体 */
  font-size: 20px; /* 设置字体大小 */
  line-height: 1; /* 确保文字与背景有良好对比 */

  -webkit-font-smoothing: antialiased; /* 优化 Webkit 内核浏览器 */
  -moz-osx-font-smoothing: grayscale; /* 优化 macOS */

  hr {
    border: none;
    border-top: 2px solid #333; /* 更深的黑色线条 */
    cursor: pointer;
    padding: 0.2rem; /* 增加一些垂直空间 */
    margin: 1rem; /* 增加上下外边距，使分隔更明显 */
    transition: border-color 0.3s ease, box-shadow 0.3s ease; /* 添加过渡效果 */
    height: 10px; /* 确保高度为0，只显示边框 */

    &.ProseMirror-selectednode {
      border-top-color: #007BFF; /* 当选中时，使用蓝色线条 */
    }

    &:hover {
      border-top-color: #0056b3; /* 鼠标悬停时线条颜色变深 */
    }
  }

  h1 {
    font-size: 33px; /* 设置字体大小 */
    margin-bottom: 8px;
  }

  h2 {
    font-size: 31px; /* 设置字体大小 */
    margin-bottom: 8px;
  }

  h3 {
    font-size: 29px; /* 设置字体大小 */
    margin-bottom: 8px;
  }

  h4 {
    font-size: 27px; /* 设置字体大小 */
    margin-bottom: 8px;
  }

  h5 {
    font-size: 25px; /* 设置字��大小 */
    margin-bottom: 8px;
  }

  h6 {
    font-size: 23px; /* 设置字体大小 */
    margin-bottom: 8px;
  }

  img {
    max-width: 100%;
  }

  span {
    line-height: 30px;
  }

  // 引用块
  blockquote {
    border-left: 4px solid #555; /* 更粗的边框和更深的颜色 */

    margin-left: 1rem; /* 增加外边距，使引用更突出 */
    padding-left: 1rem; /* 增加内边距，使内容与边框有足够的空间 */
    background-color: #f9f9f9; /* 浅灰色背景，增加层次感 */
    margin-top: 0;
    margin-bottom: 0;

    p {
      height: 28px;
      display: flex;
      align-items: center;
      margin-top: 0;
      margin-bottom: 0;
    }

    span {
      height: 28px;
      display: flex;
      align-items: center;
      margin-top: 0;
      margin-bottom: 0;
    }

    &:hover {
      border-left-color: #007BFF; /* 鼠标悬停时边框颜色变为蓝色 */
    }
  }

  /* 单列样式 */
  ul,
  ol {
    margin: 2px;

    li p {

      margin-top: 6px;
      margin-bottom: 6px;
    }

    li div {
      margin-top: 0;
    }


    li label {
      margin: 0;
    }


    li {
      height: 32px;
      margin-top: 10px;

    }
  }

  /* 表格外边框 样式 */
  .tableWrapper {
    width: 100%;
    margin-left: 16px;
  }

  /* 表格样式 */
  table {
    border-collapse: collapse;
    margin: 0;
    overflow: hidden;
    table-layout: fixed;


    td,
    th {
      border: 1px solid #c4c4c4; /* 深灰色 */
      box-sizing: border-box;
      min-width: 1em;
      padding: 4px 6px;
      position: relative;
      vertical-align: top;

      > * {
        margin: 4px;
        font-size: 14px;
      }

    }

    /* 首行 样式 */
    th {
      background-color: #f5f5f5; /* 浅灰色 */
      font-weight: normal;
      text-align: left;
    }

    /* 框选 样式 */
    .selectedCell:after {
      background-color: rgba(116, 181, 218, 0.2);
      content: "";
      left: 0;
      right: 0;
      top: 0;
      bottom: 0;
      pointer-events: none;
      position: absolute;
      z-index: 2;
    }

    /* 行选中 */
    .column-resize-handle {
      background-color: #74B5DA;
      bottom: -20px;
      // pointer-events: none;
      position: absolute;
      right: -5px;
      top: -20px;
      width: 2px;

      cursor: grab;
    }

    .column-resize-handle:active {
      cursor: grabbing;
    }
  }


  /* 任务选中框样式 */
  ul[data-type="taskList"] {
    list-style: none;
    margin-left: 0;
    padding: 0;

    li {
      align-items: flex-start;
      display: flex;

      > label {
        flex: 0 0 auto;
        margin-right: 0.5rem;
        user-select: none;
      }

      > div {
        flex: 1 1 auto;
      }
    }

    input[type="checkbox"] {
      cursor: pointer;
    }

    ul[data-type="taskList"] {
      margin: 0;
    }
  }

}

/* 设置选中文本的背景颜色 */
::selection {
  background-color: #70CFF850;
}

/* ProseMirror 编辑器的通用样式 */
.ProseMirror {
  padding: 0 1rem 1rem 0; /* 设置内容的内边距 */

  * {
    margin-top: 0.75em; /* 设置每个子元素的顶部外边距 */
  }

  > * {
    margin-left: 1rem; /* 设置直接子元素的左边距 */
    margin-right: 1rem; /* 设置直接子元素的右边距 */
  }

  .ProseMirror-widget * {
    margin-top: auto; /* 对 ProseMirror 小部件应用自动顶部外边距 */
  }

  ul,
  ol {
    margin-left: 12px !important;
    padding-left: 28px;
  }


}

/* 选区范围的样式 */
.ProseMirror-noderangeselection {
  *::selection {
    background: transparent; /* 设置选区范围背景色为透明 */
  }

  * {
    caret-color: transparent; /* 将插入符号颜色设为透明 */
  }
}

/* 设置被选中节点或选区的样式 */
.ProseMirror-selectednode,
.ProseMirror-selectednoderange {
  position: relative;

  &::before {
    position: absolute;
    pointer-events: none; /* 禁止指针事件 */
    z-index: -1; /* 设置层级 */
    content: '';
    top: -0.25rem;
    left: -0.25rem;
    right: -0.25rem;
    bottom: -0.25rem;
    background-color: #70CFF850; /* 设置背景颜色 */
    border-radius: 0.2rem; /* 圆角 */
  }

}

/* 自定义拖拽手柄样式 */
.custom-drag-handle {
  &::after {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 1.25rem;
    height: 1.25rem;
    content: '⠿'; /* 显示拖拽手柄的内容 */
    font-weight: 700;
    cursor: grab; /* 鼠标样式为抓取 */
    background: #0D0D0D10; /* 背景色 */
    color: #0D0D0D50; /* 字体颜色 */
    border-radius: 0.25rem; /* 圆角 */

  }
}

/* 编辑器占位符样式 */
.tiptap {
  /* 顶部占位符 */
  p.is-editor-empty::before {
    color: #9B9A97; /* 占位符文本颜色 */
    content: attr(data-placeholder); /* 占位符内容 */
    float: left;
    height: 0;
    pointer-events: none; /* 禁用鼠标事件 */
  }
}
</style>
