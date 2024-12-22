<script setup lang="ts">
import {onMounted, ref} from "vue";
import CommentPage from "./components/CommentPage.vue";
import SendComment from "./components/SendComment.vue";
import CommentHeader
  from "@/views/User/Main/components/Edit/PageRight/components/NoteComment/components/CommentHeader.vue";


const open = () => {

}

</script>

<template>
  <!-- 最外层容器，撑满父元素高度 -->
  <div style="height: 100%; display: flex; flex-direction: column;width: 380px;">
    <!-- 固定高度的头部 -->
    <CommentHeader/>
    
    <!-- 内容区域，flex: 1 占据剩余空间 -->
    <div style="flex: 1; display: flex; flex-direction: column; overflow: hidden;width: 100%">
      <!-- 发送评论，固定高度 -->
      <SendComment/>
      
      <!-- 评论内容区域，flex: 1 占据剩余空间 -->
      <div style="flex: 1; overflow: hidden;width: 100%;">
        <CommentPage/>
      </div>

    </div>
  </div>
</template>

<style scoped>


</style>