import axios from "../../../../../../../axios";
import { ElMessage } from "element-plus";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";

export const onExportPdf = async () => {
  const store = useRightSelectNodeId();
  // @ts-ignore
  const data = store.data as any;
  if (!data || data.type !== 'NOTE') return;
  const noteId = data.id;
  const response = await axios.get(
      "user/note/export/pdf",
      { params: { noteId } }
  );
  if (response.data.status === 200) {
    const html = response.data.data as string;
    const win = window.open('', '_blank');
    if (!win) return;
    win.document.open();
    win.document.write(html);
    win.document.close();
    win.focus();
    setTimeout(() => {
      win.print();
    }, 300);
  } else if (response.data.status === 404) {
    ElMessage.warning("该页面为空");
  } else if (response.data.status === 403) {
    ElMessage.warning("无权导出");
  } else {
    ElMessage.error("导出失败");
  }
}