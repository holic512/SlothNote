<!--
@file AdminSettingPage
@project SlothNote
@module 管理端 / 系统设置
@description 管理管理员资料、系统初始化、AI 服务配置和邮箱 SMTP 配置。
@logic 1. 使用 Element Plus Tabs 分组并在首次进入时按需加载；2. 维护数据库 AI/邮箱配置；3. 支持不落库的 AI 与邮箱测试。
@dependencies API: /admin/setting/profile, /admin/setting/systemReset, /admin/setting/aiConfig, /admin/setting/mailConfig
@index_tags 管理端设置, 系统初始化, AI配置, 邮箱配置, ElementPlus
@author holic512
-->
<script setup lang="ts">
import axios from '@/axios'
import { Check, Connection, Delete, Message, Refresh, User, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref, watch } from 'vue'
import {
  fetchAiConfig,
  fetchMailConfig,
  testAiConfig,
  testMailConfig,
  updateAiConfig,
  updateMailConfig,
  type AiConfigUpdatePayload,
  type AiConfigView,
  type MailConfigUpdatePayload,
  type MailConfigView,
} from './service/setting'

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

type SettingTab = 'profile' | 'ai' | 'mail' | 'reset'

const activeTab = ref<SettingTab>('profile')
const loadedTabs = new Set<SettingTab>()
const tabLoadTasks = new Map<SettingTab, Promise<boolean>>()
const loading = ref(false)
const resetting = ref(false)
const profileLoading = ref(false)
const profileSaving = ref(false)
const aiConfigLoading = ref(false)
const aiConfigSaving = ref(false)
const aiTesting = ref(false)
const mailConfigLoading = ref(false)
const mailConfigSaving = ref(false)
const mailTesting = ref(false)

const confirmText = ref('')
const requiredConfirmText = ref('INITIALIZE')
const tables = ref<TableSummary[]>([])
const profile = ref<AdminProfile>({})
const profileEmail = ref('')
const aiConfig = ref<AiConfigView | null>(null)
const mailConfig = ref<MailConfigView | null>(null)
const aiTestPrompt = ref('请用一句中文回复：AI 配置测试成功')
const aiTestReply = ref('')
const mailTestRecipient = ref('')

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

const mailForm = ref<MailConfigUpdatePayload>({
  host: '',
  port: 465,
  username: '',
  password: '',
  protocol: 'smtps',
  defaultEncoding: 'UTF-8',
  fromAddress: '',
  fromName: 'SlothNote',
  smtpAuth: true,
  sslEnable: true,
  starttlsEnable: true,
  starttlsRequired: true,
  enabled: false,
})

const totalRecords = computed(() => tables.value.reduce((sum, item) => sum + (item.count || 0), 0))
const canReset = computed(() => confirmText.value.trim() === requiredConfirmText.value && !resetting.value)
const apiKeyStatusText = computed(() => {
  if (!aiConfig.value?.hasApiKey) return '未配置'
  return aiConfig.value.maskedApiKey || '已配置'
})
const mailPasswordStatusText = computed(() => {
  if (!mailConfig.value?.hasPassword) return '未配置'
  return mailConfig.value.maskedPassword || '已配置'
})

const loadProfile = async (): Promise<boolean> => {
  profileLoading.value = true
  try {
    const response = await axios.get('/admin/setting/profile')
    if (response.data.status === 200) {
      profile.value = response.data.data || {}
      profileEmail.value = response.data.data?.email || ''
      return true
    }
    ElMessage.error(response.data.message || '无法获取管理员资料')
    return false
  } catch {
    ElMessage.error('无法获取管理员资料')
    return false
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

const applyMailConfigToForm = (data: MailConfigView) => {
  mailConfig.value = data
  mailForm.value = {
    host: data.host || '',
    port: data.port || 465,
    username: data.username || '',
    password: '',
    protocol: data.protocol || 'smtps',
    defaultEncoding: data.defaultEncoding || 'UTF-8',
    fromAddress: data.fromAddress || '',
    fromName: data.fromName || 'SlothNote',
    smtpAuth: data.smtpAuth ?? true,
    sslEnable: data.sslEnable ?? true,
    starttlsEnable: data.starttlsEnable ?? true,
    starttlsRequired: data.starttlsRequired ?? true,
    enabled: Boolean(data.enabled),
  }
}

const loadAiConfig = async (): Promise<boolean> => {
  aiConfigLoading.value = true
  try {
    applyAiConfigToForm(await fetchAiConfig())
    return true
  } catch {
    ElMessage.error('无法获取 AI 配置')
    return false
  } finally {
    aiConfigLoading.value = false
  }
}

const loadMailConfig = async (): Promise<boolean> => {
  mailConfigLoading.value = true
  try {
    applyMailConfigToForm(await fetchMailConfig())
    return true
  } catch {
    ElMessage.error('无法获取邮箱配置')
    return false
  } finally {
    mailConfigLoading.value = false
  }
}

const buildAiPayload = (): AiConfigUpdatePayload => ({
  ...aiForm.value,
  providerName: aiForm.value.providerName.trim() || 'openai-compatible',
  baseUrl: aiForm.value.baseUrl.trim(),
  apiKey: aiForm.value.apiKey?.trim() || '',
  model: aiForm.value.model.trim(),
})

const buildMailPayload = (): MailConfigUpdatePayload => ({
  ...mailForm.value,
  host: mailForm.value.host.trim(),
  username: mailForm.value.username.trim(),
  password: mailForm.value.password?.trim() || '',
  protocol: mailForm.value.protocol.trim() || 'smtps',
  defaultEncoding: mailForm.value.defaultEncoding.trim() || 'UTF-8',
  fromAddress: mailForm.value.fromAddress.trim(),
  fromName: mailForm.value.fromName.trim(),
})

const saveAiConfig = async () => {
  aiConfigSaving.value = true
  try {
    const result = await updateAiConfig(buildAiPayload())
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

const runAiTest = async () => {
  aiTesting.value = true
  aiTestReply.value = ''
  try {
    const result = await testAiConfig({
      ...buildAiPayload(),
      prompt: aiTestPrompt.value.trim(),
    })
    if (result.status === 200) {
      aiTestReply.value = result.data?.reply || ''
      ElMessage.success('AI 配置测试成功')
      return
    }
    ElMessage.error(result.message || 'AI 配置测试失败')
  } catch {
    ElMessage.error('AI 配置测试失败')
  } finally {
    aiTesting.value = false
  }
}

const saveMailConfig = async () => {
  mailConfigSaving.value = true
  try {
    const result = await updateMailConfig(buildMailPayload())
    if (result.status === 200 && result.data) {
      applyMailConfigToForm(result.data)
      ElMessage.success('邮箱配置已保存')
      return
    }
    ElMessage.error(result.message || '邮箱配置保存失败')
  } catch {
    ElMessage.error('邮箱配置保存失败')
  } finally {
    mailConfigSaving.value = false
  }
}

const runMailTest = async () => {
  const recipient = mailTestRecipient.value.trim()
  if (!recipient) {
    ElMessage.warning('请输入测试收件人')
    return
  }

  mailTesting.value = true
  try {
    const result = await testMailConfig({
      ...buildMailPayload(),
      testRecipient: recipient,
    })
    if (result.status === 200) {
      ElMessage.success('测试邮件已发送')
      return
    }
    ElMessage.error(result.message || '测试邮件发送失败')
  } catch {
    ElMessage.error('测试邮件发送失败')
  } finally {
    mailTesting.value = false
  }
}

const loadSummary = async (): Promise<boolean> => {
  loading.value = true
  try {
    const response = await axios.get('/admin/setting/systemReset/summary')
    if (response.data.status === 200) {
      requiredConfirmText.value = response.data.data.confirmText || 'INITIALIZE'
      tables.value = response.data.data.tables || []
      return true
    }
    ElMessage.error(response.data.message || '无法获取系统统计')
    return false
  } catch {
    ElMessage.error('无法获取系统统计')
    return false
  } finally {
    loading.value = false
  }
}

const loadTabData = (tab: SettingTab): Promise<boolean> => {
  if (tab === 'profile') return loadProfile()
  if (tab === 'ai') return loadAiConfig()
  if (tab === 'mail') return loadMailConfig()
  return loadSummary()
}

const loadTab = async (tab: SettingTab, force = false): Promise<boolean> => {
  if (!force && loadedTabs.has(tab)) return true

  const existingTask = tabLoadTasks.get(tab)
  if (existingTask) return existingTask

  const task = loadTabData(tab)
      .then((loaded) => {
        if (loaded) loadedTabs.add(tab)
        return loaded
      })
      .finally(() => {
        if (tabLoadTasks.get(tab) === task) tabLoadTasks.delete(tab)
      })

  tabLoadTasks.set(tab, task)
  return task
}

const reloadCurrentTab = async () => {
  await loadTab(activeTab.value, true)
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

watch(activeTab, (tab) => {
  void loadTab(tab)
})

onMounted(() => {
  void loadTab(activeTab.value)
})
</script>

<template>
  <el-scrollbar height="100%" class="setting-scrollbar">
    <div class="setting-page">
      <header class="setting-header">
        <div>
          <h1>系统设置</h1>
          <p>集中维护管理员资料、AI 服务、邮箱 SMTP 和初始化操作。</p>
        </div>
        <el-button :icon="Refresh" :loading="loading || profileLoading || aiConfigLoading || mailConfigLoading" @click="reloadCurrentTab">
          刷新当前页
        </el-button>
      </header>

      <el-tabs v-model="activeTab" class="setting-tabs">
        <el-tab-pane name="profile">
          <template #label>
            <span class="tab-label"><el-icon><User /></el-icon>管理员资料</span>
          </template>

          <section class="setting-panel" v-loading="profileLoading">
            <div class="panel-head">
              <div>
                <h2>管理员资料</h2>
                <p>{{ profile.hasEmail ? '当前管理员已配置邮箱。' : '当前管理员未配置邮箱。' }}</p>
              </div>
              <el-tag :type="profile.hasEmail ? 'success' : 'info'" effect="plain">
                {{ profile.hasEmail ? '邮箱已配置' : '邮箱未配置' }}
              </el-tag>
            </div>

            <el-form label-position="top" class="settings-form">
              <div class="form-grid two">
                <el-form-item label="管理员账号">
                  <el-input :model-value="profile.username || '-'" disabled />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileEmail" placeholder="admin@example.com" clearable />
                </el-form-item>
              </div>
              <div class="form-actions">
                <el-button :icon="Refresh" @click="loadProfile">重新加载</el-button>
                <el-button type="primary" :icon="Check" :loading="profileSaving" @click="saveProfile">保存资料</el-button>
              </div>
            </el-form>
          </section>
        </el-tab-pane>

        <el-tab-pane name="ai">
          <template #label>
            <span class="tab-label"><el-icon><Connection /></el-icon>AI 配置</span>
          </template>

          <section class="setting-panel" v-loading="aiConfigLoading">
            <div class="panel-head">
              <div>
                <h2>AI 服务配置</h2>
                <p>用户端 AI 对话、工具规划和笔记助手使用这组参数。</p>
              </div>
              <div class="status-row">
                <el-switch v-model="aiForm.enabled" active-text="启用" inactive-text="停用" />
                <el-tag :type="aiConfig?.hasApiKey ? 'success' : 'info'" effect="plain">Key: {{ apiKeyStatusText }}</el-tag>
              </div>
            </div>

            <el-form label-position="top" class="settings-form">
              <div class="form-grid">
                <el-form-item label="供应商标识">
                  <el-input v-model="aiForm.providerName" placeholder="openai-compatible" />
                </el-form-item>
                <el-form-item label="Base URL" class="wide">
                  <el-input v-model="aiForm.baseUrl" placeholder="https://example.com/v1" />
                </el-form-item>
                <el-form-item label="模型名称">
                  <el-input v-model="aiForm.model" placeholder="gpt-4o-mini" />
                </el-form-item>
                <el-form-item label="API Key">
                  <el-input v-model="aiForm.apiKey" type="password" show-password autocomplete="new-password" placeholder="留空保留原 Key" />
                </el-form-item>
                <el-form-item label="回答温度">
                  <el-input-number v-model="aiForm.temperature" :min="0" :max="2" :step="0.1" :precision="2" controls-position="right" />
                </el-form-item>
                <el-form-item label="回答最大 Token">
                  <el-input-number v-model="aiForm.maxTokens" :min="1" :step="256" controls-position="right" />
                </el-form-item>
                <el-form-item label="规划温度">
                  <el-input-number v-model="aiForm.plannerTemperature" :min="0" :max="2" :step="0.1" :precision="2" controls-position="right" />
                </el-form-item>
                <el-form-item label="规划最大 Token">
                  <el-input-number v-model="aiForm.plannerMaxTokens" :min="1" :step="64" controls-position="right" />
                </el-form-item>
              </div>

              <div class="test-box">
                <el-form-item label="测试提示词">
                  <el-input v-model="aiTestPrompt" type="textarea" :rows="3" maxlength="500" show-word-limit />
                </el-form-item>
                <el-alert v-if="aiTestReply" type="success" :closable="false" show-icon>
                  <template #title>{{ aiTestReply }}</template>
                </el-alert>
              </div>

              <div class="form-actions">
                <el-button :icon="Refresh" @click="loadAiConfig">重新加载</el-button>
                <el-button :icon="Connection" :loading="aiTesting" @click="runAiTest">测试 AI</el-button>
                <el-button type="primary" :icon="Check" :loading="aiConfigSaving" @click="saveAiConfig">保存 AI 配置</el-button>
              </div>
            </el-form>
          </section>
        </el-tab-pane>

        <el-tab-pane name="mail">
          <template #label>
            <span class="tab-label"><el-icon><Message /></el-icon>邮箱配置</span>
          </template>

          <section class="setting-panel" v-loading="mailConfigLoading">
            <div class="panel-head">
              <div>
                <h2>邮箱 SMTP 配置</h2>
                <p>用户验证码与管理员二次验证邮件使用这组参数。</p>
              </div>
              <div class="status-row">
                <el-switch v-model="mailForm.enabled" active-text="启用" inactive-text="停用" />
                <el-tag :type="mailConfig?.hasPassword ? 'success' : 'info'" effect="plain">密码: {{ mailPasswordStatusText }}</el-tag>
              </div>
            </div>

            <el-form label-position="top" class="settings-form">
              <div class="form-grid">
                <el-form-item label="SMTP Host">
                  <el-input v-model="mailForm.host" placeholder="smtp.example.com" />
                </el-form-item>
                <el-form-item label="SMTP Port">
                  <el-input-number v-model="mailForm.port" :min="1" :max="65535" controls-position="right" />
                </el-form-item>
                <el-form-item label="用户名">
                  <el-input v-model="mailForm.username" placeholder="notice@example.com" />
                </el-form-item>
                <el-form-item label="密码 / 授权码">
                  <el-input v-model="mailForm.password" type="password" show-password autocomplete="new-password" placeholder="留空保留原密码" />
                </el-form-item>
                <el-form-item label="协议">
                  <el-select v-model="mailForm.protocol">
                    <el-option label="smtps" value="smtps" />
                    <el-option label="smtp" value="smtp" />
                  </el-select>
                </el-form-item>
                <el-form-item label="默认编码">
                  <el-input v-model="mailForm.defaultEncoding" placeholder="UTF-8" />
                </el-form-item>
                <el-form-item label="发件人邮箱">
                  <el-input v-model="mailForm.fromAddress" placeholder="notice@example.com" />
                </el-form-item>
                <el-form-item label="发件人名称">
                  <el-input v-model="mailForm.fromName" placeholder="SlothNote" />
                </el-form-item>
              </div>

              <div class="switch-grid">
                <el-checkbox v-model="mailForm.smtpAuth">SMTP 认证</el-checkbox>
                <el-checkbox v-model="mailForm.sslEnable">SSL</el-checkbox>
                <el-checkbox v-model="mailForm.starttlsEnable">STARTTLS</el-checkbox>
                <el-checkbox v-model="mailForm.starttlsRequired">要求 STARTTLS</el-checkbox>
              </div>

              <div class="test-row">
                <el-input v-model="mailTestRecipient" placeholder="测试收件人邮箱" clearable />
                <el-button :icon="Message" :loading="mailTesting" @click="runMailTest">发送测试邮件</el-button>
              </div>

              <div class="form-actions">
                <el-button :icon="Refresh" @click="loadMailConfig">重新加载</el-button>
                <el-button type="primary" :icon="Check" :loading="mailConfigSaving" @click="saveMailConfig">保存邮箱配置</el-button>
              </div>
            </el-form>
          </section>
        </el-tab-pane>

        <el-tab-pane name="reset">
          <template #label>
            <span class="tab-label"><el-icon><Warning /></el-icon>系统初始化</span>
          </template>

          <section class="setting-panel danger" v-loading="loading">
            <div class="panel-head">
              <div>
                <h2>用户数据概览</h2>
                <p>初始化会清空用户业务数据，管理员账号会保留。</p>
              </div>
              <div class="total-pill">
                <span>总记录数</span>
                <strong>{{ totalRecords }}</strong>
              </div>
            </div>

            <el-table :data="tables" border size="small" class="summary-table">
              <el-table-column prop="tableName" label="数据表" min-width="180" />
              <el-table-column prop="count" label="记录数" width="140" align="right" />
            </el-table>

            <div class="danger-zone">
              <div>
                <h3>危险操作</h3>
                <p>该操作会清空用户、资料、文件夹、笔记、正文、评论、收藏、待办、AI 会话和票据记录。</p>
              </div>
              <div class="confirm-row">
                <el-input v-model="confirmText" :placeholder="`输入 ${requiredConfirmText}`" />
                <el-tag effect="plain">{{ requiredConfirmText }}</el-tag>
                <el-button type="danger" :icon="Delete" :loading="resetting" :disabled="!canReset" @click="executeReset">
                  初始化系统
                </el-button>
              </div>
            </div>
          </section>
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-scrollbar>
</template>

<style scoped>
.setting-scrollbar {
  background: #f5f7fb;
}

.setting-page {
  min-height: 100%;
  padding: 24px;
  color: #1f2937;
}

.setting-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.setting-header h1,
.panel-head h2,
.danger-zone h3 {
  margin: 0;
  color: #111827;
}

.setting-header p,
.panel-head p,
.danger-zone p {
  margin: 6px 0 0;
  color: #667085;
  line-height: 1.6;
}

.setting-tabs {
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  padding: 14px 18px 18px;
}

.tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.setting-panel {
  padding: 10px 0 0;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eef0f4;
  margin-bottom: 18px;
}

.status-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  align-items: center;
}

.settings-form {
  max-width: 1080px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 2px 18px;
}

.form-grid.two {
  max-width: 760px;
}

.wide {
  grid-column: span 2;
}

.settings-form :deep(.el-input-number),
.settings-form :deep(.el-select) {
  width: 100%;
}

.switch-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(120px, 1fr));
  gap: 10px;
  padding: 12px 14px;
  margin: 4px 0 18px;
  border: 1px solid #eef0f4;
  border-radius: 8px;
  background: #fafbfc;
}

.test-box {
  margin-top: 4px;
}

.test-row {
  display: grid;
  grid-template-columns: minmax(220px, 420px) auto;
  gap: 12px;
  align-items: center;
  margin: 4px 0 18px;
}

.form-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 4px;
}

.total-pill {
  min-width: 132px;
  padding: 10px 14px;
  border-radius: 8px;
  background: #111827;
  color: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.total-pill span {
  font-size: 12px;
  opacity: 0.76;
}

.total-pill strong {
  font-size: 24px;
  line-height: 1.2;
}

.summary-table {
  width: 100%;
  margin-bottom: 18px;
}

.danger-zone {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) minmax(320px, 520px);
  gap: 18px;
  align-items: end;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid #fecaca;
  background: #fff7f7;
}

.danger-zone h3 {
  color: #991b1b;
}

.confirm-row {
  display: grid;
  grid-template-columns: minmax(160px, 1fr) auto auto;
  gap: 10px;
  align-items: center;
}

@media (max-width: 900px) {
  .setting-page {
    padding: 16px;
  }

  .setting-header,
  .panel-head,
  .danger-zone {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }

  .form-grid,
  .switch-grid,
  .test-row,
  .confirm-row {
    grid-template-columns: 1fr;
  }

  .wide {
    grid-column: auto;
  }

  .form-actions {
    justify-content: stretch;
  }

  .form-actions .el-button,
  .test-row .el-button,
  .confirm-row .el-button {
    width: 100%;
  }
}
</style>
