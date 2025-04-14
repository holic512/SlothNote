/**
 * File Name: DeleteUTController.java
 * Description: 控制器类，用于处理用户待办事项（Todos）的删除请求。
 * Author: holic512
 * Created Date: 2024-12-08
 * Version: 1.0
 * Usage: 通过 HTTP DELETE 请求来调用本控制器的方法，以实现对用户待办事项的删除操作。
 */
package org.example.backend.user.todo.controller;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.todo.enums.PatchUTContextEnum;
import org.example.backend.user.todo.service.DeleteUTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user/todo")
public class DeleteUTController {

    private final DeleteUTodoService deleteUTodoService;

    @Autowired
    public DeleteUTController(DeleteUTodoService deleteUTodoService) {
        this.deleteUTodoService = deleteUTodoService;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Object>> deleteTodo(@Param("todoId") Long todoId) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        PatchUTContextEnum result = deleteUTodoService.deleteTodo(userId, todoId);

        if (result == PatchUTContextEnum.SUCCESS) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("删除成功")
                    .build()
            );
        }
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(500)
                .message("删除失败")
                .build()
        );
    }
}