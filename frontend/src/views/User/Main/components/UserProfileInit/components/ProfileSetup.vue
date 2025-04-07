<script setup lang="ts">
import { ref } from 'vue';

const emit = defineEmits(['prev', 'next']);

const userProfile = ref({
  nickname: '',
  avatar: '',
  bio: ''
});
</script>

<template>
  <div class="profile-setup">
    <div class="form-group">
      <el-input 
        v-model="userProfile.nickname" 
        placeholder="输入昵称"
        class="custom-input"
      />
      <el-input
        v-model="userProfile.bio"
        type="textarea"
        placeholder="简单介绍一下自己吧"
        :rows="4"
        class="custom-textarea"
      />
    </div>
    <div class="button-group">
      <el-button @click="$emit('prev')" class="prev-btn">上一步</el-button>
      <el-button type="primary" @click="$emit('next')" class="next-btn">下一步</el-button>
    </div>
  </div>
</template>

<style scoped>
.profile-setup {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

h2 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 32px;
  font-weight: 600;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin: 32px 0;
  padding: 0 40px;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #2c3e50;
}

:deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #dcdfe6;
}

:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #2c3e50;
}

:deep(.next-btn) {
  padding: 12px 24px;
  background-color: #2c3e50;
  border-color: #2c3e50;
}

:deep(.next-btn:hover) {
  background-color: #34495e;
  border-color: #34495e;
}

:deep(.prev-btn) {
  padding: 12px 24px;
  color: #606266;
  border-color: #dcdfe6;
}

:deep(.prev-btn:hover) {
  color: #2c3e50;
  border-color: #2c3e50;
  background-color: #f5f7fa;
}
</style> 