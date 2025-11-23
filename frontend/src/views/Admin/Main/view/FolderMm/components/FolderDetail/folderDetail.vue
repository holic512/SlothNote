<script setup lang="ts">
import Dialog from 'primevue/dialog';
import {ref, watch} from 'vue';
import axios from '../../../../../../../axios';
import {ElMessage} from 'element-plus';

const folderDetailVisible = defineModel();
const folderId = defineModel<number>('folderId');

const detail = ref<any>(null);

watch(folderDetailVisible, async (v) => {
  if (v && folderId.value) {
    const response = await axios.get('/admin/folderMm/detail', { params: { id: folderId.value } });
    detail.value = response.data.data;
  }
});

const onSave = async () => {
  if (!detail.value) return;
  const response = await axios.put('/admin/folderMm/update', {
    id: detail.value.id,
    userId: detail.value.userId,
    folderName: detail.value.folderName,
    parentId: detail.value.parentId,
    description: detail.value.description,
    folderAvatar: detail.value.folderAvatar,
    isDeleted: detail.value.isDeleted,
  });
  if (response.data.status === 200) {
    ElMessage.success('保存成功');
    folderDetailVisible.value = false;
  } else {
    ElMessage.error('无法连接服务器');
  }
}
</script>

<template>
  <Dialog v-model:visible="folderDetailVisible" :draggable="false" modal header="文件夹详情" :style="{ width: '650px'}"
          :pt="{ header: { style: { paddingBottom:'10px',paddingTop:'10px'} }, content: { style: { borderTop: '1px solid #E2E8F0'} } }">
    <div v-if="detail">
      <el-form label-width="100px" class="form-container" label-position="left">
        <el-form-item label="ID"><el-input v-model="detail.id" disabled /></el-form-item>
        <el-form-item label="用户ID"><el-input-number v-model="detail.userId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="文件夹名"><el-input v-model="detail.folderName" /></el-form-item>
        <el-form-item label="父ID"><el-input-number v-model="detail.parentId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="detail.description" /></el-form-item>
        <el-form-item label="头像"><el-input v-model="detail.folderAvatar" /></el-form-item>
        <el-form-item label="删除状态">
          <el-select v-model="detail.isDeleted" style="width: 120px">
            <el-option label="正常" :value="0" />
            <el-option label="已删除" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间"><el-input :model-value="detail.createdAt" disabled /></el-form-item>
        <el-form-item label="更新时间"><el-input :model-value="detail.updatedAt" disabled /></el-form-item>
      </el-form>
      <div style="padding: 0 16px 0 16px">
        <el-button color="#020617" @click="onSave">保存</el-button>
        <el-button type="info" @click="()=>{ folderDetailVisible.value=false }" plain>取消</el-button>
      </div>
    </div>
  </Dialog>
</template>

<style scoped>
.form-container { max-width: 680px; display: flex; flex-direction: column; gap: 6px; padding: 16px; }
</style>