/**
 * @file MailConfigUpdateRequest
 * @project SlothNote
 * @module 公共配置 / 邮箱配置
 * @description 接收管理端提交的邮箱配置更新参数。
 * @logic 1. 支持 SMTP 连接参数更新；2. password 为空时由服务层保留旧值；3. 支持启用状态调整。
 * @dependencies 无强依赖
 * @index_tags 邮箱配置, 更新请求, SMTP, 管理端
 * @author holic512
 */
package org.example.backend.common.dto.mail;

import lombok.Data;

@Data
public class MailConfigUpdateRequest {

    private String host;
    private Integer port;
    private String username;
    private String password;
    private String protocol;
    private String defaultEncoding;
    private String fromAddress;
    private String fromName;
    private Boolean smtpAuth;
    private Boolean sslEnable;
    private Boolean starttlsEnable;
    private Boolean starttlsRequired;
    private Boolean enabled;
}
