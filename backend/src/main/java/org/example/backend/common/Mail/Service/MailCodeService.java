/**
 * CreateTime: 2024-08-29
 * Description: 邮箱工具类
 * Version: 1.0
 * Author: holic512
 */
package org.example.backend.common.Mail.Service;

import org.example.backend.common.Mail.dto.MailCodeMessage;
import org.example.backend.common.util.mail.MailClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.example.backend.common.Mail.enums.MailCodePurpose;

@Component
public class MailCodeService {

    private final MailClient mailClient;
    private final TemplateEngine templateEngine;

    @Autowired
    public MailCodeService(MailClient mailClient, TemplateEngine templateEngine) {
        this.mailClient = mailClient;
        this.templateEngine = templateEngine;
    }

    public boolean sendVerificationCode(MailCodeMessage mailCodeMessage) {
        String email = mailCodeMessage.getEmail();
        String code = mailCodeMessage.getCode();
        MailCodePurpose purpose = mailCodeMessage.getPurpose();

        String subject = code + "是你的CatNote验证码";

        Context context = new Context();
        context.setVariable("username", email);
        context.setVariable("verificationCode", code);

        String template = switch (purpose) {
            case UserLogin -> "email/user/login.html";
            case UserRegister -> "email/user/register.html";
            case UserSetPassword -> "email/user/resetPassword.html";
            case AdminLogin -> "email/admin/login.html";
        };

        String htmlContent = templateEngine.process(template, context);

        mailClient.sendHtml(email, subject, htmlContent);
        return true;
    }


}
