<script setup lang="ts">

// 控制页面是否显示
import {useDescriptionState} from "@/views/User/Main/components/Sidebar/Pinia/DescriptionState";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";
import {ref, watch} from "vue";
import {
  getNoteDescription
} from "@/views/User/Main/components/Sidebar/components/Description/Service/GetNoteDescription";
import {
  getFolderDescription
} from "@/views/User/Main/components/Sidebar/components/Description/Service/GetFolderDescription";
import {
  putNoteDescription
} from "@/views/User/Main/components/Sidebar/components/Description/Service/PutNoteDescription";
import {
  putFolderDescription
} from "@/views/User/Main/components/Sidebar/components/Description/Service/PutFolderDescription";
import {ElMessage} from "element-plus";

// 控制页面是否显示
const DescriptionState = useDescriptionState();

// 获取右键 选中节点的 信息
const rightSelect: any = useRightSelectNodeId();

// 输入框内容
const textarea = ref("")

// 监听 页面是否初始化显示
watch(() => DescriptionState.DescriptionVis, async (newValue) => {
  // 当为 true时 则证明显示-获取该 id
  if (newValue) {
    // 判断是笔记还是文件夹
    if (rightSelect.data.type == "NOTE") {
      textarea.value = await getNoteDescription(rightSelect.data.id);
    } else if (rightSelect.data.type == "FOLDER") {
      textarea.value = await getFolderDescription(rightSelect.data.id);
    }

  }
})

// 执行 关闭页面
const DialogClose = () => {
  DescriptionState.NoDescription();
}

// 执行 更新简介
const putDescription = async () => {
  let status: any;
  // 判断是笔记还是文件夹
  if (rightSelect.data.type == "NOTE") {
    status = await putNoteDescription(rightSelect.data.id, textarea.value);
  } else if (rightSelect.data.type == "FOLDER") {
    status = await putFolderDescription(rightSelect.data.id, textarea.value);
  }

  if (status == 200) {
    ElMessage.success("简介更新成功")
    // 执行关闭
    DialogClose();
  }
}

</script>

<template>
  <el-dialog v-model="DescriptionState.DescriptionVis" width="480" class="dialog-custom">
    <!-- 定制对话框的标题部分 -->
    <template #header="{ close, titleId, titleClass }">
      <el-text type="info">
        {{ rightSelect.data.label }}
        简介
      </el-text>
    </template>

    <el-input
        v-model="textarea"
        :autosize="{ minRows: 5, maxRows: 5 }"
        :rows="2"
        type="textarea"
        placeholder="为这个项目写一段简介"
    />

    <div style="margin-top: 12px;display: flex;justify-content: flex-end;">
      <el-button size="small" @click="putDescription()">确认</el-button>
      <el-button size="small" @click="DialogClose()">取消</el-button>
    </div>


  </el-dialog>
</template>

<style scoped>

</style>