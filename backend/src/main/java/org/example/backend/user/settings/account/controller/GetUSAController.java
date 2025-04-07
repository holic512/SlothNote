/**
 * File Name: GetUSAController.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-12-23
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.settings.account.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.StpKit;
import org.example.backend.user.settings.account.service.GetUSAService;
import org.example.backend.user.settings.service.AccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论管理", description = "处理用户评论相关的操作")
@RestController
@RequestMapping("/user/account")
@RequiredArgsConstructor
public class GetUSAController {

    private final GetUSAService getUSAService;

    @Operation(summary = "获取是否初始化过个人信息", description = "系统获取用户是否初始化过个人信息")
    @GetMapping("/hasProfile")
    public ApiResponse<Object> hasProfile() {
        // 从上下文获取用户ID
        Long userId = (Long) StpKit.USER.getSession().get("id");
        boolean result = getUSAService.getHasProfile(userId);
        return new ApiResponse.Builder<>()
                .status(200)
                .message("获取是否初始化过个人信息成功")
                .data(result)
                .build();
    }
}
