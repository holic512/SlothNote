<script setup lang="ts">
import {ref, watch} from 'vue';
import {useFavoriteDialogStore} from "@/views/User/Main/components/Edit/Pinia/FavoriteDialogStore";
import axios from "@/axios";
import {ElMessage} from "element-plus";

const store = useFavoriteDialogStore();
const visible = ref(false);
const folders = ref<Array<{id:number,name:string}>>([]);
const selectedFolder = ref<number>(0);
const remark = ref("");

const fetchFolders = async () => {
  const resp = await axios.get("user/note/favorite/folders");
  if (resp.data.status === 200) {
    folders.value = resp.data.data.map((f: any) => ({id: f.id, name: f.folderName}));
  }
};

watch(() => store.visible, async (v) => {
  visible.value = v;
  if (v) {
    selectedFolder.value = 0;
    remark.value = "";
    await fetchFolders();
  }
});

const close = () => store.close();
const confirm = async () => {
  if (store.noteId == null) return;
  const body = {
    noteId: String(store.noteId),
    folderId: String(selectedFolder.value),
    noteRemark: remark.value,
  };
  const resp = await axios.post("user/note/favorite/add", body);
  const status = resp.data.status;
  if (status === 200) {
    ElMessage.success("已加入收藏");
    close();
  } else if (status === 409) {
    ElMessage.warning("已在收藏中");
  } else if (status === 403) {
    ElMessage.warning("无权收藏该笔记");
  } else if (status === 404) {
    ElMessage.warning("笔记不存在");
  } else {
    ElMessage.error("操作失败");
  }
};

</script>

<template>
  <el-dialog v-model="visible" title="收藏笔记" width="420px" @close="close">
    <div style="display:flex;flex-direction:column;gap:12px">
      <el-select v-model="selectedFolder" placeholder="选择收藏分类">
        <el-option :label="'未分类'" :value="0" />
        <el-option v-for="f in folders" :key="f.id" :label="f.name" :value="f.id" />
      </el-select>
      <el-input v-model="remark" placeholder="备注（可选）" />
    </div>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" @click="confirm">确认</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>

</style>
