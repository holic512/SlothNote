<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import type Node from 'element-plus/es/components/tree/src/model/node'
import Button from 'primevue/button';
import {Tree} from "./interface/treeInterface";
import {getCurrentNoteInfo} from "@/views/User/Main/components/Sidebar/NoteTree/service/GetCurrentNoteInfo.js";
import {useRouter} from "vue-router";
import TopDivRightMenu from "@/views/User/Main/components/Sidebar/RightMenu/TopDivRightMenu.vue";
import NodeRightMenu from "@/views/User/Main/components/Sidebar/RightMenu/NodeRightMenu.vue";
import FolderRightMenu from "@/views/User/Main/components/Sidebar/RightMenu/FolderRightMenu.vue";
import {getUserAllTreeData} from "@/views/User/Main/components/Sidebar/NoteTree/service/GetUserAllTreeData";
import {useNoteTreeUpdate} from "@/views/User/Main/components/Sidebar/Pinia/isNoteTreeUpdated";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";
import {getFolderIdByNoteId} from "@/views/User/Main/components/Sidebar/NoteTree/service/GetFolderIdByNoteId";
import {useSaveNoteState} from "@/views/User/Main/components/Edit/Pinia/SaveNoteState";
import {ElMessageBox} from "element-plus";


// 获取 router 实例
const router = useRouter();

// 约束笔记类型
const props = {
  label: 'label',
  children: 'children',
}


const NoteTreeData = ref<Tree[]>([]);

// 钩子函数 自动获取 笔记树
onMounted(async () => {
  NoteTreeData.value = await getUserAllTreeData();
});

// 监控 笔记树 的 更新状态
const isNoteTreeUpdated = useNoteTreeUpdate();
watch(() => isNoteTreeUpdated.isNoteTreeUpdated, async (newState) => {
  // 当 newState 为 true 时 需要更新
  if (newState) {
    NoteTreeData.value = await getUserAllTreeData();

    isNoteTreeUpdated.UpdatedNoteTreeCompleted();
  }
})

const SaveNoteState = useSaveNoteState()

// 左键点击
const handleNodeClick = (data: Tree, node: Node) => {

  // 当目标是笔记时 - 判断当前是否保存 -> 发起需要保存的提示 -> 获取笔记信息 , 重置保存状态  ->跳转到笔记路由
  if (data.type !== 'NOTE') return

  if (!SaveNoteState.isSaved) {
    // 消息提示框
    ElMessageBox.confirm(
        '当前笔记没有保存,确定要切换吗',
        '笔记未保存',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning',
        }
    )
        .then(() => {
          // 设置当前显示笔记信息
          getCurrentNoteInfo(node);
          // 重置保存状态
          SaveNoteState.saveContent();
          router.push('/user/main/edit')
        })
  } else {
    // 设置当前显示笔记信息
    getCurrentNoteInfo(node);
    router.push('/user/main/edit')
  }
}

// NoteTree  右键操作
const NodeMenuOption = ref({
  show: false,
  optionsComponent: {
    zIndex: 3,
    minWidth: 160,
    x: 500,
    y: 200
  }
})
const FolderMenuOption = ref({
  show: false,
  optionsComponent: {
    zIndex: 3,
    minWidth: 160,
    x: 500,
    y: 200
  }
})

// 当前 右键选中节点Id
const RightId = useRightSelectNodeId();

// 节点 被右键点击的时候 触发
const handleNodeRightClick = async (event: MouseEvent, data: Tree, node: Node) => {
  // 判断右键的是笔记还是文件夹
  if (data.type === "NOTE") {
    // 启用右键菜单
    NodeMenuOption.value.show = true;
    // 设置右键菜单位置为鼠标点击的位置
    NodeMenuOption.value.optionsComponent.x = event.clientX;
    NodeMenuOption.value.optionsComponent.y = event.clientY;

    // 存储 笔记 父 文件夹 的 Id
    RightId.SetSelectNodeId(await getFolderIdByNoteId(data));
  } else if (data.type === "FOLDER") {
    // 启用右键菜单s
    FolderMenuOption.value.show = true;
    // 设置右键菜单位置为鼠标点击的位置
    FolderMenuOption.value.optionsComponent.x = event.clientX;
    FolderMenuOption.value.optionsComponent.y = event.clientY;

    // 存储 文件夹 的 Id
    RightId.SetSelectNodeId(data.id);
  }
  // 存储 当前选中的 数据
  RightId.setSelectData(data);

  //存储选中位置
  RightId.setSelectPos(event.clientX, event.clientY)
}

// TopDivRight  右键操作
const TopDivMenuOption = ref({
  show: false,
  optionsComponent: {
    zIndex: 3,
    minWidth: 140,
    x: 500,
    y: 200
  }
})

function onTopDivMenuRightClick(e: MouseEvent) {
  e.preventDefault();  // 阻止默认右键菜单

  TopDivMenuOption.value.show = true;
  // 设置右键菜单位置为鼠标点击的位置
  TopDivMenuOption.value.optionsComponent.x = e.clientX;
  TopDivMenuOption.value.optionsComponent.y = e.clientY;
}


// 用于 存储 树结构的 默认打开
let expandedKey: any = []

// 用于 操作 树节点展开操作
const handleNodeExpand = (data) => {
  // 插入 默认打开 表 用来解决更新数据后的 表结构重载
  expandedKey.push(data.uniqueId);
}

// 用于操作 树节点关闭操作
const handleNodeCollapse = (data) => {
  // 当列存在 现在正在关闭的 数据 删除
  expandedKey = expandedKey.filter(key => key !== data.uniqueId);
}

</script>

<template>

  <!--  引用 笔记 右键菜单组件-->
  <NodeRightMenu v-model="NodeMenuOption"/>

  <!--  引用 文件夹 右键菜单组件-->
  <FolderRightMenu v-model="FolderMenuOption"/>

  <!--  引用 TopDiv 右键菜单组件 -->
  <TopDivRightMenu v-model="TopDivMenuOption"/>


  <div style="width: 100%" @contextmenu="onTopDivMenuRightClick">

    <!--     TopDiv    -->
    <div class="sidebar-div" style="margin-bottom: 1px;">
      <div style="display: flex; justify-content: left; align-items: center; width: 100px ;margin-left: 8px">
        <el-text size="small">
          我的文档
        </el-text>
      </div>
      <!--  隔断  -->
      <div style="flex: 1"></div>

      <div>
        <Button class="sidebar-button" text icon="pi pi-plus" size="small" severity="secondary"
                @click="onTopDivMenuRightClick"/>
      </div>
    </div>

    <!--    NoteTree   -->
    <div style="height: 40vh;width: 100%;">
      <el-scrollbar style="width: 100%;">
        <el-tree
            class="el-tree"
            :props="props"
            :data="NoteTreeData"
            :default-expanded-keys="expandedKey"
            node-key="uniqueId"
            @node-click="handleNodeClick"
            @node-contextmenu="handleNodeRightClick"
            @node-expand="handleNodeExpand"
            @node-collapse="handleNodeCollapse"
        >
          <template #default="{node,data}">
            <div class="NoteTree"
                 style="display: flex; justify-content: space-between; align-items: center;width: 100%">
              <div style="display: flex; justify-content: left; align-items: center;width: 100%;">
                <!--  判断是否使用自定义图标 -->
                <div v-if="!data.avatar || data.avatar === ''" style="margin: 6px 2px 0 2px">
                  <div v-if="data.type === 'NOTE'">
                    <el-icon size="16">
                      <Notebook/>
                    </el-icon>
                  </div>
                  <div v-else-if="data.type === 'FOLDER'">
                    <el-icon size="16">
                      <Folder/>
                    </el-icon>
                  </div>
                </div>
                <!--  显示自定义头像  -->
                <div v-else>
                  {{ data.avatar }}
                </div>

                <el-text style="margin-left: 4px;">
                  {{ (data.label == null || data.label == '') ? (data.type == 'FOLDER' ? "新建文件夹" : "新建文档") : data.label }}
                </el-text>
              </div>

              <div class="NoteTreeButton">
                <Button class="sidebar-button" text icon="pi pi-ellipsis-h" size="small" severity="secondary"/>
              </div>
            </div>
          </template>
        </el-tree>
      </el-scrollbar>
    </div>
  </div>

</template>

<style scoped>

.sidebar-div {
  width: 100%;
  height: 30px;
  display: flex;
  align-items: center;
  border-radius: 4px; /* 添加圆角 */
  padding: 4px 2px 4px 2px; /* 增加内边距以增强按钮的点击区域 */
  transition: background-color 0.1s ease; /* 添加过渡效果 */
}


.sidebar-div:hover {
  background-color: #EFEFED;
}

.sidebar-button {
  width: 24px;
  height: 24px;
}

.sidebar-button:hover {
  background-color: #f8f9fa;
}


.el-tree {
  width: 100%;
  padding: 2px;
  background-color: #f7f7f5;
}

:deep(.el-tree-node__content) {
  height: 32px;
}

:deep(.el-tree-node__content):hover {
  background-color: #EFEFED;
}

:deep(.el-tree-node__content):focus {
  background-color: #EFEFED;
}


/* 选中状态时的背景色 */
:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #EFEFED;
  color: #333; /* 选中时的文本颜色 */
}

/* 叶子节点的选中样式 */
:deep(.el-tree-node.is-leaf.is-current > .el-tree-node__content) {
  background-color: #EFEFED;
  color: #333; /* 叶子节点选中时的文本颜色 */
}

:deep(.el-tree-node.is-expanded > .el-tree-node__content) {
  background-color: #EFEFED !important; /* 保持相同的自定义背景色 */
}

.NoteTreeButton {
  margin-right: 8px;

  display: none;

}

.NoteTree:hover .NoteTreeButton {
  display: block; /* 当父元素 hover 时显示子元素 */
}

</style>