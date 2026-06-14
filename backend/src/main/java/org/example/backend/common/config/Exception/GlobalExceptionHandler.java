/**
 * @file GlobalExceptionHandler
 * @project SlothNote
 * @module 公共配置 / 全局异常
 * @description 统一拦截鉴权与运行时配置异常，返回前端可读的错误信息。
 * @logic 1. 处理 Sa-Token 异常；2. 处理系统运行时配置缺失或未启用异常。
 * @dependencies Sa-Token: SaTokenException/SaResult, Response: ApiResponse
 * @index_tags 全局异常, SaToken, 配置错误, ApiResponse
 * @author holic512
 */
package org.example.backend.common.config.Exception;

import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.util.SaResult;
import org.example.backend.common.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(SaTokenException.class)
    public SaResult handlerSaTokenException(SaTokenException e) {

        // 根据不同异常细分状态码返回不同的提示
        if(e.getCode() == 11012) {
            return SaResult.error("未读取有效token");
        }


        // 默认的提示
        return SaResult.error("服务器繁忙，请稍后重试...,错误信息:" + e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ApiResponse<Object> handleIllegalStateException(IllegalStateException e) {
        return new ApiResponse<>(400, e.getMessage());
    }
}
