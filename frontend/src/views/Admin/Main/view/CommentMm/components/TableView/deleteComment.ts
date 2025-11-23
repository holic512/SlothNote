import axios from "../../../../../../../axios";

export const deleteComment = async (id: number): Promise<number> => {
  try {
    const response = await axios.delete(
        "admin/commentMm/delete",
        { params: { id } }
    );
    return response.data.status;
  } catch {
    return 500;
  }
}