/**
 * @file AiConfigDto
 * @project SlothNote
 * @module 公共配置 / AI 配置
 * @description 返回给管理端的 AI 配置脱敏视图。
 * @logic 1. 暴露模型连接参数；2. 仅返回 API Key 是否存在和脱敏文本；3. 携带生成参数。
 * @dependencies 无强依赖
 * @index_tags AI配置, DTO, 脱敏, 管理端
 * @author holic512
 */
package org.example.backend.common.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConfigDto {

    private String providerName;
    private String baseUrl;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Double plannerTemperature;
    private Integer plannerMaxTokens;
    private Boolean enabled;
    private Boolean hasApiKey;
    private String maskedApiKey;
}
