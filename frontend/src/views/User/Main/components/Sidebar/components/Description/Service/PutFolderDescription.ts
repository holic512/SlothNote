import axios from "@/axios";

/**
 * 更新文件夹的简介
 * @param folderId 文件夹ID
 * @param description 文件夹简介内容
 * @returns 返回状态码
 */
export const putFolderDescription = async (folderId: number, description: string) => {
    try {
        const response = await axios.put(
            "user/noteTree/FolderDescription",
            {
                folderId: folderId,
                folderDescription: description
            }
        );
        return response.data.status;
    } catch (e) {
        console.error("更新文件夹简介失败:", e);
        return 500;
    }
}