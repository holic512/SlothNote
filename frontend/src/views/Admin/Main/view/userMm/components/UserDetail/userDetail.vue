<script setup lang="ts">
import Dialog from "primevue/dialog";
import {ref, watch} from "vue";
import {ElMessage, FormInstance} from "element-plus";
import axios from "../../../../../../../axios";

const visible = defineModel<boolean>();
const userId = defineModel<number>("userId");

const form = ref({
  id: 0,
  uid: "",
  username: "",
  email: "",
  status: 0,
  nickname: "",
  gender: "secret",
  age: undefined as number | undefined,
  bio: "",
  contactInfo: "",
  avatar: ""
});

const formEl = ref<FormInstance>();

const fetchDetail = async (id: number) => {
  const res = await axios.get("/admin/userMm/detail", { params: { id } });
  const d = res.data.data;
  form.value = {
    id: d.id,
    uid: d.uid,
    username: d.username,
    email: d.email,
    status: d.status,
    nickname: d.nickname || "",
    gender: d.gender || "secret",
    age: d.age,
    bio: d.bio || "",
    contactInfo: d.contactInfo || "",
    avatar: d.avatar || ""
  };
};

watch(visible, async (v) => {
  if (v && userId.value) await fetchDetail(userId.value);
});

const update = async () => {
  const res = await axios.put("/admin/userMm/update", form.value);
  const status = res.data.status;
  if (status === 200) {
    ElMessage.success("修改成功");
    visible.value = false;
  } else if (status === 40901) {
    ElMessage.warning("邮箱已存在");
  } else if (status === 40902) {
    ElMessage.warning("用户名已存在");
  } else {
    ElMessage.error("无法连接服务器");
  }
};

</script>

<template>
  <Dialog v-model:visible="visible" :draggable="false" modal header="用户详情" :style="{ width: '650px'}"
          :pt="{ header: { style: { paddingBottom:'10px',paddingTop:'10px'} }, content: { style: { borderTop: '1px solid #E2E8F0'} } }">
    <el-form :model="form" ref="formEl" label-width="90px" label-position="left">
      <el-form-item label="用户名">
        <el-input v-model="form.username" style="width: 90%" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" style="width: 90%" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="(form as any).password" type="password" placeholder="留空不修改" style="width: 90%" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio-button :value="0">正常</el-radio-button>
          <el-radio-button :value="1">停用</el-radio-button>
          <el-radio-button :value="2">封禁</el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" style="width: 90%" />
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="form.gender" style="width: 90%">
          <el-option label="男" value="male" />
          <el-option label="女" value="female" />
          <el-option label="保密" value="secret" />
        </el-select>
      </el-form-item>
      <el-form-item label="年龄">
        <el-input-number v-model="form.age" :min="0" :max="120" />
      </el-form-item>
      <el-form-item label="简介">
        <el-input v-model="form.bio" type="textarea" />
      </el-form-item>
      <el-form-item label="联系方式">
        <el-input v-model="form.contactInfo" />
      </el-form-item>
    </el-form>
    <div style="padding: 0 16px 0 16px">
      <el-button color="#020617" @click="update">保存</el-button>
      <el-button type="info" @click="visible=false" plain>取消</el-button>
    </div>
  </Dialog>
</template>

<style scoped></style>