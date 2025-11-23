import axios from '../../../../../../../axios';

const fetchInitialPageData = async (count: number) => {
    const response = await axios.get('admin/folderMm/fetchInitialFolder', { params: { count } });
    return response.data.data;
}
export default fetchInitialPageData;