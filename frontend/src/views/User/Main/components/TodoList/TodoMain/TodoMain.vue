<script setup lang="ts">
import {Calendar, Plus} from '@element-plus/icons-vue'
import {onMounted, ref, watch} from "vue";
import {useTodoState} from "@/views/User/Main/components/TodoList/Pinia/TodoState";
import {getTodayDate} from "@/views/User/Main/components/TodoList/Service/getTodayDate";
import {getAllTodoList} from "@/views/User/Main/components/TodoList/TodoMain/Service/getAllTodoList";
import {TodoTypeById} from "@/views/User/Main/components/TodoList/TodoMain/Service/TodoTypeById";
import {
  getUserTodosByCategory
} from "@/views/User/Main/components/TodoList/TodoListTree/ClassTree/Service/GetUserTodosByCategory";
import {ReopenTodo} from "@/views/User/Main/components/TodoList/TodoMain/Service/ReopenTodo";
import {ElMessage, ElMessageBox} from "element-plus";
import {CompleteTodo} from "@/views/User/Main/components/TodoList/TodoMain/Service/CompleteTodo";
import {addTodo} from "@/views/User/Main/components/TodoList/TodoMain/Service/AddTodo";
import {updateTodo} from "@/views/User/Main/components/TodoList/TodoMain/Service/UpdateTodo";
import {deleteTodo} from "@/views/User/Main/components/TodoList/TodoMain/Service/DeleteTodo";
import {getTodosByDate} from "@/views/User/Main/components/TodoList/TodoMain/Service/GetTodosByDate";
import {
  GetUserTodoClasses
} from "@/views/User/Main/components/TodoList/TodoListTree/ClassTree/Service/GetUserTodoClasses";
import {getTodosForWeek} from "@/views/User/Main/components/TodoList/TodoMain/Service/GetTodosForWeek";
import {getCompletedTodos} from "@/views/User/Main/components/TodoList/TodoMain/Service/GetCompletedTodos";
import {getExpiredTodos} from "@/views/User/Main/components/TodoList/TodoMain/Service/GetExpiredTodos";
import {getUncategorizedTodos} from "@/views/User/Main/components/TodoList/TodoMain/Service/GetUncategorizedTodos";
import {getRecycleBinTodos} from "@/views/User/Main/components/TodoList/TodoMain/Service/GetRecycleBinTodos";

// 初始化状态变量
const todoState = useTodoState()

// 监听todoState是否改变
const refreshTodoList = async () => {
  try {
    let newState = todoState.state;
    switch (newState) {
      case 0:
        pageTitle.value = "全部待办";
        TodoData.value = await getAllTodoList();
        break;

      case 1:
        pageTitle.value = "今天待办(" + getTodayDate() + ")";
        // 获取今天的日期格式为yyyy-MM-dd
        const today = new Date();
        const formattedDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`;
        TodoData.value = await getTodosByDate(formattedDate);
        break;

      case 2:
        if (todoState.AClass) {
          pageTitle.value = todoState.AClass.name;
          TodoData.value = await getUserTodosByCategory(todoState.AClass.id);
        }
        break;

      case 3:
        pageTitle.value = "未分类";
        TodoData.value = await getUncategorizedTodos();
        break;

      case 4:
        pageTitle.value = "已完成";
        // 获取已完成的待办事项
        TodoData.value = await getCompletedTodos();
        break;

      case 5:
        pageTitle.value = "已过期";
        // 获取已过期的待办事项
        TodoData.value = await getExpiredTodos();
        break;

      case 6:
        pageTitle.value = "回收站";
        TodoData.value = await getRecycleBinTodos();
        break;

      case 7:
        pageTitle.value = "七日待做";
        // 获取七日待办事项
        TodoData.value = await getTodosForWeek();
        break;
    }
  } catch (error) {
    console.error("加载待办列表出错:", error);
    ElMessage.error("加载待办列表失败");
  }
};

// 监听左侧视图状态切换，驱动右侧列表与标题更新
watch(() => todoState.isDescriptionVisible, async () => {
  await refreshTodoList();
});

// 初始化
onMounted(async () => {
  TodoData.value = await getAllTodoList();
})

// 页面标题
const pageTitle = ref<string>("全部待办")

// 进行中和已完成 显示 的变量
const isInProgressVisible = ref(true)
const isCompletedVisible = ref(true)

interface Todo {
  todo_id: number;
  title: string;
  description: string;
  startDate: string;
  dueDate: string;
  status: number;
  category_id: number | null;
  category_name: string | null;
  category_type: number | null;
  todoInfoisDeleted: boolean;
}

// 所有待做的 树结构
const TodoData = ref<Todo[]>([]);

// 进行中树标题的 点击函数
const handInProgress = () => {
  isInProgressVisible.value = !isInProgressVisible.value
}

// 已完成树标题的 点击函数
const handCompleted = () => {
  isCompletedVisible.value = !isCompletedVisible.value
}

const ReopenTodoProxy = async (todoId: number) => {
  const status = await ReopenTodo(todoId);
  if (status == 200) {
    // 更新成功 - 执行 更新表单策略
    TodoData.value = await getAllTodoList();
  } else {
    ElMessage.info("无法连接到服务器")
  }
}

const CompleteTodoProxy = async (todoId: number) => {
  const status = await CompleteTodo(todoId);
  if (status == 200) {
    // 更新成功 - 执行 更新表单策略
    TodoData.value = await getAllTodoList();
  } else {
    ElMessage.info("无法连接到服务器")
  }
}

// 添加待办相关
const newTodoInput = ref('');
const handleAddTodoInput = async (event: KeyboardEvent) => {
  if (event.key === 'Enter' && newTodoInput.value.trim()) {
    const todoData = {
      title: newTodoInput.value.trim(),
      description: '',
      categoryId: null,
      dueDate: null
    };

    const status = await addTodo(todoData);
    if (status === 200) {
      ElMessage.success("添加成功");
      // 刷新待办列表
      TodoData.value = await getAllTodoList();
      // 清空输入框
      newTodoInput.value = '';
    } else {
      ElMessage.error("添加失败");
    }
  }
};

// 新增高级表单相关数据
const addTodoDialogVisible = ref(false);
const newTodoForm = ref({
  title: '',
  description: '',
  categoryId: null as number | null,
  dueDate: null as string | null
});

// 获取分类列表
const todoCategories = ref([]);
const fetchCategories = async () => {
  todoCategories.value = await GetUserTodoClasses();
};

// 打开表单并加载分类
const openAddTodoForm = () => {
  fetchCategories();
  addTodoDialogVisible.value = true;
};

// 提交表单
const submitAddTodo = async () => {
  if (!newTodoForm.value.title) {
    ElMessage.warning("标题不能为空");
    return;
  }

  const status = await addTodo(newTodoForm.value);
  if (status === 200) {
    ElMessage.success("添加成功");
    // 刷新待办列表
    TodoData.value = await getAllTodoList();
    // 关闭表单并重置
    addTodoDialogVisible.value = false;
    newTodoForm.value = {
      title: '',
      description: '',
      categoryId: null,
      dueDate: null
    };
  } else {
    ElMessage.error("添加失败");
  }
};

// 详情和编辑相关数据
const todoDetailDialogVisible = ref(false);
const currentTodo = ref({} as Todo);
const isEditing = ref(false);

// 打开待办详情
const openTodoDetail = (todo: Todo) => {
  currentTodo.value = {...todo};
  todoDetailDialogVisible.value = true;
  isEditing.value = false;
  fetchCategories(); // 加载分类列表
};

// 更新待办
const handleUpdateTodo = async () => {
  if (!currentTodo.value.title) {
    ElMessage.warning("标题不能为空");
    return;
  }

  const todoData = {
    title: currentTodo.value.title,
    description: currentTodo.value.description || "",
    categoryId: currentTodo.value.category_id ?? null,
    dueDate: currentTodo.value.dueDate,
    status: currentTodo.value.status
  };

  const status = await updateTodo(currentTodo.value.todo_id, todoData);
  if (status === 200) {
    ElMessage.success("更新成功");
    // 刷新待办列表
    TodoData.value = await getAllTodoList();
    // 关闭编辑模式
    isEditing.value = false;
    todoDetailDialogVisible.value = false;
  } else {
    ElMessage.error("更新失败");
  }
};

// 删除待办
const handleDeleteTodo = async () => {
  const confirmResult = await ElMessageBox.confirm(
      '确定要删除这个待办事项吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).catch(() => false);

  if (confirmResult) {
    const status = await deleteTodo(currentTodo.value.todo_id);
    if (status === 200) {
      ElMessage.success("删除成功");
      // 刷新待办列表
      TodoData.value = await getAllTodoList();
      todoDetailDialogVisible.value = false;
    } else {
      ElMessage.error("删除失败");
    }
  }
};
</script>

<template>
  <div class="TodoMain">
    <!--标题-->
    <div>
      <el-text style="font-size: 24px;">{{ pageTitle }}</el-text>
    </div>

    <!--添加输入框-->
    <div style="margin-top: 16px" class="add-todo-container">
      <el-input
          v-model="newTodoInput"
          :prefix-icon="Plus"
          size="large"
          placeholder="添加待办事件,按回车保存"
          @keyup.enter="handleAddTodoInput"
      />
    </div>

    <!--待做树-->
    <div style="flex: 1; margin-top: 16px">
      <el-scrollbar>
        <div style="margin-bottom: 24px">
          <div>
            <!--  进行中缩放标签  -->
            <el-text style="font-size: 16px;margin-top: 8px;user-select: none;" @click="handInProgress">
              <el-icon size="12" v-if="!isInProgressVisible">
                <ArrowRight/>
              </el-icon>
              <el-icon size="12" v-else>
                <ArrowDown/>
              </el-icon>
              进行中
            </el-text>
          </div>

          <!-- 进行中 数据树 -->
          <div v-for="task in TodoData.filter(t => t.status === 0)" :key="task.todo_id" class="task-item"
               v-if="isInProgressVisible" @click="openTodoDetail(task)">
            <div class="task-content">
              <!-- 选中框 -->
              <el-checkbox v-show="todoState.state !== 6" class="task-checkbox" @click.stop="CompleteTodoProxy(task.todo_id)"/>

              <div class="task-label">
                <el-text>
                  {{ task.title }}
                </el-text>
              </div>

              <div class="task-description" v-if="task.description">
                <el-text size="small" type="info">
                  {{ task.description.length > 50 ? task.description.substring(0, 50) + '...' : task.description }}
                </el-text>
              </div>

              <div class="task-class">
                <el-tag :type=TodoTypeById(task.category_type) size="small">
                  {{ task.category_name || "未分类" }}
                </el-tag>
              </div>

              <div class="task-due-date" v-if="task.dueDate">
                <el-text size="small" style="color: #7f8c8d;">
                  <el-icon>
                    <Calendar/>
                  </el-icon>
                  {{ new Date(task.dueDate).toLocaleDateString() }}
                </el-text>
              </div>

              <div class="task-actions" v-show="todoState.state !== 6">
                <el-button-group size="small">
                  <el-button
                      icon="Edit"
                      type="info"
                      plain
                      @click.stop="openTodoDetail(task); isEditing = true;"
                  />
                  <el-button
                      icon="Delete"
                      type="danger"
                      plain
                      @click.stop="currentTodo = task; handleDeleteTodo()"
                  />
                </el-button-group>
              </div>
            </div>
            <div class="bottom-border"/>
          </div>
        </div>

        <div>
          <!--  已完成 缩放标签  -->
          <div @click="handCompleted">
            <el-text style="font-size: 16px;margin-top: 8px;user-select: none;">
              <el-icon size="12" v-if="!isCompletedVisible">
                <ArrowRight/>
              </el-icon>
              <el-icon size="12" v-else>
                <ArrowDown/>
              </el-icon>
              已完成
            </el-text>
          </div>

          <!-- 已完成 数据树 -->
          <div v-if="isCompletedVisible" v-for="task in TodoData.filter(t => t.status === 1)" :key="task.todo_id"
               class="task-item" @click="openTodoDetail(task)">
            <div class="task-content">
              <!--  选中框  -->
              <el-checkbox v-show="todoState.state !== 6" checked class="task-checkbox" @click.stop="ReopenTodoProxy(task.todo_id)"></el-checkbox>

              <div class="task-label completed">
                <el-text>
                  {{ task.title }}
                </el-text>
              </div>

              <div class="task-class">
                <el-tag :type=TodoTypeById(task.category_type) size="small">
                  {{ task.category_name || "未分类" }}
                </el-tag>
              </div>

              <div class="task-due-date" v-if="task.dueDate">
                <el-text size="small" style="color: #7f8c8d;">
                  <el-icon>
                    <Calendar/>
                  </el-icon>
                  {{ new Date(task.dueDate).toLocaleDateString() }}
                </el-text>
              </div>

              <div class="task-actions" v-show="todoState.state !== 6">
                <el-button-group size="small">
                  <el-button
                      icon="Delete"
                      type="danger"
                      plain
                      @click.stop="currentTodo = task; handleDeleteTodo()"
                  />
                </el-button-group>
              </div>
            </div>
            <div class="bottom-border"/>
          </div>
        </div>
      </el-scrollbar>
    </div>

    <!-- 添加待办事项表单 -->
    <el-dialog v-model="addTodoDialogVisible" title="创建待办事项" width="450">
      <el-form :model="newTodoForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="newTodoForm.title" placeholder="请输入标题"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newTodoForm.description" type="textarea" placeholder="请输入描述"/>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="newTodoForm.dueDate" type="datetime" placeholder="选择截止时间"/>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="newTodoForm.categoryId" placeholder="选择分类" clearable @clear="newTodoForm.categoryId = null">
            <el-option
                v-for="category in todoCategories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addTodoDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAddTodo">确认</el-button>
      </template>
    </el-dialog>

    <!-- 待办详情和编辑对话框 -->
    <el-dialog v-model="todoDetailDialogVisible" :title="isEditing ? '编辑待办' : '待办详情'" width="450">
      <el-form :model="currentTodo" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="currentTodo.title" placeholder="请输入标题" :disabled="!isEditing"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentTodo.description" type="textarea" placeholder="请输入描述" :disabled="!isEditing"/>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="currentTodo.dueDate" type="datetime" placeholder="选择截止时间"
                          :disabled="!isEditing"/>
        </el-form-item>
        <el-form-item label="分类" v-if="isEditing">
          <el-select v-model="currentTodo.category_id" placeholder="选择分类" clearable @clear="currentTodo.category_id = null">
            <el-option
                v-for="category in todoCategories"
                :key="category.id"
                :label="category.name"
                :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="currentTodo.status === 0 ? 'warning' : 'success'">
            {{ currentTodo.status === 0 ? '未完成' : '已完成' }}
          </el-tag>
        </el-form-item>
      </el-form>
      <template #footer>
        <div v-if="!isEditing">
          <el-button @click="todoDetailDialogVisible = false">关闭</el-button>
          <el-button v-show="todoState.state !== 6 && !currentTodo.todoInfoisDeleted" type="primary" @click="isEditing = true">编辑</el-button>
          <el-button v-show="todoState.state !== 6 && !currentTodo.todoInfoisDeleted" type="danger" @click="handleDeleteTodo">删除</el-button>
        </div>
        <div v-else>
          <el-button @click="isEditing = false">取消</el-button>
          <el-button type="primary" @click="handleUpdateTodo">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.TodoMain {
  height: 100%;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.add-todo-container {
  display: flex;
  gap: 10px;
}

.add-todo-button {
  flex-shrink: 0;
}

.task-item {
  width: 100%;
  border-radius: 4px; /* 圆角效果 */
  margin: 8px 0; /* 给每个任务添加间距 */
  padding: 4px 16px;
  cursor: pointer;
}

.task-item:hover {
  background-color: #F0F2F5;
}

.bottom-border {
  width: 95%; /* 设置边框的长度 */
  height: 1px;
  background-color: #ddd; /* 设置边框颜色 */
  margin-left: 12px;
}

.task-content {
  display: flex;
  grid-template-columns: 32px 1fr 2fr 110px 160px 120px;
  align-items: center;
  column-gap: 48px;
  margin-bottom: 6px;
  margin-left: 16px;
}

.task-checkbox {
  margin-left: 12px;
  margin-right: 12px;
  width: 16px;
}

.task-label {
  font-weight: 500;

}

.task-description {
  color: #606266;
  font-size: 13px;

}

.task-class, .task-due-date {

}

.task-actions {
  opacity: 0;
  transition: opacity 0.2s ease;
  margin-left: auto;
  margin-right: 10px;
  min-width: 100px;
}

.task-item:hover .task-actions {
  opacity: 1;
}

.completed {
  text-decoration: line-through;
  color: #909399;
}
</style>