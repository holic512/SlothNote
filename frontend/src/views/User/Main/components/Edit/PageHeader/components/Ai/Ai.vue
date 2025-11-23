<script setup lang="ts">
import {RightPageModeEnum, useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";
import {Opportunity} from '@element-plus/icons-vue';
// 控制右侧边栏状态
const rightPageState = useRightPageState()


</script>

<template>
  <el-tooltip
      class="box-item"
      effect="dark"
      content=" AI "
      :show-after="500"
      placement="bottom"
  >
    <!--Ai-->
    <el-button text class="button"
               @click="rightPageState.SwitchAi"
               :style="{
        backgroundColor: rightPageState.model === RightPageModeEnum.Ai ? '#F3F3F3' : 'transparent'
      }"
    >
      <el-icon color="#000000" size="18">
        <Opportunity/>
      </el-icon>
    </el-button>

  </el-tooltip>
</template>

<style scoped>

</style>