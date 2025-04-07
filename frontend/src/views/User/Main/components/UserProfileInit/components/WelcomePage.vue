<script setup lang="ts">
defineEmits(['next']);
</script>

<template>
  <div class="welcome-page">
    <h2>欢迎使用 SlothNote</h2>
    <p>让我们开始个性化设置，帮助你更好地使用 SlothNote！</p>
    <div class="button-group">
      <el-button 
        type="primary" 
        class="next-btn" 
        @click="$emit('next')"
      >
        开始设置
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.welcome-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

h2 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 16px;
  font-weight: 600;
}

p {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 32px;
}

.button-group {
  margin-top: 40px;
}

:deep(.next-btn) {
  padding: 12px 24px;
  font-size: 16px;
  background-color: #2c3e50;
  border-color: #2c3e50;
}

:deep(.next-btn:hover) {
  background-color: #34495e;
  border-color: #34495e;
}
</style> 