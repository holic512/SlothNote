/**
 * File Name: DeleteUTodoService.java
 * Description: 该接口提供了用户待办事项（Todos）删除服务的方法。
 * Author: holic512
 * Created Date: 2024-12-08
 * Version: 1.0
 * Usage: 实现此接口以提供待办事项删除的服务。
 */
package org.example.backend.user.todo.service;

import org.example.backend.user.todo.enums.PatchUTContextEnum;

public interface DeleteUTodoService {
    /**
     * 删除指定用户的特定待办事项。
     *
     * @param userId 用户唯一标识符。
     * @param todoId 待办事项唯一标识符。
     * @return 操作结果的状态枚举。
     */
    PatchUTContextEnum deleteTodo(Long userId, Long todoId);
}