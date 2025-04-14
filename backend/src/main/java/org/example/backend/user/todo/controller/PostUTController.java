/**
 * File Name: PostUTController.java
 * Description: 控制器类，用于处理用户待办事项（Todos）的创建请求，包括添加待办事项分类（文件夹）。
 * Author: holic512
 * Created Date: 2024-12-04
 * Version: 1.0
 * Usage: 通过 HTTP POST 请求调用本控制器的方法，以实现对用户待办事项分类的创建操作。
 */

package org.example.backend.user.todo.controller;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.todo.enums.PostUTContextEnum;
import org.example.backend.user.todo.pojo.PostAddCategoryPojo;
import org.example.backend.user.todo.pojo.TodoInfoRequest;
import org.example.backend.user.todo.service.PostUTodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("user/todo")
public class PostUTController {
    private final PostUTodoService putService;

    @Autowired
    public PostUTController(PostUTodoService putService) {
        this.putService = putService;
    }

    /**
     * 用于添加对应用户的 todoCategory(待做文件夹)
     *
     * @param pojo
     * @return
     */
    @PostMapping("addCategory")
    public Object addCategory(@RequestBody PostAddCategoryPojo pojo) {
        // 获取用户id
        Long UserId = (Long) StpKit.USER.getSession().get("id");

        // 调用服务类
        PostUTContextEnum result = putService.AddCategory(UserId, pojo);

        if (Objects.requireNonNull(result) == PostUTContextEnum.SUCCESS) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("添加成功")
                    .build()
            );
        }
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(500)
                .message("添加失败")
                .build()
        );
    }

    /**
     * 添加待办事项
     *
     * @param request 待办事项请求数据
     * @return 响应结果
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<Object>> addTodo(@RequestBody TodoInfoRequest request) {
        Long userId = (Long) StpKit.USER.getSession().get("id");
        PostUTContextEnum result = putService.addTodo(userId, request);

        if (Objects.requireNonNull(result) == PostUTContextEnum.SUCCESS) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("添加成功")
                    .build()
            );
        }
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(500)
                .message("添加失败")
                .build()
        );
    }
}