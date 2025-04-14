/**
 * File Name: PutUTodoService.java
 * Description: 该接口提供了用户待办事项（Todos）更新服务的方法。
 * Author: holic512
 * Created Date: 2024-12-08
 * Version: 1.0
 * Usage: 实现此接口以提供待办事项更新的服务。
 */
package org.example.backend.user.todo.service;

import org.example.backend.user.todo.enums.PatchUTContextEnum;
import org.example.backend.user.todo.pojo.TodoInfoRequest;

public interface PutUTodoService {
    /**
     * 更新指定用户的特定待办事项。
     *
     * @param userId 用户唯一标识符。
     * @param todoId 待办事项唯一标识符。
     * @param request 待办事项更新请求数据。
     * @return 操作结果的状态枚举。
     */
    PatchUTContextEnum updateTodo(Long userId, Long todoId, TodoInfoRequest request);
}