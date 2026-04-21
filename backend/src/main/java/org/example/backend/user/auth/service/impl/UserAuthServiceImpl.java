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
import org.example.backend.common.Mail.Service.MailCodeService;
import org.example.backend.common.Mail.dto.MailCodeMessage;
import org.example.backend.common.dto.user.UserAuthDto;
import org.example.backend.common.entity.AuthTicket;
import org.example.backend.common.entity.UserProfile;
import org.example.backend.common.enums.user.UserGenderEnum;
import org.example.backend.common.repository.UserProfileRepository;
import org.example.backend.common.service.AuthTicketService;
import org.example.backend.common.util.*;
import org.example.backend.common.Mail.enums.MailCodePurpose;
import org.example.backend.user.auth.dto.AuthDto;
import org.example.backend.user.auth.enums.AuthServiceEnum;
import org.example.backend.common.enums.user.UserStatusEnum;
import org.example.backend.common.repository.UserRepository;
import org.example.backend.common.entity.User;
import org.example.backend.user.auth.service.UserAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final UserProfileRepository profileRepository;
    private final AuthTicketService authTicketService;
    private final MailCodeService mailCodeService;

    @Autowired
    public UserAuthServiceImpl(UserRepository userRepository,
                               UserProfileRepository profileRepository,
                               AuthTicketService authTicketService,
                               MailCodeService mailCodeService) {
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
        this.profileRepository = profileRepository;
        this.authTicketService = authTicketService;
        this.mailCodeService = mailCodeService;
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
        mailCodeService.sendVerificationCode(mailCodeMessage);

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
        String normalizedUsername = username == null ? null : username.trim();
        String normalizedEmail = email == null ? null : email.trim();

        // 查询是否存在 用户名或邮箱地址
        if (userRepository.existsByUsername(normalizedUsername)) {
            return new Pair<>(AuthServiceEnum.UserAlreadyExists, null);
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            return new Pair<>(AuthServiceEnum.EmailAlreadyExists, null);
        }

        AuthServiceEnum result = createUserWithProfile(
                normalizedUsername,
                SCryptUtil.hashPassword(password),
                normalizedEmail
        );
        return new Pair<>(result, null);
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

        return createUserWithProfile(map.get("username"), map.get("password"), map.get("email"));
    }

    @Override
    public void logout() {
        StpKit.USER.logout();
    }

    private AuthServiceEnum createUserWithProfile(String username, String passwordHash, String email) {
        if (userRepository.existsByUsername(username)) {
            return AuthServiceEnum.UserAlreadyExists;
        }
        if (userRepository.existsByEmail(email)) {
            return AuthServiceEnum.EmailAlreadyExists;
        }

        String uid;
        do {
            uid = UuidUtil.getUid();
        } while (userRepository.existsByUid(uid));

        User user = new User.Builder()
                .uid(uid)
                .username(username)
                .password(passwordHash)
                .status(UserStatusEnum.ACTIVE.getValue())
                .email(email)
                .build();

        user = userRepository.save(user);

        String nickName = NicknameGenerator.generateNickname();
        UserProfile userProfile = new UserProfile(user.getId(), nickName, UserGenderEnum.OTHER.getValue());
        profileRepository.save(userProfile);

        return AuthServiceEnum.Success;
    }
}

