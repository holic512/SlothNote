package org.example.backend.common.util.mail;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mail")
public class MailProperties {

    /**
     * 发件人邮箱地址（一般与 spring.mail.username 一致）
     */
    private String from;

    /**
     * 发件人显示名称（昵称）
     */
    private String fromName;
}
