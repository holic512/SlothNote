import axios from '../../../../../../../axios';

export interface UserOption { id: number; username: string; email: string }

export const fetchUserOptions = async (q?: string, limit: number = 50): Promise<UserOption[]> => {
  const response = await axios.get('/admin/folderMm/userOptions', { params: { q, limit } });
  return response.data.data;
}