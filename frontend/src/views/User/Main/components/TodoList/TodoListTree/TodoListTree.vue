<script setup lang="ts">
import {ref, computed} from "vue";
import {Search} from '@element-plus/icons-vue';

// 导入所有日历图标组件
import Calendar1 from "@/assets/icon/TodoList/Calendar1.vue";
import Calendar2 from "@/assets/icon/TodoList/Calendar2.vue";
import Calendar3 from "@/assets/icon/TodoList/Calendar3.vue";
import Calendar4 from "@/assets/icon/TodoList/Calendar4.vue";
import Calendar5 from "@/assets/icon/TodoList/Calendar5.vue";
import Calendar6 from "@/assets/icon/TodoList/Calendar6.vue";
import Calendar7 from "@/assets/icon/TodoList/Calendar7.vue";
import Calendar8 from "@/assets/icon/TodoList/Calendar8.vue";
import Calendar9 from "@/assets/icon/TodoList/Calendar9.vue";
import Calendar10 from "@/assets/icon/TodoList/Calendar10.vue";
import Calendar11 from "@/assets/icon/TodoList/Calendar11.vue";
import Calendar12 from "@/assets/icon/TodoList/Calendar12.vue";
import Calendar13 from "@/assets/icon/TodoList/Calendar13.vue";
import Calendar14 from "@/assets/icon/TodoList/Calendar14.vue";
import Calendar15 from "@/assets/icon/TodoList/Calendar15.vue";
import Calendar16 from "@/assets/icon/TodoList/Calendar16.vue";
import Calendar17 from "@/assets/icon/TodoList/Calendar17.vue";
import Calendar18 from "@/assets/icon/TodoList/Calendar18.vue";
import Calendar19 from "@/assets/icon/TodoList/Calendar19.vue";
import Calendar20 from "@/assets/icon/TodoList/Calendar20.vue";
import Calendar21 from "@/assets/icon/TodoList/Calendar21.vue";
import Calendar22 from "@/assets/icon/TodoList/Calendar22.vue";
import Calendar23 from "@/assets/icon/TodoList/Calendar23.vue";
import Calendar24 from "@/assets/icon/TodoList/Calendar24.vue";
import Calendar25 from "@/assets/icon/TodoList/Calendar25.vue";
import Calendar26 from "@/assets/icon/TodoList/Calendar26.vue";
import Calendar27 from "@/assets/icon/TodoList/Calendar27.vue";
import Calendar28 from "@/assets/icon/TodoList/Calendar28.vue";
import Calendar29 from "@/assets/icon/TodoList/Calendar29.vue";
import Calendar30 from "@/assets/icon/TodoList/Calendar30.vue";
import Calendar31 from "@/assets/icon/TodoList/Calendar31.vue";

import CalendarComplete from "@/assets/icon/TodoList/CalendarComplete.vue";
import CalendarExpired from "@/assets/icon/TodoList/CalendarExpired.vue";
import ClassTree from "@/views/User/Main/components/TodoList/TodoListTree/ClassTree/ClassTree.vue";
import AddClass from "@/views/User/Main/components/TodoList/TodoListTree/addClass/addClass.vue";
import {useTodoState} from "@/views/User/Main/components/TodoList/Pinia/TodoState";

// 获取当前日期
const currentDate = new Date();
const day = currentDate.getDate(); // 获取当前日期的日号

// 动态映射当前日期到对应的日历图标组件
const calendarIcon = computed(() => {
  const icons = {
    1: Calendar1,
    2: Calendar2,
    3: Calendar3,
    4: Calendar4,
    5: Calendar5,
    6: Calendar6,
    7: Calendar7,
    8: Calendar8,
    9: Calendar9,
    10: Calendar10,
    11: Calendar11,
    12: Calendar12,
    13: Calendar13,
    14: Calendar14,
    15: Calendar15,
    16: Calendar16,
    17: Calendar17,
    18: Calendar18,
    19: Calendar19,
    20: Calendar20,
    21: Calendar21,
    22: Calendar22,
    23: Calendar23,
    24: Calendar24,
    25: Calendar25,
    26: Calendar26,
    27: Calendar27,
    28: Calendar28,
    29: Calendar29,
    30: Calendar30,
    31: Calendar31
  };

  // 返回当前日期对应的图标组件
  return icons[day] || Calendar1; // 如果没有对应的图标，默认显示 Calendar1
});

const input1 = ref('');

// 初始化 页面内容切换 state
const todoState = useTodoState();
// TodoListTree.vue - 修改handleSevenDays函数
const handleSevenDays = () => {
  // 切换到七日视图
  todoState.ToCustomView();
};


const handleCompleted = () => {
  // 已完成视图
  todoState.ToCompletedView();
};

const handleExpired = () => {
  // 已过期视图
  todoState.ToExpiredView();
};

// 辅助函数，格式化日期为yyyy-MM-dd
const formatDate = (date) => {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

</script>

<template>
  <div class="todo-list-tree">
    <!-- <el-input
        v-model="input1"
        style="width: 100%;margin: 2px;"
        placeholder="搜索全部待办"
        :prefix-icon="Search"
    /> -->

    <div>
      <!-- 今日待做 - 已实现 -->
      <!-- <div class="todo-list-button" @click="todoState.ToToday()">
        <component :is="calendarIcon" style="width: 18px;height: 18px;margin-right: 6px"/>
        <el-text>今日待做</el-text>
      </div> -->

      <!-- 七日待做 - 添加点击事件 -->
      <!-- <div class="todo-list-button" @click="handleSevenDays()">
        <calendar7 style="width: 18px;height: 18px;margin-right: 6px"/>
        <el-text>七日待做</el-text>
      </div> -->

      <!-- 已完成 - 添加点击事件 -->
      <!-- <div class="todo-list-button" @click="handleCompleted()">
        <CalendarComplete style="width: 18px;height: 18px;margin-right: 6px"/>
        <el-text>已完成</el-text>
      </div> -->

      <!-- 已过期 - 添加点击事件 -->
      <!-- <div class="todo-list-button" @click="handleExpired()">
        <CalendarExpired style="width: 20px;height: 20px;margin-right: 6px"/>
        <el-text>已过期</el-text>
      </div> -->
    </div>

    <!-- 分类的 树结构 -->
    <ClassTree/>
  </div>
</template>

<style scoped>
.todo-list-tree {
  padding: 10px;
}

.todo-list-button {
  display: flex;
  align-items: center;
  padding: 8px;
  border-radius: 4px;
  margin: 2px;

  user-select: none;
}

.todo-list-button:hover {
  background-color: #F0F2F5;
}
</style>
