<!--
@file AdminSettingPage
@project SlothNote
@module 管理端 / 系统设置
@description 管理管理员资料、系统初始化和 AI 服务配置。
@logic 1. 读取并保存管理员邮箱；2. 展示用户数据统计并执行初始化；3. 维护数据库中的 AI 供应商配置。
@dependencies API: /admin/setting/profile, /admin/setting/systemReset, /admin/setting/aiConfig
@index_tags 管理端设置, 系统初始化, AI配置, 管理员资料
@author holic512
-->
<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import axios from '@/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { fetchAiConfig, updateAiConfig, type AiConfigUpdatePayload, type AiConfigView } from './service/setting'

type TableSummary = {
  tableName: string
  count: number
}

type AdminProfile = {
  id?: number
  username?: string
  email?: string | null
  hasEmail?: boolean
}

const loading = ref(false)
const resetting = ref(false)
const profileLoading = ref(false)
const profileSaving = ref(false)
const aiConfigLoading = ref(false)
const aiConfigSaving = ref(false)
const confirmText = ref('')
const requiredConfirmText = ref('INITIALIZE')
const tables = ref<TableSummary[]>([])
const profile = ref<AdminProfile>({})
const profileEmail = ref('')
const aiConfig = ref<AiConfigView | null>(null)
const aiForm = ref<AiConfigUpdatePayload>({
  providerName: 'openai-compatible',
  baseUrl: '',
  apiKey: '',
  model: '',
  temperature: 0.7,
  maxTokens: 4096,
  plannerTemperature: 0.1,
  plannerMaxTokens: 256,
  enabled: false,
})

const totalRecords = computed(() => tables.value.reduce((sum, item) => sum + (item.count || 0), 0))
const canReset = computed(() => confirmText.value.trim() === requiredConfirmText.value && !resetting.value)
const apiKeyStatusText = computed(() => {
  if (!aiConfig.value?.hasApiKey) {
    return '未配置'
  }
  return aiConfig.value.maskedApiKey || '已配置'
})

const loadProfile = async () => {
  profileLoading.value = true
  try {
    const response = await axios.get('/admin/setting/profile')
    if (response.data.status === 200) {
      profile.value = response.data.data || {}
      profileEmail.value = response.data.data?.email || ''
      return
    }
    ElMessage.error(response.data.message || '无法获取管理员资料')
  } catch {
    ElMessage.error('无法获取管理员资料')
  } finally {
    profileLoading.value = false
  }
}

const saveProfile = async () => {
  profileSaving.value = true
  try {
    const response = await axios.put('/admin/setting/profile', {
      email: profileEmail.value.trim(),
    })
    if (response.data.status === 200) {
      profile.value = response.data.data || {}
      profileEmail.value = response.data.data?.email || ''
      ElMessage.success('管理员邮箱已更新')
      return
    }
    ElMessage.error(response.data.message || '管理员资料更新失败')
  } catch {
    ElMessage.error('管理员资料更新失败')
  } finally {
    profileSaving.value = false
  }
}

const applyAiConfigToForm = (data: AiConfigView) => {
  aiConfig.value = data
  aiForm.value = {
    providerName: data.providerName || 'openai-compatible',
    baseUrl: data.baseUrl || '',
    apiKey: '',
    model: data.model || '',
    temperature: data.temperature ?? 0.7,
    maxTokens: data.maxTokens ?? 4096,
    plannerTemperature: data.plannerTemperature ?? 0.1,
    plannerMaxTokens: data.plannerMaxTokens ?? 256,
    enabled: Boolean(data.enabled),
  }
}

const loadAiConfig = async () => {
  aiConfigLoading.value = true
  try {
    const data = await fetchAiConfig()
    applyAiConfigToForm(data)
  } catch {
    ElMessage.error('无法获取 AI 配置')
  } finally {
    aiConfigLoading.value = false
  }
}

const saveAiConfig = async () => {
  aiConfigSaving.value = true
  try {
    const result = await updateAiConfig({
      ...aiForm.value,
      providerName: aiForm.value.providerName.trim() || 'openai-compatible',
      baseUrl: aiForm.value.baseUrl.trim(),
      apiKey: aiForm.value.apiKey?.trim() || '',
      model: aiForm.value.model.trim(),
    })
    if (result.status === 200 && result.data) {
      applyAiConfigToForm(result.data)
      ElMessage.success('AI 配置已保存')
      return
    }
    ElMessage.error(result.message || 'AI 配置保存失败')
  } catch {
    ElMessage.error('AI 配置保存失败')
  } finally {
    aiConfigSaving.value = false
  }
}

const loadSummary = async () => {
  loading.value = true
  try {
    const response = await axios.get('/admin/setting/systemReset/summary')
    if (response.data.status === 200) {
      requiredConfirmText.value = response.data.data.confirmText || 'INITIALIZE'
      tables.value = response.data.data.tables || []
      return
    }
    ElMessage.error(response.data.message || '无法获取系统统计')
  } catch {
    ElMessage.error('无法获取系统统计')
  } finally {
    loading.value = false
  }
}

const executeReset = async () => {
  if (!canReset.value) {
    ElMessage.warning(`请输入确认词 ${requiredConfirmText.value}`)
    return
  }

  try {
    await ElMessageBox.confirm(
      '该操作会清空所有用户相关业务数据，包括用户、资料、文件夹、笔记、正文、评论、收藏、待办、AI 对话和票据记录，管理员账号不会被删除。',
      '确认初始化系统',
      {
        type: 'warning',
        confirmButtonText: '继续初始化',
        cancelButtonText: '取消',
      }
    )
  } catch {
    return
  }

  resetting.value = true
  try {
    const response = await axios.post('/admin/setting/systemReset', {
      confirmText: confirmText.value.trim(),
    })
    if (response.data.status === 200) {
      ElMessage.success('系统初始化完成')
      confirmText.value = ''
      tables.value = response.data.data.tables || []
      return
    }
    ElMessage.error(response.data.message || '系统初始化失败')
  } catch {
    ElMessage.error('系统初始化失败')
  } finally {
    resetting.value = false
  }
}

onMounted(async () => {
  await Promise.all([loadProfile(), loadSummary(), loadAiConfig()])
})
</script>

<template>
  <el-scrollbar height="100%" class="setting-scrollbar">
    <div class="setting-page">
      <section class="hero-card">
        <div>
          <p class="hero-text">
            用于处理迁移后的历史脏数据。该功能会重置所有用户相关表数据，保留管理员账号与管理员表。
          </p>
        </div>
        <Button icon="pi pi-refresh" text rounded :loading="loading" @click="loadSummary" />
      </section>

      <section class="stats-card">
        <div class="stats-header">
          <div>
            <h2>管理员资料</h2>
            <p>邮箱为可选项。留空时不启用管理员邮箱验证码，后续可随时补录。</p>
          </div>
          <Button icon="pi pi-user-edit" text rounded :loading="profileLoading" @click="loadProfile" />
        </div>

        <div class="profile-grid">
          <div class="profile-item">
            <label>管理员账号</label>
            <InputText :model-value="profile.username || '-'" disabled />
          </div>
          <div class="profile-item">
            <label>邮箱（可选）</label>
            <InputText v-model="profileEmail" placeholder="admin@example.com" />
          </div>
        </div>

        <div class="profile-actions">
          <span class="profile-tip">{{ profile.hasEmail ? '当前已配置邮箱，可继续修改。' : '当前未配置邮箱，管理员将保持单步密码登录。' }}</span>
          <Button label="保存邮箱" :loading="profileSaving" @click="saveProfile" />
        </div>
      </section>

      <section class="stats-card">
        <div class="stats-header">
          <div>
            <h2>AI 服务配置</h2>
            <p>配置 OpenAI 兼容接口后，用户端 AI 对话、工具规划和笔记助手将使用这里的参数。</p>
          </div>
          <Button icon="pi pi-cloud" text rounded :loading="aiConfigLoading" @click="loadAiConfig" />
        </div>

        <div class="ai-config-grid">
          <div class="profile-item">
            <label>供应商标识</label>
            <InputText v-model="aiForm.providerName" placeholder="openai-compatible" />
          </div>
          <div class="profile-item ai-wide">
            <label>Base URL</label>
            <InputText v-model="aiForm.baseUrl" placeholder="https://example.com/v1" />
          </div>
          <div class="profile-item">
            <label>模型名称</label>
            <InputText v-model="aiForm.model" placeholder="gpt-4o-mini" />
          </div>
          <div class="profile-item">
            <label>API Key</label>
            <InputText v-model="aiForm.apiKey" type="password" autocomplete="new-password" placeholder="留空表示保留原 Key" />
          </div>
          <div class="profile-item">
            <label>回答温度</label>
            <el-input-number v-model="aiForm.temperature" :min="0" :max="2" :step="0.1" :precision="2" controls-position="right" />
          </div>
          <div class="profile-item">
            <label>回答最大 Token</label>
            <el-input-number v-model="aiForm.maxTokens" :min="1" :step="256" controls-position="right" />
          </div>
          <div class="profile-item">
            <label>规划温度</label>
            <el-input-number v-model="aiForm.plannerTemperature" :min="0" :max="2" :step="0.1" :precision="2" controls-position="right" />
          </div>
          <div class="profile-item">
            <label>规划最大 Token</label>
            <el-input-number v-model="aiForm.plannerMaxTokens" :min="1" :step="64" controls-position="right" />
          </div>
        </div>

        <div class="ai-config-actions">
          <div class="ai-status">
            <el-switch v-model="aiForm.enabled" active-text="启用" inactive-text="停用" />
            <el-tag :type="aiConfig?.hasApiKey ? 'success' : 'info'" effect="plain">Key: {{ apiKeyStatusText }}</el-tag>
          </div>
          <Button label="保存 AI 配置" icon="pi pi-save" :loading="aiConfigSaving" @click="saveAiConfig" />
        </div>
      </section>

      <section class="stats-card">
        <div class="stats-header">
          <div>
            <h2>当前用户数据概览</h2>
            <p>执行初始化前，先确认即将清空的数据体量。</p>
          </div>
          <div class="total-pill">
            <span class="label">总记录数</span>
            <span class="value">{{ totalRecords }}</span>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-item" v-for="item in tables" :key="item.tableName">
            <span class="table-name">{{ item.tableName }}</span>
            <span class="count">{{ item.count }}</span>
          </div>
        </div>
      </section>

      <section class="danger-card">
        <div class="danger-head">
          <div class="danger-mark">!</div>
          <div>
            <h2>危险操作区</h2>
            <p>
              初始化后将清空 `users` 以及关联的资料、笔记、正文、评论、收藏、待办、AI 会话和票据数据。
            </p>
          </div>
        </div>

        <div class="danger-body">
          <div class="confirm-box">
            <label>请输入确认词</label>
            <div class="confirm-row">
              <InputText v-model="confirmText" :placeholder="`输入 ${requiredConfirmText}`" />
              <span class="confirm-tag">{{ requiredConfirmText }}</span>
            </div>
          </div>

          <div class="actions">
            <Button
              label="初始化系统"
              severity="danger"
              icon="pi pi-exclamation-triangle"
              :loading="resetting"
              :disabled="!canReset"
              @click="executeReset"
            />
          </div>
        </div>
      </section>
    </div>
  </el-scrollbar>
</template>

<style scoped>
.setting-scrollbar {
  background:
    radial-gradient(circle at top right, rgba(196, 48, 43, 0.12), transparent 28%),
    linear-gradient(180deg, #f7f3ec 0%, #efe8db 100%);
}

.setting-page {
  min-height: 100%;
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card,
.stats-card,
.danger-card {
  border-radius: 24px;
  padding: 24px;
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid rgba(74, 58, 48, 0.08);
  box-shadow: 0 16px 40px rgba(82, 62, 40, 0.08);
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.hero-card h1,
.stats-header h2,
.danger-head h2 {
  margin: 0;
  color: #2a211b;
}

.hero-text {
  margin: 0;
  color: #67584d;
  line-height: 1.7;
}

.stats-header p,
.danger-head p {
  margin: 10px 0 0;
  color: #67584d;
  line-height: 1.7;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.total-pill {
  min-width: 132px;
  padding: 14px 18px;
  border-radius: 18px;
  background: #231913;
  color: #fff8f2;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.total-pill .label {
  font-size: 12px;
  opacity: 0.72;
}

.total-pill .value {
  font-size: 28px;
  font-weight: 700;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.profile-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.ai-config-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 14px;
}

.ai-wide {
  grid-column: span 2;
}

.profile-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-item label {
  font-size: 13px;
  color: #756456;
}

.profile-actions {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.ai-config-actions {
  margin-top: 18px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.ai-status {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.profile-tip {
  color: #67584d;
  font-size: 13px;
  line-height: 1.6;
}

.stat-item {
  border-radius: 18px;
  padding: 16px;
  background: linear-gradient(180deg, #fff, #f5efe6);
  border: 1px solid rgba(113, 86, 60, 0.08);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.table-name {
  font-size: 13px;
  color: #756456;
  word-break: break-all;
}

.count {
  font-size: 28px;
  font-weight: 700;
  color: #2a211b;
}

.danger-card {
  background: linear-gradient(180deg, rgba(87, 18, 18, 0.95), rgba(47, 10, 10, 0.98));
  color: #fff5f2;
}

.danger-head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.danger-mark {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.16);
  font-size: 24px;
  font-weight: 700;
}

.danger-body {
  margin-top: 22px;
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: flex-end;
  justify-content: space-between;
}

.confirm-box {
  flex: 1;
  min-width: 260px;
}

.confirm-box label {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  color: rgba(255, 245, 242, 0.86);
}

.confirm-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.confirm-row :deep(.p-inputtext) {
  flex: 1;
  min-height: 42px;
  border-radius: 14px;
  border: none;
}

.confirm-tag {
  min-width: 106px;
  text-align: center;
  padding: 10px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
  font-size: 12px;
  letter-spacing: 0.08em;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .setting-page {
    padding: 16px;
  }

  .hero-card,
  .stats-header,
  .ai-config-actions,
  .danger-body,
  .confirm-row {
    flex-direction: column;
    align-items: stretch;
  }

  .total-pill,
  .ai-wide,
  .actions :deep(.p-button) {
    width: 100%;
  }

  .ai-wide {
    grid-column: auto;
  }
}
</style>
