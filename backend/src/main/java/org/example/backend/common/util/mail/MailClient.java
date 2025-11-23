package org.example.backend.common.util.mail;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailClient {

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    @PostConstruct
    public void checkConfig() {
        log.info("[MailClient] 初始化完成，from={}, fromName={}",
                mailProperties.getFrom(), mailProperties.getFromName());
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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(buildFromAddress());
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
        try {
            if (toList == null || toList.isEmpty()) {
                throw new IllegalArgumentException("收件人列表不能为空");
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            // 第二个参数 true 表示 multipart（支持附件）
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    attachments != null && !attachments.isEmpty(),
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(new InternetAddress(
                    mailProperties.getFrom(),
                    mailProperties.getFromName(),
                    StandardCharsets.UTF_8.name()
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
            throw new RuntimeException("发送邮件失败", e);
        }
    }

    /**
     * 构造 from 字段（如果不需要昵称，可直接返回 mailProperties.getFrom()）
     */
    private String buildFromAddress() {
        // 只用于 SimpleMailMessage（不支持昵称），这里直接返回邮箱地址
        return mailProperties.getFrom();
    }
}
