<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { initAdmin, login, verCode } from "../services/login";
import InputOtp from 'primevue/inputotp';
import { useRouter } from 'vue-router';
import { Lock, Message, Monitor } from '@element-plus/icons-vue';

const router = useRouter();
const currentView = ref<'login' | 'otp' | 'init'>('login');
const isLoading = ref(false);

const username = ref('');
const password = ref('');
const codeValue = ref('');

const initUsername = ref('');
const initPassword = ref('');
const initConfirmPassword = ref('');
const initEmail = ref('');

const initEmailHint = computed(() => initEmail.value.trim() ? '已配置邮箱，后续可用于安全通知。' : '邮箱可暂时留空，后续可在设置页补录。');

const enterAdmin = async () => {
  await router.push("/admin/main");
};

const sendLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入完整的账号和密码');
    return;
  }

  isLoading.value = true;
  try {
    const result = await login(username.value, password.value);
    if (result.status === 200 && result.data?.token) {
      ElMessage.success(result.message || '欢迎回来，管理员');
      await enterAdmin();
      return;
    }
    if (result.data?.needInit) {
      initUsername.value = username.value;
      currentView.value = 'init';
      ElMessage.warning(result.message || '系统尚未初始化管理员');
      return;
    }
    if (result.data?.requiresVerification) {
      currentView.value = 'otp';
      ElMessage.success(result.message || '验证码已发送');
      return;
    }
    ElMessage.error(result.message || '登录失败');
  } finally {
    isLoading.value = false;
  }
};

const verifyLogin = async () => {
  if (!codeValue.value || codeValue.value.length !== 6) {
    ElMessage.warning('请输入6位验证码');
    return;
  }

  isLoading.value = true;
  try {
    const result = await verCode(codeValue.value);
    if (result.status === 200 && result.data?.token) {
      ElMessage.success(result.message || '欢迎回来，管理员');
      await enterAdmin();
      return;
    }
    ElMessage.error(result.message || '验证失败');
  } finally {
    isLoading.value = false;
  }
};

const submitInit = async () => {
  if (!initUsername.value || !initPassword.value) {
    ElMessage.warning('请输入管理员账号和密码');
    return;
  }
  if (initPassword.value !== initConfirmPassword.value) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }

  isLoading.value = true;
  try {
    const result = await initAdmin(initUsername.value, initPassword.value, initEmail.value.trim());
    if (result.status === 200 && result.data?.token) {
      ElMessage.success(result.message || '管理员初始化成功');
      await enterAdmin();
      return;
    }
    if (result.status === 409 && !result.data?.needInit) {
      currentView.value = 'login';
    }
    ElMessage.error(result.message || '管理员初始化失败');
  } finally {
    isLoading.value = false;
  }
};

const backToLogin = () => {
  codeValue.value = '';
  currentView.value = 'login';
};
</script>

<template>
  <div class="admin-layout">
    <!-- 背景点阵纹理 -->
    <div class="bg-pattern"></div>

    <div class="login-wrapper">
      <!-- 顶部 Logo 区域 -->
      <div class="brand-header">
        <div class="icon-box">
          <el-icon :size="24" color="#333"><Monitor /></el-icon>
        </div>
        <div class="brand-text">
          <h2>Admin Console</h2>
          <p>SlothNote 管理控制台</p>
        </div>
      </div>

      <!-- 动画过渡容器 -->
      <Transition name="fade-slide" mode="out-in">

        <div class="form-card" v-if="currentView === 'login'" key="step1">
          <h3 class="card-title">身份验证</h3>
          <el-form @submit.prevent="sendLogin">
            <div class="input-item">
              <label>管理员账号</label>
              <el-input
                  v-model="username"
                  placeholder="admin"
                  class="custom-input"
                  :prefix-icon="Monitor"
              />
            </div>

            <div class="input-item">
              <label>密码</label>
              <el-input
                  v-model="password"
                  type="password"
                  placeholder="••••••••"
                  show-password
                  class="custom-input"
                  @keyup.enter="sendLogin"
              />
            </div>

            <el-button
                type="primary"
                class="submit-btn"
                :loading="isLoading"
                @click="sendLogin"
            >
              登录后台
            </el-button>
          </el-form>
        </div>

        <div class="form-card" v-else-if="currentView === 'otp'" key="step2">
          <div class="back-btn" @click="backToLogin">← 返回</div>
          <div class="otp-header">
            <div class="lock-icon">
              <el-icon :size="28"><Lock /></el-icon>
            </div>
            <h3 class="card-title">安全检查</h3>
            <p class="otp-desc">请输入发送至您安全邮箱的 6 位动态口令</p>
          </div>

          <div class="otp-container">
            <InputOtp v-model="codeValue" integerOnly :length="6" />
          </div>

          <el-button
              type="primary"
              class="submit-btn"
              :loading="isLoading"
              @click="verifyLogin"
          >
            验证并进入
          </el-button>
        </div>

        <div class="form-card" v-else key="step3">
          <div class="back-btn" @click="backToLogin">← 返回登录</div>
          <div class="otp-header">
            <div class="lock-icon">
              <el-icon :size="28"><Monitor /></el-icon>
            </div>
            <h3 class="card-title">初始化管理员</h3>
            <p class="otp-desc">当前系统尚未创建管理员账号，请先完成首次初始化。</p>
          </div>

          <el-form @submit.prevent="submitInit">
            <div class="input-item">
              <label>管理员账号</label>
              <el-input v-model="initUsername" placeholder="admin" class="custom-input" :prefix-icon="Monitor" />
            </div>

            <div class="input-item">
              <label>密码</label>
              <el-input v-model="initPassword" type="password" placeholder="请输入密码" show-password class="custom-input" />
            </div>

            <div class="input-item">
              <label>确认密码</label>
              <el-input v-model="initConfirmPassword" type="password" placeholder="请再次输入密码" show-password class="custom-input" />
            </div>

            <div class="input-item">
              <label>邮箱（可选）</label>
              <el-input v-model="initEmail" placeholder="admin@example.com" class="custom-input" :prefix-icon="Message" />
              <p class="inline-hint">{{ initEmailHint }}</p>
            </div>

            <el-button
                type="primary"
                class="submit-btn"
                :loading="isLoading"
                @click="submitInit"
            >
              创建并进入后台
            </el-button>
          </el-form>
        </div>

      </Transition>
    </div>
  </div>
</template>

<style scoped>
/* 1. 布局与背景 */
.admin-layout {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background-color: #F7F7F5; /* Notion 灰 */
  overflow: hidden;
  font-family: ui-sans-serif, -apple-system, BlinkMacSystemFont, "Segoe UI", Helvetica, Arial, sans-serif;
}

/* 点阵背景特效 */
.bg-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: radial-gradient(#d0d0d0 1px, transparent 1px);
  background-size: 24px 24px;
  opacity: 0.6;
  z-index: 0;
}

.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 400px;
}

/* 2. 品牌头部 */
.brand-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
}

.icon-box {
  width: 48px;
  height: 48px;
  background: #fff;
  border: 1px solid rgba(0,0,0,0.1);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.brand-text h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #333;
  letter-spacing: -0.5px;
}

.brand-text p {
  margin: 2px 0 0 0;
  font-size: 13px;
  color: #888;
}

/* 3. 卡片通用样式 */
.form-card {
  width: 100%;
  background: #fff;
  padding: 32px;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.06);
  box-shadow: 0 4px 24px rgba(0,0,0,0.04);
}

.card-title {
  margin: 0 0 24px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
}

/* 4. 表单元素 */
.input-item {
  margin-bottom: 20px;
}

.input-item label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #666;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.inline-hint {
  margin: 8px 0 0;
  font-size: 12px;
  line-height: 1.5;
  color: #8a7768;
}

/* 深度修改 Element Plus Input 样式 */
:deep(.custom-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px #E0E0E0 !important;
  border-radius: 6px;
  padding: 6px 12px;
  background-color: #FAFAFA;
  transition: all 0.2s;
}

:deep(.custom-input .el-input__wrapper:hover) {
  background-color: #fff;
}

:deep(.custom-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #333 !important; /* 聚焦时黑色边框 */
  background-color: #fff;
}

:deep(.custom-input .el-input__inner) {
  height: 38px;
  color: #333;
  font-weight: 500;
}

.submit-btn {
  width: 100%;
  height: 44px;
  background-color: #333;
  border-color: #333;
  font-size: 15px;
  font-weight: 600;
  border-radius: 6px;
  margin-top: 8px;
  transition: all 0.2s;
}

.submit-btn:hover {
  background-color: #000;
  border-color: #000;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

/* 5. 二次验证页样式 */
.back-btn {
  font-size: 13px;
  color: #888;
  cursor: pointer;
  margin-bottom: 10px;
  transition: color 0.2s;
  display: inline-block;
}
.back-btn:hover { color: #333; }

.otp-header {
  text-align: center;
  margin-bottom: 24px;
}

.lock-icon {
  width: 50px;
  height: 50px;
  background: #F0F0F0;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px auto;
  color: #333;
}

.otp-desc {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.otp-container {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

/* 深度修改 PrimeVue OTP 样式 */
:deep(.p-inputotp-input) {
  width: 42px;
  height: 48px;
  font-size: 20px;
  text-align: center;
  border: 1px solid #E0E0E0;
  border-radius: 6px;
  background-color: #FAFAFA;
  color: #333;
  font-weight: 600;
  margin: 0 4px;
  transition: all 0.2s;
}

:deep(.p-inputotp-input:focus) {
  border-color: #333;
  background-color: #fff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(0,0,0,0.1);
}

/* 6. 动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateX(20px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
