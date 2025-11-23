import axios from '@/axios';

export const searchNotes = async (q: string) => {
  try {
    const resp = await axios.get('user/note/search', { params: { q } });
    if (resp.data?.status === 200) return resp.data.data;
    return [];
  } catch {
    return [];
  }
}