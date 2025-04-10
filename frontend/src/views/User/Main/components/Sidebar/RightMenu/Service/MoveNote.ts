// 用于移动笔记的服务

import axios from "../../../../../../../axios";
import { useNoteTreeUpdate } from "../../Pinia/isNoteTreeUpdated";
import { ElMessage } from "element-plus";

/**
 * 移动笔记到指定文件夹
 * @param NoteId 要移动的笔记ID
 * @param TargetFolderId 目标文件夹ID
 * @returns 移动结果的状态码
 */
const MoveNote = async (NoteId: number, TargetFolderId: number) => {
    try {
        const response = await axios.post(
            "user/noteTree/moveNote",
            {
                NoteId: NoteId,
                TargetFolderId: TargetFolderId
            }
        );

        // 当状态码返回200则更新笔记树
        if (response.data.status === 200) {
            const isNoteTreeUpdated = useNoteTreeUpdate();
            isNoteTreeUpdated.UpdatedNoteTree();
            ElMessage.success("笔记已移动");
            return 200;
        } else {
            ElMessage.error(response.data.message || "移动笔记失败");
            return response.data.status;
        }
    } catch (err) {
        console.error("移动笔记出错:", err);
        ElMessage.error("移动笔记失败");
        return 500;
    }
}

export { MoveNote }; 