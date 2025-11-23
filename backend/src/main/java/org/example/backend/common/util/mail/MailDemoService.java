package org.example.backend.common.util.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailDemoService {

    private final MailClient mailClient;

    public void sendDemoMail(String to) {

        String subject = "测试邮件 - 智慧系统通知";
        String html = """
                <h1>欢迎使用邮件通知</h1>
                <p>这是一封来自 <b>Spring Boot + MailClient</b> 的测试邮件。</p>
                <p>发送时间：<span style='color:#555;'>由后端自动生成</span></p>
                """;

        mailClient.sendHtml(to, subject, html);
    }
}
