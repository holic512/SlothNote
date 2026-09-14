import axios from "../../../../../../../axios";
import {Ref} from "vue";

export interface AddCommentForm {
  noteId: number | undefined;
  userId: number | undefined;
  content: string;
  parentId?: number;
}

export const addComment = async (form: Ref<AddCommentForm>) => {
  try {
    const response = await axios.post(
        "/admin/commentMm/addComment",
        {
          noteId: form.value.noteId ?? null,
          userId: form.value.userId ?? null,
          content: form.value.content,
          parentId: form.value.parentId ?? null,
        }
    );
    return response.data.status;
  } catch {
    return 500;
  }
}
