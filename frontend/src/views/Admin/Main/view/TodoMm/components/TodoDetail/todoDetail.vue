<script setup lang="ts">
import Dialog from 'primevue/dialog';
import {ref, watch} from 'vue';
import axios from '../../../../../../../axios';
import {ElMessage} from 'element-plus';

const visible = defineModel();
const todoId = defineModel<number>('todoId');

const detail = ref<any>(null);

watch(visible, async (v) => {
  if (v && todoId.value) {
    const r = await axios.get('/admin/todoMm/todo/detail', { params: { id: todoId.value } });
    detail.value = r.data.data;
  }
});

const onSave = async () => {
  if (!detail.value) return;
  const r = await axios.put('/admin/todoMm/todo/update', {
    id: detail.value.id,
    userId: detail.value.userId,
    categoryId: detail.value.categoryId,
    title: detail.value.title,
    description: detail.value.description,
    status: detail.value.status,
    isDeleted: detail.value.isDeleted,
  });
  if (r.data.status === 200) { ElMessage.success('保存成功'); visible.value = false; } else { ElMessage.error('无法连接服务器'); }
}
</script>

<template>
  <Dialog v-model:visible="visible" :draggable="false" modal header="待做详情" :style="{ width: '650px'}">
    <div v-if="detail">
      <el-form label-width="120px" class="form-container" label-position="left">
        <el-form-item label="ID"><el-input v-model="detail.id" disabled /></el-form-item>
        <el-form-item label="用户ID"><el-input-number v-model="detail.userId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="分类ID"><el-input-number v-model="detail.categoryId" :min="0" :step="1" /></el-form-item>
        <el-form-item label="标题"><el-input v-model="detail.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="detail.description" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="detail.status" style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
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