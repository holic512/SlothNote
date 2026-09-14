import axios from '../../../../../../../axios';
import type { Ref } from 'vue';

export interface AddFolderForm {
  userId: number | undefined;
  folderName: string;
  parentId: number | undefined;
  description: string;
  folderAvatar?: string;
}

export const addFolder = async (form: Ref<AddFolderForm>) => {
  try {
    const response = await axios.post('/admin/folderMm/addFolder', {
      userId: form.value.userId ?? null,
      folderName: form.value.folderName,
      parentId: form.value.parentId ?? null,
      description: form.value.description,
      folderAvatar: form.value.folderAvatar,
    });
    return response.data.status;
  } catch {
    return 500;
  }
}
