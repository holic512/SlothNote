<script setup lang="ts">
import {reactive, ref} from 'vue';
import {ElMessage, FormInstance, FormRules} from 'element-plus';
import Dialog from 'primevue/dialog';
import {addFolder, AddFolderForm} from './addFolder';
import {fetchUserOptions} from '../TableView/userOptions';

const addFolderVisible = defineModel();

const form = ref<AddFolderForm>({
  userId: null,
  folderName: '',
  parentId: 0,
  description: '',
  folderAvatar: '',
});

const rules = reactive<FormRules<AddFolderForm>>({
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  folderName: [
    { required: true, message: '请输入文件夹名', trigger: 'blur' },
    { min: 1, max: 100, message: '长度应在 1 到 100 之间', trigger: 'blur' },
  ],
  parentId: [{ required: true, message: '请输入父ID', trigger: 'blur' }],
});

const formEl = ref<FormInstance>();
const userOptions = ref<any[]>([]);
const loadUserOptions = async (q?: string) => {
  userOptions.value = await fetchUserOptions(q, 50);
}

const onSubmit = async () => {
  if (!formEl.value) return;
  try {
    await formEl.value.validate();
    const status = await addFolder(form);
    switch (status) {
      case 200:
        ElMessage.success('添加成功');
        formEl.value.resetFields();
        addFolderVisible.value = false;
        break;
      case 40901:
        ElMessage.warning('同名文件夹已存在');
        break;
      default:
        ElMessage.error('无法连接服务器');
        break;
    }
  } catch {
    ElMessage.warning('请填写完整信息');
  }
};

const resetForm = () => {
  if (formEl.value) {
    formEl.value.resetFields();
    addFolderVisible.value = false;
  }
}
</script>

<template>
  <Dialog v-model:visible="addFolderVisible" :draggable="false" modal header="添加文件夹" :style="{ width: '450px'}"
          :pt="{ header: { style: { paddingBottom:'10px',paddingTop:'10px'} }, content: { style: { borderTop: '1px solid #E2E8F0'} } }">
    <el-form :model="form" :rules="rules" ref="formEl" label-width="80px" class="form-container" label-position="left">
      <el-form-item label="用户" prop="userId">
        <el-select v-model="form.userId" placeholder="选择用户" style="width: 90%" filterable remote clearable :remote-method="loadUserOptions" :reserve-keyword="true" @visible-change="(v:boolean)=>{ if(v) loadUserOptions() }">
          <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="文件夹名" prop="folderName">
        <el-input v-model="form.folderName" class="input" placeholder="请输入文件夹名"/>
      </el-form-item>
      <el-form-item label="父ID" prop="parentId">
        <el-input-number v-model="form.parentId" :min="0" :step="1" class="input" placeholder="请输入父ID"/>
      </el-form-item>
      <el-form-item label="简介" prop="description">
        <el-input v-model="form.description" class="input" placeholder="请输入简介"/>
      </el-form-item>
      <el-form-item label="头像" prop="folderAvatar">
        <el-input v-model="form.folderAvatar" class="input" placeholder="可填emoji"/>
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