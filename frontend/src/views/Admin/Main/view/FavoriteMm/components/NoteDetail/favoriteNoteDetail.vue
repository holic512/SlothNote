<script setup lang="ts">
import Dialog from 'primevue/dialog';
import {ref, watch} from 'vue';
import axios from '../../../../../../../axios';
import {ElMessage} from 'element-plus';

const visible = defineModel();
const noteId = defineModel<number>('noteId');

const detail = ref<any>(null);

watch(visible, async (v) => {
  if (v && noteId.value) {
    const r = await axios.get('/admin/favoriteMm/note/detail', { params: { id: noteId.value } });
    detail.value = r.data.data;
  }
});

const onSave = async () => {
  if (!detail.value) return;
  const r = await axios.put('/admin/favoriteMm/note/update', {
    id: detail.value.id,
    userId: detail.value.userId,
    noteId: detail.value.noteId,
    favoriteFolderId: detail.value.favoriteFolderId,
    favoriteStatus: detail.value.favoriteStatus,
    noteRemark: detail.value.noteRemark,
    isDeleted: detail.value.isDeleted,
  });
  if (r.data.status === 200) { ElMessage.success('保存成功'); visible.value = false; } else { ElMessage.error('无法连接服务器'); }
}
</script>

<template>
  <Dialog v-model:visible="visible" :draggable="false" modal header="收藏记录详情" :style="{ width: '650px'}">
    <div v-if="detail">
      <el-form label-width="120px" class="form-container" label-position="left">
        <el-form-item label="ID"><el-input v-model="detail.id" disabled /></el-form-item>
        <el-form-item label="用户ID"><el-input-number v-model="detail.userId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="笔记ID"><el-input-number v-model="detail.noteId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="收藏夹ID"><el-input-number v-model="detail.favoriteFolderId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="detail.favoriteStatus" style="width: 120px">
            <el-option label="已收藏" :value="true" />
            <el-option label="取消" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="detail.noteRemark" /></el-form-item>
        <el-form-item label="删除状态">
          <el-select v-model="detail.isDeleted" style="width: 120px">
            <el-option label="正常" :value="false" />
            <el-option label="已删除" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间"><el-input :model-value="detail.createdAt" disabled /></el-form-item>
        <el-form-item label="更新时间"><el-input :model-value="detail.updatedAt" disabled /></el-form-item>
      </el-form>
      <div style="padding: 0 16px 0 16px">
        <el-button color="#020617" @click="onSave">保存</el-button>
        <el-button type="info" @click="()=>{ visible.value=false }" plain>取消</el-button>
      </div>
    </div>
  </Dialog>
</template>

<style scoped>
.form-container { max-width: 680px; display: flex; flex-direction: column; gap: 6px; padding: 16px; }
</style>