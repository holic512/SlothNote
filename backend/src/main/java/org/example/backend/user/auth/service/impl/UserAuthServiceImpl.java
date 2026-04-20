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
import org.example.backend.common.entity.AuthTicket;
import org.example.backend.common.entity.UserProfile;
import org.example.backend.common.enums.user.UserGenderEnum;
import org.example.backend.common.repository.UserProfileRepository;
import org.example.backend.common.service.AuthTicketService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final UserProfileRepository profileRepository;
    private final AuthTicketService authTicketService;

    @Autowired
    public UserAuthServiceImpl(UserRepository userRepository,
                               RabbitTemplate rabbitTemplate,
                               UserProfileRepository profileRepository,
                               AuthTicketService authTicketService) {
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = new ObjectMapper();
        this.profileRepository = profileRepository;
        this.authTicketService = authTicketService;
    }

    final int timeout = 5;

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

        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", user.getUid());
        payload.put("userId", user.getId());
        payload.put("status", user.getStatus());
        payload.put("code", code);
        authTicketService.createTicket(AuthTicketService.USER_LOGIN, email, logID, code, payload, timeout);

        MailCodeMessage mailCodeMessage = new MailCodeMessage(email, code, MailCodePurpose.UserLogin);
        String message = objectMapper.writeValueAsString(mailCodeMessage);
        rabbitTemplate.convertAndSend(MQExchangeType.DIRECT_EXCHANGE.getValue(), MQRoutingKey.EMAIL_ROUTING_KEY.getKey(), message);

        return new Pair<>(AuthServiceEnum.Success, logID);
    }

    @Override
    public Pair<AuthServiceEnum, String> verifyLoginCode(String logID, String code) throws JsonProcessingException {
        AuthTicket ticket = authTicketService.findValidTicket(logID, AuthTicketService.USER_LOGIN).orElse(null);
        if (ticket == null) {
            return new Pair<>(AuthServiceEnum.LogIdNotFound, null);
        }

        AuthDto authDto = objectMapper.readValue(ticket.getPayloadJson(), AuthDto.class);

        if (!authDto.getCode().equals(code)) {
            return new Pair<>(AuthServiceEnum.INCORRECT, null);
        }

        authTicketService.markUsed(ticket);

        StpKit.USER.login(authDto.getUid());
        SaSession session = StpKit.USER.getSession();
        Object dataId = authTicketService.readObjectMap(ticket).get("userId");
        if (dataId instanceof Number number) {
            session.set("id", number.longValue());
        } else if (dataId != null) {
            session.set("id", Long.valueOf(String.valueOf(dataId)));
        }
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

        String regID = UuidUtil.getUuid();  // 注册会话标识符
        String code = VerificationCodeUtil.generateVerificationCode();

        Map<String, String> registerData = new HashMap<>();
        registerData.put("username", username);
        registerData.put("password", SCryptUtil.hashPassword(password));
        registerData.put("email", email);
        registerData.put("code", code);

        authTicketService.createTicket(AuthTicketService.USER_REGISTER, username, regID, code, registerData, timeout);
        authTicketService.createTicket(AuthTicketService.USER_REGISTER_PENDING, email, regID, regID, Map.of("regId", regID), timeout);

        MailCodeMessage mailCodeMessage = new MailCodeMessage(email, code, MailCodePurpose.UserRegister);
        String message = objectMapper.writeValueAsString(mailCodeMessage);
        rabbitTemplate.convertAndSend(MQExchangeType.DIRECT_EXCHANGE.getValue(), MQRoutingKey.EMAIL_ROUTING_KEY.getKey(), message);

        return new Pair<>(AuthServiceEnum.Success, regID);
    }

    @Override
    @Transactional
    public AuthServiceEnum verificationReg(String regID, String code) {

        AuthTicket ticket = authTicketService.findValidTicket(regID, AuthTicketService.USER_REGISTER).orElse(null);
        if (ticket == null) {
            return AuthServiceEnum.RegIdNotFound;
        }

        Map<String, String> map;
        try {
            map = objectMapper.readValue(ticket.getPayloadJson(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            return AuthServiceEnum.JsonParseError;
        }


        String mapCode = map.get("code");
        if (code == null || !code.equals(mapCode)) {
            return AuthServiceEnum.INVALID_CODE; // 验证码错误
        }

        authTicketService.markUsed(ticket);

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

