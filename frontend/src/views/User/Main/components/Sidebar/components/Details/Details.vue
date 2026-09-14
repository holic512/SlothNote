<!--
@file NodeDetailsDialog
@project SlothNote
@module 用户端 / 侧栏节点详情
@description 展示笔记或文件夹的简介、创建时间、更新时间和保存时间。
@logic 1. 打开时捕获节点快照并并行读取详情；2. 关闭、卸载或节点变化时使旧请求失效；3. 仅最新快照可更新展示数据。
@dependencies Store: DetailsState/RightSelectNodeId, Service: Description/Details APIs
@index_tags 节点详情, 笔记树, PromiseAll, 请求竞态
@author holic512
-->
<script setup lang="ts">
import {useDetailsState} from "@/views/User/Main/components/Sidebar/Pinia/DetailsState";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";
import {onBeforeUnmount, ref, watch} from "vue";
import {
  getNoteDescription
} from "@/views/User/Main/components/Sidebar/components/Description/Service/GetNoteDescription";
import {
  getFolderDescription
} from "@/views/User/Main/components/Sidebar/components/Description/Service/GetFolderDescription";
import {
  GetNoteCreatedAtAndUpdatedAt
} from "@/views/User/Main/components/Sidebar/components/Details/Service/GetNoteCreatedAtAndUpdatedAt";
import {GetNoteSaveAt} from "@/views/User/Main/components/Sidebar/components/Details/Service/GetNoteSaveAt";
import {
  getFolderCreatedAtAndUpdatedAt
} from "@/views/User/Main/components/Sidebar/components/Details/Service/GetFolderCreatedAtAndUpdatedAt";

// 控制页面是否显示
const DetailsState = useDetailsState();

// 获取右键 选中节点的 信息
const rightSelect: any = useRightSelectNodeId();

// 简介
const description = ref("");
// 占用空间
const occupiedSpace = ref("暂无");
// 创建时间
const createdAt = ref("");
// 修改时间
const updatedAt = ref("");
// 保存时间
const savedAt = ref("");
let detailsRequestId = 0;

const resetDetails = () => {
  description.value = "";
  createdAt.value = "";
  updatedAt.value = "";
  savedAt.value = "";
};

// 监听 当此页面被换出时 获取右键菜单的 信息
watch(() => DetailsState.DetailsIs, async (newValue) => {
  const requestId = ++detailsRequestId;
  if (!newValue) return;

  const selectedId = rightSelect.data.id;
  const selectedType = rightSelect.data.type;
  resetDetails();

  // 当为 true 则显示
  // 判断是笔记还是文件夹
  if (selectedType == "NOTE") {

    // 启动所有异步调用
    const [doc1, doc2, doc3] = await Promise.all([
      getNoteDescription(selectedId),
      GetNoteCreatedAtAndUpdatedAt(selectedId),
      GetNoteSaveAt(selectedId),
    ]);

    if (
        requestId !== detailsRequestId
        || !DetailsState.DetailsIs
        || selectedId !== rightSelect.data.id
        || selectedType !== rightSelect.data.type
    ) return;

    description.value = doc1;
    createdAt.value = doc2.CreateAt;
    updatedAt.value = doc2.UpdatedAt;
    savedAt.value = doc3;

  } else if (selectedType == "FOLDER") {
    // 启动所有异步调用
    const [doc1, doc2] = await Promise.all([
      getFolderDescription(selectedId),
      getFolderCreatedAtAndUpdatedAt(selectedId),
    ]);

    if (
        requestId !== detailsRequestId
        || !DetailsState.DetailsIs
        || selectedId !== rightSelect.data.id
        || selectedType !== rightSelect.data.type
    ) return;

    description.value = doc1;

    createdAt.value = doc2.CreateAt;
    updatedAt.value = doc2.UpdatedAt;

    savedAt.value = "仅查看笔记";
  }
})

onBeforeUnmount(() => {
  detailsRequestId += 1;
});

</script>

<template>
  <el-dialog v-model="DetailsState.DetailsIs" width="480" class="dialog-custom">

    <!-- 定制对话框的标题部分 -->
    <template #header="{ close, titleId, titleClass }">
      <el-text type="info">
        {{ rightSelect.data.label }}
        属性
      </el-text>
    </template>

    <!-- 对话框主体内容 -->
    <div class="dialog-body">

      <!-- 显示笔记图标与笔记名称输入框 -->
      <div class="note-info">
        <!-- 笔记图标 -->
        <div style="margin: 6px 8px 0 16px;" v-if="rightSelect.data.avatar == null ">
          <div v-if='rightSelect.data.type == "NOTE" '>
            <el-icon size="32">
              <Notebook/>
            </el-icon>
          </div>
          <div v-else-if='rightSelect.data.type == "FOLDER" '>
            <el-icon size="32">
              <Folder/>
            </el-icon>
          </div>
        </div>
        <div class="icon-container" v-else>
          {{ rightSelect.data.avatar }}
        </div>
        <!-- 笔记名称输入框 -->
        <div class="input-container">
          <!--          <el-input placeholder="新页面"></el-input> &lt;!&ndash; 设置占位符文本 &ndash;&gt;-->
          <el-text size="large"> {{ rightSelect.data.label }}</el-text>
        </div>
      </div>


      <!-- 分割线 -->
      <el-divider style="margin: 16px 0 16px 0"/>

      <div class="info-block">
        <div style="width: 80px;margin-left: 8px;">简介:</div>
        <div style="width:344px;">
          {{ (description != null) ? description : "暂无" }}
        </div>
      </div>

      <!-- 分割线 -->
      <el-divider style="margin: 16px 0 16px 0"/>

      <div class="info-block">
        <div style="width: 80px;margin-left: 8px;">
          文件类型:
        </div>
        <div>
          {{ rightSelect.data.type }}
        </div>
      </div>

      <!-- 分割线 -->
      <el-divider style="margin: 16px 0 16px 0"/>


      <div class="info-block">
        <div style="width: 80px;margin-left: 8px;">
          占用空间:
        </div>
        <div>
          1.54KB(1,585字节)
        </div>
      </div>

      <!-- 分割线 -->
      <el-divider style="margin: 16px 0 16px 0"/>

      <div class="info-block">
        <div style="width: 80px;margin-left: 8px;">
          创建时间:
        </div>
        <div>
          {{ createdAt }}
        </div>
      </div>

      <div class="info-block">
        <div style="width: 80px;margin-left: 8px;">
          上次修改:
        </div>
        <div>
          {{ updatedAt }}
        </div>

        <el-tooltip
            content="指该选中单元最近一次修改信息的时间"
            placement="right"
        >
          <el-icon style="margin-left: 8px">
            <InfoFilled/>
          </el-icon>
        </el-tooltip>
      </div>

      <div class="info-block">
        <div style="width: 80px;margin-left: 8px;">
          上次保存:
        </div>
        <div>
          {{ (savedAt != null) ? savedAt : "暂无" }}
        </div>

        <el-tooltip
            content="指笔记内容最近一次保存的时间"
            placement="right"
        >
          <el-icon style="margin-left: 8px">
            <InfoFilled/>
          </el-icon>
        </el-tooltip>


      </div>

    </div>


  </el-dialog>
</template>

<style scoped>
/* 对话框自定义样式 */
.dialog-custom {
  padding: 12px;
}

.dialog-body {
  padding: 0 4px 24px 4px;
}

.note-info {
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 16px;
}

.icon-container {
  font-size: 32px;
  margin-left: 12px;
}

.input-container {
  flex: 1;
}

.info-block {
  display: flex;
  align-items: center;
  margin-top: 12px;
}

.label {
  width: 80px;
  margin-left: 8px;
}

.value {
  flex: 1;
}

.el-divider {
  margin: 16px 0;
}
</style>
