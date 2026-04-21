import axios from "@/axios";

export interface NoteShareInfo {
  noteId: number;
  noteName: string;
  noteLocation: string[];
  avatar: string;
  cover: string | null;
}

export const getNoteShareInfo = async (noteId: number): Promise<NoteShareInfo | null> => {
  const response = await axios.get("user/note/share/info", {
    params: {noteId}
  });

  if (response.data?.status !== 200) {
    return null;
  }

  return response.data.data ?? null;
}
