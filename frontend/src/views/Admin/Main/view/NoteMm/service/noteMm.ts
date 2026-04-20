import axios from '../../../../../../axios';

export interface UserOption {
  id: number;
  username: string;
  email: string;
}

export interface FolderOption {
  id: number;
  folderName: string;
  userId: number;
}

export interface NoteRow {
  id: number;
  userId: number;
  folderId: number | null;
  noteTitle: string;
  noteSummary: string | null;
  noteAvatar: string | null;
  noteCoverUrl: string | null;
  notePassword: string | null;
  noteType: number;
  isDeleted: number;
  hasContent: boolean;
  lastSavedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface NoteSearchParams {
  q?: string;
  userId?: number;
  folderId?: number;
  noteType?: number;
  isDeleted?: number;
  pageNum: number;
  pageSize: number;
}

export interface NoteSearchResult {
  list: NoteRow[];
  total: number;
}

export interface NoteDetailResponse {
  data: NoteRow;
  content: string;
}

export interface AddNotePayload {
  userId: number | null;
  folderId: number | null;
  noteTitle: string;
  noteSummary: string;
  noteAvatar?: string;
  noteCoverUrl?: string;
  notePassword?: string;
  noteType: number | null;
}

export interface UpdateNotePayload {
  id: number;
  userId: number | null;
  folderId: number | null;
  noteTitle: string;
  noteSummary: string | null;
  noteAvatar?: string | null;
  noteCoverUrl?: string | null;
  notePassword?: string | null;
  noteType: number | null;
  isDeleted: number;
}

export interface ContentMeta {
  hasContent: boolean;
  lastSavedAt: string | null;
}

export async function searchNotes(params: NoteSearchParams): Promise<NoteSearchResult> {
  const response = await axios.post('/admin/noteMm/search', params);
  return response.data.data;
}

export async function fetchNoteDetail(id: number): Promise<NoteRow> {
  const response = await axios.get('/admin/noteMm/detail', { params: { id } });
  return response.data.data;
}

export async function fetchNoteContent(noteId: number): Promise<string> {
  const response = await axios.get('/admin/noteMm/content/get', { params: { noteId } });
  return response.data.data || '';
}

export async function fetchNoteFullDetail(id: number): Promise<NoteDetailResponse> {
  const [data, content] = await Promise.all([fetchNoteDetail(id), fetchNoteContent(id)]);
  return { data, content };
}

export async function addNote(payload: AddNotePayload): Promise<{ status: number; message: string }> {
  const response = await axios.post('/admin/noteMm/add', payload);
  return { status: response.data.status, message: response.data.message };
}

export async function updateNote(payload: UpdateNotePayload): Promise<{ status: number; message: string }> {
  const response = await axios.put('/admin/noteMm/update', payload);
  return { status: response.data.status, message: response.data.message };
}

export async function deleteNote(id: number): Promise<{ status: number; message: string }> {
  const response = await axios.delete('/admin/noteMm/delete', { params: { id } });
  return { status: response.data.status, message: response.data.message };
}

export async function restoreNote(id: number): Promise<{ status: number; message: string }> {
  const response = await axios.post('/admin/noteMm/restore', { id });
  return { status: response.data.status, message: response.data.message };
}

export async function batchDeleteNotes(ids: number[]): Promise<{ status: number; message: string }> {
  const response = await axios.post('/admin/noteMm/batchDelete', ids);
  return { status: response.data.status, message: response.data.message };
}

export async function batchRestoreNotes(ids: number[]): Promise<{ status: number; message: string }> {
  const response = await axios.post('/admin/noteMm/batchRestore', ids);
  return { status: response.data.status, message: response.data.message };
}

export async function updateNoteContent(noteId: number, content: string): Promise<{ status: number; message: string; meta: ContentMeta | null }> {
  const response = await axios.post('/admin/noteMm/content/update', { noteId, content });
  return {
    status: response.data.status,
    message: response.data.message,
    meta: response.data.data ?? null,
  };
}

export async function fetchUserOptions(q?: string, limit: number = 50): Promise<UserOption[]> {
  const response = await axios.get('/admin/noteMm/userOptions', { params: { q, limit } });
  return response.data.data;
}

export async function fetchFolderOptions(q?: string, userId?: number | null, limit: number = 50): Promise<FolderOption[]> {
  const response = await axios.get('/admin/noteMm/folderOptions', {
    params: {
      q,
      userId: userId ?? undefined,
      limit,
    },
  });
  return response.data.data;
}
