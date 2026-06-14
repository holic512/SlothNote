<!--
@file UserProfileInitDialog
@project SlothNote
@module 用户端 / 资料初始化引导
@description 提供首次进入用户端时的资料初始化步骤弹窗。
@logic 1. 通过 defineModel 接收父组件显示状态；2. 在欢迎、资料、功能介绍步骤间切换；3. 完成或跳过时关闭弹窗并记录本地完成状态。
@dependencies ElementPlus: el-steps/el-button, Component: WelcomePage/ProfileSetup/FeatureGuide
@index_tags 用户资料初始化, v-model, 引导弹窗, 首次进入
@author holic512
-->
<script setup lang="ts">
import {ref} from 'vue';
import {ElMessage} from 'element-plus';
import WelcomePage from './components/WelcomePage.vue';
import ProfileSetup from './components/ProfileSetup.vue';
import FeatureGuide from './components/FeatureGuide.vue';

interface StepConfig {
  title: string;
}

// 步骤配置
const steps: StepConfig[] = [
  {title: '欢迎'},
  {title: '个人信息'},
  {title: 'More'}
];

// 控制整个引导流程的显示
const UserProfileInitVis = defineModel<boolean>({default: false});

// 当前步骤（0: 欢迎页面, 1: 个人信息, 2: 功能指导）
const currentStep = ref(0);
// 是否显示过渡动画
const isTransitioning = ref(false);

// 处理下一步操作
const handleNextStep = async () => {
  if (isTransitioning.value) return;

  isTransitioning.value = true;
  if (currentStep.value < steps.length - 1) {
    currentStep.value++;
  } else {
    completeInitialization();
  }

  setTimeout(() => {
    isTransitioning.value = false;
  }, 300);
};

// 处理上一步操作
const handlePrevStep = async () => {
  if (isTransitioning.value) return;

  isTransitioning.value = true;
  if (currentStep.value > 0) {
    currentStep.value--;
  }

  setTimeout(() => {
    isTransitioning.value = false;
  }, 300);
};

// 完成初始化
const completeInitialization = () => {
  localStorage.setItem('hasCompletedInit', 'true');
  UserProfileInitVis.value = false;
};

// 跳过引导
const skipInitialization = () => {
  ElMessage({
    message: '已跳过引导，你可以在设置中重新查看',
    type: 'info'
  });
  completeInitialization();
};


</script>

<template>
  <!-- 遮罩层 -->
  <div v-if="UserProfileInitVis" class="user-profile-overlay"></div>

  <Transition name="fade">
    <div v-if="UserProfileInitVis" class="user-profile-container">
      <!-- 关闭按钮 -->
      <div class="skip-btn-box">
        <el-button text class="skip-btn" @click="skipInitialization" size="small">
          跳过
        </el-button>
      </div>

      <div>
        <!-- 步骤指示器 -->
        <el-steps
            :active="currentStep"
            process-status="process"
            align-center
        >
          <el-step
              v-for="(step, index) in steps"
              :key="index"
              :title="step.title"
          />
        </el-steps>
      </div>

      <!-- 步骤内容 -->
      <div class="step-content" :class="{ transitioning: isTransitioning }">
        <Transition name="slide" mode="out-in">
          <WelcomePage
              v-if="currentStep === 0"
              @next="handleNextStep"
          />
          <ProfileSetup
              v-else-if="currentStep === 1"
              @prev="handlePrevStep"
              @next="handleNextStep"
          />
          <FeatureGuide
              v-else-if="currentStep === 2"
              @prev="handlePrevStep"
              @next="handleNextStep"
          />
        </Transition>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.user-profile-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(2px);

  z-index: 1000;
}

.user-profile-container {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);

  width: 500px;
  height: 600px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
  padding: 12px;
  z-index: 1000;

  display: flex;
  flex-direction: column;

}

.skip-btn-box {
  display: flex;
  justify-content: end;
}


.step-content {
  flex: 1;
  padding: 4px;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.transitioning {
  pointer-events: none;
}
</style>
