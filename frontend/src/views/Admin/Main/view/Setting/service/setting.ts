/**
 * @file adminSettingService
 * @project SlothNote
 * @module 管理端 / 系统设置
 * @description 封装系统设置页的 AI 配置、邮箱配置与测试接口请求。
 * @logic 1. 读取脱敏 AI/邮箱配置；2. 提交管理员编辑后的配置；3. 触发不落库的 AI/邮箱测试。
 * @dependencies Axios: @/axios, API: /admin/setting/aiConfig and /admin/setting/mailConfig
 * @index_tags 管理端设置, AI配置, 邮箱配置, 接口请求, 脱敏密钥
 * @author holic512
 */
import axios from '@/axios'

export interface ApiResult<T = unknown> {
  status: number
  message: string
  data?: T
}

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

export interface AiConfigTestPayload extends AiConfigUpdatePayload {
  prompt: string
}

export interface AiConfigTestResult {
  reply: string
}

export interface MailConfigView {
  host: string
  port: number
  username: string
  protocol: string
  defaultEncoding: string
  fromAddress: string
  fromName: string
  smtpAuth: boolean
  sslEnable: boolean
  starttlsEnable: boolean
  starttlsRequired: boolean
  enabled: boolean
  hasPassword: boolean
  maskedPassword?: string | null
}

export interface MailConfigUpdatePayload {
  host: string
  port: number
  username: string
  password?: string
  protocol: string
  defaultEncoding: string
  fromAddress: string
  fromName: string
  smtpAuth: boolean
  sslEnable: boolean
  starttlsEnable: boolean
  starttlsRequired: boolean
  enabled: boolean
}

export interface MailConfigTestPayload extends MailConfigUpdatePayload {
  testRecipient: string
}

export async function fetchAiConfig(): Promise<AiConfigView> {
  const response = await axios.get('/admin/setting/aiConfig')
  return response.data.data
}

export async function updateAiConfig(payload: AiConfigUpdatePayload): Promise<ApiResult<AiConfigView>> {
  const response = await axios.put('/admin/setting/aiConfig', payload)
  return response.data
}

export async function testAiConfig(payload: AiConfigTestPayload): Promise<ApiResult<AiConfigTestResult>> {
  const response = await axios.post('/admin/setting/aiConfig/test', payload)
  return response.data
}

export async function fetchMailConfig(): Promise<MailConfigView> {
  const response = await axios.get('/admin/setting/mailConfig')
  return response.data.data
}

export async function updateMailConfig(payload: MailConfigUpdatePayload): Promise<ApiResult<MailConfigView>> {
  const response = await axios.put('/admin/setting/mailConfig', payload)
  return response.data
}

export async function testMailConfig(payload: MailConfigTestPayload): Promise<ApiResult> {
  const response = await axios.post('/admin/setting/mailConfig/test', payload)
  return response.data
}
