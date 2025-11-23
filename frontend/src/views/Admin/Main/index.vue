<script setup lang="ts">
import Button from 'primevue/button';
import {onMounted, ref} from "vue";

import {useRouter, useRoute} from "vue-router";

const router = useRouter()
const route = useRoute()

// 页面枚举，用于定义不同的页面索引值，便于在导航时进行选择
  enum index {
    HOME,   // 首页
    DASHBOARDMm, // 仪表盘管理页面
    USERMm, // 用户管理页面
    NOTEMm, // 笔记管理页面
    COMMENTMm, // 评论管理页面
    FOLDERMm, // 文件夹管理页面
    FAVORITEMm, // 收藏管理页面
    TODOMm, // 待做管理页面
    SETTING // 设置页面
  }

// 按钮状态的响应式变量，表示当前激活的导航按钮的索引
const button = ref(index.HOME)

// 导航切换路由方法，根据传入的页面索引进行路由跳转
const toRouter = (page: index) => {
  // 更新按钮状态，表示当前选中的页面索引
  button.value = page;

  // 根据页面索引执行相应的路由跳转逻辑
  switch (page) {
    case index.HOME:
      // 跳转到首页
      router.push('/admin/main/home');
      break;
    case index.USERMm:
      // 可添加用户管理页面的跳转逻辑
      router.push('/admin/main/userMm');
      break;
    case index.NOTEMm:
      // 可添加笔记管理页面的跳转逻辑
      router.push('/admin/main/noteMm');
      break;
    case index.COMMENTMm:
      router.push('/admin/main/commentMm');
      break;
    case index.FOLDERMm:
      router.push('/admin/main/folderMm');
      break;
    case index.FAVORITEMm:
      router.push('/admin/main/favoriteMm');
      break;
    case index.TODOMm:
      router.push('/admin/main/todoMm');
      break;
    case index.SETTING:
      // 可添加设置页面的跳转逻辑
      router.push('/admin/main/setting');
      break;
    default:
      // 可以处理无效页面的跳转逻辑
      console.warn("未知页面索引");
  }
}

// 根据当前路由初始化按钮状态
const initButtonState = () => {
  switch (route.path) {
    case '/admin/main':
      button.value = index.HOME;
      break;
    case '/admin/main/home':
      button.value = index.HOME;
      break;
    case '/admin/main/dashboardMm':
      button.value = index.DASHBOARDMm;
      break;
    case '/admin/main/userMm':
      button.value = index.USERMm;
      break;
    case '/admin/main/noteMm':
      button.value = index.NOTEMm;
      break;
    case '/admin/main/commentMm':
      button.value = index.COMMENTMm;
      break;
    case '/admin/main/folderMm':
      button.value = index.FOLDERMm;
      break;
    case '/admin/main/favoriteMm':
      button.value = index.FAVORITEMm;
      break;
    case '/admin/main/todoMm':
      button.value = index.TODOMm;
      break;
    case '/admin/main/setting':
      button.value = index.SETTING;
      break;
    default:
      console.warn("未知路由路径:", route.path);
  }
}

// 在组件挂载时初始化按钮状态
onMounted(() => {
  initButtonState();
});


</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="110px" class="aside-layout">
        <div class="aside-layout-div">
          <!--          &lt;!&ndash; 图像 &ndash;&gt;-->
          <!--          <div class="aside-item">-->
          <!--            <img src="../assets/images/logo.png" class="logo"/>-->
          <!--          </div>-->

          <!-- 按钮组 -->
          <div class="button-group">
            <div
                v-tooltip="{ value: '主页', showDelay: 1000, hideDelay: 300 }">
              <Button icon="pi pi-home" v-if="button == index.HOME"/>
              <Button icon="pi pi-home" text v-else @click="toRouter(index.HOME)"/>
            </div>

            <div
                v-tooltip="{ value: '用户管理', showDelay: 1000, hideDelay: 300 }">
              <Button icon="pi pi-user" v-if="button == index.USERMm"/>
              <Button icon="pi pi-user" text v-else @click="toRouter(index.USERMm)"/>
            </div>

            <div
                v-tooltip="{ value: '笔记管理', showDelay: 1000, hideDelay: 300 }">
              <Button icon="pi pi-book" v-if="button == index.NOTEMm"/>
              <Button icon="pi pi-book" text v-else @click="toRouter(index.NOTEMm)"/>
            </div>

            <div
                v-tooltip="{ value: '评论管理', showDelay: 1000, hideDelay: 300 }">
              <Button icon="pi pi-comments" v-if="button == index.COMMENTMm"/>
              <Button icon="pi pi-comments" text v-else @click="toRouter(index.COMMENTMm)"/>
            </div>

            <div
                v-tooltip="{ value: '文件夹管理', showDelay: 1000, hideDelay: 300 }">
              <Button icon="pi pi-folder" v-if="button == index.FOLDERMm"/>
              <Button icon="pi pi-folder" text v-else @click="toRouter(index.FOLDERMm)"/>
            </div>

            <div
                v-tooltip="{ value: '收藏管理', showDelay: 1000, hideDelay: 300 }">
              <Button icon="pi pi-heart" v-if="button == index.FAVORITEMm"/>
              <Button icon="pi pi-heart" text v-else @click="toRouter(index.FAVORITEMm)"/>
            </div>

            <div
                v-tooltip="{ value: '待做管理', showDelay: 1000, hideDelay: 300 }">
              <Button icon="pi pi-check-square" v-if="button == index.TODOMm"/>
              <Button icon="pi pi-check-square" text v-else @click="toRouter(index.TODOMm)"/>
            </div>
          </div>

          <!-- 单独按钮 -->
          <div class="aside-item">
            <div
                v-tooltip="{ value: '设置', showDelay: 1000, hideDelay: 300 }">
              <Button icon="pi pi-cog" v-if="button == index.SETTING"/>
              <Button icon="pi pi-cog" text v-else @click="toRouter(index.SETTING)"/>
            </div>

          </div>
        </div>
      </el-aside>


      <el-main class="main-layout">
        <div class="main-content">
          <router-view/>
        </div>
      </el-main>

    </el-container>
  </div>
</template>

<style>
.common-layout {
  background-color: white;
}

.aside-layout {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 10px 20px 20px;
  margin-left: 5px;
}

.aside-layout-div {
  height: 94.5%;
  width: 90%; /* 根据需要调整宽度 */
  background-color: #F4F6F8;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  /*
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
   */
}

.aside-item {
  margin: 10px 0; /* 设置上下间距 */
}

.logo {
  width: 32px;
  height: 32px;
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 10px; /* 按钮间距 */
}

.button-group button {
  transition: transform 0.2s ease; /* 按钮的点击缩放动效 */
}

.button-group button:active {
  transform: scale(0.9); /* 点击时缩小效果 */
}

.main-layout {
  height: 100vh;
  padding-left: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.main-content {

  height: 94.5%;
  width: 100%;
  background-color: white;
  box-shadow: 0 0 0 1px #D9D9D9;
  border-radius: 10px;
  padding: 20px; /* 内边距 */


}

</style>
