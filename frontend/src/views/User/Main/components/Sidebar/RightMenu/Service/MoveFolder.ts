// 用于移动文件夹的服务

import axios from "../../../../../../../axios";
import { useNoteTreeUpdate } from "../../Pinia/isNoteTreeUpdated";
import { ElMessage } from "element-plus";

/**
 * 移动文件夹到指定文件夹
 * @param FolderId 要移动的文件夹ID
 * @param TargetFolderId 目标文件夹ID
 * @returns 移动结果的状态码
 */
const MoveFolder = async (FolderId: number, TargetFolderId: number) => {
    try {
        const response = await axios.post(
            "user/noteTree/moveFolder",
            {
                FolderId: FolderId,
                TargetFolderId: TargetFolderId
            }
        );

        // 当状态码返回200则更新笔记树
        if (response.data.status === 200) {
            const isNoteTreeUpdated = useNoteTreeUpdate();
            isNoteTreeUpdated.UpdatedNoteTree();
            ElMessage.success("文件夹已移动");
            return 200;
        } else {
            ElMessage.error(response.data.message || "移动文件夹失败");
            return response.data.status;
        }
    } catch (err) {
        console.error("移动文件夹出错:", err);
        ElMessage.error("移动文件夹失败");
        return 500;
    }
}

export { MoveFolder }; 