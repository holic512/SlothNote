<script setup lang="ts">
import {computed, ref, onMounted, onBeforeUnmount, shallowRef, defineAsyncComponent} from "vue";
import { debounce } from 'lodash-es';

import '../css/editTool-button.css'

// 动态导入表情选择器，减少初始加载时间
const EmojiPicker = defineAsyncComponent(() => import("vue3-emoji-picker"));
import type {EmojiExt} from "vue3-emoji-picker";

// 懒加载CSS，只在显示表情选择器时加载
const loadEmojiCSS = () => {
  if (!document.getElementById('emoji-picker-css')) {
    import("vue3-emoji-picker/css").then(() => {
      const link = document.createElement('link');
      link.id = 'emoji-picker-css';
      link.rel = 'stylesheet';
      link.href = 'vue3-emoji-picker/css';
      document.head.appendChild(link);
    });
  }
};

const editor: any = defineModel();

// 表情选择器分组名称 - 使用shallowRef减少响应式开销
const iln = shallowRef({
  smileys_people: "笑脸与人物",
  animals_nature: "动物与自然",
  food_drink: "食物与饮品",
  activities: "活动",
  travel_places: "旅行与地点",
  objects: "物品",
  symbols: "符号",
  flags: "旗帜",
});

// 表情选择器的显示与位置控制
const showEmojiPicker = ref(false);
const buttonRef = shallowRef<HTMLElement | null>(null);
const emojiPosition = shallowRef({x: 0, y: 0});
const isEmojiLoaded = ref(false);

// 添加防抖优化
const toggleEmojiPicker = debounce(() => {
  const buttonElement = (buttonRef.value as any)?.$el
  if (buttonElement) {
    const rect = buttonElement.getBoundingClientRect();
    emojiPosition.value = {
      x: rect.left - 140, // 按钮左侧位置
      y: rect.bottom + 10, // 按钮底部向下 20px
    };
    
    // 如果准备显示表情选择器，加载CSS
    if (!showEmojiPicker.value) {
      loadEmojiCSS();
      if (!isEmojiLoaded.value) {
        // 只在第一次加载
        isEmojiLoaded.value = true;
      }
    }
    
    showEmojiPicker.value = !showEmojiPicker.value;
  } else {
    console.warn("buttonRef is not a valid DOM element");
  }
}, 50);

// 表情选择回调
const onSelectEmoji = (emoji: EmojiExt) => {
  editor.value?.chain().focus().insertContent(emoji.i).run(); // 插入表情
  showEmojiPicker.value = false; // 隐藏表情选择器
};

const emojiPickerRef = shallowRef<HTMLElement | null>(null);

// 监听点击事件，点击表情选择器外部时关闭它 - 使用防抖优化
const handleClickOutside = debounce((event: MouseEvent) => {
  const buttonElement = (buttonRef.value as any)?.$el
  if (
      showEmojiPicker.value && 
      buttonElement && 
      !buttonElement.contains(event.target as Node)
  ) {
    // 检查是否点击在表情选择器外部
    const emojiBox = document.querySelector('.tool-emoji-box');
    if (emojiBox && !emojiBox.contains(event.target as Node)) {
      showEmojiPicker.value = false; // 隐藏表情选择器
    }
  }
}, 50);

// 注册和注销全局点击事件监听
onMounted(() => {
  document.addEventListener("click", handleClickOutside, { passive: true });
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
});

// 预加载最常用的表情分类
const preloadCommonEmojis = () => {
  // 创建一个不可见的图像元素，预加载常用表情
  const preloadImage = new Image();
  preloadImage.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjQiIGhlaWdodD0iMjQiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHRleHQgeD0iMCIgeT0iMjAiIGZvbnQtc2l6ZT0iMjAiPvCfmIQ8L3RleHQ+PC9zdmc+';
};

// 优化表情选择器初始渲染
onMounted(() => {
  // 使用 requestIdleCallback 在浏览器空闲时预加载常用表情
  if ('requestIdleCallback' in window) {
    (window as any).requestIdleCallback(preloadCommonEmojis);
  } else {
    setTimeout(preloadCommonEmojis, 1000);
  }
});

</script>

<template>

  <el-tooltip
      class="box-item"
      effect="dark"
      content=" 表情选择器 "
      :show-after="500"
      placement="bottom"
  >
    <!-- 按钮 -->
    <el-button text class="tool-button tool-emoji-button" ref="buttonRef" @click="toggleEmojiPicker">
      😀
    </el-button>
  </el-tooltip>

  <!-- 表情选择器 -->
  <div v-show="showEmojiPicker"
       :style="{ position: 'absolute', top: emojiPosition.y + 'px', left: emojiPosition.x + 'px' }"
       class="tool-emoji-box"
  >
    <div style="border-bottom: 1px #EDEDEC solid;padding: 4px;display: flex;  height: 36px;">
      <el-text style="margin-left: 8px" size="small">插入表情</el-text>
    </div>

    <div class="emoji-loading" v-if="!isEmojiLoaded">
      <div class="emoji-loading-spinner"></div>
    </div>
    
    <EmojiPicker
        v-if="showEmojiPicker"
        ref="emojiPickerRef"
        @select="onSelectEmoji"
        :group-names="iln"
        class="insert-emoji"
        :disable-skin-tones="true"
        :hide-group-icons="true"
        :hide-search="true"
    />
  </div>

</template>

<style scoped>
.tool-emoji-box {
  z-index: 1000; /* 确保表情选择器在其他内容之上 */
  background-color: white;
  border-radius: 4px;
  width: 290px;
  box-shadow: 4px 4px 8px rgba(0, 0, 0, 0.2);
  border: #EDEDEC 1px solid;
  will-change: transform, opacity; /* 优化动画性能 */
  contain: content; /* 限制重绘范围 */
  transform: translateZ(0); /* 启用GPU加速 */
  overscroll-behavior: contain; /* 防止滚动传播 */
  content-visibility: auto; /* 提高渲染性能 */
}

.insert-emoji {
  border-radius: 8px;
  box-shadow: 0 0;
  will-change: transform; /* 优化滚动性能 */
  contain: content; /* 提高渲染性能 */
  content-visibility: auto; /* 优化渲染流程 */
}

.tool-emoji-button{
  font-size: 18px;
  padding-top: 10px;
  contain: layout style paint; /* 隔离布局和样式影响 */
  content-visibility: auto; /* 优化渲染 */
}

/* 优化emoji渲染性能 */
:deep(.emoji-mart-emoji) {
  will-change: transform;
  backface-visibility: hidden;
  transform: translateZ(0);
  contain: layout style paint; /* 最大程度隔离 */
}

/* 优化分类切换性能 */
:deep(.emoji-mart-category) {
  contain: layout style;
  will-change: transform;
  content-visibility: auto; /* 优化可视区域外的内容 */
}

/* 优化表情滚动容器性能 */
:deep(.emoji-mart-scroll) {
  overscroll-behavior: contain;
  contain: content;
  scroll-behavior: smooth; /* 平滑滚动 */
}

/* 优化表情分类性能 */
:deep(.emoji-mart-anchors) {
  contain: layout style;
  will-change: transform;
}

/* 表情容器优化 */
:deep(.emoji-mart-search) {
  contain: layout style;
}

/* 加载指示器 */
.emoji-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.emoji-loading-spinner {
  width: 30px;
  height: 30px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #74B5DA;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 优化表情面板内部渲染 */
:deep(.emoji-mart) {
  contain: content;
  will-change: transform;
  transform: translateZ(0);
}
</style>
