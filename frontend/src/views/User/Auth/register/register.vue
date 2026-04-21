<script setup lang="ts">
import {reactive, ref} from "vue";
import {ElMessage, FormInstance, FormRules} from "element-plus";
import {useRouter} from "vue-router";
import Dialog from "primevue/dialog";
import UserAgreement from "../components/userAgreement.vue";
import {initiateReg} from "./services/register";

const router = useRouter();
const formEl = ref<FormInstance>();
const agreedToTerms = ref(false);
const addUserVisible = ref(false);

const form = ref({
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
  status: "ACTIVE"
});

// 保持原有验证规则
const rules = reactive<FormRules>({
  username: [{required: true, message: "请输入用户名", trigger: "blur"}],
  password: [{required: true, message: "请输入密码", trigger: "blur"}],
  confirmPassword: [{required: true, message: "请确认密码", trigger: "blur"}],
  email: [{required: true, message: "请输入邮箱", trigger: "blur"}, {type:'email', message:'格式不正确'}]
});

// 邮箱补全逻辑
const restaurants = [
  {value: '@qq.com'}, {value: '@gmail.com'}, {value: '@outlook.com'}
];
const fetchSuggestions = (queryString: string, cb: any) => {
  if (!queryString || queryString.indexOf('@') > -1) return cb([]);
  cb(restaurants.map(i => ({ value: queryString + i.value })));
};

const submitForm = async () => {
  if (!agreedToTerms.value) return ElMessage.warning('请同意协议');
  if (!formEl.value) return;

  try {
    await formEl.value.validate();
  } catch {
    return;
  }

  if (form.value.password !== form.value.confirmPassword) {
    ElMessage.error('密码不一致');
    return;
  }

  const result = await initiateReg(form.value.username, form.value.password, form.value.email);
  if (result.status === 200) {
    ElMessage.success(result.message || '注册成功');
    router.push({ name: 'user-login' });
    return;
  }

  ElMessage.error(result.message || '请求失败');
};
</script>

<template>
  <div class="notion-auth-form">
    <h1 class="title">创建账号</h1>
    <p class="subtitle">开始构建你的知识库。</p>

    <el-form :model="form" :rules="rules" ref="formEl" class="custom-form">
      <div class="input-group">
        <label>用户名</label>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="设置用户名" class="notion-input" />
        </el-form-item>
      </div>

      <div class="input-group">
        <label>邮箱</label>
        <el-form-item prop="email">
          <el-autocomplete
              v-model="form.email"
              :fetch-suggestions="fetchSuggestions"
              placeholder="name@example.com"
              class="notion-input full-width"
              :trigger-on-focus="false"
          />
        </el-form-item>
      </div>

      <div class="input-group">
        <label>密码</label>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="设置密码" class="notion-input" />
        </el-form-item>
      </div>

      <div class="input-group">
        <label>确认密码</label>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="再次输入密码" class="notion-input" />
        </el-form-item>
      </div>

      <div class="terms-check">
        <el-checkbox v-model="agreedToTerms">
          同意 <span class="link" @click="addUserVisible = true">SlothNote 协议</span>
        </el-checkbox>
      </div>

      <button class="notion-btn primary" @click.prevent="submitForm" :disabled="!agreedToTerms">
        注册
      </button>

      <div class="auth-footer">
        <span class="switch-link" @click="router.push({ name: 'user-login' })">
          已有账号？直接登录
        </span>
      </div>
    </el-form>

    <Dialog v-model:visible="addUserVisible" modal header="服务协议" :style="{ width: '600px' }">
      <UserAgreement/>
    </Dialog>
  </div>
</template>

<style scoped>
/* 复用 Login 的大部分样式，保持一致性 */
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
  margin-bottom: 30px;
  font-size: 15px;
}

.input-group {
  margin-bottom: 12px; /* 注册项多，间距稍微调小 */
}

.input-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #76746e;
  margin-bottom: 4px;
  text-transform: uppercase;
}

/* 强制移除 el-form-item 默认底边距，由 input-group 控制 */
:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.notion-input .el-input__wrapper) {
  box-shadow: 0 0 0 1px rgba(15, 15, 15, 0.1);
  border-radius: 4px;
  padding: 4px 10px;
}
:deep(.notion-input .el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(35, 131, 226, 0.3) !important;
}
:deep(.full-width) {
  width: 100%;
}

.notion-btn {
  width: 100%;
  height: 40px;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  margin-top: 16px;
  background-color: #2383e2;
  color: white;
  transition: background 0.2s;
}
.notion-btn:hover {
  background-color: #1a73ca;
}
.notion-btn:disabled {
  background-color: rgba(55, 53, 47, 0.16);
  cursor: not-allowed;
}

.auth-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
}
.switch-link {
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 4px;
  text-decoration-color: rgba(55, 53, 47, 0.2);
  color: #76746e;
}
.link {
  color: #2383e2;
  cursor: pointer;
}
</style>
