<script setup lang="ts">
import {ref, provide} from 'vue'
import {BubbleMenu} from '@tiptap/vue-3'
import {ElMessage, ElDrawer, ElLoading} from 'element-plus'
import Explanation from "../../../../../../../icon/Explanation.vue";
import AI from "@/icon/AI.vue";
import Review from "@/icon/Review.vue";
import SetText from "@/views/User/Main/components/Edit/Main/BubbleMenu/SetText/SetText.vue";
import BoldText from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/BoldText.vue";
import ItalicText from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/ItalicText.vue";
import Underline from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/Underline.vue";
import Strikethrough from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/Strikethrough.vue";
import TextColor from "@/views/User/Main/components/Edit/Main/BubbleMenu/components/TextColor.vue";
import {is} from "date-fns/locale";
import {RightPageModeEnum, useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import {useAiChatStore} from "@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiChat";

const editor: any = defineModel()
provide('editor', editor)

// 配置 button显示
const showExplanationSidebar = ref(false)
const selectedText = ref('')
const explanationResult = ref('')
const isLoading = ref(false)

const handleAIClick = async () => {
  try {
    const {from, to, empty} = editor.value.state.selection

    if (empty) {
      ElMessage.warning('请先选中需要解释的文本')
      return
    }


    // 立即显示对话框
    showExplanationSidebar.value = true
    const textToExplain = editor.value.state.doc.textBetween(from, to, ' ')
    selectedText.value = textToExplain
    explanationResult.value = '' // 清空之前的结果
    isLoading.value = true

    // 发送API请求
    const response = await fetch('http://localhost:8080/user/ai/explain', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({text: textToExplain})
    })

    if (!response.ok) throw new Error('API请求失败')
    const result = await response.json()

    if (result.success) {
      explanationResult.value = result.explanation
    } else {
      ElMessage.error('获取解释失败，请稍后重试')
    }

  } catch (error) {
    console.error('解释请求出错:', error)
    ElMessage.error('请求解释时发生错误')
  } finally {
    isLoading.value = false
  }
}
// 关闭边栏时恢复编辑器焦点
const handleClose = () => {
  showExplanationSidebar.value = false
  editor.value?.commands.focus()
}

//ai润色
const AiTouch = async () => {
  try {
    const {from, to, empty} = editor.value.state.selection
    const rightPageState = useRightPageState()
    const aiChat = useAiChatStore()

    if (empty) {
      ElMessage.warning('请先选中需要润色的文本')
      return
    }

    const textToPolish = editor.value.state.doc.textBetween(from, to, ' ')
    
    // 打开AI助手侧边栏
    rightPageState.OpenAi()
    
    // 发送文本到AI助手
    await aiChat.sendMessage(`请帮我润色以下文本：\n${textToPolish}`)

  } catch (error) {
    console.error('AI润色请求出错:', error)
    ElMessage.error('AI润色处理时发生错误')
  }
}
</script>

<template>
  <bubble-menu
      :editor="editor"
      :tippy-options="{ duration: 100 }"
      v-if="editor "
  >
    <div class="bubbleBox">
      <el-button
          text
          class="buttonH"
          style="margin: 0 !important; padding-left: 4px;padding-right: 8px"
          @click="handleAIClick"
          :disabled="isLoading"
      >
        <el-icon size="20">
          <Explanation/>
        </el-icon>
        <el-text style="color: #353535">解释
        </el-text>
      </el-button>

      <el-button class="buttonH"
                 text style="margin: 0 !important; padding-left: 6px;padding-right: 10px"
                 @click="AiTouch"
                 :disabled="isLoading"

      >
        <el-icon size="20" color="#000000">
          <AI/>
        </el-icon>
        <el-text style="color: #353535">
          Ai润色
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

  <!-- 解释结果对话框 -->
  <el-drawer
      v-model="showExplanationSidebar"
      title="文本解释"
      direction="rtl"
      size="400px"
      :before-close="handleClose"
      :modal="false"
      :append-to-body="true"
      :close-on-click-modal="false"
      :close-on-press-escape="true"
      :show-close="false"
      :wrapper-closable="true"
      class="explanation-sidebar"
  >
    <template #header>
      <div class="drawer-header">
        <h4>文本解释</h4>
        <el-button
            text
            @click="handleClose"
            class="close-btn"
        >
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </template>
    <div class="sidebar-content">
      <div class="original-text">
        <span class="highlight">选中文本：</span>
        <span>{{ selectedText }}</span>
      </div>

      <div class="explanation-result">
        <span class="highlight">解释结果：</span>
        <div v-if="isLoading" class="loading-box">
          <el-icon class="loading-icon"><Loading /></el-icon>
          正在生成解释...
        </div>
        <pre v-else-if="explanationResult">{{ explanationResult }}</pre>
        <el-empty v-else description="暂无解释结果" />
      </div>
    </div>
  </el-drawer>
</template>


<style scoped>
.explanation-sidebar {
  z-index: 1000 !important;
  --el-drawer-bg-color: #F9F5F0;
  pointer-events: auto;
}


:deep(.el-overlay .el-drawer) {
  pointer-events: auto;
}

.drawer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.close-btn {
  padding: 0;
  margin-left: auto;
}
.bubbleBox {
  width: 460px;
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


.explanation-sidebar {
  --el-drawer-bg-color: #F9F5F0; /* 米色背景 */
  --el-drawer-padding-primary: 20px;
}

.sidebar-content {
  padding: 0 20px;
  height: 100%;
  overflow-y: auto;
}

.highlight {
  color: #8B4513; /* 深棕色标题 */
  margin-right: 8px;
  font-weight: bold;
  position: relative;
  padding-left: 24px;
}

.highlight::before {
  content: "•";
  position: absolute;
  left: 12px;
  color: #D2B48C; /* 浅米色圆点装饰 */
}

.original-text, .explanation-result {
  margin-bottom: 15px;
  border-left: 3px solid #D2B48C; /* 浅卡其色装饰线 */
  padding-left: 12px;
}

pre {
  background-color: #F0EAD6; /* 浅杏仁色背景 */
  color: #333; /* 深灰色文字 */
  padding: 15px;
  border-radius: 6px;
  white-space: pre-wrap;
  word-break: break-word;
  margin-top: 12px;
  border: 1px solid #E0D3B6; /* 浅米色边框 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

/* 加载样式 */
.loading-box {
  padding: 20px;
  text-align: center;
  color: #666;
  background: #F5F1E8; /* 浅米色背景 */
  border-radius: 6px;
  border: 1px dashed #D2B48C; /* 浅卡其色虚线边框 */
  margin: 15px 0;
}

.loading-icon {
  animation: rotating 2s linear infinite;
  margin-right: 8px;
  color: #8B4513; /* 深棕色图标 */
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>