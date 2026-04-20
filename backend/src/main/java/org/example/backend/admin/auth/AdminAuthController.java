/**
 * File Name: AuthController.java
 * Description: 管理员身份验证 控制器
 * Author: holic512
 * Created Date: 2024-09-04
 * Version: 1.0
 * Usage:
 * 前端调用 restful接口 进行身份验证
 */
package org.example.backend.admin.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/auth")
public class AdminAuthController {
    AdminAuthService adminAuthService;

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthController.class);


    @Autowired
    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> requestBody) throws JsonProcessingException {
        String username = requestBody.get("username");
        String password = requestBody.get("password");

        // 日志
        logger.info("管理员:{}请求登录", username);


        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.ok(new ApiResponse<>(404, "用户名或密码为空", authPayload(null, false, null, false, false)));
        }

        // 调用服务类
        Pair<AuthServiceEnum, String> result = adminAuthService.login(username, password);

        switch (result.a) {
            case Success -> {
                return ResponseEntity.ok(new ApiResponse<>(200,
                        "登录成功",
                        authPayload(
                                result.b,
                                false,
                                null,
                                false,
                                false
                        )));
            }
            case NeedInit -> {
                return ResponseEntity.ok(new ApiResponse<>(409, "系统尚未初始化管理员", authPayload(null, false, null, false, true)));
            }
            case UserNotExists -> {
                return ResponseEntity.ok(new ApiResponse<>(404, "用户不存在", authPayload(null, false, null, false, false)));
            }
            case INCORRECT -> {
                return ResponseEntity.ok(new ApiResponse<>(401, "密码错误", authPayload(null, false, null, false, false)));
            }
            case EmailSendFailure -> {
                return ResponseEntity.ok(new ApiResponse<>(405, "邮箱发送失败", authPayload(null, false, null, true, false)));
            }
            default -> {
                return ResponseEntity.ok(new ApiResponse<>(500, "无法连接服务器", authPayload(null, false, null, false, false)));
            }

        }
    }

    @PostMapping("init")
    public ResponseEntity<Object> init(@RequestBody Map<String, String> requestBody) {
        String username = requestBody == null ? null : requestBody.get("username");
        String password = requestBody == null ? null : requestBody.get("password");
        String email = requestBody == null ? null : requestBody.get("email");

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.ok(new ApiResponse<>(404, "管理员账号或密码为空", authPayload(null, false, null, email != null && !email.isBlank(), true)));
        }

        Pair<AuthServiceEnum, String> result = adminAuthService.initAdmin(username, password, email);
        return switch (result.a) {
            case Success -> ResponseEntity.ok(new ApiResponse<>(200, "管理员初始化成功", authPayload(result.b, false, null, email != null && !email.isBlank(), false)));
            case AdminAlreadyInitialized -> ResponseEntity.ok(new ApiResponse<>(409, "管理员已初始化", authPayload(null, false, null, false, false)));
            case UserAlreadyExists -> ResponseEntity.ok(new ApiResponse<>(409, "管理员账号已存在", authPayload(null, false, null, false, false)));
            case EmailAlreadyExists -> ResponseEntity.ok(new ApiResponse<>(409, "邮箱已存在", authPayload(null, false, null, true, false)));
            case InvalidEmail -> ResponseEntity.ok(new ApiResponse<>(400, "邮箱格式错误", authPayload(null, false, null, true, true)));
            default -> ResponseEntity.ok(new ApiResponse<>(500, "管理员初始化失败", authPayload(null, false, null, false, true)));
        };
    }

    @PostMapping("verLogin")
    public ResponseEntity<Object> verLogin(@RequestBody Map<String, String> requestBody) {
        String logID = requestBody.get("logID");
        String code = requestBody.get("code");


        if (code == null || logID == null) {
            return ResponseEntity.ok(new ApiResponse<>(404, "登录验证码或验证请求为空", authPayload(null, true, logID, true, false)));
        }

        // service
        Pair<AuthServiceEnum, String> result = adminAuthService.verLogin(logID, code);

        // 验证状态
        switch (result.a) {
            case Success -> {
                return ResponseEntity.ok(new ApiResponse<>(200, "管理员登陆成功", authPayload(result.b, false, null, true, false)));
            }
            case JsonParseError -> {
                return ResponseEntity.ok(new ApiResponse<>(400, "json解析异常", authPayload(null, true, logID, true, false)));
            }
            case RegIdNotFound -> {
                return ResponseEntity.ok(new ApiResponse<>(404, "未找到注册请求", authPayload(null, true, logID, true, false)));
            }
            case INVALID_CODE -> {
                return ResponseEntity.ok(new ApiResponse<>(401, "验证码无效", authPayload(null, true, logID, true, false)));
            }
            default -> {
                return ResponseEntity.ok(new ApiResponse<>(500, "无法连接服务器", authPayload(null, true, logID, true, false)));
            }
        }
    }

    private Map<String, Object> authPayload(String token, boolean requiresVerification, String logId, boolean hasEmail, boolean needInit) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("token", token);
        payload.put("requiresVerification", requiresVerification);
        payload.put("logId", logId);
        payload.put("hasEmail", hasEmail);
        payload.put("needInit", needInit);
        return payload;
    }


}
