import axios from "../../../../../../../axios";

export interface SearchParams {
  q?: string;
  status?: number;
  gender?: string;
  pageNum: number;
  pageSize: number;
}

export const searchUsers = async (params: SearchParams) => {
  const response = await axios.post("/admin/userMm/search", {
    q: params.q,
    status: params.status,
    gender: params.gender,
    pageNum: params.pageNum,
    pageSize: params.pageSize,
  });
  return response.data.data;
};