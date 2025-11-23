import axios from "../../../../../../../axios";

export interface SearchCommentRequest {
  q?: string;
  noteId?: number;
  userId?: number;
  isDeleted?: boolean;
  topLevelOnly?: boolean;
  pageNum: number;
  pageSize: number;
}

export const searchComments = async (req: SearchCommentRequest) => {
  const response = await axios.post(
      "admin/commentMm/search",
      req
  );
  return response.data.data as { list: any[]; total: number };
}