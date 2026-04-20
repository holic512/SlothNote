<script setup lang="ts">
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import axios from '@/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { computed, onMounted, ref } from 'vue'

type TableSummary = {
  tableName: string
  count: number
}

const loading = ref(false)
const resetting = ref(false)
const confirmText = ref('')
const requiredConfirmText = ref('INITIALIZE')
const tables = ref<TableSummary[]>([])

const totalRecords = computed(() => tables.value.reduce((sum, item) => sum + (item.count || 0), 0))
const canReset = computed(() => confirmText.value.trim() === requiredConfirmText.value && !resetting.value)

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
  await loadSummary()
})
</script>

<template>
  <el-scrollbar height="100%" class="setting-scrollbar">
    <div class="setting-page">
      <section class="hero-card">
        <div>
          <p class="eyebrow">System Settings</p>
          <h1>系统初始化</h1>
          <p class="hero-text">
            用于处理迁移后的历史脏数据。该功能会重置所有用户相关表数据，保留管理员账号与管理员表。
          </p>
        </div>
        <Button icon="pi pi-refresh" text rounded :loading="loading" @click="loadSummary" />
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

.eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #986c46;
}

.hero-card h1,
.stats-header h2,
.danger-head h2 {
  margin: 0;
  color: #2a211b;
}

.hero-text,
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
  .danger-body,
  .confirm-row {
    flex-direction: column;
    align-items: stretch;
  }

  .total-pill,
  .actions :deep(.p-button) {
    width: 100%;
  }
}
</style>
