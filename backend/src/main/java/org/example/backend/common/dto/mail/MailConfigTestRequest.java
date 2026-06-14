/**
 * @file MailConfigTestRequest
 * @project SlothNote
 * @module 公共配置 / 邮箱配置
 * @description 接收管理端发起邮箱配置测试的临时参数。
 * @logic 1. 继承邮箱配置表单字段；2. 增加测试收件人；3. 由服务层合并旧密码后发送测试邮件。
 * @dependencies DTO: MailConfigUpdateRequest
 * @index_tags 邮箱配置, 测试邮件, SMTP, 管理端
 * @author holic512
 */
package org.example.backend.common.dto.mail;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MailConfigTestRequest extends MailConfigUpdateRequest {

    private String testRecipient;
}
