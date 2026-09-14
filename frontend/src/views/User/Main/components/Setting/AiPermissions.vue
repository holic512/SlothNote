<!--
@file AiPermissions
@project SlothNote
@module 用户端 / AI 权限设置
@description 展示并更新当前用户对其全部笔记授予 AI 的全局权限开关。
@logic 1. 加载用户级 AI 权限；2. 每次切换提交完整权限对象；3. 保存失败由状态层回滚并提示用户。
@dependencies Store: AiPermissions, ElementPlus: el-switch
@index_tags 用户AI, 全局权限, 笔记读取, 笔记写入
@author holic512
-->
<script setup lang="ts">
import {onMounted} from 'vue'
import {ElMessage} from 'element-plus'
import {type AiPermissions, useAiPermissionStore} from '@/views/User/Main/components/Edit/PageRight/components/NoteAi/service/AiPermissions'

const aiPermissions = useAiPermissionStore()

const savePermission = async (key: keyof AiPermissions, value: string | number | boolean) => {
  try {
    await aiPermissions.updatePermissions({
      ...aiPermissions.permissions,
      [key]: value === true
    })
    ElMessage.success('AI 权限已保存')
  } catch (error) {
    ElMessage.error('AI 权限保存失败，已恢复原设置')
  }
}

onMounted(async () => {
  try {
    await aiPermissions.loadPermissions()
  } catch (error) {
    ElMessage.error('AI 权限加载失败，请稍后重试')
  }
})
</script>

<template>
  <section class="ai-permissions" v-loading="aiPermissions.loading">
    <h1>AI 权限</h1>
    <p class="intro">以下开关对你名下的全部笔记生效，不针对单独笔记保存。</p>
    <el-divider class="divider"/>

    <div class="permission-card">
      <div class="permission-copy">
        <h3>允许 AI 读取全部笔记</h3>
        <p>允许搜索、读取、关联和同步你名下笔记的内容与选中文本。</p>
      </div>
      <el-switch
        :model-value="aiPermissions.permissions.canReadAllNotes"
        :disabled="aiPermissions.saving"
        @update:model-value="savePermission('canReadAllNotes', $event)"
      />
    </div>

    <h2>允许 AI 修改笔记</h2>
    <div class="permission-list">
      <div class="permission-card">
        <div class="permission-copy">
          <h3>修改正文</h3>
          <p>允许 AI 替换选中文本、在选区后插入内容或追加正文。</p>
        </div>
        <el-switch :model-value="aiPermissions.permissions.canWriteNoteContent" :disabled="aiPermissions.saving" @update:model-value="savePermission('canWriteNoteContent', $event)"/>
      </div>
      <div class="permission-card">
        <div class="permission-copy">
          <h3>修改标题</h3>
          <p>允许 AI 根据你的自然语言要求更新当前笔记标题。</p>
        </div>
        <el-switch :model-value="aiPermissions.permissions.canWriteNoteTitle" :disabled="aiPermissions.saving" @update:model-value="savePermission('canWriteNoteTitle', $event)"/>
      </div>
      <div class="permission-card">
        <div class="permission-copy">
          <h3>修改简介</h3>
          <p>允许 AI 生成并保存当前笔记的简介。</p>
        </div>
        <el-switch :model-value="aiPermissions.permissions.canWriteNoteSummary" :disabled="aiPermissions.saving" @update:model-value="savePermission('canWriteNoteSummary', $event)"/>
      </div>
      <div class="permission-card">
        <div class="permission-copy">
          <h3>修改封面</h3>
          <p>允许 AI 从可用封面中选择并更新当前笔记封面。</p>
        </div>
        <el-switch :model-value="aiPermissions.permissions.canWriteNoteCover" :disabled="aiPermissions.saving" @update:model-value="savePermission('canWriteNoteCover', $event)"/>
      </div>
    </div>
  </section>
</template>

<style scoped>
.ai-permissions {
  padding-right: 4px;
}

h1, h2, h3, p {
  margin-top: 0;
}

h1 {
  margin-bottom: 8px;
  font-size: 28px;
  color: #1f2937;
}

h2 {
  margin: 24px 0 12px;
  font-size: 17px;
  color: #374151;
}

.intro {
  margin-bottom: 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.6;
}

.divider {
  margin: 14px 0 20px;
}

.permission-list {
  display: grid;
  gap: 10px;
}

.permission-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 16px 18px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
}

.permission-copy h3 {
  margin-bottom: 5px;
  font-size: 15px;
  color: #111827;
}

.permission-copy p {
  margin-bottom: 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.55;
}
</style>
