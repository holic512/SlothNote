import axios from '@/axios'
import type { ContextNote } from './AiChat'

type NoteSearchResult = {
  noteId: number
  title?: string | null
  summary?: string | null
  snippet?: string | null
  icon?: string | null
}

export const searchContextNotes = async (query: string, signal: AbortSignal): Promise<ContextNote[]> => {
  const response = await axios.get('user/note/search', {
    params: { q: query },
    signal
  })

  if (response.data?.status !== 200 || !Array.isArray(response.data.data)) {
    return []
  }

  return (response.data.data as NoteSearchResult[]).map(item => ({
    noteId: item.noteId,
    title: item.title || '未命名笔记',
    summary: item.summary || item.snippet || '',
    icon: item.icon || null
  }))
}
