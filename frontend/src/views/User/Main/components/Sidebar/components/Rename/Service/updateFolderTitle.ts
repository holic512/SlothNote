import axios from "../../../../../../../../axios";

export const updateFolderTitle = async (folderId: number, folderTitle: string, signal?: AbortSignal) => {
    try {
        const response = await axios.put(
            "user/noteTree/FolderTitle",
            {
                folderId: folderId,
                folderTitle: folderTitle,
            },
            {signal},
        )
        return response.data.status
    } catch (e) {
        if (!signal?.aborted) console.error(e)
    }
}
