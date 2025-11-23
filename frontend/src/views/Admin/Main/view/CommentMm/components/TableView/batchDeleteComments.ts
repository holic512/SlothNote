import axios from "../../../../../../../axios";

export const batchDeleteComments = async (ids: number[]): Promise<number> => {
  try {
    const response = await axios.post(
        "admin/commentMm/batchDelete",
        ids
    );
    return response.data.status;
  } catch {
    return 500;
  }
}