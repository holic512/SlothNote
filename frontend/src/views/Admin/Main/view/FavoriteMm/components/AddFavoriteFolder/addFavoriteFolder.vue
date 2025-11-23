<script setup lang="ts">
import {reactive, ref} from 'vue';
import {ElMessage, FormInstance, FormRules} from 'element-plus';
import Dialog from 'primevue/dialog';
import {addFavoriteFolder, AddFavoriteFolderForm} from './addFavoriteFolder';
import {fetchUserOptions} from '../../../FolderMm/components/TableView/userOptions';

const visible = defineModel();

const form = ref<AddFavoriteFolderForm>({ userId: null, folderName: '', parentId: 0, favoriteFolderDescription: '' });
const rules = reactive<FormRules<AddFavoriteFolderForm>>({
  userId: [{ required: true, message: '请选择用户', trigger: 'blur' }],
  folderName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  parentId: [{ required: true, message: '请输入父ID', trigger: 'blur' }],
});
const formEl = ref<FormInstance>();
const userOptions = ref<any[]>([]);
const loadUserOptions = async (q?: string) => { userOptions.value = await fetchUserOptions(q, 50); }

const onSubmit = async () => {
  if (!formEl.value) return;
  try {
    await formEl.value.validate();
    const s = await addFavoriteFolder(form);
    if (s===200) { ElMessage.success('添加成功'); formEl.value.resetFields(); visible.value=false } else if (s===40901) { ElMessage.warning('同名文件夹已存在') } else { ElMessage.error('无法连接服务器') }
  } catch { ElMessage.warning('请填写完整信息'); }
}

const resetForm = () => { if (formEl.value) { formEl.value.resetFields(); visible.value=false } }
</script>

<template>
  <Dialog v-model:visible="visible" :draggable="false" modal header="添加收藏文件夹" :style="{ width: '450px'}">
    <el-form :model="form" :rules="rules" ref="formEl" label-width="80px" class="form-container" label-position="left">
      <el-form-item label="用户" prop="userId">
        <el-select v-model="form.userId" placeholder="选择用户" style="width: 90%" filterable remote clearable :remote-method="loadUserOptions" :reserve-keyword="true" @visible-change="(v:boolean)=>{ if(v) loadUserOptions() }">
          <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="名称" prop="folderName">
        <el-input v-model="form.folderName" class="input" placeholder="请输入名称"/>
      </el-form-item>
      <el-form-item label="父ID" prop="parentId">
        <el-input-number v-model="form.parentId" :min="0" :step="1" class="input" placeholder="请输入父ID"/>
      </el-form-item>
      <el-form-item label="简介" prop="favoriteFolderDescription">
        <el-input v-model="form.favoriteFolderDescription" class="input" placeholder="请输入简介"/>
      </el-form-item>
    </el-form>
    <div style="padding: 0 16px 0 16px">
      <el-button color="#020617" @click="onSubmit">添加</el-button>
      <el-button type="info" @click="resetForm" plain>取消</el-button>
    </div>
  </Dialog>
</template>

<style scoped>
.form-container { max-width: 600px; display: flex; flex-direction: column; gap: 1px; padding: 16px 16px 0 16px; }
.input { width: 90%; }
</style>