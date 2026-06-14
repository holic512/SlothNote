/**
 * @file AdminSettingController
 * @project SlothNote
 * @module 管理端 / 系统设置
 * @description 提供管理员资料、系统初始化、AI 配置与邮箱配置维护接口。
 * @logic 1. 读取和更新管理员资料；2. 执行用户数据初始化；3. 维护并测试数据库 AI/邮箱配置。
 * @dependencies Service: AdminSettingService/AiConfigService/MailConfigService, DTO: AiConfigUpdateRequest/MailConfigUpdateRequest
 * @index_tags 管理端设置, 系统初始化, 管理员资料, AI配置, 邮箱配置
 * @author holic512
 */
package org.example.backend.admin.setting.Controller;

import org.example.backend.admin.setting.service.AdminSettingService;
import org.example.backend.common.config.ai.AiConfigService;
import org.example.backend.common.config.mail.MailConfigService;
import org.example.backend.common.dto.ai.AiConfigTestRequest;
import org.example.backend.common.dto.ai.AiConfigUpdateRequest;
import org.example.backend.common.dto.mail.MailConfigTestRequest;
import org.example.backend.common.dto.mail.MailConfigUpdateRequest;
import org.example.backend.common.response.ApiResponse;
import org.example.backend.common.util.mail.MailClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/setting")
public class AdminSettingController {

    private final AdminSettingService adminSettingService;
    private final AiConfigService aiConfigService;
    private final MailConfigService mailConfigService;
    private final MailClient mailClient;

    public AdminSettingController(AdminSettingService adminSettingService,
                                  AiConfigService aiConfigService,
                                  MailConfigService mailConfigService,
                                  MailClient mailClient) {
        this.adminSettingService = adminSettingService;
        this.aiConfigService = aiConfigService;
        this.mailConfigService = mailConfigService;
        this.mailClient = mailClient;
    }

    @GetMapping("/systemReset/summary")
    public ResponseEntity<Object> summary() {
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取系统初始化统计")
                .data(adminSettingService.getSystemResetSummary())
                .build());
    }

    @GetMapping("/profile")
    public ResponseEntity<Object> profile() {
        try {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("成功获取管理员资料")
                    .data(adminSettingService.getProfile())
                    .build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(404)
                    .message(ex.getMessage())
                    .build());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<Object> updateProfile(@RequestBody Map<String, Object> body) {
        try {
            String email = body == null ? null : (String) body.get("email");
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("管理员资料更新成功")
                    .data(adminSettingService.updateProfile(email))
                    .build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(400)
                    .message(ex.getMessage())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("管理员资料更新失败")
                    .build());
        }
    }

    @GetMapping("/aiConfig")
    public ResponseEntity<Object> aiConfig() {
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取 AI 配置")
                .data(aiConfigService.getPublicConfig())
                .build());
    }

    @PutMapping("/aiConfig")
    public ResponseEntity<Object> updateAiConfig(@RequestBody AiConfigUpdateRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("AI 配置保存成功")
                    .data(aiConfigService.updateConfig(request))
                    .build());
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(400)
                    .message(ex.getMessage())
                    .build());
        }
    }

    @PostMapping("/aiConfig/test")
    public ResponseEntity<Object> testAiConfig(@RequestBody AiConfigTestRequest request) {
        try {
            String reply = aiConfigService.testConfig(request);
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("AI 配置测试成功")
                    .data(Map.of("reply", reply == null ? "" : reply))
                    .build());
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(400)
                    .message(ex.getMessage())
                    .build());
        }
    }

    @GetMapping("/mailConfig")
    public ResponseEntity<Object> mailConfig() {
        return ResponseEntity.ok(new ApiResponse.Builder<>()
                .status(200)
                .message("成功获取邮箱配置")
                .data(mailConfigService.getPublicConfig())
                .build());
    }

    @PutMapping("/mailConfig")
    public ResponseEntity<Object> updateMailConfig(@RequestBody MailConfigUpdateRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("邮箱配置保存成功")
                    .data(mailConfigService.updateConfig(request))
                    .build());
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(400)
                    .message(ex.getMessage())
                    .build());
        }
    }

    @PostMapping("/mailConfig/test")
    public ResponseEntity<Object> testMailConfig(@RequestBody MailConfigTestRequest request) {
        try {
            String recipient = request == null ? null : request.getTestRecipient();
            if (recipient == null || recipient.trim().isEmpty()) {
                throw new IllegalArgumentException("测试收件人不能为空");
            }
            mailClient.sendTestMail(mailConfigService.buildTestConfig(request), recipient.trim());
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("测试邮件发送成功")
                    .build());
        } catch (RuntimeException ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(400)
                    .message(ex.getMessage())
                    .build());
        }
    }

    @PostMapping("/systemReset")
    public ResponseEntity<Object> systemReset(@RequestBody Map<String, Object> body) {
        try {
            String confirmText = body == null ? null : (String) body.get("confirmText");
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(200)
                    .message("系统初始化成功")
                    .data(adminSettingService.resetUserData(confirmText))
                    .build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(400)
                    .message(ex.getMessage())
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.ok(new ApiResponse.Builder<>()
                    .status(500)
                    .message("系统初始化失败")
                    .build());
        }
    }
}
