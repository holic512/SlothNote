/**
 * File Name: PutUTController.java
 * Description: 控制器类，用于处理用户待办事项（Todos）的更新请求。
 * Author: holic512
 * Created Date: 2024-12-08
 * Version: 1.0
 * Usage: 通过 HTTP PUT 请求来调用本控制器的方法，以实现对用户待办事项的更新操作。
 */
package org.example.backend.user.todo.controller;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.todo.enums.PatchUTContextEnum;
import org.example.backend.user.todo.pojo.TodoInfoRequest;
import org.example.backend.user.todo.service.PutUTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/todo")
public class PutUTController {

    private final PutUTodoService putUTodoService;

    @Autowired
    public PutUTController(PutUTodoService putUTodoService) {
        this.putUTodoService = putUTodoService;
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Object>> updateTodo(@Param("todoId") Long todoId, @RequestBody TodoInfoRequest request) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        PatchUTContextEnum result = putUTodoService.updateTodo(userId, todoId, request);

        if (result == PatchUTContextEnum.SUCCESS) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("更新成功")
                    .build()
            );
        }
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(500)
                .message("更新失败")
                .build()
        );
    }
}