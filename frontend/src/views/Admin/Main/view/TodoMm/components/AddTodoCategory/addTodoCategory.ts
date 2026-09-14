import axios from '@/axios/index';
import type { Ref } from 'vue';

export interface AddTodoCategoryForm {
  userId: number | undefined;
  type: number | undefined;
  name: string;
}

export const addTodoCategory = async (form: Ref<AddTodoCategoryForm>) => {
  try {
    const r = await axios.post('/admin/todoMm/category/add', {
      userId: form.value.userId ?? null,
      type: form.value.type ?? null,
      name: form.value.name,
    });
    return r.data.status;
  } catch { return 500 }
}
