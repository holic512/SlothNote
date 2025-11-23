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
import org.example.backend.common.config.Redis.RedisConfig;
import org.example.backend.common.entity.Admin;
import org.example.backend.common.rabbitMQ.enums.MQExchangeType;
import org.example.backend.common.rabbitMQ.enums.MQRoutingKey;
import org.example.backend.common.repository.AdminRepository;
import org.example.backend.common.util.SCryptUtil;
import org.example.backend.common.util.StpKit;
import org.example.backend.common.util.UuidUtil;
import org.example.backend.common.util.VerificationCodeUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AdminAuthService {

    private final AdminRepository adminRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private final RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    private final static String loginKey = RedisConfig.getKey() + "admin:login:";
    final int timeout = 5;

    @Autowired
    public AdminAuthService(AdminRepository adminRepository, RedisTemplate<String, Object> redisTemplate, RabbitTemplate rabbitTemplate) {
        this.adminRepository = adminRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
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

        // 生成对应的  邮箱鉴权 以  uid 验证码的  形式完成  写入 json
        String uid = UuidUtil.getUid();
        map.put("uid", uid);
        map.put("code", code);
        String jsonData = objectMapper.writeValueAsString(map);
        redisTemplate.opsForValue().set(loginKey + logID, jsonData, timeout, TimeUnit.MINUTES);

        // 验证码发送邮箱操作 添加到队列
        MailCodeMessage mailCodeMessage = new MailCodeMessage(email, code, MailCodePurpose.AdminLogin);
        String message = objectMapper.writeValueAsString(mailCodeMessage);
        rabbitTemplate.convertAndSend(MQExchangeType.DIRECT_EXCHANGE.getValue(), MQRoutingKey.EMAIL_ROUTING_KEY.getKey(), message);

        // success
        return new Pair<>(AuthServiceEnum.Success, logID);
    }

    public Pair<AuthServiceEnum, String> verLogin(String logID, String code) {
        // redis 中提取数据
        String loginData = (String) redisTemplate.opsForValue().get(loginKey + logID);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> map;
        try {
            map = objectMapper.readValue(loginData, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            // 处理解析异常
            return new Pair<>(AuthServiceEnum.JsonParseError, null);
        }
        if (map == null) return new Pair<>(AuthServiceEnum.RegIdNotFound, null);

        // 验证结果
        if (!map.get("code").equals(code)) {
            return new Pair<>(AuthServiceEnum.INVALID_CODE, null);
        }

        // 生成并返回token
        String uid = map.get("uid");
        StpKit.ADMIN.login(uid);
        return new Pair<>(AuthServiceEnum.Success, StpKit.ADMIN.getTokenValue());
    }
}

