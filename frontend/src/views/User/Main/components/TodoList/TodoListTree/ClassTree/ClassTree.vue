<script setup lang="ts">

import AddClass from "@/views/User/Main/components/TodoList/TodoListTree/addClass/addClass.vue";
import {onMounted, ref, watch} from "vue";
import {useTodoState} from "@/views/User/Main/components/TodoList/Pinia/TodoState";
import {
  GetUserTodoClasses
} from "@/views/User/Main/components/TodoList/TodoListTree/ClassTree/Service/GetUserTodoClasses";
import {TodoTypeById} from "@/views/User/Main/components/TodoList/TodoListTree/ClassTree/Service/TodoTypeById";
import {useTodoCategoryState} from "@/views/User/Main/components/TodoList/Pinia/TodoCategoryState";
// 用来存储分类
const todoClasses = ref<ITodoClass[]>([])

// 分类接口
interface ITodoClass {
  id: number,
  user_id: number,
  type: number,
  name: string,
}

// 钩子函数
onMounted(async () => {
  todoClasses.value = await GetUserTodoClasses();
})

// 检测更新
// TodoClassTree 更新策略
const todoCategoryState = useTodoCategoryState();

watch(() => todoCategoryState.isDescriptionVisible, async () => {
  todoClasses.value = await GetUserTodoClasses();
})

// 控制是否显示 添加分类的 Dialog
const AddClassDialogVisible = ref(false);

// 初始化 页面内容切换 state
const todoState = useTodoState();

// ... 现有的导入和代码 ...

// 添加处理函数
const handleUncategorized = () => {
  // 未分类视图
  todoState.ToUncategorized();
};

const handleRecycleBin = () => {
  // 回收站视图
  todoState.ToRecycleBin();
};
</script>


<template>
  <div>
    <!--    分类大标题   -->
    <div style="display: flex;">
      <el-text>分类</el-text>
      <div style="flex: 1;"/>

      <!--      新建 分类按钮   -->
      <el-button text style="width: 30px;height: 30px;" @click="AddClassDialogVisible = true">
        <el-icon>
          <Plus/>
        </el-icon>
      </el-button>
    </div>

    <!--    全部 和 未分类   -->
    <div class="class-tree-button" @click="todoState.ToAll()">
      <el-text class="class-tree-button-text">
        <el-icon size="18">
          <Menu/>
        </el-icon>
        全部待办
      </el-text>
    </div>

    <!-- 未分类待办，添加点击事件 -->
    <div class="class-tree-button" @click="handleUncategorized()">
      <el-text class="class-tree-button-text">
        <el-icon size="18">
          <Grid/>
        </el-icon>
        未分类
      </el-text>
    </div>

    <!--    自定义分类   -->
    <div v-for="todoClass in todoClasses" class="class-tree-button" @click="todoState.ToClass(todoClass)">
      <el-text class="class-tree-button-text" :type=TodoTypeById(todoClass.type)>
        <el-icon size="18">
          <Calendar/>
        </el-icon>
        {{ todoClass.name }}
      </el-text>
    </div>

    <!--    回收站 - 添加点击事件   -->
    <div class="class-tree-button" @click="handleRecycleBin()">
      <el-text class="class-tree-button-text">
        <el-icon size="18">
          <DeleteFilled/>
        </el-icon>
        回收站
      </el-text>
    </div>

    <!--    添加分类结构   -->
    <AddClass v-model="AddClassDialogVisible"/>
  </div>
</template>

<style scoped>
.class-tree-button {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  margin: 2px;

  user-select: none;
}

.class-tree-button:hover {
  background-color: #F0F2F5;
}

.class-tree-button-text {
  display: flex;
  gap: 6px;
}

</style>