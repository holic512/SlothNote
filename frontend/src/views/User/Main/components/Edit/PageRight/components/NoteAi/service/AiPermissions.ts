/**
 * @file AiPermissions
 * @project SlothNote
 * @module 用户端 / AI 全局权限状态
 * @description 缓存并更新当前登录用户对其全部笔记授予 AI 的全局能力开关。
 * @logic 1. 从服务端加载用户级默认或已保存权限；2. 保存前乐观更新界面；3. 保存失败时回滚；4. 退出登录时清空状态。
 * @dependencies Pinia, Axios
 * @index_tags 用户AI, 全局权限, 设置同步, 状态回滚
 * @author holic512
 */
import {defineStore} from 'pinia'
import {ref} from 'vue'
import axios from '@/axios'

export interface AiPermissions {
  canReadAllNotes: boolean
  canWriteNoteContent: boolean
  canWriteNoteTitle: boolean
  canWriteNoteSummary: boolean
  canWriteNoteCover: boolean
}

const defaultPermissions = (): AiPermissions => ({
  canReadAllNotes: true,
  canWriteNoteContent: false,
  canWriteNoteTitle: false,
  canWriteNoteSummary: false,
  canWriteNoteCover: false
})

const normalizePermissions = (value?: Partial<AiPermissions> | null): AiPermissions => ({
  canReadAllNotes: value?.canReadAllNotes === undefined ? true : value.canReadAllNotes === true,
  canWriteNoteContent: value?.canWriteNoteContent === true,
  canWriteNoteTitle: value?.canWriteNoteTitle === true,
  canWriteNoteSummary: value?.canWriteNoteSummary === true,
  canWriteNoteCover: value?.canWriteNoteCover === true
})

export const useAiPermissionStore = defineStore('aiPermissions', () => {
  const permissions = ref<AiPermissions>(defaultPermissions())
  const loaded = ref(false)
  const loading = ref(false)
  const saving = ref(false)

  const loadPermissions = async (force = false) => {
    if (loaded.value && !force) return permissions.value
    loading.value = true
    try {
      const response = await axios.get('user/ai/permissions')
      permissions.value = normalizePermissions(response.data?.data)
      loaded.value = true
      return permissions.value
    } finally {
      loading.value = false
    }
  }

  const updatePermissions = async (nextPermissions: AiPermissions) => {
    const previous = permissions.value
    const normalized = normalizePermissions(nextPermissions)
    permissions.value = normalized
    saving.value = true
    try {
      const response = await axios.put('user/ai/permissions', normalized)
      permissions.value = normalizePermissions(response.data?.data)
      loaded.value = true
      return permissions.value
    } catch (error) {
      permissions.value = previous
      throw error
    } finally {
      saving.value = false
    }
  }

  const resetPermissions = () => {
    permissions.value = defaultPermissions()
    loaded.value = false
    loading.value = false
    saving.value = false
  }

  return {
    permissions,
    loaded,
    loading,
    saving,
    loadPermissions,
    updatePermissions,
    resetPermissions
  }
})
