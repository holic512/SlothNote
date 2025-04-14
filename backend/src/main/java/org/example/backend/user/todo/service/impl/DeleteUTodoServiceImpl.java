/**
 * File Name: DeleteUTodoServiceImpl.java
 * Description: 服务实现类，用于处理用户待办事项的删除请求。
 * Author: holic512
 * Created Date: 2024-12-08
 * Version: 1.0
 * Usage: 在需要删除待办事项时使用此服务实现类。
 */
package org.example.backend.user.todo.service.impl;

import org.example.backend.common.entity.TodoInfo;
import org.example.backend.user.todo.enums.PatchUTContextEnum;
import org.example.backend.user.todo.repository.UserTodoInfoRep;
import org.example.backend.user.todo.service.DeleteUTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeleteUTodoServiceImpl implements DeleteUTodoService {

    private final UserTodoInfoRep userTodoInfoRep;

    @Autowired
    public DeleteUTodoServiceImpl(UserTodoInfoRep userTodoInfoRep) {
        this.userTodoInfoRep = userTodoInfoRep;
    }

    @Override
    public PatchUTContextEnum deleteTodo(Long userId, Long todoId) {
        try {
            // 查找待办事项并验证所有权
            Optional<TodoInfo> todoOpt = userTodoInfoRep.findById(todoId);
            if (todoOpt.isEmpty() || !todoOpt.get().getUser_id().equals(userId)) {
                return PatchUTContextEnum.NOT_FOUND;
            }

            TodoInfo todoInfo = todoOpt.get();

            // 逻辑删除：将isDeleted设置为true
            todoInfo.setIsDeleted(true);
            userTodoInfoRep.save(todoInfo);

            // 或者物理删除：直接从数据库删除
            // userTodoInfoRep.delete(todoInfo);

            return PatchUTContextEnum.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return PatchUTContextEnum.NOT_FOUND;
        }
    }
}