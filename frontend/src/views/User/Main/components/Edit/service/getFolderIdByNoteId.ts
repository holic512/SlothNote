// 用于获取笔记所属的文件夹ID
import axios from '@/axios';

/**
 * 根据笔记ID获取其所属文件夹ID
 * @param noteId 笔记ID
 * @returns 返回文件夹ID
 */
export const getFolderIdByNoteId = async (noteId: number): Promise<number> => {
    try {
        const response = await axios.get('user/noteTree/folderIdByNoteId', {
            params: {
                noteId: noteId
            }
        });
        
        if (response.data.status === 200) {
            return response.data.data;
        } else {
            throw new Error('获取文件夹ID失败');
        }
    } catch (error) {
        console.error('获取笔记所属文件夹ID失败:', error);
        throw error;
    }
}; 