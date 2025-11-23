<script setup lang="ts">
import {ref} from "vue";
import {ElMessage, FormInstance, FormRules} from "element-plus";
import Dialog from "primevue/dialog";
import {AddCommentForm, addComment} from "./addComment";

const addVisible = defineModel()

const form = ref<AddCommentForm>({
  noteId: null,
  userId: null,
  content: "",
  parentId: null
});

const rules = ref<FormRules<AddCommentForm>>({
  noteId: [{ required: true, message: "请输入笔记ID", trigger: "blur" }],
  userId: [{ required: true, message: "请输入用户ID", trigger: "blur" }],
  content: [{ required: true, message: "请输入评论内容", trigger: "blur" }],
});

const formEl = ref<FormInstance>();

const onSubmit = async () => {
  if (!formEl.value) return;
  try {
    await formEl.value.validate();
    const status = await addComment(form);
    if (status === 200) {
      ElMessage.success("添加成功");
      formEl.value.resetFields();
      addVisible.value = false;
    } else {
      ElMessage.error("无法连接服务器");
    }
  } catch {
    ElMessage.warning("请填写完整评论信息");
  }
}

const resetForm = () => {
  if (formEl.value) {
    formEl.value.resetFields();
    addVisible.value = false;
  }
}
</script>

<template>
  <Dialog v-model:visible="addVisible" :draggable="false" modal header="添加评论" :style="{ width: '480px'}"
          :pt="{ header: { style: { paddingBottom:'10px',paddingTop:'10px'} }, content: { style: { borderTop: '1px solid #E2E8F0'} } }">
    <el-form :model="form" :rules="rules" ref="formEl" label-width="80px" class="form-container" label-position="left">
      <el-form-item label="笔记ID" prop="noteId">
        <el-input v-model.number="form.noteId" class="input" placeholder="请输入笔记ID"/>
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model.number="form.userId" class="input" placeholder="请输入用户ID"/>
      </el-form-item>
      <el-form-item label="父评论ID" prop="parentId">
        <el-input v-model.number="form.parentId" class="input" placeholder="可选：父评论ID"/>
      </el-form-item>
      <el-form-item label="内容" prop="content">
        <el-input v-model="form.content" type="textarea" rows="4" class="input" placeholder="请输入评论内容"/>
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