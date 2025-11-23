<script setup lang="ts">
import Dialog from 'primevue/dialog';
import {ref, watch} from 'vue';
import axios from '../../../../../../../axios';
import {ElMessage} from 'element-plus';

const visible = defineModel();
const noteId = defineModel<number>('noteId');

const detail = ref<any>(null);
const content = ref<string>('');

watch(visible, async (v) => {
  if (v && noteId.value) {
    const infoResp = await axios.get('/admin/noteMm/detail', { params: { id: noteId.value } });
    detail.value = infoResp.data.data;
    const contentResp = await axios.get('/admin/noteMm/content/get', { params: { noteId: noteId.value } });
    content.value = contentResp.data.data || '';
  }
});

const onSaveInfo = async () => {
  if (!detail.value) return;
  const r = await axios.put('/admin/noteMm/update', {
    id: detail.value.id,
    userId: detail.value.userId,
    folderId: detail.value.folderId,
    noteTitle: detail.value.noteTitle,
    noteSummary: detail.value.noteSummary,
    noteAvatar: detail.value.noteAvatar,
    noteCoverUrl: detail.value.noteCoverUrl,
    notePassword: detail.value.notePassword,
    noteType: detail.value.noteType,
    isDeleted: detail.value.isDeleted,
  });
  if (r.data.status === 200) { ElMessage.success('信息保存成功'); visible.value = false; } else { ElMessage.error('无法连接服务器'); }
}

const onSaveContent = async () => {
  if (!noteId.value) return;
  const r = await axios.post('/admin/noteMm/content/update', { noteId: noteId.value, content: content.value });
  if (r.data.status === 200) { ElMessage.success('内容保存成功'); } else { ElMessage.error('无法连接服务器'); }
}
</script>

<template>
  <Dialog v-model:visible="visible" :draggable="false" modal header="笔记编辑" :style="{ width: '760px'}">
    <div v-if="detail">
      <el-form label-width="120px" class="form-container" label-position="left">
        <el-form-item label="ID"><el-input v-model="detail.id" disabled /></el-form-item>
        <el-form-item label="用户ID"><el-input-number v-model="detail.userId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="文件夹ID"><el-input-number v-model="detail.folderId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="detail.noteTitle" /></el-form-item>
        <el-form-item label="摘要"><el-input v-model="detail.noteSummary" /></el-form-item>
        <el-form-item label="头像"><el-input v-model="detail.noteAvatar" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="detail.noteCoverUrl" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="detail.notePassword" /></el-form-item>
        <el-form-item label="类型"><el-input-number v-model="detail.noteType" :min="0" :step="1" /></el-form-item>
        <el-form-item label="删除状态">
          <el-select v-model="detail.isDeleted" style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="已删除" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间"><el-input :model-value="detail.createdAt" disabled /></el-form-item>
        <el-form-item label="更新时间"><el-input :model-value="detail.updatedAt" disabled /></el-form-item>
      </el-form>

      <el-divider />
      <el-form label-width="120px" class="form-container" label-position="left">
        <el-form-item label="内容(JSON)">
          <el-input v-model="content" type="textarea" :rows="10" placeholder="tiptap JSON" />
        </el-form-item>
      </el-form>

      <div style="padding: 0 16px 0 16px; display: flex; gap: 8px">
        <el-button color="#020617" @click="onSaveInfo">保存信息</el-button>
        <el-button type="primary" @click="onSaveContent">保存内容</el-button>
        <el-button type="info" @click="()=>{ visible.value=false }" plain>取消</el-button>
      </div>
    </div>
  </Dialog>
</template>

<style scoped>
.form-container { max-width: 720px; display: flex; flex-direction: column; gap: 6px; padding: 16px; }
</style>