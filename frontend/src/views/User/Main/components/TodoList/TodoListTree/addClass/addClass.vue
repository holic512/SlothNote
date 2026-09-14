<script setup lang="ts">
import {ref} from "vue";
import {ElMessage} from "element-plus";
import {addClass} from "@/views/User/Main/components/TodoList/TodoListTree/addClass/AddClass";
import {useTodoCategoryState} from "@/views/User/Main/components/TodoList/Pinia/TodoCategoryState";

// 控制是否显示
const AddClassDialogVisible = defineModel<boolean>({default: false});

// 输入框内容
const categoryName = ref("");

// 关闭前的回调指令
const handleClose = () => {
  // 清空输入框
  categoryName.value = "";

  // 关闭
  AddClassDialogVisible.value = false;
}

// 用于存储 分类类别 单选数据
const classTypeRadio = ref<number>(1);

// TodoClassTree 更新策略
const todoCategoryState = useTodoCategoryState();

// 代理 添加分类策略
const addClassProxy = async () => {
  // 检查数据
  if (categoryName.value == "") {
    ElMessage.warning("标题名称不能为空")
    return
  }

  const status = await addClass(categoryName.value, classTypeRadio.value);

  if (status == 200) {
    ElMessage.success("待做分类添加成功")

    // 执行更新策略
    todoCategoryState.description();

    // 关闭输入框
    handleClose();
  } else {
    ElMessage.info("无法连接到服务器")
  }
}
</script>

<template>
  <el-dialog
      v-model="AddClassDialogVisible"
      title="新建分类"
      width="450"
      :before-close="handleClose"
  >
    <el-form label-width="80px" style="padding:4px 20px 0 20px">

      <el-form-item label="分类名称">
        <el-input
            v-model="categoryName"
            placeholder="请输入分类名称"
            maxlength="20"
            show-word-limit
            style="width:260px"
        />
      </el-form-item>

      <el-form-item label="分类样式">
        <el-radio-group v-model="classTypeRadio">
          <el-radio :value="0">
            <el-tag size="small" type="primary">默认</el-tag>
          </el-radio>
          <el-radio :value="1">
            <el-tag size="small" type="info">基础</el-tag>
          </el-radio>
          <el-radio :value="2">
            <el-tag size="small" type="success">成功</el-tag>
          </el-radio>
          <el-radio :value="3">
            <el-tag size="small" type="warning">重要</el-tag>
          </el-radio>
          <el-radio :value="4">
            <el-tag size="small" type="danger">严重</el-tag>
          </el-radio>
        </el-radio-group>

      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="addClassProxy">确认</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-footer {
  text-align: right;
}

.classCheck {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
