/**
 * @file AdminAuthService
 * @project SlothNote
 * @module 管理后台 / 认证
 * @description 提供管理员初始化、登录和二次验证的核心业务逻辑。
 * @logic 1. 查询管理员初始化状态；2. 初始化时校验输入并处理并发创建竞争；3. 验证登录凭据并建立管理员会话。
 * @dependencies AdminRepository, AuthTicketService, MailCodeService, Sa-Token
 * @index_tags 管理员认证, 首次初始化, 并发安全, 登录, 验证码
 * @author holic512
 */
package org.example.backend.admin.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.Mail.Service.MailCodeService;
import org.example.backend.common.Mail.dto.MailCodeMessage;
import org.example.backend.common.Mail.enums.MailCodePurpose;
import org.example.backend.common.entity.Admin;
import org.example.backend.common.entity.AuthTicket;
import org.example.backend.common.repository.AdminRepository;
import org.example.backend.common.service.AuthTicketService;
import org.example.backend.common.util.SCryptUtil;
import org.example.backend.common.util.StpKit;
import org.example.backend.common.util.UuidUtil;
import org.example.backend.common.util.VerificationCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AdminAuthService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final boolean REQUIRE_EMAIL_VERIFICATION = false;

    private final AdminRepository adminRepository;
    private final AuthTicketService authTicketService;
    private final MailCodeService mailCodeService;

    private final ObjectMapper objectMapper;

    final int timeout = 5;

    @Autowired
    public AdminAuthService(AdminRepository adminRepository,
                            AuthTicketService authTicketService,
                            MailCodeService mailCodeService) {
        this.adminRepository = adminRepository;
        this.authTicketService = authTicketService;
        this.mailCodeService = mailCodeService;
        this.objectMapper = new ObjectMapper();
    }


    /**
     * @param username 用户名
     * @param password 密码
     * @return pair(登录状态, 登录会话标识符)
     * @throws JsonProcessingException json解析错误
     */
    public Pair<AuthServiceEnum, String> login(String username, String password) throws JsonProcessingException {
        if (!hasInitializedAdmin()) {
            return new Pair<>(AuthServiceEnum.NeedInit, null);
        }

        // 根据username 获取用户数据
        Admin admin = adminRepository.findByUsername(username);

        if (admin == null || !Integer.valueOf(0).equals(admin.getIsDeleted())) {
            return new Pair<>(AuthServiceEnum.UserNotExists, null);
        }

        if (!SCryptUtil.verifyPassword(password, admin.getPassword())) {
            return new Pair<>(AuthServiceEnum.INCORRECT, null);
        }

        if (!shouldRequireVerification(admin)) {
            StpKit.ADMIN.login(admin.getId());
            return new Pair<>(AuthServiceEnum.Success, StpKit.ADMIN.getTokenValue());
        }

        // 密码验证成功->开始二次邮箱验证
        String code = VerificationCodeUtil.generateVerificationCode();
        String email = admin.getEmail();
        String logID = UuidUtil.getUuid();
        Map<String, String> map = new HashMap<>();

        // 生成对应的邮箱鉴权票据
        map.put("adminId", admin.getId().toString());
        map.put("code", code);
        authTicketService.createTicket(AuthTicketService.ADMIN_LOGIN, username, logID, code, map, timeout);

        // 验证码发送邮箱操作 添加到队列
        MailCodeMessage mailCodeMessage = new MailCodeMessage(email, code, MailCodePurpose.AdminLogin);
        mailCodeService.sendVerificationCode(mailCodeMessage);

        // success
        return new Pair<>(AuthServiceEnum.Success, logID);
    }

    public Pair<AuthServiceEnum, String> initAdmin(String username, String password, String email) {
        if (hasInitializedAdmin()) {
            return new Pair<>(AuthServiceEnum.AdminAlreadyInitialized, null);
        }

        String normalizedUsername = username == null ? null : username.trim();
        String normalizedEmail = normalizeEmail(email);
        if (normalizedUsername == null || normalizedUsername.isBlank() || password == null || password.isBlank()) {
            return new Pair<>(AuthServiceEnum.UserNotExists, null);
        }
        if (adminRepository.existsByUsername(normalizedUsername)) {
            return new Pair<>(AuthServiceEnum.UserAlreadyExists, null);
        }
        if (normalizedEmail != null && !EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            return new Pair<>(AuthServiceEnum.InvalidEmail, null);
        }
        if (normalizedEmail != null && adminRepository.existsByEmail(normalizedEmail)) {
            return new Pair<>(AuthServiceEnum.EmailAlreadyExists, null);
        }

        Admin admin = new Admin();
        admin.setUsername(normalizedUsername);
        admin.setPassword(SCryptUtil.hashPassword(password));
        admin.setEmail(normalizedEmail);
        admin.setIsDeleted(0);
        Admin saved;
        try {
            saved = adminRepository.save(admin);
        } catch (DataAccessException exception) {
            // The pre-insert existence check is necessarily racy. SQLite serializes writers,
            // so a unique-key failure here means another request finished initialization first.
            if (hasInitializedAdmin()) {
                return new Pair<>(AuthServiceEnum.AdminAlreadyInitialized, null);
            }
            throw exception;
        }
        StpKit.ADMIN.login(saved.getId());
        return new Pair<>(AuthServiceEnum.Success, StpKit.ADMIN.getTokenValue());
    }

    public Pair<AuthServiceEnum, String> verLogin(String logID, String code) {
        AuthTicket ticket = authTicketService.findValidTicket(logID, AuthTicketService.ADMIN_LOGIN).orElse(null);
        if (ticket == null) {
            return new Pair<>(AuthServiceEnum.RegIdNotFound, null);
        }

        Map<String, String> map;
        try {
            map = objectMapper.readValue(ticket.getPayloadJson(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return new Pair<>(AuthServiceEnum.JsonParseError, null);
        }
        if (map == null) {
            return new Pair<>(AuthServiceEnum.RegIdNotFound, null);
        }

        if (!map.get("code").equals(code)) {
            return new Pair<>(AuthServiceEnum.INVALID_CODE, null);
        }

        authTicketService.markUsed(ticket);
        Long adminId = Long.valueOf(map.get("adminId"));
        StpKit.ADMIN.login(adminId);
        return new Pair<>(AuthServiceEnum.Success, StpKit.ADMIN.getTokenValue());
    }

    public boolean hasInitializedAdmin() {
        return adminRepository.countByIsDeleted(0) > 0;
    }

    private boolean shouldRequireVerification(Admin admin) {
        return REQUIRE_EMAIL_VERIFICATION && admin.getEmail() != null && !admin.getEmail().isBlank();
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

