<script setup lang="ts">

</script>

<template>
  <div class="comment-header">
    <el-text class="title">评论与建议</el-text>
    <div class="spacer"/>
    <el-button text class="button">
      <el-icon>
        <Operation/>
      </el-icon>
    </el-button>
  </div>
</template>

<style scoped>
.comment-header {
  height: 45px;
  display: flex;
  align-items: center;
  padding: 8px;
  border-bottom: 1px #F0F3F5 solid;
}

.spacer {
  flex: 1;
}
</style>