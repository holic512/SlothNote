/**
 * File Name: GetUTodoService.java
 * Description: 该接口提供了用户待办事项（Todos）相关服务的方法。
 * Author: holic512
 * Created Date: 2024-12-02
 * Version: 1.0
 * Usage: 实现此接口以提供待办事项管理服务。
 */

package org.example.backend.user.todo.service;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.user.todo.enums.GetUTContextEnum;

/**
 * 定义了用于获取用户待办事项的服务接口。
 */
public interface GetUTodoService {

    /**
     * 获取指定用户的全部待办事项。
     *
     * @param userId 用户唯一标识符。
     * @return 包含状态枚举和结果对象的Pair。
     */
    Pair<GetUTContextEnum, Object> getAllTodos(Long userId);

    /**
     * 获取指定用户的全部待办事项分类。
     *
     * @param userId 用户唯一标识符。
     * @return 包含状态枚举和结果对象的Pair。
     */
    Pair<GetUTContextEnum, Object> getAllTodoCategory(Long userId);

    /**
     * 根据类别ID获取指定用户的待办事项。
     *
     * @param userId     用户唯一标识符。
     * @param categoryId 类别唯一标识符。
     * @return 包含状态枚举和结果对象的Pair。
     */
    Pair<GetUTContextEnum, Object> getAllTodosByCategoryId(Long userId, Long categoryId);

    /**
     * 根据日期获取指定用户的待办事项。
     *
     * @param userId 用户唯一标识符。
     * @param date   日期字符串，格式为yyyy-MM-dd。
     * @return 包含状态枚举和结果对象的Pair。
     */
    Pair<GetUTContextEnum, Object> getTodosByDate(Long userId, String date);
    /**
     * 获取指定用户未来7天内的待办事项。
     *
     * @param userId 用户唯一标识符。
     * @return 包含状态枚举和结果对象的Pair。
     */
    Pair<GetUTContextEnum, Object> getTodosForWeek(Long userId);

    /**
     * 获取指定用户已完成的待办事项。
     *
     * @param userId 用户唯一标识符。
     * @return 包含状态枚举和结果对象的Pair。
     */
    Pair<GetUTContextEnum, Object> getCompletedTodos(Long userId);

    /**
     * 获取指定用户已过期的待办事项。
     *
     * @param userId 用户唯一标识符。
     * @return 包含状态枚举和结果对象的Pair。
     */
    Pair<GetUTContextEnum, Object> getExpiredTodos(Long userId);

    /**
     * 获取指定用户的未分类待办事项。
     */
    Pair<GetUTContextEnum, Object> getUncategorizedTodos(Long userId);

    /**
     * 获取指定用户的回收站待办事项（逻辑删除）。
     */
    Pair<GetUTContextEnum, Object> getRecycleBinTodos(Long userId);
}
