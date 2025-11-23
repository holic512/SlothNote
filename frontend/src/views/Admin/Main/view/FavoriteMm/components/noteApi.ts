import axios from '@/axios/index';

export const fetchNoteInitial = async (count: number) => {
  const r = await axios.get('admin/favoriteMm/note/fetchInitial', { params: { count } });
  return r.data.data;
}

export const fetchNotePageData = async (pageSize: number, pageNum: number) => {
  const r = await axios.get('admin/favoriteMm/note/fetchPageData', { params: { pageSize, pageNum } });
  return r.data.data;
}

export const searchFavoriteNotes = async (params: any) => {
  const r = await axios.post('admin/favoriteMm/note/search', params);
  return r.data.data;
}

export const batchDeleteFavoriteNotes = async (ids: number[]) => {
  try { const r = await axios.post('admin/favoriteMm/note/batchDelete', ids); return r.data.status; } catch { return 500 }
}

export const batchEnableFavoriteNotes = async (ids: number[]) => {
  try { const r = await axios.post('admin/favoriteMm/note/batchEnable', ids); return r.data.status; } catch { return 500 }
}

export const batchDisableFavoriteNotes = async (ids: number[]) => {
  try { const r = await axios.post('admin/favoriteMm/note/batchDisable', ids); return r.data.status; } catch { return 500 }
}

export const batchRestoreFavoriteNotes = async (ids: number[]) => {
  try { const r = await axios.post('admin/favoriteMm/note/batchRestore', ids); return r.data.status; } catch { return 500 }
}