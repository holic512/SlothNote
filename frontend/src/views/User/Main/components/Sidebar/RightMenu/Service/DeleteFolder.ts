// 用于删除文件夹的服务

import axios from "../../../../../../../axios";
import { useNoteTreeUpdate } from "../../Pinia/isNoteTreeUpdated";
import { ElMessage } from "element-plus";

/**
 * 删除文件夹（伪删除）
 * @param FolderId 要删除的文件夹ID
 * @returns 删除结果的状态码
 */
const DeleteFolder = async (FolderId: number) => {
    try {
        const response = await axios.post(
            "user/noteTree/deleteFolder",
            {
                FolderId: FolderId
            }
        );

        // 当状态码返回200则更新笔记树
        if (response.data.status === 200) {
            const isNoteTreeUpdated = useNoteTreeUpdate();
            isNoteTreeUpdated.UpdatedNoteTree();
            ElMessage.success("文件夹已删除");
            return 200;
        } else {
            ElMessage.error(response.data.message || "删除文件夹失败");
            return response.data.status;
        }
    } catch (err) {
        console.error("删除文件夹出错:", err);
        ElMessage.error("删除文件夹失败");
        return 500;
    }
}

export { DeleteFolder }; 