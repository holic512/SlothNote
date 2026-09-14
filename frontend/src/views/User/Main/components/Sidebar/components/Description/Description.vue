<!--
@file NodeDescriptionDialog
@project SlothNote
@module 用户端 / 侧栏节点简介
@description 查看和更新笔记或文件夹简介。
@logic 1. 打开时捕获节点快照并加载简介；2. 仅当前弹窗和当前节点对应的最新响应可写入表单；3. 提交仍按捕获节点执行。
@dependencies Store: DescriptionState/RightSelectNodeId, Service: Description APIs
@index_tags 节点简介, 笔记树, 请求竞态, 对话框
@author holic512
-->
<script setup lang="ts">

// 控制页面是否显示
import {useDescriptionState} from "@/views/User/Main/components/Sidebar/Pinia/DescriptionState";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";
import {onBeforeUnmount, ref, watch} from "vue";
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
let descriptionRequestId = 0;

// 监听 页面是否初始化显示
watch(() => DescriptionState.DescriptionVis, async (newValue) => {
  const requestId = ++descriptionRequestId;
  if (!newValue) return;

  const selectedId = rightSelect.data.id;
  const selectedType = rightSelect.data.type;
  textarea.value = "";

  // 当为 true时 则证明显示-获取该 id
  let nextDescription = "";
  if (selectedType == "NOTE") {
    nextDescription = await getNoteDescription(selectedId);
  } else if (selectedType == "FOLDER") {
    nextDescription = await getFolderDescription(selectedId);
  }

  if (
      requestId !== descriptionRequestId
      || !DescriptionState.DescriptionVis
      || selectedId !== rightSelect.data.id
      || selectedType !== rightSelect.data.type
  ) return;

  textarea.value = nextDescription ?? "";
})

onBeforeUnmount(() => {
  descriptionRequestId += 1;
});

// 执行 关闭页面
const DialogClose = () => {
  DescriptionState.NoDescription();
}

// 执行 更新简介
const putDescription = async () => {
  let status: any;
  const selectedId = rightSelect.data.id;
  const selectedType = rightSelect.data.type;
  // 判断是笔记还是文件夹
  if (selectedType == "NOTE") {
    status = await putNoteDescription(selectedId, textarea.value);
  } else if (selectedType == "FOLDER") {
    status = await putFolderDescription(selectedId, textarea.value);
  }

  if (status == 200 && selectedId === rightSelect.data.id && selectedType === rightSelect.data.type) {
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
