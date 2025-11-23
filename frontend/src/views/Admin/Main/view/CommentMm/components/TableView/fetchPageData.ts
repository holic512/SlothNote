import axios from "../../../../../../../axios";

export const fetchPageData = async (rows: number, pageNum: number) => {
  const response = await axios.get(
      "admin/commentMm/fetchPageData",
      { params: { pageNum, pageSize: rows } }
  );
  return response.data.data;
}