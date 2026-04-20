/**
 * File Name: MQEmailCode.java
 * Description: 邮箱验证码消息类
 * Author: holic512
 * Created Date: 2024-09-10
 * Version: 1.0
 * Usage:
 * 封装发送验证码邮件所需的最小信息
 */
package org.example.backend.common.Mail.dto;

import org.example.backend.common.Mail.enums.MailCodePurpose;

public class MailCodeMessage {
    private String email;
    private String code;
    private MailCodePurpose purpose;

    // 无参构造函数
    public MailCodeMessage() {
    }

    public MailCodeMessage(String email, String code, MailCodePurpose purpose) {
        this.email = email;
        this.code = code;
        this.purpose = purpose;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MailCodePurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(MailCodePurpose purpose) {
        this.purpose = purpose;
    }
}
