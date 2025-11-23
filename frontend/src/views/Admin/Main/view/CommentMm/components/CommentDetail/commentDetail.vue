<script setup lang="ts">
import Dialog from "primevue/dialog";
import {ref, watch} from "vue";
import axios from "../../../../../../../axios";

const detailVisible = defineModel()
const commentId = defineModel<number>('commentId')

const detail = ref<any>(null);
const editing = ref<boolean>(false);
const editContent = ref<string>("");

watch(detailVisible, async (v) => {
  if (v && commentId.value) {
    const resp = await axios.get("admin/commentMm/detail", { params: { id: commentId.value } });
    detail.value = resp.data.data;
    editing.value = false;
    editContent.value = detail.value?.content || "";
  }
});

const saveEdit = async () => {
  if (!commentId.value) return;
  const resp = await axios.put("admin/commentMm/update", { id: commentId.value, content: editContent.value });
  if (resp.data.status === 200) {
    const d = await axios.get("admin/commentMm/detail", { params: { id: commentId.value } });
    detail.value = d.data.data;
    editing.value = false;
  }
}
</script>

<template>
  <Dialog v-model:visible="detailVisible" :draggable="false" modal header="评论详情" :style="{ width: '520px'}"
          :pt="{ header: { style: { paddingBottom:'10px',paddingTop:'10px'} }, content: { style: { borderTop: '1px solid #E2E8F0'} } }">
    <div v-if="detail">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="笔记ID">{{ detail.noteId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
        <el-descriptions-item label="父评论ID">{{ detail.parentId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ detail.updatedAt }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.isDeleted ? '已删除' : '有效' }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div v-if="!editing">
            <el-text>{{ detail.content }}</el-text>
          </div>
          <div v-else>
            <el-input v-model="editContent" type="textarea" rows="6" />
            <div style="margin-top:8px;display:flex;gap:8px">
              <el-button size="small" type="primary" @click="saveEdit">保存</el-button>
              <el-button size="small" @click="editing=false">取消</el-button>
            </div>
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <div style="margin-top:10px;display:flex;justify-content:flex-end">
        <el-button size="small" type="info" plain @click="editing = !editing">{{ editing ? '关闭编辑' : '编辑内容' }}</el-button>
      </div>
    </div>
  </Dialog>
</template>