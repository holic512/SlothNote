import axios from '@/axios/index';
import type { Ref } from 'vue';

export interface AddTodoCategoryForm {
  userId: number | null;
  type: number | null;
  name: string;
}

export const addTodoCategory = async (form: Ref<AddTodoCategoryForm>) => {
  try {
    const r = await axios.post('/admin/todoMm/category/add', {
      userId: form.value.userId,
      type: form.value.type,
      name: form.value.name,
    });
    return r.data.status;
  } catch { return 500 }
}