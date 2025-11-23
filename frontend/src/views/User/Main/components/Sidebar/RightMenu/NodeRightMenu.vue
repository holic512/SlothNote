<script setup lang="ts">

import {ContextMenu, ContextMenuGroup, ContextMenuItem, ContextMenuSeparator} from "@imengyu/vue3-context-menu";
import {onRightNFAddNote} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onRightNFAddNote";
import {onRightNFAddFolder} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onRightNFAddFolder";
import {onRightNFDeleteNote} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onRightNFDelete";
import {onRightNFMoveNote} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onRightNFMove";
import {useRenameData} from "@/views/User/Main/components/Sidebar/Pinia/RenameData";
import {useDetailsState} from "@/views/User/Main/components/Sidebar/Pinia/DetailsState";
import {useDescriptionState} from "@/views/User/Main/components/Sidebar/Pinia/DescriptionState";
import {onExportHtml} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onExportHtml";
import {onExportPdf} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onExportPdf";
import {useFavoriteDialogStore} from "@/views/User/Main/components/Edit/Pinia/FavoriteDialogStore";
import {useRightSelectNodeId} from "@/views/User/Main/components/Sidebar/Pinia/RightSelectNodeId";


const NodeMenuOption: any = defineModel();

// 控制 重命名是否 显示的State
const IsRename = useRenameData()

// 控制 详细信息是否 显示的State
const IsDetails = useDetailsState()

// 控制 编辑简介是否 显示的State
const DescriptionState = useDescriptionState();
const favStore = useFavoriteDialogStore();
const rightId = useRightSelectNodeId();
const openFavoriteDialog = () => {
  // @ts-ignore
  const data = rightId.data as any;
  if (data && data.type === 'NOTE') {
    favStore.open(data.id);
  }
}

</script>

<template>
  <context-menu
      v-model:show="NodeMenuOption.show"
      :options="NodeMenuOption.optionsComponent"
  >
    <context-menu-group label="新建">
      <context-menu-item label="空白文档" @click="onRightNFAddNote()"/>
      <context-menu-item label="文件夹" @click="onRightNFAddFolder()"/>
    </context-menu-group>

    <context-menu-separator/>

    <context-menu-item label="删除" @click="onRightNFDeleteNote()"/>

    <context-menu-separator/>

    <context-menu-item label="重命名" @click="IsRename.IsRename()"/>
    <context-menu-item label="简介" @click="DescriptionState.IsDescription"/>
    <context-menu-item label="移动至" @click="onRightNFMoveNote()"/>

    <context-menu-separator/>
    <context-menu-group label="导出">
      <context-menu-item label="HTML" @click="onExportHtml"/>
      <context-menu-item label="Pdf" @click="onExportPdf"/>
    </context-menu-group>

    <context-menu-separator/>

    <context-menu-item label="详细信息" @click="IsDetails.IsDetails"/>
    <context-menu-item label="收藏" @click="openFavoriteDialog"/>

  </context-menu>
</template>

<style scoped>

</style>