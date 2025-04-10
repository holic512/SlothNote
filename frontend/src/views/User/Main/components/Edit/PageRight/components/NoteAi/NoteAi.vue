<script setup lang="ts">
import AiPage from "./components/AiPage.vue";

const editor = defineModel()

</script>

<template>
  <div class="ai-container">
    <AiPage v-model="editor"/>
  </div>
</template>

<style scoped>
.ai-container {
  height: 100%;
  width: 380px;
  display: flex;
  flex-direction: column;
  background-color: #ffffff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);
  overflow: hidden; /* 防止子元素溢出 */
}
</style>