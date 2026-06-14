/**
 * @file AiConfigTestRequest
 * @project SlothNote
 * @module 公共配置 / AI 配置
 * @description 接收管理端发起 AI 配置测试的临时参数。
 * @logic 1. 继承 AI 配置表单字段；2. 增加测试提示词；3. 由服务层合并旧 Key 后调用模型。
 * @dependencies DTO: AiConfigUpdateRequest
 * @index_tags AI配置, 测试调用, OpenAI兼容, 管理端
 * @author holic512
 */
package org.example.backend.common.dto.ai;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AiConfigTestRequest extends AiConfigUpdateRequest {

    private String prompt;
}
