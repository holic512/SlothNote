import type { Ref } from 'vue';
import {addNote as submitAddNote} from '../../service/noteMm';

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
  const result = await submitAddNote({
    userId: form.value.userId,
    folderId: form.value.folderId,
    noteTitle: form.value.noteTitle,
    noteSummary: form.value.noteSummary,
    noteAvatar: form.value.noteAvatar,
    noteCoverUrl: form.value.noteCoverUrl,
    notePassword: form.value.notePassword,
    noteType: form.value.noteType,
  });
  return result;
}
