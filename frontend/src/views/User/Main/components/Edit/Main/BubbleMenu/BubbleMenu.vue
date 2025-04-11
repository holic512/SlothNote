<script setup lang="ts">
import {ref, provide} from 'vue'
import {BubbleMenu} from '@tiptap/vue-3'
import {ElMessage} from 'element-plus'
import AI from "@/icon/AI.vue";
import Review from "@/icon/Review.vue";
import SetText from "@/views/User/Main/components/Edit/Main/BubbleMenu/SetText/SetText.vue";
import BoldText from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/BoldText.vue";
import ItalicText from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/ItalicText.vue";
import Underline from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/Underline.vue";
import Strikethrough from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/Strikethrough.vue";
import TextColor from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/TextColor.vue";
import {RightPageModeEnum, useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import {useAiChatStore} from "@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat";

const editor: any = defineModel()
provide('editor', editor)

// AI辅助功能 - 仅打开侧边栏
const handleAiAssist = () => {
  try {
    const {from, to, empty} = editor.value.state.selection
    const rightPageState = useRightPageState()
    const aiChat = useAiChatStore()

    if (empty) {
      ElMessage.warning('请先选中需要AI辅助的文本')
      return
    }

    const selectedText = editor.value.state.doc.textBetween(from, to, ' ')
    
    // 存储选中的文本到全局状态
    aiChat.setSelectedText(selectedText)
    
    // 切换AI助手侧边栏（已打开则关闭，未打开则打开）
    rightPageState.SwitchAi()
    
  } catch (error) {
    console.error('AI辅助请求出错:', error)
    ElMessage.error('AI辅助处理时发生错误')
  }
}
</script>

<template>
  <bubble-menu
      :editor="editor"
      :tippy-options="{ duration: 100 }"
      v-if="editor"
  >
    <div class="bubbleBox">
      <!-- AI辅助按钮 -->
      <el-button class="buttonH"
                 text style="margin: 0 !important; padding-left: 6px;padding-right: 10px"
                 @click="handleAiAssist"
      >
        <el-icon size="20" color="#000000">
          <AI/>
        </el-icon>
        <el-text style="color: #353535">
          AI辅助
        </el-text>
      </el-button>

      <el-divider direction="vertical"/>

      <el-button class="buttonH" text style="margin: 0 !important; padding-left: 6px;padding-right: 8px">
        <el-icon size="18" color="#000000">
          <Review/>
        </el-icon>
        <el-text style="color: #353535">
          评论
        </el-text>
      </el-button>

      <el-divider direction="vertical"/>

      <SetText v-model="editor"/>
      <BoldText v-model="editor"/>
      <ItalicText v-model="editor"/>
      <Underline v-model="editor"/>
      <Strikethrough v-model="editor"/>
      <TextColor v-model="editor"/>
    </div>
  </bubble-menu>
</template>


<style scoped>
.bubbleBox {
  width: 400px;
  display: flex;
  flex-wrap: nowrap;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
  border-radius: 4px;
  padding: 2px;
}

.buttonH {
  height: 28px;
}
</style>