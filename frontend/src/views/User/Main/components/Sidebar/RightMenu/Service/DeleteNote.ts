// 用于删除笔记的服务

import axios from "../../../../../../../axios";
import { useNoteTreeUpdate } from "../../Pinia/isNoteTreeUpdated";
import { ElMessage } from "element-plus";

/**
 * 删除笔记（伪删除）
 * @param NoteId 要删除的笔记ID
 * @returns 删除结果的状态码
 */
const DeleteNote = async (NoteId: number) => {
    try {
        const response = await axios.post(
            "user/noteTree/deleteNote",
            {
                NoteId: NoteId
            }
        );

        // 当状态码返回200则更新笔记树
        if (response.data.status === 200) {
            const isNoteTreeUpdated = useNoteTreeUpdate();
            isNoteTreeUpdated.UpdatedNoteTree();
            ElMessage.success("笔记已删除");
            return 200;
        } else {
            ElMessage.error(response.data.message || "删除笔记失败");
            return response.data.status;
        }
    } catch (err) {
        console.error("删除笔记出错:", err);
        ElMessage.error("删除笔记失败");
        return 500;
    }
}

export { DeleteNote }; 