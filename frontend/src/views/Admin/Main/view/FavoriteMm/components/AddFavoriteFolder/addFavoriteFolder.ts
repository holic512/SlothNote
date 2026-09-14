import axios from '../../../../../../../axios';
import type { Ref } from 'vue';

export interface AddFavoriteFolderForm {
  userId: number | undefined;
  folderName: string;
  parentId: number | undefined;
  favoriteFolderDescription: string;
}

export const addFavoriteFolder = async (form: Ref<AddFavoriteFolderForm>) => {
  try {
    const r = await axios.post('/admin/favoriteMm/folder/add', {
      userId: form.value.userId ?? null,
      folderName: form.value.folderName,
      parentId: form.value.parentId ?? null,
      favoriteFolderDescription: form.value.favoriteFolderDescription,
    });
    return r.data.status;
  } catch { return 500 }
}
