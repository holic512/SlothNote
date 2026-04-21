<script setup lang="ts">
import {ModelRef, onBeforeUnmount, ref, watch} from "vue";
import {Editor} from "@tiptap/vue-3";
import IconText from "@/views/User/Main/components/Edit/Main/Tools/icon/IconText.vue";
import IconBColor from "@/views/User/Main/components/Edit/Main/Tools/icon/IconBColor.vue";
import {DropdownInstance} from "element-plus";

const editor: ModelRef<Editor | undefined> = defineModel();

// 文本颜色
const GetTextBColor = ref('#333333');  // 使用 ref 来存储颜色值

const syncTextColor = () => {
  GetTextBColor.value = editor.value?.getAttributes('textStyle').color || '#333333';
}


const boolBColor = (color: string | null) => {
  if (editor.value?.isActive('highlight', {color: color})) {
    return true
  }
  return color == null && !editor.value?.isActive('highlight')
}


// 移除高亮
const removeHigh = () => {
  editor.value?.chain().focus().unsetHighlight().run()
}


let removeListeners: (() => void) | undefined;

watch(() => editor.value, (instance) => {
  removeListeners?.();
  removeListeners = undefined;

  if (!instance) {
    syncTextColor();
    return;
  }

  const events = ['selectionUpdate', 'transaction', 'focus'] as const;
  events.forEach((eventName) => instance.on(eventName, syncTextColor));
  syncTextColor();

  removeListeners = () => {
    events.forEach((eventName) => instance.off(eventName, syncTextColor));
  };
}, {immediate: true});

onBeforeUnmount(() => {
  removeListeners?.();
});

// 颜色选项
const colorOptions = [
  {label: '柔和黑', color: '#333333'},   // 深灰色
  {label: '暖灰', color: '#A8A8A8'},     // 暖灰色
  {label: '温暖金', color: '#F4C542'},   // 柔和的金色
  {label: '海洋蓝', color: '#4C9E9A'},   // 清新的海洋蓝色
  {label: '温柔粉', color: '#F0A7C2'},   // 浅粉色
  {label: '草地绿', color: '#8FBC8F'},   // 清新的草地绿色
  {label: '雾霾紫', color: '#C6A6D2'},   // 柔和紫色
  {label: '浅褐色', color: '#D0B59B'},   // 浅褐色
  {label: '淡橙色', color: '#FFB06B'},   // 浅橙色
  {label: '清新蓝', color: '#5D8C9E'},   // 清新的蓝色
  {label: '浅玫瑰', color: '#F1D0C1'},   // 淡玫瑰色
  {label: '晨曦黄', color: '#F9E5A3'}    // 温柔的晨曦黄色
];

// 背景色选项
const backgroundColorOptions = [
  {label: '清晨雾霭', color: '#E3E3E3'},   // 浅灰色
  {label: '柔和奶油', color: '#F5F3E5'},   // 浅奶油色
  {label: '浅天蓝', color: '#A9D8E8'},     // 浅天蓝色
  {label: '温暖米黄', color: '#F7E2B6'},   // 温暖的米黄色
  {label: '薄荷绿', color: '#D9F6F1'},     // 淡薄荷绿
  {label: '桃花粉', color: '#F9D1D7'},     // 浅桃粉色
  {label: '淡紫丁香', color: '#D1B2E0'},   // 浅紫色
  {label: '浅杏色', color: '#F3CBB5'},     // 浅杏色
  {label: '温和雾蓝', color: '#A6C7E5'},   // 雾霾蓝色
  {label: '浅草绿', color: '#D9F2D7'},     // 清新的浅草绿色
  {label: '柔和桃橙', color: '#F7C0A0'},   // 柔和的桃橙色
  {label: '浅沙棕', color: '#E8D0B3'}      // 浅沙棕色
];


// 设置 文本颜色
const setTextColor = (color: string) => {
  // 保留焦点
  editor.value?.chain().focus()
  handleClose()
  editor.value?.chain().focus().setColor(color).run();
};

// 设置 高亮颜色
const setHighlightColor = (color: string) => {
  // 保留焦点
  editor.value?.chain().focus()
  handleClose()

  editor.value?.chain().focus().toggleHighlight({color}).run()
}


const dropdown = ref<DropdownInstance>()

function handleClose() {
  if (dropdown.value) dropdown.value?.handleClose()
}

</script>

<template>
  <!-- 文字颜色 -->
  <el-tooltip
      class="box-item"
      effect="dark"
      content="文本颜色"
      :show-after="500"
      placement="bottom"
  >
    <el-dropdown trigger="click" ref="dropdown">
      <el-button text style="width: 48px;height: 30px">
        <div class="circle" :style="{ backgroundColor: GetTextBColor }"></div>
        <el-icon size="14">
          <ArrowDown/>
        </el-icon>
      </el-button>
      <template #dropdown>
        <el-scrollbar height="300px">
          <div style="width: 200px; padding: 12px">

            <!--  文本颜色  -->
            <el-text size="small">文本颜色</el-text>

            <div v-for="(option, index) in colorOptions"
                 :key="index"
                 class="button-div"
                 @click="setTextColor(option.color)">

              <!--  文本色彩图标  -->
              <el-icon :color="option.color" size="15" style="margin-right: 8px">
                <IconText/>
              </el-icon>
              <!--  文本文字  -->
              <el-text>{{ option.label }}</el-text>

              <div style="flex: 1"/>

              <!--  选中图标  -->
              <el-icon size="14" style="margin-left: 24px" v-if="GetTextBColor === option.color">
                <Select/>
              </el-icon>

            </div>


            <el-divider style="margin-top: 8px;margin-bottom: 8px"/>


            <!--  背景颜色  -->
            <el-text size="small">背景颜色</el-text>

            <!--默认背景-->
            <div class="button-div" @click="removeHigh()">
              <el-icon size="15" style="margin-right: 8px">
                <IconBColor/>
              </el-icon>
              <el-text> 默认背景</el-text>

              <div style="flex: 1"/>

              <!--  选中图标  -->
              <el-icon size="14" style="margin-left: 24px" v-if="boolBColor(null)">
                <Select/>
              </el-icon>
            </div>

            <!--循环颜色背景-->
            <div v-for="(option, index) in backgroundColorOptions"
                 :key="index"
                 class="button-div"
                 @click="setHighlightColor(option.color)">

              <el-icon :color="option.color" size="15" style="margin-right: 8px">
                <IconBColor/>
              </el-icon>
              <el-text>{{ option.label }}</el-text>

              <div style="flex: 1"/>

              <!--  选中图标  -->
              <el-icon size="14" style="margin-left: 24px" v-if="boolBColor(option.color)">
                <Select/>
              </el-icon>

            </div>

          </div>
        </el-scrollbar>
      </template>
    </el-dropdown>
  </el-tooltip>
</template>

<style scoped>
.circle {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 2px;
  backface-visibility: hidden;
}

.button-div {
  display: flex;
  align-items: center;
  padding: 5px;
  border-radius: 6px;
}

.button-div:hover {
  background-color: #EFEFED;
}
</style>
