import axios from '../../../../../../../axios';

export interface SearchParams {
  q?: string;
  isDeleted?: number;
  userId?: number;
  parentId?: number;
  pageNum: number;
  pageSize: number;
}

export const searchFolders = async (params: SearchParams) => {
  const response = await axios.post('/admin/folderMm/search', {
    q: params.q,
    isDeleted: params.isDeleted,
    userId: params.userId,
    parentId: params.parentId,
    pageNum: params.pageNum,
    pageSize: params.pageSize,
  });
  return response.data.data;
};