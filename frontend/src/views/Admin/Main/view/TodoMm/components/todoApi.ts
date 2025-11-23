import axios from '@/axios/index';

export const fetchTodoInitial = async (count: number) => {
  const r = await axios.get('admin/todoMm/todo/fetchInitial', { params: { count } });
  return r.data.data;
}

export const fetchTodoPageData = async (pageSize: number, pageNum: number) => {
  const r = await axios.get('admin/todoMm/todo/fetchPageData', { params: { pageSize, pageNum } });
  return r.data.data;
}

export const searchTodos = async (params: any) => {
  const r = await axios.post('admin/todoMm/todo/search', params);
  return r.data.data;
}

export const batchDeleteTodos = async (ids: number[]) => {
  try { const r = await axios.post('admin/todoMm/todo/batchDelete', ids); return r.data.status; } catch { return 500 }
}

export const batchRestoreTodos = async (ids: number[]) => {
  try { const r = await axios.post('admin/todoMm/todo/batchRestore', ids); return r.data.status; } catch { return 500 }
}

export const batchEnableTodos = async (ids: number[]) => {
  try { const r = await axios.post('admin/todoMm/todo/batchEnable', ids); return r.data.status; } catch { return 500 }
}

export const batchDisableTodos = async (ids: number[]) => {
  try { const r = await axios.post('admin/todoMm/todo/batchDisable', ids); return r.data.status; } catch { return 500 }
}