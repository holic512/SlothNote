<script setup lang="ts">
import {ref} from "vue";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
import Dialog from "primevue/dialog";
import UserAgreement from "../components/userAgreement.vue";
// 假设 service 路径正确
import pwLogin from "./service/pwLogin"
import {sendMail, verifyLoginCode} from "./service/emLogin";

const router = useRouter();
const loginType = ref<string>('password');
const username = ref<string>('');
const password = ref<string>('');
const email = ref('');
const code = ref('');
const codeSent = ref(false);
const agreedToTerms = ref(false);
const addUserVisible = ref(false);

const switchLoginType = () => {
  loginType.value = loginType.value === 'password' ? 'code' : 'password';
};

const validateTerms = () => {
  if (!agreedToTerms.value) {
    ElMessage.warning('请先阅读并同意服务协议');
    return false;
  }
  return true;
};

const submitForm = async () => {
  if (!validateTerms()) return;
  if (loginType.value === 'password') {
    await handlePasswordLogin();
  } else {
    await handleCodeLogin();
  }
};

const handlePasswordLogin = async () => {
  if (!username.value || !password.value) {
    ElMessage.warning('请输入账号密码');
    return;
  }
  // 模拟登录逻辑...
  const status = await pwLogin(username.value, password.value);
  if (status === 200) router.push("/user/main");
  else ElMessage.error('登录失败');
};

const sendEmCode = async () => {
  if (!email.value) return ElMessage.warning('请输入邮箱');
  const status = await sendMail(email.value);
  if(status === 200) {
    ElMessage.success('验证码已发送');
    codeSent.value = true;
    setTimeout(() => (codeSent.value = false), 60000);
  }
};

const handleCodeLogin = async () => {
  // 模拟验证...
  const status = await verifyLoginCode(code.value);
  if (status === 200) router.push("/user/main");
};
</script>

<template>
  <div class="notion-auth-form">
    <h1 class="title">登录</h1>
    <p class="subtitle">欢迎回来，请继续您的创作。</p>

    <el-form @submit.prevent="submitForm" class="custom-form">
      <!-- 密码登录 -->
      <div v-if="loginType === 'password'" class="form-animate">
        <div class="input-group">
          <label>用户名</label>
          <el-input v-model="username" placeholder="请输入用户名" class="notion-input"></el-input>
        </div>
        <div class="input-group">
          <label>密码</label>
          <el-input type="password" v-model="password" placeholder="请输入密码" show-password class="notion-input"></el-input>
        </div>
      </div>

      <!-- 验证码登录 -->
      <div v-else class="form-animate">
        <div class="input-group">
          <label>邮箱地址</label>
          <el-input v-model="email" placeholder="name@company.com" class="notion-input"></el-input>
        </div>
        <div class="input-group">
          <label>验证码</label>
          <div class="code-wrapper">
            <el-input v-model="code" placeholder="6位验证码" class="notion-input"></el-input>
            <button type="button" class="send-btn" @click="sendEmCode" :disabled="codeSent">
              {{ codeSent ? '已发送' : '获取验证码' }}
            </button>
          </div>
        </div>
      </div>

      <div class="terms-check">
        <el-checkbox v-model="agreedToTerms" size="large">
          我已同意 <span class="link" @click.stop="addUserVisible = true">用户服务协议</span>
        </el-checkbox>
      </div>

      <button class="notion-btn primary" @click.prevent="submitForm" :disabled="!agreedToTerms">
        继续
      </button>

      <div class="auth-footer">
        <span class="switch-link" @click="switchLoginType">
          {{ loginType === 'password' ? '使用验证码登录' : '使用密码登录' }}
        </span>
        <span class="divider">•</span>
        <span class="switch-link" @click="router.push({name:'user-register'})">
          创建新账号
        </span>
      </div>
    </el-form>

    <Dialog v-model:visible="addUserVisible" modal header="服务协议" :style="{ width: '600px' }">
      <UserAgreement/>
    </Dialog>
  </div>
</template>

<style scoped>
.notion-auth-form {
  width: 100%;
  padding: 0 20px;
}

.title {
  font-size: 32px;
  font-weight: 700;
  color: #37352f;
  text-align: center;
  margin-bottom: 8px;
}

.subtitle {
  text-align: center;
  color: #9b9a97;
  margin-bottom: 40px;
  font-size: 16px;
}

.input-group {
  margin-bottom: 18px;
}

.input-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #76746e; /* Notion Label Grey */
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 覆盖 Element Plus 样式以符合 Notion 风格 */
:deep(.notion-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(15, 15, 15, 0.1);
  border-radius: 4px;
  padding: 4px 10px;
  background: #fff;
  transition: all 0.2s ease;
}

:deep(.notion-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(15, 15, 15, 0.2);
}

:deep(.notion-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(35, 131, 226, 0.3) !important; /* Notion Blue Focus */
}

:deep(.el-input__inner) {
  height: 36px;
  color: #37352f;
}

/* 验证码特殊布局 */
.code-wrapper {
  display: flex;
  gap: 8px;
}
.send-btn {
  white-space: nowrap;
  background: transparent;
  border: 1px solid rgba(15, 15, 15, 0.1);
  border-radius: 4px;
  color: #37352f;
  cursor: pointer;
  padding: 0 12px;
  font-size: 13px;
  transition: background 0.2s;
}
.send-btn:hover:not(:disabled) {
  background: rgba(55, 53, 47, 0.08);
}
.send-btn:disabled {
  color: #9b9a97;
  cursor: not-allowed;
}

/* 自定义按钮 */
.notion-btn {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 10px;
}

.notion-btn.primary {
  background-color: #2383e2; /* Notion Blue */
  color: white;
}

.notion-btn.primary:hover {
  background-color: #1a73ca;
}

.notion-btn:disabled {
  background-color: rgba(55, 53, 47, 0.16);
  color: rgba(255, 255, 255, 0.6);
  cursor: not-allowed;
}

/* 底部链接 */
.auth-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 14px;
  color: #76746e;
}

.switch-link {
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 4px;
  text-decoration-color: rgba(55, 53, 47, 0.2);
  transition: color 0.2s;
}

.switch-link:hover {
  color: #37352f;
  text-decoration-color: #37352f;
}

.divider {
  margin: 0 10px;
  color: #d3d1cb;
}

.link {
  color: #2383e2;
  cursor: pointer;
}

.form-animate {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>