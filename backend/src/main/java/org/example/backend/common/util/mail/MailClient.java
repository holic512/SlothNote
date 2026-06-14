/**
 * @file MailClient
 * @project SlothNote
 * @module 公共工具 / 邮件发送
 * @description 基于数据库 SMTP 配置发送文本邮件、HTML 邮件和测试邮件。
 * @logic 1. 发送前读取启用的系统邮箱配置；2. 使用运行时 JavaMailSender 发送邮件；3. 支持管理端临时配置测试。
 * @dependencies Service: MailConfigService, JavaMail: MimeMessageHelper
 * @index_tags 邮件发送, SMTP, 数据库配置, 测试邮件
 * @author holic512
 */
package org.example.backend.common.util.mail;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.common.config.mail.MailConfigService;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailClient {

    private final MailConfigService mailConfigService;

    @PostConstruct
    public void checkConfig() {
        log.info("[MailClient] 初始化完成，SMTP 参数将从数据库系统邮箱配置读取");
    }

    /**
     * 发送简单文本邮件（单收件人）
     */
    public void sendText(String to, String subject, String content) {
        sendText(List.of(to), subject, content);
    }

    /**
     * 发送简单文本邮件（多收件人）
     */
    public void sendText(List<String> toList, String subject, String content) {
        Objects.requireNonNull(toList, "收件人不能为空");
        if (toList.isEmpty()) {
            throw new IllegalArgumentException("收件人列表为空");
        }

        MailConfigService.RuntimeConfig config = mailConfigService.requireEnabledConfig();
        JavaMailSenderImpl mailSender = mailConfigService.createMailSender(config);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(buildFromAddress(config));
        message.setTo(toList.toArray(String[]::new));
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
        log.info("[MailClient] 文本邮件发送成功, to={}, subject={}", toList, subject);
    }

    /**
     * 发送 HTML 邮件（单收件人）
     */
    public void sendHtml(String to, String subject, String htmlContent) {
        sendHtml(List.of(to), subject, htmlContent, null, null, null);
    }

    public void sendTestMail(MailConfigService.RuntimeConfig config, String to) {
        String subject = "SlothNote 邮箱配置测试";
        String htmlContent = """
                <p>这是一封来自 SlothNote 管理端的邮箱配置测试邮件。</p>
                <p>如果你收到这封邮件，说明当前 SMTP 参数可以正常发送邮件。</p>
                """;
        sendHtml(config, List.of(to), subject, htmlContent, null, null, null);
    }

    /**
     * 发送 HTML 邮件（多收件人，带可选抄送/密送/附件）
     *
     * @param toList      收件人列表（必填）
     * @param subject     标题
     * @param htmlContent HTML 内容
     * @param ccList      抄送列表（可空）
     * @param bccList     密送列表（可空）
     * @param attachments 附件 name -> File（可空）
     */
    public void sendHtml(List<String> toList,
                         String subject,
                         String htmlContent,
                         @Nullable List<String> ccList,
                         @Nullable List<String> bccList,
                         @Nullable Map<String, File> attachments) {
        MailConfigService.RuntimeConfig config = mailConfigService.requireEnabledConfig();
        sendHtml(config, toList, subject, htmlContent, ccList, bccList, attachments);
    }

    public void sendHtml(MailConfigService.RuntimeConfig config,
                         List<String> toList,
                         String subject,
                         String htmlContent,
                         @Nullable List<String> ccList,
                         @Nullable List<String> bccList,
                         @Nullable Map<String, File> attachments) {
        try {
            if (toList == null || toList.isEmpty()) {
                throw new IllegalArgumentException("收件人列表不能为空");
            }

            JavaMailSenderImpl mailSender = mailConfigService.createMailSender(config);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // 第二个参数 true 表示 multipart（支持附件）
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    attachments != null && !attachments.isEmpty(),
                    config.defaultEncoding()
            );

            helper.setFrom(new InternetAddress(
                    config.fromAddress(),
                    config.fromName(),
                    config.defaultEncoding()
            ));

            helper.setTo(toList.toArray(String[]::new));
            if (ccList != null && !ccList.isEmpty()) {
                helper.setCc(ccList.toArray(String[]::new));
            }
            if (bccList != null && !bccList.isEmpty()) {
                helper.setBcc(bccList.toArray(String[]::new));
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            if (attachments != null) {
                for (Map.Entry<String, File> entry : attachments.entrySet()) {
                    if (entry.getValue() != null && entry.getValue().exists()) {
                        helper.addAttachment(entry.getKey(), entry.getValue());
                    }
                }
            }

            mailSender.send(mimeMessage);
            log.info("[MailClient] HTML 邮件发送成功, to={}, subject={}", toList, subject);
        } catch (Exception e) {
            log.error("[MailClient] 发送邮件失败, subject={}, error={}", subject, e.getMessage(), e);
            throw new RuntimeException("发送邮件失败：" + e.getMessage(), e);
        }
    }

    /**
     * 构造 SimpleMailMessage 的 from 字段。
     */
    private String buildFromAddress(MailConfigService.RuntimeConfig config) {
        // SimpleMailMessage 不支持昵称，这里直接返回邮箱地址。
        return config.fromAddress();
    }
}
