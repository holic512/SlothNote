<script setup lang="ts">
import {onMounted, ref} from 'vue';
import Button from 'primevue/button';
import {Edit} from '@element-plus/icons-vue';
import {AccountInfo, fetchAccountInfo} from "@/views/User/Main/components/Setting/service/getUserInfo.ts";
import {putNickname} from "@/views/User/Main/components/Setting/service/putNickname";
import {ElMessage} from "element-plus";
import {putGender} from "@/views/User/Main/components/Setting/service/putGender";
import {putAge} from "@/views/User/Main/components/Setting/service/putAge";
import {putContactInfo} from "@/views/User/Main/components/Setting/service/putContactInfo";
import {putBio} from "@/views/User/Main/components/Setting/service/putBio";

const Info = ref<AccountInfo>({
  age: 0,
  avatar: "",
  bio: "",
  contactInfo: "",
  email: "",
  gender: "",
  id: 0,
  nickname: "",
  uid: "",
  username: ""
});

onMounted(async () => {
  Info.value = await fetchAccountInfo();
})

// 控制点击编辑 切换输入框
const nicknameVis = ref(true);
const genderVis = ref(true);
const ageVis = ref(true);
const contactInfoVis = ref(true);
const bioVis = ref(true);

const nickname = ref("");
const gender = ref("");
const age = ref<number>();
const contactInfo = ref("");
const bio = ref("");

const options = [
  {
    value: '男',
    label: '男',
  },
  {
    value: '女',
    label: '女',
  },
]

import {putAvatar} from "@/views/User/Main/components/Setting/service/putAvatar";
import {putPassword} from "@/views/User/Main/components/Setting/service/putPassword";
const avatarInput = ref<HTMLInputElement | null>(null);
const triggerUpload = () => {
  avatarInput.value?.click();
}
const onAvatarSelected = async (e: Event) => {
  const target = e.target as HTMLInputElement;
  const file = target.files && target.files[0];
  if (!file) return;
  const r = await putAvatar(file);
  if (r.status === 200) {
    Info.value.avatar = r.url;
    ElMessage.success("头像上传成功");
  } else {
    ElMessage.warning("头像上传失败");
  }
  target.value = "";
}

const pwDialogVis = ref(false);
const oldPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const openPwDialog = () => {
  pwDialogVis.value = true;
  oldPassword.value = "";
  newPassword.value = "";
  confirmPassword.value = "";
}
const submitPassword = async () => {
  if (!oldPassword.value || !newPassword.value) {
    ElMessage.warning("请输入完整信息");
    return;
  }
  if (newPassword.value.length < 6) {
    ElMessage.warning("新密码至少6位");
    return;
  }
  if (newPassword.value !== confirmPassword.value) {
    ElMessage.warning("两次输入的密码不一致");
    return;
  }
  const r = await putPassword(oldPassword.value, newPassword.value);
  if (r === 200) {
    ElMessage.success("密码修改成功");
    pwDialogVis.value = false;
  } else if (r === 400) {
    ElMessage.warning("原密码不正确");
  } else {
    ElMessage.error("密码修改失败");
  }
}

const editVis = (n: number) => {
  switch (n) {
    case 1:
      nicknameVis.value = false;
      nickname.value = Info.value.nickname;
      break;
    case 2:
      genderVis.value = false;
      gender.value = Info.value.gender;
      break;
    case 3:
      ageVis.value = false;
      age.value = Info.value.age ? Info.value.age : undefined;
      break;
    case 4:
      contactInfoVis.value = false;
      contactInfo.value = Info.value.contactInfo || "";
      break;
    case 5:
      bioVis.value = false;
      bio.value = Info.value.bio || "";
      break;
  }
}

const edit = async (n: number) => {
  let status;
  switch (n) {
    case 1:
      status = await putNickname(nickname.value);
      if (status == 200) {
        Info.value.nickname = nickname.value;
        nicknameVis.value = true;
        ElMessage.success("用户名修改成功");
      } else {
        ElMessage.warning("用户名修改失败");
      }
      break;

    case 2:
      status = await putGender(gender.value);
      if (status == 200) {
        Info.value.gender = gender.value;
        genderVis.value = true;
        ElMessage.success("性别修改成功");
      } else {
        ElMessage.warning("性别修改失败");
      }
      break;

    case 3:
      status = await putAge(<number>age.value);
      if (status == 200) {
        Info.value.age = <number>age.value;
        ageVis.value = true;
        ElMessage.success("年龄修改成功");
      } else {
        ElMessage.warning("年龄修改失败");
      }
      break;

    case 4:
      status = await putContactInfo(contactInfo.value);
      if (status == 200) {
        Info.value.contactInfo = contactInfo.value;
        contactInfoVis.value = true;
        ElMessage.success("联系方式修改成功");
      } else {
        ElMessage.warning("联系方式修改失败");
      }
      break;

    case 5:
      status = await putBio(bio.value);
      if (status == 200) {
        Info.value.bio = bio.value;
        bioVis.value = true;
        ElMessage.success("个人简介修改成功");
      } else {
        ElMessage.warning("联系方式修改失败");
      }
      break;
  }

}

</script>

<template>
  <h1 style="margin: 0">我的账号</h1>
  <el-divider class="divider"/>

  <div class="info-container">
    <!-- Avatar Section -->
    <div class="info-row">
      <el-text class="label" type="info">个人头像</el-text>
      <div class="avatar-container">
        <el-avatar size="large" :src="Info.avatar">
          {{ Info.username }}
        </el-avatar>
        <div class="avatar-actions">
          <input ref="avatarInput" type="file" accept="image/*" style="display:none" @change="onAvatarSelected" />
          <Button label="上传新头像" severity="secondary" rounded class="upload-btn" @click="triggerUpload"/>
          <el-text size="small">支持JPG、PNG、GIF格式，小于2MB</el-text>
        </div>
      </div>
    </div>

    <!-- Username Section -->
    <div class="info-row">
      <el-text class="label" type="info">用户昵称</el-text>

      <div v-if="nicknameVis" class="field-view-1">
        <el-text class="user-text">{{ Info.nickname }}</el-text>
        <el-link class="edit-link" :icon="Edit" type="primary" @click="editVis(1)">编辑</el-link>
      </div>

      <div v-else class="field-view-1">
        <el-input
            v-model="nickname"
            size="small"
            style="margin-right: 16px"
            maxlength="10"
            minlength="2"
            placeholder="Please input"
            show-word-limit
            type="text"
        />
        <el-button size="small" type="primary" @click="edit(1)" :disabled="nickname.length < 2">确认</el-button>
        <el-button size="small" @click="nicknameVis = true">取消</el-button>
      </div>

    </div>

    <!-- User UID Section -->
    <div class="info-row">
      <el-text class="label" type="info">用户 Uid</el-text>
      <el-text class="user-text">{{ Info.uid }}</el-text>
    </div>

    <!-- Account Section -->
    <div class="info-row">
      <el-text class="label" type="info">账号</el-text>
      <el-text class="user-text">{{ Info.username }}</el-text>
    </div>

    <!-- Password Section -->
    <div class="info-row">
      <el-text class="label" type="info">密码</el-text>
      <el-text class="user-text">******</el-text>
      <el-link class="edit-link" :icon="Edit" type="primary" @click="openPwDialog">修改密码</el-link>
    </div>

    <!-- Email Section -->
    <div class="info-row">
      <el-text class="label" type="info">邮件地址</el-text>
      <el-text class="user-text">{{ Info.email }}</el-text>
    </div>

    <!-- Gender Section -->
    <div class="info-row">
      <el-text class="label" type="info">性别</el-text>
      <div v-if="genderVis" class="field-view-1">
        <el-text class="user-text">{{ Info.gender }}</el-text>
        <el-link class="edit-link" :icon="Edit" type="primary" @click="editVis(2)">编辑</el-link>
      </div>

      <div v-else class="field-view-1">
        <el-select
            v-model="gender"
            placeholder="Select"
            size="small"
            style="width: 100px"
            class="custom-select"
        >
          <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>
        <el-button size="small" type="primary" @click="edit(2)">确认</el-button>
        <el-button size="small" @click="genderVis = true">取消</el-button>
      </div>


    </div>

    <!-- Age Section -->
    <div class="info-row">
      <el-text class="label" type="info">年龄</el-text>
      <div v-if="ageVis" class="field-view-1">
        <el-text class="user-text">{{ Info.age == undefined ? "暂无" : Info.age }}</el-text>
        <el-link class="edit-link" :icon="Edit" type="primary" @click="editVis(3)">编辑</el-link>
      </div>
      <div v-else class="field-view-1">
        <el-input size="small" style="margin-right: 16px" v-model="age"/>
        <el-button size="small" type="primary" @click="edit(3)">确认</el-button>
        <el-button size="small" @click="ageVis = true">取消</el-button>
      </div>

    </div>

    <!-- Contact Information Section -->
    <div class="info-row">
      <el-text class="label" type="info">联系方式</el-text>
      <div v-if="contactInfoVis" class="field-view-1">
        <el-text class="user-text">{{ Info.contactInfo == "" ? "暂无" : Info.contactInfo }}</el-text>
        <el-link class="edit-link" :icon="Edit" type="primary" @click="editVis(4)">编辑</el-link>
      </div>

      <div v-else class="field-view-1">
        <el-input size="small" style="margin-right: 16px" v-model="contactInfo"/>
        <el-button size="small" type="primary" @click="edit(4)">确认</el-button>
        <el-button size="small" @click="contactInfoVis = true">取消</el-button>
      </div>

    </div>

    <!-- Bio Section -->
    <div class="info-row">
      <el-text class="label" type="info">个人简介</el-text>
      <div v-if="bioVis" class="field-view-2">
        <el-text class="user-text">{{ Info.bio == "" ? "暂无" : Info.bio }}</el-text>
        <el-link class="edit-link" :icon="Edit" type="primary" @click="editVis(5)">编辑</el-link>
      </div>

      <div v-else class="field-view-2">
        <el-input size="small" style="margin-right: 16px" v-model="bio" type="textarea"/>
        <el-button size="small" type="primary" @click="edit(5)">确认</el-button>
        <el-button size="small" @click="bioVis = true">取消</el-button>
      </div>

    </div>
  </div>

  <el-dialog v-model="pwDialogVis" title="修改密码" width="360px">
    <div style="display:flex;flex-direction:column;gap:12px">
      <el-input v-model="oldPassword" type="password" placeholder="原密码"/>
      <el-input v-model="newPassword" type="password" placeholder="新密码（至少6位）"/>
      <el-input v-model="confirmPassword" type="password" placeholder="确认新密码"/>
    </div>
    <template #footer>
      <el-button @click="pwDialogVis=false">取消</el-button>
      <el-button type="primary" @click="submitPassword">确认</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.divider {
  margin: 8px 0 8px 0;
}

.info-container {
  padding: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}

.label {
  margin-right: 32px;
  width: 64px;
}

.user-text {
  margin-right: 8px;
}

/* 只有在悬停时才显示编辑链接 */
.edit-link {
  visibility: hidden; /* 默认隐藏 */
  opacity: 0; /* 默认透明 */
  transition: visibility 0s, opacity 0.2s ease-in-out; /* 平滑过渡 */
}

.info-row:hover .edit-link {
  visibility: visible; /* 悬停时可见 */
  opacity: 1; /* 悬停时不透明 */
}

.avatar-container {
  display: flex;
  align-items: center;
}

.avatar-actions {
  display: flex;
  flex-direction: column;
  margin-left: 16px;
}

.upload-btn {
  width: 128px;
  height: 28px;
  font-size: 14px;
}

.field-view-1 {
  display: flex;
  align-items: center;
  height: 18px;
}

.field-view-2 {
  display: flex;
  align-items: center;

}

.custom-select {
  margin-right: 15px;
}
</style>
