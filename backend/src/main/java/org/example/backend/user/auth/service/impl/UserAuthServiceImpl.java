/**
 * File Name: UserAuthServiceImpl.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-09-20
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.auth.service.impl;

import cn.dev33.satoken.session.SaSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.common.Mail.dto.MailCodeMessage;
import org.example.backend.common.dto.user.UserAuthDto;
import org.example.backend.common.entity.UserProfile;
import org.example.backend.common.enums.user.UserGenderEnum;
import org.example.backend.common.repository.UserProfileRepository;
import org.example.backend.common.util.*;
import org.example.backend.common.rabbitMQ.enums.MQExchangeType;
import org.example.backend.common.rabbitMQ.enums.MQRoutingKey;
import org.example.backend.common.Mail.enums.MailCodePurpose;
import org.example.backend.user.auth.dto.AuthDto;
import org.example.backend.user.auth.enums.AuthServiceEnum;
import org.example.backend.common.enums.user.UserStatusEnum;
import org.example.backend.common.repository.UserRepository;
import org.example.backend.common.entity.User;
import org.example.backend.user.auth.service.UserAuthService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final UserProfileRepository profileRepository;

    @Autowired
    public UserAuthServiceImpl(UserRepository userRepository,
                               RedisTemplate<String, Object> redisTemplate,
                               RabbitTemplate rabbitTemplate,
                               UserProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
        this.profileRepository = profileRepository;
    }

    final int timeout = 5;
    final String keyRegPend = "CodeNote:user:register:pending:";
    final String keyReg = "CodeNote:user:register:";

    @Override
    public Pair<AuthServiceEnum, String> PLogin(String username, String password) {

        // 获取用户数据
        final UserAuthDto user = userRepository.findAuthDtoByUsername(username);

        if (user == null) {
            return new Pair<>(AuthServiceEnum.UserNotFound, null); // 用户未找到
        }

        if (user.getStatus().equals(UserStatusEnum.DISABLED)) {
            return new Pair<>(AuthServiceEnum.AccountDisabled, null);
        }

        if (user.getStatus().equals(UserStatusEnum.BANNED)) {
            return new Pair<>(AuthServiceEnum.AccountBanned, null);
        }

        // 比对密码
        if (!SCryptUtil.verifyPassword(password, user.getPassword())) {
            return new Pair<>(AuthServiceEnum.INCORRECT, null); // 密码不匹配
        }

        // 登录成功
        StpKit.USER.login(user.getUid());

        // 在session中插入id
        SaSession session = StpKit.USER.getSession();
        session.set("id", user.getId());

        return new Pair<>(AuthServiceEnum.Success, StpKit.USER.getTokenValue());
    }

    @Override
    public Pair<AuthServiceEnum, String> sendLoginCode(String email) throws JsonProcessingException {
        final String keyName = "CodeNote:user:login:";
        final int timeout = 5;

        final boolean exists = userRepository.existsByEmail(email);
        if (!exists) {
            return new Pair<>(AuthServiceEnum.EmailNotFound, null);
        }

        final UserAuthDto user = userRepository.findAuthDtoByEmail(email);

        if (user.getStatus().equals(UserStatusEnum.DISABLED)) {
            return new Pair<>(AuthServiceEnum.AccountDisabled, null);
        }

        if (user.getStatus().equals(UserStatusEnum.BANNED)) {
            return new Pair<>(AuthServiceEnum.AccountBanned, null);
        }

        String logID = UuidUtil.getUuid();
        String code = VerificationCodeUtil.generateVerificationCode();

        AuthDto authDto = new AuthDto(user.getUid(), user.getStatus(), code);
        String userInfo = objectMapper.writeValueAsString(authDto);

        redisTemplate.opsForValue().set(keyName + logID, userInfo, timeout, TimeUnit.MINUTES);

        MailCodeMessage mailCodeMessage = new MailCodeMessage(email, code, MailCodePurpose.UserLogin);
        String message = objectMapper.writeValueAsString(mailCodeMessage);
        rabbitTemplate.convertAndSend(MQExchangeType.DIRECT_EXCHANGE.getValue(), MQRoutingKey.EMAIL_ROUTING_KEY.getKey(), message);

        return new Pair<>(AuthServiceEnum.Success, logID);
    }

    @Override
    public Pair<AuthServiceEnum, String> verifyLoginCode(String logID, String code) throws JsonProcessingException {
        final String keyName = "CodeNote:user:login:";

        String userInfoJson = (String) redisTemplate.opsForValue().get(keyName + logID);

        if (userInfoJson == null) {
            return new Pair<>(AuthServiceEnum.LogIdNotFound, null);
        }

        AuthDto authDto = objectMapper.readValue(userInfoJson, AuthDto.class);

        if (!authDto.getCode().equals(code)) {
            return new Pair<>(AuthServiceEnum.INCORRECT, null);
        }

        redisTemplate.delete(keyName + logID);

        StpKit.USER.login(authDto.getUid());
        return new Pair<>(AuthServiceEnum.Success, StpKit.USER.getTokenValue());
    }

    @Override
    public Pair<AuthServiceEnum, String> initiateReg(String username, String password, String email) throws JsonProcessingException {
        // 查询是否存在 用户名或邮箱地址
        if (userRepository.existsByUsername(username)) {
            return new Pair<>(AuthServiceEnum.UserAlreadyExists, null);
        }
        if (userRepository.existsByEmail(email)) {
            return new Pair<>(AuthServiceEnum.EmailAlreadyExists, null);
        }

        // 查询redis中是否有已有的 注册请求
        boolean exUser = Boolean.TRUE.equals(redisTemplate.hasKey(keyRegPend + username));
        boolean exEmail = Boolean.TRUE.equals(redisTemplate.hasKey(keyRegPend + email));

        if (exUser) {
            String ex1 = (String) redisTemplate.opsForValue().get(keyRegPend + username);
            redisTemplate.delete(keyRegPend + username);
            if (ex1 != null) {
                redisTemplate.delete(keyReg + ex1);
            }
        }
        if (exEmail) {
            String ex2 = (String) redisTemplate.opsForValue().get(keyRegPend + email);
            redisTemplate.delete(keyRegPend + email);
            if (ex2 != null) {
                redisTemplate.delete(keyReg + ex2);
            }
        }

        // 向 redis中插入新的注册请求
        String regID = UuidUtil.getUuid();  // 注册会话标识符
        String code = VerificationCodeUtil.generateVerificationCode();

        redisTemplate.opsForValue().set(keyRegPend + username, regID, timeout, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(keyRegPend + email, regID, timeout, TimeUnit.MINUTES);

        Map<String, String> registerData = new HashMap<>();
        registerData.put("username", username);
        registerData.put("password", SCryptUtil.hashPassword(password));
        registerData.put("email", email);
        registerData.put("code", code);

        String jsonString = objectMapper.writeValueAsString(registerData);
        redisTemplate.opsForValue().set(keyReg + regID, jsonString, timeout, TimeUnit.MINUTES);

        MailCodeMessage mailCodeMessage = new MailCodeMessage(email, code, MailCodePurpose.UserRegister);
        String message = objectMapper.writeValueAsString(mailCodeMessage);
        rabbitTemplate.convertAndSend(MQExchangeType.DIRECT_EXCHANGE.getValue(), MQRoutingKey.EMAIL_ROUTING_KEY.getKey(), message);

        return new Pair<>(AuthServiceEnum.Success, regID);
    }

    @Override
    @Transactional
    public AuthServiceEnum verificationReg(String regID, String code) {

        // 获取注册信息
        String userData = (String) redisTemplate.opsForValue().get(keyReg + regID);
        if (userData == null) return AuthServiceEnum.RegIdNotFound;
        Map<String, String> map;
        try {
            map = objectMapper.readValue(userData, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            return AuthServiceEnum.JsonParseError;
        }


        String mapCode = map.get("code");
        if (code == null || !code.equals(mapCode)) {
            return AuthServiceEnum.INVALID_CODE; // 验证码错误
        }

        // 验证成功
        redisTemplate.delete(keyReg + regID);
        redisTemplate.delete(keyRegPend + map.get("username"));
        redisTemplate.delete(keyRegPend + map.get("email"));

        // 生成uid
        String uid;
        do {
            uid = UuidUtil.getUid();
        } while (userRepository.existsByUid(uid));

        User user = new User.Builder()
                .uid(uid)
                .username(map.get("username"))
                .password(map.get("password"))
                .status(UserStatusEnum.ACTIVE.getValue())
                .email(map.get("email"))
                .build();

        // 保存用户
        user = userRepository.save(user);  // save() 会返回保存后的实体对象

        // 获取生成的主键
        Long userId = user.getId();  // 获取数据库自动生成的主键（id）

        // 生成用户随机详细信息 并根据uid插入
        String nickName = NicknameGenerator.generateNickname();

        UserProfile userProfile = new UserProfile(userId,nickName,UserGenderEnum.OTHER.getValue());

        // 保存用户详情信息
        profileRepository.save(userProfile);

        return AuthServiceEnum.Success;
    }
}

