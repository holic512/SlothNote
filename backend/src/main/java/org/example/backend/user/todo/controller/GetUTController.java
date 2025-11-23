/**
 * File Name: GUTController.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-11-29
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.todo.controller;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.todo.enums.GetUTContextEnum;
import org.example.backend.user.todo.service.GetUTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("user/todo")
@RestController
public class GetUTController {

    private final GetUTodoService gutService;

    @Autowired
    public GetUTController(GetUTodoService gutService) {
        this.gutService = gutService;
    }

    /**
     * 获取 该用户的 全部todo - 对应前端的 全部
     *
     * @return
     */
    @GetMapping("/all")
    public Object getAllTodos() {
        // 获取用户Id
        Long UserId = (Long) StpKit.USER.getSession().get("id");

        // 调用服务类
        Pair<GetUTContextEnum, Object> result = gutService.getAllTodos(UserId);

        return result.b;
    }


    /**
     * 获取 该用户的 未分类待做
     *
     * @return
     */
    @GetMapping("/uncategorized")
    public Object getUncategorizedTodos() {
        Long UserId = (Long) StpKit.USER.getSession().get("id");
        Pair<GetUTContextEnum, Object> result = gutService.getUncategorizedTodos(UserId);
        return result.b;
    }

    /**
     * 获取 该用户的 某个分类的 待做
     *
     * @return
     */
    @GetMapping("/TodosByCategory")
    public Object getTodosByCategory(@Param("categoryId") Long categoryId) {
        // 获取用户Id
        Long UserId = (Long) StpKit.USER.getSession().get("id");

        // 调用服务类
        Pair<GetUTContextEnum, Object> result = gutService.getAllTodosByCategoryId(UserId, categoryId);

        return result.b;
    }

    /**
     * 获取 该用户的 todoTree
     */
    @GetMapping("/todoCategory")
    public Object getAllTodoCategory() {
        Long UserId = (Long) StpKit.USER.getSession().get("id");
        // 调用服务类
        Pair<GetUTContextEnum, Object> result = gutService.getAllTodoCategory(UserId);
        return result.b;
    }

    /**
     * 获取该用户回收站中的待办事项（逻辑删除项）
     */
    @GetMapping("/recycleBin")
    public Object getRecycleBinTodos() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        Pair<GetUTContextEnum, Object> result = gutService.getRecycleBinTodos(userId);
        return result.b;
    }

    /**
     * 按日期获取用户的待办事项
     *
     * @param date 日期字符串，格式为yyyy-MM-dd
     * @return 待办事项列表
     */
    @GetMapping("/byDate")
    public Object getTodosByDate(@RequestParam String date) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        Pair<GetUTContextEnum, Object> result = gutService.getTodosByDate(userId, date);
        return result.b;
    }
    /**
     * 获取未来7天的待办事项
     *
     * @return 未来7天的待办事项列表
     */
    @GetMapping("/week")
    public Object getTodosForWeek() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        Pair<GetUTContextEnum, Object> result = gutService.getTodosForWeek(userId);
        return result.b;
    }

    /**
     * 获取已完成的待办事项
     *
     * @return 已完成的待办事项列表
     */
    @GetMapping("/completed")
    public Object getCompletedTodos() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        Pair<GetUTContextEnum, Object> result = gutService.getCompletedTodos(userId);
        return result.b;
    }

    /**
     * 获取已过期的待办事项
     *
     * @return 已过期的待办事项列表
     */
    @GetMapping("/expired")
    public Object getExpiredTodos() {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        Pair<GetUTContextEnum, Object> result = gutService.getExpiredTodos(userId);
        return result.b;
    }
}