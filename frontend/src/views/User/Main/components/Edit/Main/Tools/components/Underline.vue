<script setup lang="ts">

import {isUnderlineActive, underline} from "@/views/User/Main/components/Edit/Main/Tools/Service/underline";
import '../css/editTool-button.css'
import IconUnderline from "@/views/User/Main/components/Edit/Main/Tools/icon/IconUnderline.vue";

const editor:any = defineModel()
</script>

<template>
  <el-tooltip
      class="box-item"
      effect="dark"
      content=" 下划线 "
      :show-after="500"
      placement="bottom"
  >

    <!-- 下划线 -->
    <el-button text class="tool-button" @click="underline(editor)">
      <el-icon color="#000000" size="18" v-show="!isUnderlineActive(editor)">
        <IconUnderline/>
      </el-icon>
      <el-icon color="#2383E2" size="18" v-show="isUnderlineActive(editor)">
        <IconUnderline/>
      </el-icon>
    </el-button>


  </el-tooltip>
</template>

<style scoped>

</style>