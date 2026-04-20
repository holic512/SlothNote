import axios from '@/axios'

export interface NoteVersionRow {
  id: number
  versionNo: number
  contentPreview: string
  sourceType: string
  createdAt: string
}

export interface NoteVersionDetail {
  id: number
  versionNo: number
  contentJson: string
  contentPreview: string
  sourceType: string
  createdAt: string
}

export const fetchNoteVersions = async (noteId: number): Promise<NoteVersionRow[]> => {
  const response = await axios.get('user/note/versions', { params: { noteId } })
  return response.data?.data || []
}

export const fetchNoteVersionDetail = async (noteId: number, versionId: number): Promise<NoteVersionDetail | null> => {
  const response = await axios.get('user/note/version/detail', { params: { noteId, versionId } })
  return response.data?.status === 200 ? response.data.data : null
}

export const restoreNoteVersion = async (noteId: number, versionId: number) => {
  const response = await axios.post('user/note/RestoreVersion', { noteId, versionId })
  return response.data
}
