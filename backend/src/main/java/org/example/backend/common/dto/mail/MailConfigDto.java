/**
 * @file MailConfigDto
 * @project SlothNote
 * @module 公共配置 / 邮箱配置
 * @description 返回给管理端的邮箱配置脱敏视图。
 * @logic 1. 暴露 SMTP 与发件人配置；2. 仅返回密码是否存在和脱敏文本；3. 携带启用状态。
 * @dependencies 无强依赖
 * @index_tags 邮箱配置, DTO, 脱敏, 管理端
 * @author holic512
 */
package org.example.backend.common.dto.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailConfigDto {

    private String host;
    private Integer port;
    private String username;
    private String protocol;
    private String defaultEncoding;
    private String fromAddress;
    private String fromName;
    private Boolean smtpAuth;
    private Boolean sslEnable;
    private Boolean starttlsEnable;
    private Boolean starttlsRequired;
    private Boolean enabled;
    private Boolean hasPassword;
    private String maskedPassword;
}
