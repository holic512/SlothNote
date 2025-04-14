/**
 * File Name: PutUTodoServiceImpl.java
 * Description: 服务实现类，用于处理用户待办事项的更新请求。
 * Author: holic512
 * Created Date: 2024-12-08
 * Version: 1.0
 * Usage: 在需要更新待办事项时使用此服务实现类。
 */
package org.example.backend.user.todo.service.impl;

import org.example.backend.common.entity.TodoInfo;
import org.example.backend.user.todo.enums.PatchUTContextEnum;
import org.example.backend.user.todo.pojo.TodoInfoRequest;
import org.example.backend.user.todo.repository.UserTodoInfoRep;
import org.example.backend.user.todo.service.PutUTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PutUTodoServiceImpl implements PutUTodoService {

    private final UserTodoInfoRep userTodoInfoRep;

    @Autowired
    public PutUTodoServiceImpl(UserTodoInfoRep userTodoInfoRep) {
        this.userTodoInfoRep = userTodoInfoRep;
    }

    @Override
    public PatchUTContextEnum updateTodo(Long userId, Long todoId, TodoInfoRequest request) {
        try {
            // 查找待办事项并验证所有权
            Optional<TodoInfo> todoOpt = userTodoInfoRep.findById(todoId);
            if (todoOpt.isEmpty() || !todoOpt.get().getUser_id().equals(userId)) {
                return PatchUTContextEnum.NOT_FOUND;
            }

            TodoInfo todoInfo = todoOpt.get();

            // 更新待办事项信息
            todoInfo.setTitle(request.getTitle());
            todoInfo.setDescription(request.getDescription());
            todoInfo.setCategory_id(request.getCategoryId());
            todoInfo.setDueDate(request.getDueDate());

            if (request.getStatus() != null) {
                todoInfo.setStatus(request.getStatus());
            }

            // 保存更新后的待办事项
            userTodoInfoRep.save(todoInfo);

            return PatchUTContextEnum.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return PatchUTContextEnum.NOT_FOUND;
        }
    }
}