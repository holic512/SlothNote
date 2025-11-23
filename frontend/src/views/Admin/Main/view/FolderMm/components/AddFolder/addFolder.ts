import axios from '../../../../../../../axios';
import type { Ref } from 'vue';

export interface AddFolderForm {
  userId: number | null;
  folderName: string;
  parentId: number | null;
  description: string;
  folderAvatar?: string;
}

export const addFolder = async (form: Ref<AddFolderForm>) => {
  try {
    const response = await axios.post('/admin/folderMm/addFolder', {
      userId: form.value.userId,
      folderName: form.value.folderName,
      parentId: form.value.parentId,
      description: form.value.description,
      folderAvatar: form.value.folderAvatar,
    });
    return response.data.status;
  } catch {
    return 500;
  }
}