import axios from "../../../../../../../axios";
import { ElMessage } from "element-plus";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";

export const onExportHtml = async () => {
  const store = useRightSelectNodeId();
  // 仅当选中的是笔记时导出
  // @ts-ignore
  const data = store.data as any;
  if (!data || data.type !== 'NOTE') return;
  const noteId = data.id;
  const response = await axios.get(
      "user/note/export/html",
      { params: { noteId } }
  );
  if (response.data.status === 200) {
    const html = response.data.data as string;
    const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `note-${noteId}.html`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  } else if (response.data.status === 404) {
    ElMessage.warning("该页面为空");
  } else if (response.data.status === 403) {
    ElMessage.warning("无权导出");
  } else {
    ElMessage.error("导出失败");
  }
}