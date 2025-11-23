<script setup lang="ts">
</script>

<template>
  <div class="notion-layout">
    <!-- 左侧：品牌与介绍 (模拟 Notion 文档风格) -->
    <div class="left-panel">
      <div class="content-wrapper">
        <div class="brand-header">
          <span class="logo-icon">🦥</span>
          <h1 class="brand-text">SlothNote</h1>
        </div>

        <div class="hero-text">
          All-in-one workspace<br>
          for your notes & tasks.
        </div>

        <div class="feature-list">
          <div class="feature-item">
            <span class="icon">📦</span>
            <span class="text">多端实时同步</span>
          </div>
          <div class="feature-item">
            <span class="icon">📝</span>
            <span class="text">Markdown 完整支持</span>
          </div>
          <div class="feature-item">
            <span class="icon">🧠</span>
            <span class="text">AI 智能补全</span>
          </div>
          <div class="feature-item">
            <span class="icon">🔒</span>
            <span class="text">端到端加密</span>
          </div>
        </div>

        <div class="quote-block">
          "🌤 今日记录，明日可见。"
        </div>
      </div>
    </div>

    <!-- 右侧：路由视图 (登录/注册) -->
    <div class="right-panel">
      <div class="auth-container">
        <Transition name="fade-slide" mode="out-in">
          <router-view />
        </Transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 全局容器 */
.notion-layout {
  display: flex;
  min-height: 100vh;
  width: 100vw;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol";
  color: #37352f;
}

/* 左侧面板 - 类似 Notion 侧边栏或 Landing Page */
.left-panel {
  flex: 1;
  background-color: #F7F7F5; /* Notion 浅灰背景 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
  position: relative;
  border-right: 1px solid rgba(55, 53, 47, 0.09);
}

/* 响应式：小屏幕隐藏左侧 */
@media (max-width: 900px) {
  .left-panel {
    display: none;
  }
}

.content-wrapper {
  max-width: 480px;
  margin: 0 auto;
}

.brand-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 32px;
}

.brand-text {
  font-size: 24px;
  font-weight: 600;
  letter-spacing: -0.5px;
  color: #37352f;
  margin: 0;
}

.hero-text {
  font-size: 42px;
  font-weight: 700;
  line-height: 1.1;
  margin-bottom: 40px;
  letter-spacing: -1px;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 60px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 500;
  color: #4f4d46;
}

.quote-block {
  padding-left: 14px;
  border-left: 3px solid #37352f;
  font-style: italic;
  color: #76746e;
  font-size: 14px;
}

/* 右侧面板 - 纯白背景 */
.right-panel {
  flex: 1.2;
  background-color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.auth-container {
  width: 100%;
  max-width: 420px;
}

/* 动画效果 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>