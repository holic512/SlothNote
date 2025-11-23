import axios from "../../../../../../axios";

export const fetchMetrics = async () => {
  const resp = await axios.get("admin/dashboard/metrics");
  return resp.data.data as {
    userCount: number; noteCount: number; folderCount: number; commentCount: number;
    favoriteNoteCount: number; favoriteFolderCount: number; todoCount: number;
  };
}

export interface RecentReq {
  category: string;
  q?: string;
  userId?: number;
  isDeleted?: boolean;
  pageNum: number;
  pageSize: number;
}

export const fetchRecent = async (req: RecentReq) => {
  const resp = await axios.post("admin/dashboard/recent", req);
  return resp.data.data as { list: any[]; total: number };
}

export const todoAdd = async (payload: any) => {
  const resp = await axios.post("admin/dashboard/todo/add", payload);
  return resp.data.status as number;
}

export const todoUpdate = async (payload: any) => {
  const resp = await axios.put("admin/dashboard/todo/update", payload);
  return resp.data.status as number;
}

export const todoDelete = async (id: number) => {
  const resp = await axios.delete("admin/dashboard/todo/delete", { params: { id } });
  return resp.data.status as number;
}

export const todoBatchDelete = async (ids: number[]) => {
  const resp = await axios.post("admin/dashboard/todo/batchDelete", ids);
  return resp.data.status as number;
}

export const todoBatchEnable = async (ids: number[]) => {
  const resp = await axios.post("admin/dashboard/todo/batchEnable", ids);
  return resp.data.status as number;
}

export const todoBatchDisable = async (ids: number[]) => {
  const resp = await axios.post("admin/dashboard/todo/batchDisable", ids);
  return resp.data.status as number;
}