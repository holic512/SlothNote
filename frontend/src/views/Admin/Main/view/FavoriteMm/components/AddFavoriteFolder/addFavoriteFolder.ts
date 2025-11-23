import axios from '../../../../../../../axios';
import type { Ref } from 'vue';

export interface AddFavoriteFolderForm {
  userId: number | null;
  folderName: string;
  parentId: number | null;
  favoriteFolderDescription: string;
}

export const addFavoriteFolder = async (form: Ref<AddFavoriteFolderForm>) => {
  try {
    const r = await axios.post('/admin/favoriteMm/folder/add', {
      userId: form.value.userId,
      folderName: form.value.folderName,
      parentId: form.value.parentId,
      favoriteFolderDescription: form.value.favoriteFolderDescription,
    });
    return r.data.status;
  } catch { return 500 }
}