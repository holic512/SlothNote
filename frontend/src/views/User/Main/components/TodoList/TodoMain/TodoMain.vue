<!--
@file UserTodoMain
@project SlothNote
@module 用户端 / 待办主列表
@description 展示待办列表、分页、快捷创建和详情编辑交互。
@logic 1. 委托 useTodoMain 编排查询与变更；2. 按状态分组展示当前页；3. 仅维护进行中与已完成区域的折叠状态。
@dependencies Composable: useTodoMain, Element Plus
@index_tags 待办列表, 用户任务, 分类筛选, 任务编辑
@author holic512
-->
<script setup lang="ts">
import { ref } from 'vue'
import { Calendar, Delete, Edit, Plus } from '@element-plus/icons-vue'
import { useTodoMain } from './composables/useTodoMain'

const {
  currentViewState,
  pageTitle,
  TodoData,
  currentPage,
  pageSize,
  inProgressTodos,
  completedTodos,
  newTodoInput,
  addTodoDialogVisible,
  newTodoForm,
  todoCategories,
  todoDetailDialogVisible,
  currentTodo,
  isEditing,
  ReopenTodoProxy,
  CompleteTodoProxy,
  handleAddTodoInput,
  submitAddTodo,
  openTodoDetail,
  handleUpdateTodo,
  handleDeleteTodo,
  resolveTodoTagType
} = useTodoMain()

const isInProgressVisible = ref(true)
const isCompletedVisible = ref(true)

const handInProgress = () => {
  isInProgressVisible.value = !isInProgressVisible.value
}

const handCompleted = () => {
  isCompletedVisible.value = !isCompletedVisible.value
}
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
          <div v-for="task in inProgressTodos" :key="task.todo_id" class="task-item"
               v-if="isInProgressVisible" @click="openTodoDetail(task)">
            <div class="task-content">
              <!-- 选中框 -->
              <el-checkbox v-show="currentViewState !== 6" class="task-checkbox" @click.stop="CompleteTodoProxy(task.todo_id)"/>

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
                <el-tag :type="resolveTodoTagType(task.category_type)" size="small">
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

              <div class="task-actions" v-show="currentViewState !== 6">
                <el-button-group size="small">
                  <el-button
                      :icon="Edit"
                      type="info"
                      plain
                      @click.stop="openTodoDetail(task, true)"
                  />
                  <el-button
                      :icon="Delete"
                      type="danger"
                      plain
                      @click.stop="handleDeleteTodo(task)"
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
          <div v-if="isCompletedVisible" v-for="task in completedTodos" :key="task.todo_id"
               class="task-item" @click="openTodoDetail(task)">
            <div class="task-content">
              <!--  选中框  -->
              <el-checkbox v-show="currentViewState !== 6" checked class="task-checkbox" @click.stop="ReopenTodoProxy(task.todo_id)"></el-checkbox>

              <div class="task-label completed">
                <el-text>
                  {{ task.title }}
                </el-text>
              </div>

              <div class="task-class">
                <el-tag :type="resolveTodoTagType(task.category_type)" size="small">
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

              <div class="task-actions" v-show="currentViewState !== 6">
                <el-button-group size="small">
                  <el-button
                      :icon="Delete"
                      type="danger"
                      plain
                      @click.stop="handleDeleteTodo(task)"
                  />
                </el-button-group>
              </div>
            </div>
            <div class="bottom-border"/>
          </div>
        </div>

        <el-pagination
            v-if="TodoData.length > pageSize"
            v-model:current-page="currentPage"
            :page-size="pageSize"
            :total="TodoData.length"
            layout="prev, pager, next"
            class="todo-pagination"
        />
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
          <el-select v-model="newTodoForm.categoryId" placeholder="选择分类" clearable @clear="newTodoForm.categoryId = undefined">
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
          <el-select v-model="currentTodo.category_id" placeholder="选择分类" clearable @clear="currentTodo.category_id = undefined">
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
          <el-button v-show="currentViewState !== 6 && !currentTodo.todoInfoisDeleted" type="primary" @click="isEditing = true">编辑</el-button>
          <el-button v-show="currentViewState !== 6 && !currentTodo.todoInfoisDeleted" type="danger" @click="handleDeleteTodo()">删除</el-button>
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

.todo-pagination {
  justify-content: flex-end;
  padding: 12px 8px 4px;
}
</style>
