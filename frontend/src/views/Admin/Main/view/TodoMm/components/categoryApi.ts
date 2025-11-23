import axios from '@/axios/index';

export const fetchCategoryInitial = async (count: number) => {
  const r = await axios.get('admin/todoMm/category/fetchInitial', { params: { count } });
  return r.data.data;
}

export const fetchCategoryPageData = async (pageSize: number, pageNum: number) => {
  const r = await axios.get('admin/todoMm/category/fetchPageData', { params: { pageSize, pageNum } });
  return r.data.data;
}

export const searchCategories = async (params: any) => {
  const r = await axios.post('admin/todoMm/category/search', params);
  return r.data.data;
}

export const batchDeleteCategories = async (ids: number[]) => {
  try { const r = await axios.post('admin/todoMm/category/batchDelete', ids); return r.data.status; } catch { return 500 }
}

export const batchRestoreCategories = async (ids: number[]) => {
  try { const r = await axios.post('admin/todoMm/category/batchRestore', ids); return r.data.status; } catch { return 500 }
}