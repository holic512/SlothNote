<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { login, verCode } from "../services/login";
import InputOtp from 'primevue/inputotp';
import { useRouter } from 'vue-router';
import { Monitor, Lock } from '@element-plus/icons-vue'; // 引入图标，需确保安装了 @element-plus/icons-vue

const router = useRouter();
const logView = ref(true);
const isLoading = ref(false); // 添加加载状态

// 账号密码
const username = ref<string>('');
const password = ref<string>('');

// 登录逻辑
const sendLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入完整的账号和密码');
    return;
  }

  isLoading.value = true;
  try {
    const status = await login(username.value, password.value);
    const messages: { [key: number]: string } = {
      200: '验证通过',
      401: '账号或密码错误',
      404: '账号不存在',
      500: '服务器连接失败'
    };

    if (status === 200) {
      ElMessage.success(messages[status]);
      // 延迟一点切换，让用户感觉到状态变化
      setTimeout(() => {
        logView.value = false;
        isLoading.value = false;
      }, 500);
    } else {
      ElMessage.error(messages[status] || '登录失败');
      isLoading.value = false;
    }
  } catch (e) {
    isLoading.value = false;
    ElMessage.error('未知错误');
  }
};

// 二次邮箱验证逻辑
const codeValue = ref<string>('');
const verLogin = async () => {
  if (!codeValue.value || codeValue.value.length !== 6) {
    ElMessage.warning('请输入6位验证码');
    return;
  }

  isLoading.value = true;
  try {
    const status = await verCode(codeValue.value);
    const messages: { [key: number]: string } = {
      200: '欢迎回来，管理员',
      401: '验证码错误',
      404: '请求已失效',
      400: '数据格式错误',
      500: '服务器连接失败'
    };

    if (status === 200) {
      ElMessage.success(messages[status]);
      await router.push("/admin/main");
    } else {
      ElMessage.error(messages[status] || '验证失败');
    }
  } catch (e) {
    ElMessage.error('验证过程发生错误');
  } finally {
    isLoading.value = false;
  }
}
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

        <!-- 阶段一：账号密码 -->
        <div class="form-card" v-if="logView" key="step1">
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
              下一步
            </el-button>
          </el-form>
        </div>

        <!-- 阶段二：二次验证 -->
        <div class="form-card" v-else key="step2">
          <div class="back-btn" @click="logView = true">← 返回</div>
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
              @click="verLogin"
          >
            验证并进入
          </el-button>
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