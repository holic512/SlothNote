<script setup lang="ts">
import {reactive, ref} from 'vue';
import {ElMessage, FormInstance, FormRules} from 'element-plus';
import Dialog from 'primevue/dialog';
import {addTodo, AddTodoForm} from './addTodo';
import {fetchUserOptions} from '../../../FolderMm/components/TableView/userOptions';

const visible = defineModel();

const form = ref<AddTodoForm>({ userId: null, categoryId: null, title: '', description: '', status: 1 });
const rules = reactive<FormRules<AddTodoForm>>({
  userId: [{ required: true, message: '请选择用户', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
});
const formEl = ref<FormInstance>();
const userOptions = ref<any[]>([]);
const loadUserOptions = async (q?: string) => { userOptions.value = await fetchUserOptions(q, 50); }

const onSubmit = async () => {
  if (!formEl.value) return;
  try {
    await formEl.value.validate();
    const s = await addTodo(form);
    if (s===200) { ElMessage.success('添加成功'); formEl.value.resetFields(); visible.value=false } else { ElMessage.error('无法连接服务器') }
  } catch { ElMessage.warning('请填写完整信息'); }
}

const resetForm = () => { if (formEl.value) { formEl.value.resetFields(); visible.value=false } }
</script>

<template>
  <Dialog v-model:visible="visible" :draggable="false" modal header="添加待做" :style="{ width: '480px'}">
    <el-form :model="form" :rules="rules" ref="formEl" label-width="100px" class="form-container" label-position="left">
      <el-form-item label="用户" prop="userId">
        <el-select v-model="form.userId" placeholder="选择用户" style="width: 90%" filterable remote clearable :remote-method="loadUserOptions" :reserve-keyword="true" @visible-change="(v:boolean)=>{ if(v) loadUserOptions() }">
          <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="分类ID" prop="categoryId">
        <el-input-number v-model="form.categoryId" :min="0" :step="1" class="input" placeholder="请输入分类ID"/>
      </el-form-item>
      <el-form-item label="标题" prop="title">
        <el-input v-model="form.title" class="input" placeholder="请输入标题"/>
      </el-form-item>
      <el-form-item label="描述" prop="description">
        <el-input v-model="form.description" class="input" placeholder="请输入描述"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" style="width: 90%">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
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