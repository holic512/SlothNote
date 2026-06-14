<script setup lang="ts">
//组件加载
import {ref, watch} from "vue";
import {fetchPageData} from "./fetchPageData";
import {logout} from "./logout";
import {kickout} from "./kickout";
import {ElMessage} from "element-plus";


// 控制添加用户页面
const onlineUserVisible = defineModel()

// 表格数据
const products = ref([]);

// 监控 onlineUserVisible 变量
watch(onlineUserVisible, async (newValue) => {
  if (newValue) {
    // 当 onlineUserVisible 变为 true 时执行
    products.value = await fetchPageData(1, 10);
  }
});

const logoutR = async (uid: number) => {
  const status = await logout(uid)
  if (status == 200) {
    ElMessage.success("成功注销Uid:" + uid)
    // 获取新的数据
    products.value = await fetchPageData(1, 10);
  }
}

const kickoutR = async (uid: number) => {
  const status = await kickout(uid)
  if (status == 200) {
    ElMessage.success("成功踢Uid:" + uid + "下线")
    // 获取新的数据
    products.value = await fetchPageData(1, 10);
  }
}
</script>

<template>


  <!--  todo 类型不确定-->
  <Dialog v-model:visible?="onlineUserVisible" :draggable="false" modal header="在线用户页面" :style="{ width: '850px'}"
          :pt="{
    header: { style: { paddingBottom:'10px',paddingTop:'10px'} },
    content: { style: { borderTop: '1px solid #E2E8F0'} },
  }">
    <DataTable :value="products" size="small" tableStyle="min-width: 50rem">
      <Column field="id" header="ID"></Column>
      <Column field="uid" header="UID"></Column>
      <Column field="username" header="用户名"></Column>
      <Column field="email" header="邮箱"></Column>
      <Column field="createTime" header="登录时间"></Column>

      <Column header="操作" headerStyle="width: 16%">
        <template #body="{ data }">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <Button class="col-button" type="button" label="注销" severity="info" outlined @click="logoutR(data.uid)"/>
            <Button class="col-button" type="button" label="踢出" severity="warn" outlined @click="kickoutR(data.uid)"/>
          </div>
        </template>
      </Column>

      <Column header="更多" headerStyle="width: 8%">
        <template #body>
          <Button type="button" icon="Ellipsis" rounded outlined style=" height: 32px;width: 32px"/>
        </template>
      </Column>

    </DataTable>

  </Dialog>
</template>

<style scoped>
.col-button {
  height: 28px;
  width: 54px;
  font-size: 14px
}

</style>