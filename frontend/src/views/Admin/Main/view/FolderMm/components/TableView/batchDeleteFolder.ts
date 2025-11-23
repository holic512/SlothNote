import axios from '../../../../../../../axios';

export const BatchDeleteFolder = async (folderIds: number[]) => {
    try {
        const response = await axios.post('/admin/folderMm/batchDelete', folderIds);
        return response.data.status;
    } catch (err) {
        return 500;
    }
}