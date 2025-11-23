<script setup lang="ts">
import {useRouter} from "vue-router";
import {onMounted, ref} from "vue";

const router = useRouter();
const isScrolled = ref(false);

// 监听滚动以改变导航栏样式
onMounted(() => {
  window.addEventListener('scroll', () => {
    isScrolled.value = window.scrollY > 20;
  });
});
</script>

<template>
  <div class="landing-page">

    <!-- 1. 顶部导航栏: 滚动后显示边框 -->
    <nav class="navbar" :class="{ 'scrolled': isScrolled }">
      <div class="nav-content">
        <div class="nav-left">
          <img src="/favicon.ico" alt="logo" class="logo-img"/>
          <span class="brand-name">SlothNote</span>
        </div>

        <div class="nav-right">
          <div class="divider"></div>
          <button class="btn-text" @click="router.push('/user/login')">登录</button>
          <button class="btn-primary" @click="router.push('/user/register')">免费获取 SlothNote</button>
        </div>
      </div>
    </nav>

    <!-- 2. Hero 区域: 核心价值主张 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="main-title">
          记下，<br>
          所有的一切。
        </h1>
        <div class="sub-title-wrapper">
          <span class="brand-gradient">SlothNote</span>
          <p class="sub-description">
            为你的团队打造的互联工作空间。<br>
            高效完成任务，保持专注，心无旁骛。
          </p>
        </div>

        <div class="cta-group">
          <button class="btn-large-primary" @click="router.push('/user/register')">免费试用 SlothNote</button>
          <button class="btn-large-text" @click="router.push('/user/register')">查看演示 →</button>
        </div>
      </div>

      <!-- 模拟软件界面的截图展示 -->
      <div class="app-preview-container">
        <!-- 如果没有图片，可以使用 placeholder 或替换为你真实的 image-removebg-preview.png -->
        <!-- 这里模拟一个带有浏览器窗口效果的图片容器 -->
        <div class="browser-window">
          <div class="window-header">
            <div class="dots">
              <span></span><span></span><span></span>
            </div>
            <div class="address-bar">slothnote.com/workspace</div>
          </div>
          <div class="window-content">
            <img src="/image-removebg-preview.png" alt="App Preview" class="hero-img"/>
            <!-- 若无图片，暂时用文字占位，实际使用请删掉下方 h3 -->
            <div class="placeholder-content" v-if="false">
              <h3>此处放置你的应用截图</h3>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 3. 功能特性区域 (Bento Grid 风格) -->
    <section class="features-section">
      <div class="section-header">
        <h2 class="section-title">
          <span class="highlight">功能。</span>
          <span class="normal">为您打开无限可能。</span>
        </h2>
      </div>

      <div class="feature-grid">
        <!-- 卡片 1 -->
        <div class="feature-card large">
          <div class="card-icon">📝</div>
          <h3>像文档一样流畅</h3>
          <p>支持 Markdown，即写即存。无论是会议纪要还是灵感随笔，都能优雅呈现。</p>
        </div>

        <!-- 卡片 2 -->
        <div class="feature-card">
          <div class="card-icon">✅</div>
          <h3>任务管理</h3>
          <p>看板、列表、日历视图，随心切换。</p>
        </div>

        <!-- 卡片 3 -->
        <div class="feature-card">
          <div class="card-icon">🧠</div>
          <h3>AI 辅助</h3>
          <p>自动续写、润色、总结，让创作更简单。</p>
        </div>

        <!-- 卡片 4 -->
        <div class="feature-card wide">
          <div class="card-icon">🎨</div>
          <h3>拒绝混乱，井井有条</h3>
          <p>通过无限层级的页面结构，构建属于你的知识网络。</p>
        </div>
      </div>
    </section>

    <!-- 4. 底部 Footer -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-logo">
          <img src="/favicon.ico" style="width: 24px;"/>
          <span>SlothNote</span>
        </div>
        <div class="footer-links">
          <div class="col">
            <h4>产品</h4>
            <a href="#">功能</a>
            <a href="#">AI</a>
            <a href="#">定价</a>
          </div>
          <div class="col">
            <h4>资源</h4>
            <a href="#">博客</a>
            <a href="#">社区</a>
            <a href="#">帮助中心</a>
          </div>
          <div class="col">
            <h4>公司</h4>
            <a href="#">关于我们</a>
            <a href="#">联系方式</a>
            <a href="#" class="beian">备案号: XXXXXX</a>
          </div>
        </div>
      </div>
      <div class="copyright">
        © 2025 SlothNote Inc. All rights reserved.
      </div>
    </footer>

  </div>
</template>

<style scoped>
/* --- 1. 全局基础设定 (仿 Notion 字体) --- */
.landing-page {
  width: 100%;
  min-height: 100vh;
  background-color: #FFFFFF;
  color: #37352f; /* Notion 标志性黑色 */
  font-family: ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, "Apple Color Emoji", Arial, sans-serif, "Segoe UI Emoji", "Segoe UI Symbol";
  overflow-x: hidden;
}

/* --- 2. 导航栏 --- */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 60px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  z-index: 100;
  display: flex;
  justify-content: center;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s ease;
}

.navbar.scrolled {
  border-bottom: 1px solid rgba(55, 53, 47, 0.09);
}

.nav-content {
  width: 100%;
  max-width: 1200px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.brand-name {
  font-weight: 700;
  font-size: 18px;
  letter-spacing: -0.5px;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-links {
  display: flex;
  gap: 24px;
  margin-right: 16px;
}

/* 隐藏在小屏幕下的链接 */
@media (max-width: 768px) {
  .nav-links, .divider {
    display: none;
  }
}

.nav-item {
  text-decoration: none;
  color: #37352f;
  font-size: 15px;
  font-weight: 500;
}

.nav-item:hover {
  background-color: rgba(55, 53, 47, 0.08);
  border-radius: 4px;
}

.divider {
  width: 1px;
  height: 24px;
  background-color: #E0E0E0;
}

.btn-text {
  background: none;
  border: none;
  font-size: 15px;
  cursor: pointer;
  color: #37352f;
  padding: 6px 12px;
  border-radius: 4px;
}

.btn-text:hover {
  background-color: rgba(55, 53, 47, 0.08);
}

.btn-primary {
  background-color: #111; /* Notion 风格黑色按钮 */
  color: #fff;
  border: none;
  padding: 8px 14px;
  border-radius: 5px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-primary:hover {
  background-color: #333;
}

/* --- 3. Hero 区域 --- */
.hero-section {
  padding-top: 120px;
  padding-bottom: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 1100px;
  margin: 0 auto;
}

.main-title {
  font-size: 64px;
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: -2px;
  margin-bottom: 24px;
  color: #000;
}

.sub-title-wrapper {
  margin-bottom: 32px;
}

.brand-gradient {
  font-size: 42px;
  font-weight: 700;
  background: linear-gradient(90deg, #8EBCF3, #9185ED, #F675B9);
  -webkit-background-clip: text;
  color: transparent;
  display: block;
  margin-bottom: 16px;
}

.sub-description {
  font-size: 22px;
  line-height: 1.5;
  color: #37352f;
  max-width: 600px;
  margin: 0 auto;
  font-weight: 500;
}

.cta-group {
  display: flex;
  gap: 16px;
  margin-bottom: 60px;
}

.btn-large-primary {
  background-color: #111;
  color: #fff;
  border: none;
  padding: 12px 24px;
  border-radius: 6px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
}

.btn-large-primary:hover {
  background-color: #333;
}

.btn-large-text {
  background: none;
  border: none;
  color: #2383e2;
  font-size: 18px;
  font-weight: 500;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 4px;
  text-decoration-color: rgba(35, 131, 226, 0.4);
}

.btn-large-text:hover {
  color: #1a73ca;
  text-decoration-color: #1a73ca;
}

/* --- 预览图窗口效果 --- */
.app-preview-container {
  width: 90%;
  max-width: 1000px;
  perspective: 1000px; /* 3D 透视 */
}

.browser-window {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 50px -12px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: transform 0.4s ease;
}

.browser-window:hover {
  transform: translateY(-5px);
}

.window-header {
  height: 40px;
  background: #f7f7f5;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  padding: 0 16px;
  position: relative;
}

.dots {
  display: flex;
  gap: 6px;
}

.dots span {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: #e0e0e0;
}

.dots span:nth-child(1) {
  background: #FF5F57;
}

.dots span:nth-child(2) {
  background: #FEBC2E;
}

.dots span:nth-child(3) {
  background: #28C840;
}

.address-bar {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #999;
  background: #fff;
  padding: 4px 20px;
  border-radius: 4px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  width: 40%;
  text-align: center;
}

.window-content {
  width: 100%;
  min-height: 400px;
  background: #fff;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.hero-img {
  width: 100%;
  height: auto;
  display: block;
}

/* --- 4. 特性区域 --- */
.features-section {
  padding: 80px 24px;
  max-width: 1100px;
  margin: 0 auto;
}

.section-header {
  margin-bottom: 48px;
  text-align: left;
  padding-left: 10px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: -1px;
}

.highlight {
  background: linear-gradient(90deg, #8EBCF3, #9185ED);
  -webkit-background-clip: text;
  color: transparent;
  margin-right: 12px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, auto);
  gap: 24px;
}

.feature-card {
  background: #F7F7F5; /* Notion 浅灰背景 */
  border: 1px solid rgba(55, 53, 47, 0.05);
  border-radius: 16px;
  padding: 32px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.feature-card:hover {
  background: #F0F0ED;
  transform: translateY(-4px);
}

.card-icon {
  font-size: 32px;
  margin-bottom: 16px;
}

.feature-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
}

.feature-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

/* 网格布局控制 */
.large {
  grid-column: span 2; /* 占据两列 */
}

.wide {
  grid-column: span 3; /* 占据整行 */
}

@media (max-width: 768px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }

  .large, .wide {
    grid-column: span 1;
  }

  .main-title {
    font-size: 42px;
  }
}

/* --- 5. Footer --- */
.footer {
  border-top: 1px solid #EFEFEF;
  padding: 60px 24px 30px;
  background-color: #FBFAF9;
}

.footer-content {
  max-width: 1100px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 40px;
}

.footer-logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 16px;
}

.footer-links {
  display: flex;
  gap: 60px;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.col h4 {
  font-size: 14px;
  font-weight: 700;
  color: #37352f;
  margin-bottom: 8px;
}

.col a {
  font-size: 14px;
  color: #6B6B6B;
  text-decoration: none;
  transition: color 0.2s;
}

.col a:hover {
  color: #2383e2;
  text-decoration: underline;
}

.copyright {
  text-align: center;
  margin-top: 60px;
  color: #999;
  font-size: 12px;
}
</style>