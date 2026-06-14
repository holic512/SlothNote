<script setup lang="ts">
import {reactive, ref} from "vue";
import {ElMessage, FormInstance, FormRules} from "element-plus";
import {addUser, AddUserForm} from "./addUser";

// 控制添加用户页面
const addUserVisible = defineModel()

// 表单模型
const form = ref<AddUserForm>({
  username: "",
  password: "",
  confirmPassword: "",
  email: "",
  status: "ACTIVE"
});

// 验证规则
const rules = reactive<FormRules<AddUserForm>>({
  username: [
    {required: true, message: "请输入用户名", trigger: "blur"},
    {min: 6, max: 12, message: "用户名长度应在 6 到 12 个字符之间", trigger: "blur"},
    {
      // @ts-ignore
      validator: (rule, value, callback) => {
        const noChineseRegEx = /^[A-Za-z0-9_]+$/; // 匹配不含中文的字符串
        if (!noChineseRegEx.test(value)) {
          callback(new Error("用户名只能包含大小写英文、数字以及_"));
        } else {
          callback();
        }
      }, trigger: "blur"
    }
  ],
  password: [
    {required: true, message: "请输入密码", trigger: "blur"},
    {min: 6, message: "密码长度应不小于 6 位", trigger: "blur"}
  ],
  confirmPassword: [
    {required: true, message: "请确认密码", trigger: "blur"},
    {
      // @ts-ignore
      validator: (rule, value, callback) => {
        if (value !== form.value.password) {
          callback(new Error("两次输入密码不一致"));
        } else {
          callback();
        }
      }, trigger: "blur"
    }
  ],
  email: [
    {required: true, message: "请输入邮箱地址", trigger: "blur"},
    {type: "email", message: "请输入正确的邮箱地址", trigger: "change"}
  ]
});


// restaurants 用于实现 邮箱补全
const restaurants = [
  {value: '@qq.com'},
  {value: '@163.com'},
  {value: '@126.com'},
  {value: '@139.com'},
  {value: '@sina.com'},
  {value: '@gmail.com'},

];

// fetchSuggestions 用于 邮箱自动填充规则
const fetchSuggestions = (queryString: string, cb: any) => {
  if (queryString === '') {
    cb([])
    return
  }
  const hasAtSymbol = /@/.test(queryString);
  if (hasAtSymbol) {
    cb([])
    return
  }
  const suggestions = restaurants.map(item => {
        return {...item, value: queryString + item.value}
      }
  )
  cb(suggestions)
}

// 表单实例引用
const formEl = ref<FormInstance>();

// 表单提交 - 添加用户
const onSubmit = async () => {
  // 获取表单实例，进行验证
  if (!formEl) return
  if (formEl.value) {
    try {
      await formEl.value.validate();
      const status = await addUser(form)
      switch (status) {
        case 200:
          ElMessage.success("验证成功");
          // 清理form
          formEl.value.resetFields();
          addUserVisible.value = false;
          break;

        case 40901:
          ElMessage.warning("邮箱已存在");
          break;

        case 40902:
          ElMessage.warning("用户名已存在");
          break;

        default:
          ElMessage.error("无法连接服务器");
          break;
      }

    } catch {
      ElMessage.warning("请填写完整用户信息");
    }
  }
};

const resetForm = () => {
  if (formEl.value) {
    formEl.value.resetFields();
    addUserVisible.value = false

  }
}

</script>

<template>

  <!--  todo 类型不确定-->
  <Dialog v-model:visible="addUserVisible" :draggable="false" modal header="添加用户" :style="{ width: '450px'}"
          :pt="{
    header: { style: { paddingBottom:'10px',paddingTop:'10px'} },
    content: { style: { borderTop: '1px solid #E2E8F0'} },
  }">

    <el-form
        :model="form"
        :rules="rules"
        ref="formEl"
        label-width="80px"
        class="form-container"
        label-position="left"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" class="input" placeholder="请输入用户名"/>
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input v-model="form.password" class="input" show-password placeholder="请输入密码"/>
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" class="input" show-password placeholder="请确认密码"/>
      </el-form-item>

      <el-form-item label="邮箱地址" prop="email">
        <el-autocomplete
            v-model="form.email"
            :fetch-suggestions="fetchSuggestions"
            placeholder="请输入邮箱地址"
            style="width: 90%"
        />

      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status" fill="#020617">
          <el-radio-button value="ACTIVE">正常</el-radio-button>
          <el-radio-button value="DISABLED">停用</el-radio-button>
          <el-radio-button value="BANNED">封禁</el-radio-button>
        </el-radio-group>
      </el-form-item>


    </el-form>
    <div style="padding: 0 16px 0 16px">
      <el-button color="#020617" @click="onSubmit">添加</el-button>
      <el-button type="info" @click="resetForm" plain>取消</el-button>
    </div>
  </Dialog>

</template>

<style scoped>
.form-container {
  max-width: 600px;
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding: 16px 16px 0 16px;
}

.input {
  width: 90%;
}
</style>
