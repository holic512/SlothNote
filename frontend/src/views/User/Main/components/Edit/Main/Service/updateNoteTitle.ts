// 用于更新 笔记 的 名称
import axios from "@/axios";

export const updateNoteTitle = async (noteId: number, noteTitle: string, signal?: AbortSignal) => {
    try {
        const response = await axios.put(
            "user/noteTree/NoteTitle",
            {
                noteId: noteId,
                noteTitle: noteTitle,
            },
            {signal},
        )
        return response.data.status
    } catch (e) {
        if (!signal?.aborted) console.error(e)
    }
}
