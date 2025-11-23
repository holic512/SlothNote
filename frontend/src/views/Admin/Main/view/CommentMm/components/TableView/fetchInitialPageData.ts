import axios from "../../../../../../../axios";

const fetchInitialPageData = async (rows: number) => {
  const response = await axios.get(
      "admin/commentMm/fetchInitialComment",
      { params: { count: rows } }
  );
  return response.data.data;
}
export default fetchInitialPageData