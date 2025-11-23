import axios from '@/axios/index';

export const fetchFolderInitial = async (count: number) => {
  const r = await axios.get('admin/favoriteMm/folder/fetchInitial', { params: { count } });
  return r.data.data;
}

export const fetchFolderPageData = async (pageSize: number, pageNum: number) => {
  const r = await axios.get('admin/favoriteMm/folder/fetchPageData', { params: { pageSize, pageNum } });
  return r.data.data;
}

export const searchFavoriteFolders = async (params: any) => {
  const r = await axios.post('admin/favoriteMm/folder/search', params);
  return r.data.data;
}

export const batchDeleteFavoriteFolders = async (ids: number[]) => {
  try { const r = await axios.post('admin/favoriteMm/folder/batchDelete', ids); return r.data.status; } catch { return 500 }
}

export const batchRestoreFavoriteFolders = async (ids: number[]) => {
  try { const r = await axios.post('admin/favoriteMm/folder/batchRestore', ids); return r.data.status; } catch { return 500 }
}