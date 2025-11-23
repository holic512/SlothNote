import axios from '../../../../../../../axios';
import type { Ref } from 'vue';

export interface AddFavoriteNoteForm {
  userId: number | null;
  noteId: number | null;
  favoriteFolderId: number | null;
  favoriteStatus: boolean;
  noteRemark: string;
}

export const addFavoriteNote = async (form: Ref<AddFavoriteNoteForm>) => {
  try {
    const r = await axios.post('/admin/favoriteMm/note/add', {
      userId: form.value.userId,
      noteId: form.value.noteId,
      favoriteFolderId: form.value.favoriteFolderId,
      favoriteStatus: form.value.favoriteStatus,
      noteRemark: form.value.noteRemark,
    });
    return r.data.status;
  } catch { return 500 }
}