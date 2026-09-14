import axios from '../../../../../../../axios';
import type { Ref } from 'vue';

export interface AddFavoriteNoteForm {
  userId: number | undefined;
  noteId: number | undefined;
  favoriteFolderId: number | undefined;
  favoriteStatus: boolean;
  noteRemark: string;
}

export const addFavoriteNote = async (form: Ref<AddFavoriteNoteForm>) => {
  try {
    const r = await axios.post('/admin/favoriteMm/note/add', {
      userId: form.value.userId ?? null,
      noteId: form.value.noteId ?? null,
      favoriteFolderId: form.value.favoriteFolderId ?? null,
      favoriteStatus: form.value.favoriteStatus,
      noteRemark: form.value.noteRemark,
    });
    return r.data.status;
  } catch { return 500 }
}
