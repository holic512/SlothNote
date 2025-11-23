/**
 * File Name: UserTodoInfoRep.java
 * Description: 待办事项数据访问层接口，提供对TodoInfo实体的CRUD操作。
 * Author: holic512
 * Created Date: 2024-12-03
 * Version: 1.0
 * Usage: 在需要与待办事项数据交互时使用此接口。
 */
package org.example.backend.user.todo.repository;

import org.example.backend.common.entity.TodoInfo;
import org.example.backend.user.todo.dto.TodoCombinedDTO;
import org.example.backend.user.todo.enums.TodoStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserTodoInfoRep extends JpaRepository<TodoInfo, Long> {

    @Query("SELECT new org.example.backend.user.todo.dto.TodoCombinedDTO(" +
            "ti.id, ti.title, ti.description, ti.startDate, ti.dueDate, ti.status, ti.isDeleted, " +
            "tc.id, tc.name, tc.type) " +
            "FROM TodoInfo ti " +
            "LEFT JOIN TodoCategory tc ON ti.category_id = tc.id " +
            "WHERE ti.user_id = :userId AND ti.isDeleted = false AND (tc.isDeleted = false OR tc.isDeleted IS NULL)")
    List<TodoCombinedDTO> findCombinedTodoInfoByUserId(@Param("userId") Long userId);

    @Query("SELECT new org.example.backend.user.todo.dto.TodoCombinedDTO(" +
            "ti.id, ti.title, ti.description, ti.startDate, ti.dueDate, ti.status, ti.isDeleted, " +
            "tc.id, tc.name, tc.type) " +
            "FROM TodoInfo ti " +
            "JOIN TodoCategory tc ON ti.category_id = tc.id " +
            "WHERE ti.user_id = :userId AND ti.isDeleted = false AND tc.isDeleted = false AND ti.category_id = :categoryId")
    List<TodoCombinedDTO> findCombinedTodoInfoByUserIdAAndCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /**
     * 更新指定用户和待办事项ID的待办事项状态。
     *
     * @param status 新的状态值。
     * @param todoId 待办事项的唯一标识符。
     * @param userId 用户的唯一标识符。
     * @return 被更新的记录数量。
     */
    @Modifying
    @Transactional
    @Query("UPDATE TodoInfo t SET t.status = :status WHERE t.id = :todoId AND t.user_id = :userId")
    int updateStatusByIdAndUserId(int status, @Param("userId") Long userId, @Param("todoId") Long todoId);

    /**
     * 查询指定日期范围内的待办事项。
     *
     * @param userId 用户的唯一标识符。
     * @param startDateTime 开始日期时间。
     * @param endDateTime 结束日期时间。
     * @return 符合条件的待办事项列表。
     */
    @Query("SELECT new org.example.backend.user.todo.dto.TodoCombinedDTO(" +
            "ti.id, ti.title, ti.description, ti.startDate, ti.dueDate, ti.status, ti.isDeleted, " +
            "tc.id, tc.name, tc.type) " +
            "FROM TodoInfo ti " +
            "LEFT JOIN TodoCategory tc ON ti.category_id = tc.id " +
            "WHERE ti.user_id = :userId AND ti.isDeleted = false " +
            "AND ((ti.dueDate BETWEEN :startDateTime AND :endDateTime) " +
            "OR (ti.startDate BETWEEN :startDateTime AND :endDateTime))")
    List<TodoCombinedDTO> findTodosByDateRange(
            @Param("userId") Long userId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
    /**
     * 查询指定用户所有已完成的待办事项。
     *
     * @param userId 用户的唯一标识符。
     * @return 符合条件的待办事项列表。
     */
    @Query("SELECT new org.example.backend.user.todo.dto.TodoCombinedDTO(" +
            "ti.id, ti.title, ti.description, ti.startDate, ti.dueDate, ti.status, ti.isDeleted, " +
            "tc.id, tc.name, tc.type) " +
            "FROM TodoInfo ti " +
            "LEFT JOIN TodoCategory tc ON ti.category_id = tc.id " +
            "WHERE ti.user_id = :userId AND ti.isDeleted = false AND ti.status = 1")
    List<TodoCombinedDTO> findCompletedTodosByUserId(@Param("userId") Long userId);

    /**
     * 查询指定用户所有已过期的待办事项（截止日期在当前时间之前且状态为未完成）。
     *
     * @param userId 用户的唯一标识符。
     * @param currentDateTime 当前日期时间。
     * @return 符合条件的待办事项列表。
     */
    @Query("SELECT new org.example.backend.user.todo.dto.TodoCombinedDTO(" +
            "ti.id, ti.title, ti.description, ti.startDate, ti.dueDate, ti.status, ti.isDeleted, " +
            "tc.id, tc.name, tc.type) " +
            "FROM TodoInfo ti " +
            "LEFT JOIN TodoCategory tc ON ti.category_id = tc.id " +
            "WHERE ti.user_id = :userId AND ti.isDeleted = false " +
            "AND ti.status = 0 AND ti.dueDate < :currentDateTime")
    List<TodoCombinedDTO> findExpiredTodosByUserId(
            @Param("userId") Long userId,
            @Param("currentDateTime") LocalDateTime currentDateTime);

    /**
     * 查询指定用户的未分类待办事项（category_id 为 0 或无关联分类）。
     */
    @Query("SELECT new org.example.backend.user.todo.dto.TodoCombinedDTO(" +
            "ti.id, ti.title, ti.description, ti.startDate, ti.dueDate, ti.status, ti.isDeleted, " +
            "tc.id, tc.name, tc.type) " +
            "FROM TodoInfo ti " +
            "LEFT JOIN TodoCategory tc ON ti.category_id = tc.id " +
            "WHERE ti.user_id = :userId AND ti.isDeleted = false " +
            "AND ti.category_id IS NULL")
    List<TodoCombinedDTO> findUncategorizedTodosByUserId(@Param("userId") Long userId);

    /**
     * 查询指定用户的回收站待办事项（逻辑删除项）。
     */
    @Query("SELECT new org.example.backend.user.todo.dto.TodoCombinedDTO(" +
            "ti.id, ti.title, ti.description, ti.startDate, ti.dueDate, ti.status, ti.isDeleted, " +
            "tc.id, tc.name, tc.type) " +
            "FROM TodoInfo ti " +
            "LEFT JOIN TodoCategory tc ON ti.category_id = tc.id " +
            "WHERE ti.user_id = :userId AND ti.isDeleted = true")
    List<TodoCombinedDTO> findDeletedTodosByUserId(@Param("userId") Long userId);
}
