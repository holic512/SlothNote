<script setup lang="ts">
defineEmits(['prev', 'next']);

const features = [
  {
    title: '笔记创建',
    description: '轻松创建和组织你的笔记'
  },
  {
    title: '标签管理',
    description: '使用标签对笔记进行分类'
  },
  {
    title: '快速搜索',
    description: '快速找到你需要的笔记'
  }
];
</script>

<template>
  <div class="feature-guide">
    <h2>功能介绍</h2>
    <div class="feature-list">
      <div v-for="(feature, index) in features" 
           :key="index" 
           class="feature-item">
        <h3>{{ feature.title }}</h3>
        <p>{{ feature.description }}</p>
      </div>
    </div>
    <div class="button-group">
      <el-button @click="$emit('prev')" class="prev-btn">上一步</el-button>
      <el-button type="primary" @click="$emit('next')" class="next-btn">开始使用</el-button>
    </div>
  </div>
</template>

<style scoped>
.feature-guide {
  text-align: center;
  padding: 40px 0;
}

h2 {
  font-size: 28px;
  color: #2c3e50;
  margin-bottom: 32px;
  font-weight: 600;
}

.feature-list {
  margin: 32px 0;
  padding: 0 40px;
}

.feature-item {
  margin: 24px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.feature-item h3 {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 8px;
  font-weight: 600;
}

.feature-item p {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 40px;
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