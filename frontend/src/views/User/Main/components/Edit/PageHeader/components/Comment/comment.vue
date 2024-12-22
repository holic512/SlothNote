<script setup lang="ts">
import {RightPageModeEnum, useRightPageState} from "@/views/User/Main/components/Edit/Pinia/RightPageState";

// 控制右侧边栏状态
const rightPageState = useRightPageState()


</script>

<template>
  <el-tooltip
      class="box-item"
      effect="dark"
      content=" 评论 "
      :show-after="500"
      placement="bottom"
  >
    <!--评论-->
    <el-button text class="button"
               @click="rightPageState.toComment"
               :style="{
        backgroundColor: rightPageState.model === RightPageModeEnum.comment ? '#F3F3F3' : 'transparent'
      }"
    >
      <el-icon color="#000000" size="18">
        <ChatLineSquare/>
      </el-icon>
    </el-button>

  </el-tooltip>
</template>

<style scoped>

</style>