import axios from "@/axios";

/**
 * 更新笔记的简介
 * @param noteId 笔记ID
 * @param description 笔记简介内容
 * @returns 返回状态码
 */
export const putNoteDescription = async (noteId: number, description: string) => {
    try {
        const response = await axios.put(
            "user/noteTree/NoteSummary",
            {
                noteId: noteId,
                noteDescription: description
            }
        );
        return response.data.status;
    } catch (e) {
        console.error("更新笔记简介失败:", e);
        return 500;
    }
}