/**
 * File Name: AuthService.java
 * Description: 管理员 授权 服务类
 * Author: holic512
 * Created Date: 2024-09-04
 * Version: 1.0
 * Usage:
 * 用于管理员授权的 服务类
 */
package org.example.backend.admin.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.Mail.dto.MailCodeMessage;
import org.example.backend.common.Mail.enums.MailCodePurpose;
import org.example.backend.common.entity.Admin;
import org.example.backend.common.entity.AuthTicket;
import org.example.backend.common.rabbitMQ.enums.MQExchangeType;
import org.example.backend.common.rabbitMQ.enums.MQRoutingKey;
import org.example.backend.common.repository.AdminRepository;
import org.example.backend.common.service.AuthTicketService;
import org.example.backend.common.util.SCryptUtil;
import org.example.backend.common.util.StpKit;
import org.example.backend.common.util.UuidUtil;
import org.example.backend.common.util.VerificationCodeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final RabbitTemplate rabbitTemplate;
    private final AuthTicketService authTicketService;

    private final ObjectMapper objectMapper;

    final int timeout = 5;

    @Autowired
    public AdminAuthService(AdminRepository adminRepository,
                            RabbitTemplate rabbitTemplate,
                            AuthTicketService authTicketService) {
        this.adminRepository = adminRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.authTicketService = authTicketService;
        this.objectMapper = new ObjectMapper();
    }


    /**
     * @param username 用户名
     * @param password 密码
     * @return pair(登录状态, 登录会话标识符)
     * @throws JsonProcessingException json解析错误
     */
    public Pair<AuthServiceEnum, String> login(String username, String password) throws JsonProcessingException {
        // 根据username 获取用户数据
        Admin admin = adminRepository.findByUsername(username);

        if (admin == null) {
            return new Pair<>(AuthServiceEnum.UserNotExists, null);
        }

        if (!SCryptUtil.verifyPassword(password, admin.getPassword())) {
            return new Pair<>(AuthServiceEnum.INCORRECT, null);
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
        String message = objectMapper.writeValueAsString(mailCodeMessage);
        rabbitTemplate.convertAndSend(MQExchangeType.DIRECT_EXCHANGE.getValue(), MQRoutingKey.EMAIL_ROUTING_KEY.getKey(), message);

        // success
        return new Pair<>(AuthServiceEnum.Success, logID);
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
}

