<script setup lang="ts">
import {reactive, ref} from 'vue';
import {ElMessage, FormInstance, FormRules} from 'element-plus';
import Dialog from 'primevue/dialog';
import {addNote, AddNoteForm} from './addNote';
import {fetchUserOptions} from '../../../FolderMm/components/TableView/userOptions';

const visible = defineModel();

const form = ref<AddNoteForm>({ userId: null, folderId: null, noteTitle: '', noteSummary: '', noteAvatar: '', noteCoverUrl: '', notePassword: '', noteType: 0 });
const rules = reactive<FormRules<AddNoteForm>>({
  userId: [{ required: true, message: '请选择用户', trigger: 'blur' }],
  noteTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  noteType: [{ required: true, message: '请输入类型', trigger: 'blur' }],
});
const formEl = ref<FormInstance>();
const userOptions = ref<any[]>([]);
const loadUserOptions = async (q?: string) => { userOptions.value = await fetchUserOptions(q, 50); }

const onSubmit = async () => {
  if (!formEl.value) return;
  try {
    await formEl.value.validate();
    const s = await addNote(form);
    if (s===200) { ElMessage.success('添加成功'); formEl.value.resetFields(); visible.value=false } else { ElMessage.error('无法连接服务器') }
  } catch { ElMessage.warning('请填写完整信息'); }
}

const resetForm = () => { if (formEl.value) { formEl.value.resetFields(); visible.value=false } }
</script>

<template>
  <Dialog v-model:visible="visible" :draggable="false" modal header="添加笔记" :style="{ width: '520px'}">
    <el-form :model="form" :rules="rules" ref="formEl" label-width="100px" class="form-container" label-position="left">
      <el-form-item label="用户" prop="userId">
        <el-select v-model="form.userId" placeholder="选择用户" style="width: 90%" filterable remote clearable :remote-method="loadUserOptions" :reserve-keyword="true" @visible-change="(v:boolean)=>{ if(v) loadUserOptions() }">
          <el-option v-for="u in userOptions" :key="u.id" :label="`${u.username} (${u.email})`" :value="u.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="文件夹ID" prop="folderId">
        <el-input-number v-model="form.folderId" :min="0" :step="1" class="input" placeholder="请输入文件夹ID"/>
      </el-form-item>
      <el-form-item label="标题" prop="noteTitle">
        <el-input v-model="form.noteTitle" class="input" placeholder="请输入标题"/>
      </el-form-item>
      <el-form-item label="摘要" prop="noteSummary">
        <el-input v-model="form.noteSummary" class="input" placeholder="请输入摘要"/>
      </el-form-item>
      <el-form-item label="头像" prop="noteAvatar">
        <el-input v-model="form.noteAvatar" class="input" placeholder="可填emoji"/>
      </el-form-item>
      <el-form-item label="封面URL" prop="noteCoverUrl">
        <el-input v-model="form.noteCoverUrl" class="input" placeholder="请输入封面URL"/>
      </el-form-item>
      <el-form-item label="密码" prop="notePassword">
        <el-input v-model="form.notePassword" class="input" placeholder="可选密码"/>
      </el-form-item>
      <el-form-item label="类型" prop="noteType">
        <el-input-number v-model="form.noteType" :min="0" :step="1" class="input" placeholder="请输入类型"/>
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