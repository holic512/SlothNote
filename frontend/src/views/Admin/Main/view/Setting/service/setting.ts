/**
 * @file adminSettingService
 * @project SlothNote
 * @module 管理端 / 系统设置
 * @description 封装系统设置页的 AI 配置接口请求。
 * @logic 1. 读取脱敏 AI 配置；2. 提交管理员编辑后的 AI 配置；3. 保持 apiKey 空值由后端保留旧值。
 * @dependencies Axios: @/axios, API: /admin/setting/aiConfig
 * @index_tags 管理端设置, AI配置, 接口请求, 脱敏密钥
 * @author holic512
 */
import axios from '@/axios'

export interface AiConfigView {
  providerName: string
  baseUrl: string
  model: string
  temperature: number
  maxTokens: number
  plannerTemperature: number
  plannerMaxTokens: number
  enabled: boolean
  hasApiKey: boolean
  maskedApiKey?: string | null
}

export interface AiConfigUpdatePayload {
  providerName: string
  baseUrl: string
  apiKey?: string
  model: string
  temperature: number
  maxTokens: number
  plannerTemperature: number
  plannerMaxTokens: number
  enabled: boolean
}

export async function fetchAiConfig(): Promise<AiConfigView> {
  const response = await axios.get('/admin/setting/aiConfig')
  return response.data.data
}

export async function updateAiConfig(payload: AiConfigUpdatePayload): Promise<{ status: number; message: string; data?: AiConfigView }> {
  const response = await axios.put('/admin/setting/aiConfig', payload)
  return {
    status: response.data.status,
    message: response.data.message,
    data: response.data.data,
  }
}
