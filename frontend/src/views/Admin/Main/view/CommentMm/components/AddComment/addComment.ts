import axios from "../../../../../../../axios";
import {Ref} from "vue";

export interface AddCommentForm {
  noteId: number | null;
  userId: number | null;
  content: string;
  parentId?: number | null;
}

export const addComment = async (form: Ref<AddCommentForm>) => {
  try {
    const response = await axios.post(
        "/admin/commentMm/addComment",
        {
          noteId: form.value.noteId,
          userId: form.value.userId,
          content: form.value.content,
          parentId: form.value.parentId || null,
        }
    );
    return response.data.status;
  } catch {
    return 500;
  }
}