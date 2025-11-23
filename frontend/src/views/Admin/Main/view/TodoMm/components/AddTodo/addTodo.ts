import axios from '@/axios/index';
import type { Ref } from 'vue';

export interface AddTodoForm {
  userId: number | null;
  categoryId: number | null;
  title: string;
  description: string;
  status: number | null;
}

export const addTodo = async (form: Ref<AddTodoForm>) => {
  try {
    const r = await axios.post('/admin/todoMm/todo/add', {
      userId: form.value.userId,
      categoryId: form.value.categoryId,
      title: form.value.title,
      description: form.value.description,
      status: form.value.status,
    });
    return r.data.status;
  } catch { return 500 }
}