import axios from "../../../../../../axios";

export interface NoteContentPayload {
    noteId?: number;
    content?: string | null;
}

/**
 * 笔记正文会在刷新、AI 写入和版本恢复后立即重读；不要复用浏览器或中间层的旧响应。
 */
export const getNoteContent = async (noteId: number): Promise<NoteContentPayload | null> => {
    try {
        const response = await axios.get("user/note/context", {
            params: {
                noteId,
                _fresh: Date.now(),
            },
            headers: {
                "Cache-Control": "no-cache",
                Pragma: "no-cache",
            },
        });
        return response.data?.status === 200 ? response.data.data ?? null : null;
    } catch (err) {
        console.error(err);
        return null;
    }
}
