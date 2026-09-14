import type { Ref } from 'vue';
import {addNote as submitAddNote} from '../../service/noteMm';

export interface AddNoteForm {
  userId: number | undefined;
  folderId: number | undefined;
  noteTitle: string;
  noteSummary: string;
  noteAvatar?: string;
  noteCoverUrl?: string;
  notePassword?: string;
  noteType: number | undefined;
}

export const addNote = async (form: Ref<AddNoteForm>) => {
  const result = await submitAddNote({
    userId: form.value.userId ?? null,
    folderId: form.value.folderId ?? null,
    noteTitle: form.value.noteTitle,
    noteSummary: form.value.noteSummary,
    noteAvatar: form.value.noteAvatar,
    noteCoverUrl: form.value.noteCoverUrl,
    notePassword: form.value.notePassword,
    noteType: form.value.noteType ?? null,
  });
  return result;
}
