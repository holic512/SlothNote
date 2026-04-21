package org.example.backend.user.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.antlr.v4.runtime.misc.Pair;
import org.example.backend.user.auth.enums.AuthServiceEnum;

public interface UserAuthService {

    /**
     * 账号密码登录 服务层
     *
     * @param username 用户名
     * @param password 密码
     * @return 由 uid 用户权限组成 token
     */
    Pair<AuthServiceEnum, String> PLogin(String username, String password);

    /**
     * 登录验证码的邮件发送,数据存储 服务层
     *
     * @param email 邮箱地址
     * @return 状态和日志ID
     */
    Pair<AuthServiceEnum, String> sendLoginCode(String email) throws JsonProcessingException;

    /**
     * 验证 登录验证码 的 服务层
     *
     * @param logID 登录标识符
     * @param code  验证码
     * @return token
     */
    Pair<AuthServiceEnum, String> verifyLoginCode(String logID, String code) throws JsonProcessingException;

    /**
     * 发送 注册请求
     *
     * @param username 用户名
     * @param password 密码
     * @param email    邮箱
     * @return 是否成功添加请求
     */
    Pair<AuthServiceEnum, String> initiateReg(String username, String password, String email) throws JsonProcessingException;

    /**
     * 用于验证 注册申请
     *
     * @param regID 注册会话标识符
     * @param code  验证码
     * @return 用户授权服务 状态(枚举)
     */
    AuthServiceEnum verificationReg(String regID, String code);

    /**
     * 退出当前用户登录状态
     */
    void logout();
}
