import axios from "../../../../../../../axios";

export const batchEnable = async (ids: number[]) => {
  try {
    const response = await axios.post("/admin/userMm/batchEnable", ids);
    return response.data.status;
  } catch {
    return 500;
  }
};

export const batchDisable = async (ids: number[]) => {
  try {
    const response = await axios.post("/admin/userMm/batchDisable", ids);
    return response.data.status;
  } catch {
    return 500;
  }
};

export const deleteUser = async (id: number) => {
  try {
    const response = await axios.delete("/admin/userMm/delete", { params: { id } });
    return response.data.status;
  } catch {
    return 500;
  }
};