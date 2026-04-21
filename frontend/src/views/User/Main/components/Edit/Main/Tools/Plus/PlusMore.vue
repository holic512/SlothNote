<script setup lang="ts">
import '../css/editTool-button.css'

// blocks1
import IconHeading1 from "./Icon/blocks1/IconHeading-1.vue"
import IconHeading2 from "./Icon/blocks1/IconHeading-2.vue"
import IconHeading3 from "./Icon/blocks1/IconHeading-3.vue"
import IconHeading4 from "./Icon/blocks1/IconHeading-4.vue"
import IconHeading5 from "./Icon/blocks1/IconHeading-5.vue"
import IconHeading6 from "./Icon/blocks1/IconHeading-6.vue"

import IconTodo from "./Icon/blocks1/IconTodo.vue";
import IconBlockquote from "./Icon/blocks1/IconBlockquote.vue";
import IconDivider from "./Icon/blocks1/IconDivider.vue";
import IconDetails from "./Icon/blocks1/IconDetails.vue";

// blocks2
import IconImage from "./Icon/blocks2/IconImage.vue";
import IconCode from "./Icon/blocks2/IconCode.vue";
import IconVideo from "./Icon/blocks2/IconVideo.vue";
import {insertHeading} from "@/views/User/Main/components/Edit/Main/Tools/Plus/Service/insertHeading";
import {DropdownInstance} from "element-plus";
import {ref} from "vue";
import {InsertTaskList} from "@/views/User/Main/components/Edit/Main/Tools/Plus/Service/insertTaskList";
import {insertHorizontal} from "@/views/User/Main/components/Edit/Main/Tools/Plus/Service/insertHorizontal";
import {InsertBlockquote} from "@/views/User/Main/components/Edit/Main/Tools/Plus/Service/insertBlockquote";


const dropdown = ref<DropdownInstance>()

const editor:any = defineModel();

// 数据结构，包含 label, tag 和 icon 字段
const blocks1 = [
  // {label: '文本', tag: '编辑你的文本内容', icon: IconText},
  {label: '一级标题', tag: '设置一级标题的格式', icon: IconHeading1},
  {label: '二级标题', tag: '设置二级标题的格式', icon: IconHeading2},
  {label: '三级标题', tag: '设置三级标题的格式', icon: IconHeading3},
  {label: '四级标题', tag: '设置四级标题的格式', icon: IconHeading4},
  {label: '五级标题', tag: '设置五级标题的格式', icon: IconHeading5},
  {label: '六级标题', tag: '设置六级标题的格式', icon: IconHeading6},

  {label: '待办', tag: '管理任务和待办事项', icon: IconTodo},
  {label: '引用', tag: '引用其他内容', icon: IconBlockquote},
  {label: '分隔线', tag: '插入一条分隔线', icon: IconDivider},
  {label: '详细信息', tag: '查看详细信息', icon: IconDetails}
];

// 绑定 blocks1 的代办
const click1 = (label: string) => {
  switch (label) {
    case '一级标题':
      insertHeading(editor.value, 1);
      break;
    case '二级标题':
      insertHeading(editor.value, 2);
      break;
    case '三级标题':
      insertHeading(editor.value, 3);
      break;
    case '四级标题':
      insertHeading(editor.value, 4);
      break;
    case '五级标题':
      insertHeading(editor.value, 5);
      break;
    case '六级标题':
      insertHeading(editor.value, 6);
      break;
    case '待办':
      InsertTaskList(editor.value);
      break;
    case'分隔线':
      insertHorizontal(editor.value);
      break;
    case '引用':
      InsertBlockquote(editor.value);
      break;
    case '代码块':
      editor.value?.chain().focus().toggleCodeBlock().run();
      break;

  }

  // 关闭下拉菜单
  if (dropdown.value)
    dropdown.value.handleClose()

}

const blocks2 = [
  {label: '图片', tag: '插入图片', icon: IconImage},
  {label: '代码块', tag: '显示代码段', icon: IconCode},
  {label: '视频', tag: '嵌入视频内容', icon: IconVideo}
];


</script>

<template>
  <el-tooltip
      class="box-item"
      effect="dark"
      content=" 插入 "
      :show-after="500"
      placement="bottom"
  >

    <el-dropdown trigger="click" ref="dropdown">
      <el-button text class="button">
        <el-icon color="#000000" size="18">
          <Plus/>
        </el-icon>
      </el-button>
      <template #dropdown>

        <el-scrollbar height="400">
          <div style="width: 250px; padding: 12px">


            <!--  基础区块  -->
            <div>
              <el-text size="small">基础区块</el-text>
              <div class="button-divs">
                <div
                    v-for="(block, index) in blocks1"
                    :key="index"
                    class="button-div"
                    @click="click1(block.label)"
                >
                  <!-- 图标 -->
                  <el-icon size="28" style="margin-right: 8px">
                    <component :is="block.icon"/>
                  </el-icon>

                  <!-- 文本和标签 -->
                  <div style="padding: 2px">
                    <el-text style="display: block;">{{ block.label }}</el-text>
                    <el-text style="display: block;" size="small" type="info">{{ block.tag }}</el-text>
                  </div>
                </div>
              </div>
            </div>

            <el-divider style="margin-top: 8px;margin-bottom: 8px"/>

            <!--  媒体内容  -->
            <div>
              <el-text size="small">媒体内容</el-text>
              <div class="button-divs">
                <div
                    v-for="(block, index) in blocks2"
                    :key="index"
                    class="button-div"
                >
                  <!-- 图标 -->
                  <el-icon size="28" style="margin-right: 8px">
                    <component :is="block.icon"/>
                  </el-icon>

                  <!-- 文本和标签 -->
                  <div style="padding: 2px">
                    <el-text style="display: block;">{{ block.label }}</el-text>
                    <el-text style="display: block;" size="small" type="info">{{ block.tag }}</el-text>
                  </div>
                </div>
              </div>
            </div>


          </div>
        </el-scrollbar>


      </template>
    </el-dropdown>

  </el-tooltip>
</template>

<style scoped>
.button-div {
  display: flex;
  align-items: center;
  padding: 2px;
  border-radius: 6px;
}

.button-div:hover {
  background-color: #EFEFED;
}

</style>
