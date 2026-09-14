import axios from '@/axios/index';
import type { Ref } from 'vue';

export interface AddTodoForm {
  userId: number | undefined;
  categoryId: number | undefined;
  title: string;
  description: string;
  status: number | undefined;
}

export const addTodo = async (form: Ref<AddTodoForm>) => {
  try {
    const r = await axios.post('/admin/todoMm/todo/add', {
      userId: form.value.userId ?? null,
      categoryId: form.value.categoryId ?? null,
      title: form.value.title,
      description: form.value.description,
      status: form.value.status ?? null,
    });
    return r.data.status;
  } catch { return 500 }
}
