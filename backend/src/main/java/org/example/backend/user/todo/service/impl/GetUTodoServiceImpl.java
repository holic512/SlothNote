/**
 * File Name: GetUTodoServiceImpl.java
 * Description: 服务实现类，用于处理获取用户待办事项（Todos）及其分类（Categories）的业务逻辑。
 * Author: holic512
 * Created Date: 2024-12-02
 * Version: 1.0
 * Usage: 在需要从数据库中检索用户待办事项或分类信息时使用本服务实现类。
 */
package org.example.backend.user.todo.service.impl;

import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.entity.TodoCategory;
import org.example.backend.user.todo.dto.TodoCombinedDTO;
import org.example.backend.user.todo.enums.GetUTContextEnum;
import org.example.backend.user.todo.repository.UserTodoCategoryRep;
import org.example.backend.user.todo.repository.UserTodoInfoRep;
import org.example.backend.user.todo.service.GetUTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GetUTodoServiceImpl implements GetUTodoService {

    private final UserTodoCategoryRep gutTodoCategoryRep;
    private final UserTodoInfoRep userTodoInfoRep;

    @Autowired
    public GetUTodoServiceImpl(UserTodoCategoryRep userTodoCategoryRep,
                               UserTodoInfoRep userTodoInfoRep) {
        this.gutTodoCategoryRep = userTodoCategoryRep;
        this.userTodoInfoRep = userTodoInfoRep;
    }

    @Override
    public Pair<GetUTContextEnum, Object> getAllTodos(Long userId) {
        // 获取该用户名下的全部TodosCombined
        List<TodoCombinedDTO> TodosCombined = userTodoInfoRep.findCombinedTodoInfoByUserId(userId);
        return new Pair<>(GetUTContextEnum.SUCCESS, TodosCombined);
    }

    @Override
    public Pair<GetUTContextEnum, Object> getAllTodoCategory(Long userId) {
        // 获取该用户名下的全部TodoTree
        List<TodoCategory> todoCategories = gutTodoCategoryRep.findAllByUserId(userId);
        return new Pair<>(GetUTContextEnum.SUCCESS, todoCategories);
    }

    @Override
    public Pair<GetUTContextEnum, Object> getAllTodosByCategoryId(Long userId, Long categoryId) {
        List<TodoCombinedDTO> todoCombinedDTOS = userTodoInfoRep.findCombinedTodoInfoByUserIdAAndCategoryId(userId, categoryId);
        return new Pair<>(GetUTContextEnum.SUCCESS, todoCombinedDTOS);
    }

    @Override
    public Pair<GetUTContextEnum, Object> getTodosByDate(Long userId, String date) {
        try {
            // 解析日期字符串
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            // 计算当天的开始和结束时间
            LocalDateTime startOfDay = localDate.atStartOfDay();
            LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

            // 查询该日期范围内的待办事项
            List<TodoCombinedDTO> todos = userTodoInfoRep.findTodosByDateRange(userId, startOfDay, endOfDay);

            return new Pair<>(GetUTContextEnum.SUCCESS, todos);
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>(GetUTContextEnum.SUCCESS, List.of());
        }
    }
    @Override
    public Pair<GetUTContextEnum, Object> getTodosForWeek(Long userId) {
        try {
            // 计算今天和7天后的日期
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime sevenDaysLater = now.plusDays(7);

            // 查询未来7天的待办事项
            List<TodoCombinedDTO> todos = userTodoInfoRep.findTodosByDateRange(userId, now, sevenDaysLater);

            return new Pair<>(GetUTContextEnum.SUCCESS, todos);
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>(GetUTContextEnum.SUCCESS, List.of());
        }
    }

    @Override
    public Pair<GetUTContextEnum, Object> getCompletedTodos(Long userId) {
        try {
            // 查询已完成的待办事项
            List<TodoCombinedDTO> todos = userTodoInfoRep.findCompletedTodosByUserId(userId);

            return new Pair<>(GetUTContextEnum.SUCCESS, todos);
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>(GetUTContextEnum.SUCCESS, List.of());
        }
    }

    @Override
    public Pair<GetUTContextEnum, Object> getExpiredTodos(Long userId) {
        try {
            // 查询已过期的待办事项
            LocalDateTime now = LocalDateTime.now();
            List<TodoCombinedDTO> todos = userTodoInfoRep.findExpiredTodosByUserId(userId, now);

            return new Pair<>(GetUTContextEnum.SUCCESS, todos);
        } catch (Exception e) {
            e.printStackTrace();
            return new Pair<>(GetUTContextEnum.SUCCESS, List.of());
        }
    }
}