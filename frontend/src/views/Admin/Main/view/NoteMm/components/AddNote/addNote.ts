import axios from '../../../../../../../axios';
import type { Ref } from 'vue';

export interface AddNoteForm {
  userId: number | null;
  folderId: number | null;
  noteTitle: string;
  noteSummary: string;
  noteAvatar?: string;
  noteCoverUrl?: string;
  notePassword?: string;
  noteType: number | null;
}

export const addNote = async (form: Ref<AddNoteForm>) => {
  try {
    const r = await axios.post('/admin/noteMm/add', {
      userId: form.value.userId,
      folderId: form.value.folderId,
      noteTitle: form.value.noteTitle,
      noteSummary: form.value.noteSummary,
      noteAvatar: form.value.noteAvatar,
      noteCoverUrl: form.value.noteCoverUrl,
      notePassword: form.value.notePassword,
      noteType: form.value.noteType,
    });
    return r.data.status;
  } catch { return 500 }
}