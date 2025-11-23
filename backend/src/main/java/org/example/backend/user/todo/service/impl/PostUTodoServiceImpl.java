/**
 * File Name: PostUTodoServiceImpl.java
 * Description: 服务实现类，用于处理用户待办事项及分类的创建请求。
 * Author: holic512
 * Created Date: 2024-12-05
 * Version: 1.0
 * Usage: 在需要创建新的待办事项或分类时使用本服务实现类。
 */
package org.example.backend.user.todo.service.impl;

import org.example.backend.common.entity.TodoCategory;
import org.example.backend.common.entity.TodoInfo;
import org.example.backend.user.todo.enums.PostUTContextEnum;
import org.example.backend.user.todo.enums.TodoStatusEnum;
import org.example.backend.user.todo.pojo.PostAddCategoryPojo;
import org.example.backend.user.todo.pojo.TodoInfoRequest;
import org.example.backend.user.todo.repository.UserTodoCategoryRep;
import org.example.backend.user.todo.repository.UserTodoInfoRep;
import org.example.backend.user.todo.service.PostUTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostUTodoServiceImpl implements PostUTodoService {

    private final UserTodoCategoryRep gutTodoCategoryRep;
    private final UserTodoInfoRep userTodoInfoRep;

    @Autowired
    public PostUTodoServiceImpl(UserTodoCategoryRep userTodoCategoryRep, UserTodoInfoRep userTodoInfoRep) {
        this.gutTodoCategoryRep = userTodoCategoryRep;
        this.userTodoInfoRep = userTodoInfoRep;
    }

    @Override
    public PostUTContextEnum AddCategory(Long userId, PostAddCategoryPojo category) {
        try {
            // 实例化 TodoCategory
            TodoCategory todoCategory = new TodoCategory();
            todoCategory.setUser_id(userId);
            todoCategory.setName(category.getName());
            todoCategory.setType(category.getType());

            // 保存分类
            gutTodoCategoryRep.save(todoCategory);
            return PostUTContextEnum.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PostUTContextEnum addTodo(Long userId, TodoInfoRequest request) {
        try {
            // 创建新的待办事项
            TodoInfo todoInfo = new TodoInfo();
            todoInfo.setUser_id(userId);
            todoInfo.setTitle(request.getTitle());
            todoInfo.setDescription(request.getDescription());
            // 分类可为空：未分类待办
            todoInfo.setCategory_id(request.getCategoryId());
            todoInfo.setDueDate(request.getDueDate());
            todoInfo.setStartDate(LocalDateTime.now());
            todoInfo.setStatus(TodoStatusEnum.INCOMPLETE.getValue());
            todoInfo.setIsDeleted(false);

            // 保存待办事项
            userTodoInfoRep.save(todoInfo);

            return PostUTContextEnum.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}