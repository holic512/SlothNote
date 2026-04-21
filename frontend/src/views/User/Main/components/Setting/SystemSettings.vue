<script setup lang="ts">
import {SwitchButton} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {useRouter} from "vue-router";
import {tokenStore} from "@/pinia/token";
import {logout} from "@/views/User/Main/components/Setting/service/logout";

const router = useRouter();

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确认退出当前账号登录状态？', '退出登录', {
      type: 'warning',
      confirmButtonText: '退出',
      cancelButtonText: '取消',
    });

    const status = await logout();
    if (status !== 200) {
      ElMessage.error('退出登录失败，请稍后重试');
      return;
    }

    tokenStore().clearUserToken();
    await router.push('/user/auth/login');
    ElMessage.success('已退出登录');
  } catch (error) {
    // 用户取消时不提示
  }
}
</script>

<template>
  <div class="system-settings">
    <h1 class="title">系统设置</h1>
    <el-divider class="divider"/>

    <div class="setting-card">
      <div class="setting-info">
        <h3>退出登录</h3>
        <p>退出当前账号并返回登录页。</p>
      </div>

      <el-button type="danger" plain @click="handleLogout">
        <el-icon class="el-icon--left"><SwitchButton /></el-icon>
        退出登录
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.system-settings {
  padding-right: 4px;
}

.title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.divider {
  margin: 14px 0 20px 0;
}

.setting-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #ebeef5;
}

.setting-info h3 {
  margin: 0 0 6px 0;
  font-size: 16px;
  color: #111827;
}

.setting-info p {
  margin: 0;
  font-size: 13px;
  color: #6b7280;
}
</style>
