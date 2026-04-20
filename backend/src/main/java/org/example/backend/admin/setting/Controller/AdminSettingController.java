package org.example.backend.admin.setting.Controller;

import org.example.backend.admin.setting.service.AdminSettingService;
import org.example.backend.common.response.ApiResponse;
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

    public AdminSettingController(AdminSettingService adminSettingService) {
        this.adminSettingService = adminSettingService;
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
