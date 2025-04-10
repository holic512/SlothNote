<script setup lang="ts">

import {ContextMenu, ContextMenuGroup, ContextMenuItem, ContextMenuSeparator} from "@imengyu/vue3-context-menu";
import {onRightNFAddNote} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onRightNFAddNote";
import {onRightNFAddFolder} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onRightNFAddFolder";
import {onRightNFDeleteFolder} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onRightNFDelete";
import {onRightNFMoveFolder} from "@/views/User/Main/components/Sidebar/RightMenu/Service/onRightNFMove";
import {useRenameData} from "@/views/User/Main/components/Sidebar/Pinia/RenameData";
import {useDetailsState} from "@/views/User/Main/components/Sidebar/Pinia/DetailsState";
import {useDescriptionState} from "@/views/User/Main/components/Sidebar/Pinia/DescriptionState";


const NodeMenuOption: any = defineModel();

// 控制 重命名是否 显示的State
const IsRename = useRenameData()

// 控制 详细信息是否 显示的State
const IsDetails = useDetailsState()

// 控制 编辑简介是否 显示的State
const DescriptionState = useDescriptionState();

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

    <context-menu-item label="删除" @click="onRightNFDeleteFolder()"/>

    <context-menu-separator/>

    <context-menu-item label="重命名" @click="IsRename.IsRename()"/>
    <context-menu-item label="编辑简介" @click="DescriptionState.IsDescription"/>
    <context-menu-item label="移动至" @click="onRightNFMoveFolder()"/>
    <context-menu-item label="复制到"/>

    <context-menu-separator/>

    <context-menu-item label="批量导出" disabled/>

    <context-menu-separator/>
    <context-menu-item label="详细信息" @click="IsDetails.IsDetails"/>

    <context-menu-group label="其他">
      <context-menu-item label="分享"/>
      <context-menu-item label="收藏"/>
      <context-menu-item label="阅读密码"/>
    </context-menu-group>

  </context-menu>
</template>

<style scoped>

</style>