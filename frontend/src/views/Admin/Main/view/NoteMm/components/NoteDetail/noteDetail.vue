<script setup lang="ts">
import Dialog from 'primevue/dialog';
import {computed, ref, watch} from 'vue';
import {fetchFolderOptions, fetchNoteFullDetail, fetchUserOptions, type FolderOption, type NoteRow, type UserOption, updateNote, updateNoteContent} from '../../service/noteMm';
import {ElMessage} from 'element-plus';

const visible = defineModel<boolean>();
const noteId = defineModel<number>('noteId');
const emit = defineEmits<{
  success: []
}>();

const detail = ref<NoteRow | null>(null);
const content = ref<string>('');
const userOptions = ref<UserOption[]>([]);
const folderOptions = ref<FolderOption[]>([]);
const currentFolderOption = ref<FolderOption | null>(null);
const contentError = ref<string>('');
const previewState = computed(() => {
  if (!content.value) {
    return { invalid: false, formatted: '' };
  }
  try {
    const parsed = JSON.parse(content.value);
    return { invalid: false, formatted: JSON.stringify(parsed, null, 2) };
  } catch {
    return { invalid: true, formatted: content.value };
  }
});

watch(visible, async (v) => {
  if (v && noteId.value) {
    const response = await fetchNoteFullDetail(noteId.value);
    detail.value = response.data;
    content.value = response.content;
    contentError.value = '';
    userOptions.value = await fetchUserOptions(undefined, 50);
    await loadFolderOptions(undefined, true);
  }
});

watch(() => detail.value?.userId, async (nextUserId, prevUserId) => {
  if (!visible.value || !detail.value || nextUserId == null) {
    return;
  }
  if (prevUserId !== undefined && nextUserId !== prevUserId) {
    detail.value.folderId = null;
    currentFolderOption.value = null;
  }
  await loadFolderOptions(undefined, true);
});

const loadUserOptions = async (q?: string) => {
  userOptions.value = await fetchUserOptions(q, 50);
};

const loadFolderOptions = async (q?: string, includeCurrent = false) => {
  if (!detail.value?.userId) {
    folderOptions.value = [];
    return;
  }
  const options = await fetchFolderOptions(q, detail.value.userId, 50);
  folderOptions.value = options;
  if (includeCurrent && detail.value.folderId != null) {
    const exists = options.some((item) => item.id === detail.value?.folderId);
    if (!exists) {
      currentFolderOption.value = {
        id: detail.value.folderId,
        folderName: `当前文件夹 #${detail.value.folderId}（可能已删除）`,
        userId: detail.value.userId,
      };
    } else {
      currentFolderOption.value = null;
    }
  }
};

const formatJson = () => {
  if (!content.value.trim()) {
    contentError.value = '';
    return;
  }
  try {
    content.value = JSON.stringify(JSON.parse(content.value), null, 2);
    contentError.value = '';
  } catch {
    contentError.value = '内容不是合法的 JSON，无法格式化';
  }
};

const onSaveInfo = async () => {
  if (!detail.value) return;
  const result = await updateNote({
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
  if (result.status === 200) {
    ElMessage.success('信息保存成功');
    visible.value = false;
    emit('success');
  } else {
    ElMessage.error(result.message || '无法连接服务器');
  }
}

const onSaveContent = async () => {
  if (!noteId.value) return;
  try {
    JSON.parse(content.value);
    contentError.value = '';
  } catch {
    contentError.value = '内容不是合法的 JSON，请修正后再保存';
    ElMessage.error(contentError.value);
    return;
  }
  const result = await updateNoteContent(noteId.value, content.value);
  if (result.status === 200) {
    ElMessage.success('内容保存成功');
    if (detail.value && result.meta) {
      detail.value.hasContent = result.meta.hasContent;
      detail.value.lastSavedAt = result.meta.lastSavedAt;
    }
    emit('success');
  } else {
    contentError.value = result.message;
    ElMessage.error(result.message || '无法连接服务器');
  }
}
</script>

<template>
  <Dialog v-model:visible="visible" :draggable="false" modal header="笔记编辑" :style="{ width: '760px'}">
    <div v-if="detail">
      <el-form label-width="120px" class="form-container" label-position="left">
        <el-form-item label="ID"><el-input v-model="detail.id" disabled /></el-form-item>
        <el-form-item label="用户">
          <el-select v-model="detail.userId" style="width: 100%" filterable remote clearable :remote-method="loadUserOptions" :reserve-keyword="true" @visible-change="(v:boolean)=>{ if(v) loadUserOptions() }">
            <el-option v-for="user in userOptions" :key="user.id" :label="`${user.username} (${user.email})`" :value="user.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件夹">
          <el-select v-model="detail.folderId" style="width: 100%" filterable remote clearable :disabled="!detail.userId" :remote-method="(q:string)=>loadFolderOptions(q, true)" :reserve-keyword="true" @visible-change="(v:boolean)=>{ if(v) loadFolderOptions(undefined, true) }">
            <el-option v-for="folder in folderOptions" :key="folder.id" :label="`${folder.folderName} (#${folder.id})`" :value="folder.id" />
            <el-option v-if="currentFolderOption" :key="`current-${currentFolderOption.id}`" :label="`${currentFolderOption.folderName}`" :value="currentFolderOption.id" />
          </el-select>
        </el-form-item>
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
        <el-form-item label="正文状态">
          <div class="meta-row">
            <el-tag :type="detail.hasContent ? 'success' : 'info'">{{ detail.hasContent ? '有正文' : '无正文' }}</el-tag>
            <span class="meta-text">最后保存：{{ detail.lastSavedAt || '未保存' }}</span>
          </div>
        </el-form-item>
      </el-form>

      <el-divider />
      <el-form label-width="120px" class="form-container" label-position="left">
        <el-form-item label="内容(JSON)">
          <el-input v-model="content" type="textarea" :rows="10" placeholder="tiptap JSON" />
        </el-form-item>
        <el-form-item label="预览">
          <div class="preview-panel">
            <el-tag :type="previewState.invalid ? 'danger' : 'success'">{{ previewState.invalid ? 'JSON异常' : 'JSON有效' }}</el-tag>
            <pre class="preview-text">{{ previewState.formatted }}</pre>
          </div>
        </el-form-item>
        <el-form-item v-if="contentError" label="校验">
          <el-alert :title="contentError" type="error" :closable="false" show-icon />
        </el-form-item>
      </el-form>

      <div style="padding: 0 16px 0 16px; display: flex; gap: 8px">
        <el-button color="#020617" @click="onSaveInfo">保存信息</el-button>
        <el-button @click="formatJson">格式化 JSON</el-button>
        <el-button type="primary" @click="onSaveContent">保存内容</el-button>
        <el-button type="info" @click="()=>{ visible = false }" plain>取消</el-button>
      </div>
    </div>
  </Dialog>
</template>

<style scoped>
.form-container { max-width: 720px; display: flex; flex-direction: column; gap: 6px; padding: 16px; }
.meta-row { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.meta-text { color: #64748b; font-size: 13px; }
.preview-panel { width: 100%; border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; background: #f8fafc; }
.preview-text { margin: 12px 0 0; white-space: pre-wrap; word-break: break-word; max-height: 240px; overflow: auto; }
</style>
