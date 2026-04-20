import axios from '../../../../../../axios'

export interface AiSessionRow {
  id: number
  userId: number
  username: string | null
  email: string | null
  title: string
  isDeleted: number
  messageCount: number
  contextNoteCount: number
  lastMessageAt: string | null
  createdAt: string
  updatedAt: string
}

export interface AiMessageRow {
  id: number
  role: 'user' | 'assistant'
  messageType: string
  content: string
  status: string
  createdAt: string
}

export interface AiContextNote {
  noteId: number
  title: string
  summary?: string
  icon?: string | null
}

export interface AiSessionDetail {
  session: AiSessionRow
  messages: AiMessageRow[]
  contextNotes: AiContextNote[]
}

export interface AiSearchParams {
  q?: string
  userId?: number
  username?: string
  isDeleted?: number
  pageNum: number
  pageSize: number
}

export interface AiSearchResult {
  list: AiSessionRow[]
  total: number
}

export async function searchAiSessions(params: AiSearchParams): Promise<AiSearchResult> {
  const response = await axios.post('/admin/aiMm/search', params)
  return response.data.data
}

export async function fetchAiSessionDetail(id: number): Promise<AiSessionDetail> {
  const response = await axios.get('/admin/aiMm/detail', { params: { id } })
  return response.data.data
}

export async function deleteAiSession(id: number): Promise<{ status: number; message: string }> {
  const response = await axios.delete('/admin/aiMm/delete', { params: { id } })
  return { status: response.data.status, message: response.data.message }
}

export async function restoreAiSession(id: number): Promise<{ status: number; message: string }> {
  const response = await axios.post('/admin/aiMm/restore', { id })
  return { status: response.data.status, message: response.data.message }
}

export async function batchDeleteAiSessions(ids: number[]): Promise<{ status: number; message: string }> {
  const response = await axios.post('/admin/aiMm/batchDelete', ids)
  return { status: response.data.status, message: response.data.message }
}

export async function batchRestoreAiSessions(ids: number[]): Promise<{ status: number; message: string }> {
  const response = await axios.post('/admin/aiMm/batchRestore', ids)
  return { status: response.data.status, message: response.data.message }
}
