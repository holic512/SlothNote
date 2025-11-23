import axios from '../../../../../../../axios';

const fetchPageData = async (pageSize: number, pageNum: number) => {
    const response = await axios.get('admin/folderMm/fetchPageData', {
        params: { pageSize, pageNum }
    });
    return response.data.data;
}
export { fetchPageData };