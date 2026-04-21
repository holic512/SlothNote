<script setup lang="ts">
import SetSidebar from "@/views/User/Main/components/Setting/SetSidebar.vue";
import UserInfo from "@/views/User/Main/components/Setting/UserInfo.vue";
import SystemSettings from "@/views/User/Main/components/Setting/SystemSettings.vue";
import {ref} from "vue";

const addUserVisible = defineModel()
const activePanel = ref<'account' | 'system'>('account')
</script>

<template>
  <el-dialog v-model="addUserVisible" class="dialog">
    <template #header="{}">

    </template>

    <el-container>
      <el-aside width="140px" style="padding:0 8px 0 0;">
        <SetSidebar v-model="activePanel"/>
      </el-aside>
      <el-main style="padding:0;">
        <!--  滚动条  -->
        <el-scrollbar height="65vh" style="padding: 0 12px 0 0">
          <UserInfo v-if="activePanel === 'account'"/>
          <SystemSettings v-else/>
        </el-scrollbar>
      </el-main>
    </el-container>
  </el-dialog>
</template>

<style scoped>
.dialog {
  min-width: 400px;
  max-width: 850px;
  width: 65vw;

  max-height: 650px;
  height: calc(70vh + 16px);
  padding: 0;
  background-color: #F7F7F5;
}
</style>
